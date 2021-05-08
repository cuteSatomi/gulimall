package com.zzx.gulimall.product;

import com.zzx.gulimall.product.entity.AttrGroupEntity;
import com.zzx.gulimall.product.entity.BrandEntity;
import com.zzx.gulimall.product.service.BrandService;
import com.zzx.gulimall.product.service.CategoryService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallProductApplicationTests {

	@Autowired
	private BrandService brandService;

	@Autowired
	private CategoryService categoryService;

	@Test
	public void contextLoads() {
		BrandEntity brandEntity = new BrandEntity();
		brandEntity.setBrandId(1L);
		brandEntity.setDescript("上圆头第一vn");
		brandEntity.setName("蜘蛛侠");
		brandService.updateById(brandEntity);
	}

	@Test
	public void testFindPath(){
		AttrGroupEntity entity = new AttrGroupEntity();
		entity.setCatelogId(225L);
		categoryService.setCatelogPath(entity);
		System.out.println(entity);
	}

}
