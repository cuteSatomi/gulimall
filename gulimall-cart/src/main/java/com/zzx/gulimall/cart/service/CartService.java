package com.zzx.gulimall.cart.service;

import com.zzx.gulimall.cart.vo.Cart;
import com.zzx.gulimall.cart.vo.CartItem;

import java.util.concurrent.ExecutionException;

/**
 * @author zzx
 * @date 2021-06-04 21:17
 */
public interface CartService {
    /**
     * 将商品添加到购物车
     *
     * @param skuId
     * @param num
     * @return
     */
    CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException;

    /**
     * 根据skuId获取CartItem
     *
     * @param skuId
     * @return
     */
    CartItem getCartItem(Long skuId);

    /**
     * 获取购物车
     *
     * @return
     */
    Cart getCart() throws ExecutionException, InterruptedException;

    /**
     * 根据cartKey清空redis
     *
     * @param cartKey
     */
    void clearCart(String cartKey);

    /**
     * 购物车中商品是否选中
     *
     * @param skuId
     * @param check
     */
    void checkItem(Long skuId, Integer check);

    /**
     * 统计每个购物项的数量
     *
     * @param skuId
     * @param num
     */
    void countItem(Long skuId, Integer num);
}
