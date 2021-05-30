package com.zzx.gulimall.product;

import com.zzx.gulimall.product.dao.AttrGroupDao;
import com.zzx.gulimall.product.dao.SkuSaleAttrValueDao;
import com.zzx.gulimall.product.entity.AttrGroupEntity;
import com.zzx.gulimall.product.entity.BrandEntity;
import com.zzx.gulimall.product.service.BrandService;
import com.zzx.gulimall.product.service.CategoryService;
import com.zzx.gulimall.product.vo.web.SkuItemVO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallProductApplicationTests {

    @Autowired
    private BrandService brandService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private AttrGroupDao attrGroupDao;

    @Autowired
    private SkuSaleAttrValueDao skuSaleAttrValueDao;

    @Test
    public void test(){
        List<SkuItemVO.SkuItemSaleAttrVO> saleAttrVOS = skuSaleAttrValueDao.getSaleAttrsBySpuId(2L);
        System.out.println(saleAttrVOS);
    }

    @Test
    public void testRedis() {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        ops.set("hello", "world" + UUID.randomUUID().toString());
        String hello = ops.get("hello");
        System.out.println("存入的数据是:" + hello);
    }

    @Test
    public void contextLoads() {
        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setBrandId(1L);
        brandEntity.setDescript("上圆头第一vn");
        brandEntity.setName("蜘蛛侠");
        brandService.updateById(brandEntity);
    }

    @Test
    public void testFindPath() {
        AttrGroupEntity entity = new AttrGroupEntity();
        entity.setCatelogId(225L);
        categoryService.setCatelogPath(entity);
        System.out.println(entity);
    }

}
