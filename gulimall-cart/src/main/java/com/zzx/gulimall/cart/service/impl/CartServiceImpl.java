package com.zzx.gulimall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.zzx.common.utils.R;
import com.zzx.gulimall.cart.feign.ProductFeignService;
import com.zzx.gulimall.cart.interceptor.CartInterceptor;
import com.zzx.gulimall.cart.service.CartService;
import com.zzx.gulimall.cart.to.SkuInfoTO;
import com.zzx.gulimall.cart.to.UserInfoTO;
import com.zzx.gulimall.cart.vo.Cart;
import com.zzx.gulimall.cart.vo.CartItem;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @author zzx
 * @date 2021-06-04 21:18
 */
@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private ThreadPoolExecutor executor;

    public static final String REDIS_CART_PREFIX = "gulimall:cart:";

    /**
     * 将商品添加到购物车
     *
     * @param skuId
     * @param num
     * @return
     */
    @Override
    public CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {
        BoundHashOperations<String, Object, Object> ops = getRedisOps();

        String res = (String) ops.get(skuId.toString());
        if (StringUtils.isEmpty(res)) {
            // 如果查出购物车中不存在该商品，则直接添加到redis中
            CartItem cartItem = new CartItem();
            // 异步远程调用product微服务获取sku基本信息
            CompletableFuture<Void> getSkuInfoTask = CompletableFuture.runAsync(() -> {
                R r = productFeignService.getSkuInfo(skuId);
                SkuInfoTO skuInfo = r.getData("skuInfo", new TypeReference<SkuInfoTO>() {
                });
                cartItem.setCheck(true);
                cartItem.setCount(num);
                cartItem.setImage(skuInfo.getSkuDefaultImg());
                cartItem.setPrice(skuInfo.getPrice());
                cartItem.setTitle(skuInfo.getSkuTitle());
                cartItem.setSkuId(skuId);
            }, executor);

            // 异步调用远程服务获取该商品的销售属性
            CompletableFuture<Void> getSaleAttrsTask = CompletableFuture.runAsync(() -> {
                List<String> saleAttrs = productFeignService.getSkuSaleAttrValueAsStringList(skuId);
                cartItem.setSkuAttr(saleAttrs);
            }, executor);

            CompletableFuture.allOf(getSkuInfoTask, getSaleAttrsTask).get();

            // 将cartItem转为json
            String s = JSON.toJSONString(cartItem);

            // 将购物车存入redis中
            ops.put(skuId.toString(), s);
            return cartItem;
        } else {
            // 如果查出购物车已存在该商品，则需要合并
            CartItem cartItem = JSON.parseObject(res, CartItem.class);
            cartItem.setCount(cartItem.getCount() + num);
            // 将合并后的购物车存入redis中
            ops.put(skuId.toString(), JSON.toJSONString(cartItem));
            return cartItem;
        }
    }

    @Override
    public CartItem getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getRedisOps();
        String s = (String) cartOps.get(skuId.toString());

        return JSON.parseObject(s, CartItem.class);
    }

    /**
     * 获取购物车列表
     *
     * @return
     */
    @Override
    public Cart getCart() throws ExecutionException, InterruptedException {
        // 当前用户是否登陆
        UserInfoTO userInfoTo = CartInterceptor.threadLocal.get();
        String tempCartKey = REDIS_CART_PREFIX + userInfoTo.getUserKey();
        Cart cart = new Cart();
        if (userInfoTo.getUserId() != null) {
            // 当前用户已经登陆，需要将临时购物车中的商品合并到当前用户的购物车
            List<CartItem> tempCartItems = getCartItems(tempCartKey);
            if (tempCartItems != null && tempCartItems.size() > 0) {
                for (CartItem item : tempCartItems) {
                    addToCart(item.getSkuId(), item.getCount());
                }
                // 合并完成需要删除临时购物车的内容
                clearCart(tempCartKey);
            }
            // 查询用户购物车的信息
            String userCartKey = REDIS_CART_PREFIX + userInfoTo.getUserId();
            List<CartItem> items = getCartItems(userCartKey);
            cart.setItems(items);

        } else {
            // 当前用户未登陆，
            List<CartItem> items = getCartItems(tempCartKey);
            cart.setItems(items);
        }

        return cart;
    }

    /**
     * 根据cartKey到redis中获取相应的购物车信息
     *
     * @param cartKey
     * @return
     */
    private List<CartItem> getCartItems(String cartKey) {
        BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(cartKey);
        List<Object> values = ops.values();
        if (values != null && values.size() > 0) {
            return values.stream().map(obj -> {
                String s = (String) obj;
                return JSON.parseObject(s, CartItem.class);
            }).collect(Collectors.toList());
        }
        return null;
    }

    /**
     * 根据cartKey清空redis
     *
     * @param cartKey
     */
    @Override
    public void clearCart(String cartKey) {
        redisTemplate.delete(cartKey);
    }

    /**
     * 购物车商品是否选中
     *
     * @param skuId
     * @param check
     */
    @Override
    public void checkItem(Long skuId, Integer check) {
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCheck(check == 1);
        BoundHashOperations<String, Object, Object> ops = getRedisOps();
        ops.put(skuId.toString(), JSON.toJSONString(cartItem));
    }

    /**
     * 统计每个购物项的数量
     * @param skuId
     * @param num
     */
    @Override
    public void countItem(Long skuId, Integer num) {
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCount(num);
        BoundHashOperations<String, Object, Object> ops = getRedisOps();
        ops.put(skuId.toString(), JSON.toJSONString(cartItem));
    }

    /**
     * 根据key获取操作redis的对象
     *
     * @return
     */
    private BoundHashOperations<String, Object, Object> getRedisOps() {
        // 获取当前用户信息，判断是否登陆
        UserInfoTO userInfoTo = CartInterceptor.threadLocal.get();
        String cartKey = "";
        if (userInfoTo.getUserId() == null) {
            // 没有登陆，将user-key作为redis的key
            cartKey = REDIS_CART_PREFIX + userInfoTo.getUserKey();
        } else {
            // 登陆了，将userId作为redis的key
            cartKey = REDIS_CART_PREFIX + userInfoTo.getUserId();
        }
        return redisTemplate.boundHashOps(cartKey);
    }
}
