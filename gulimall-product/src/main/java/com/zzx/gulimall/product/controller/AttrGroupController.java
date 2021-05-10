package com.zzx.gulimall.product.controller;

import com.zzx.common.utils.PageUtils;
import com.zzx.common.utils.R;
import com.zzx.gulimall.product.entity.AttrEntity;
import com.zzx.gulimall.product.entity.AttrGroupEntity;
import com.zzx.gulimall.product.service.AttrAttrgroupRelationService;
import com.zzx.gulimall.product.service.AttrGroupService;
import com.zzx.gulimall.product.service.AttrService;
import com.zzx.gulimall.product.service.CategoryService;
import com.zzx.gulimall.product.vo.request.AttrGroupRelationVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * 属性分组
 *
 * @author zzx
 * @email hdsomedezzx@gmail.com
 * @date 2021-05-03 22:26:54
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AttrService attrService;

    @Autowired
    private AttrAttrgroupRelationService relationService;

    /**
     * 列表
     */
    @RequestMapping("/list/{catelogId}")
    // @RequiresPermissions("product:attrgroup:list")
    public R list(@RequestParam Map<String, Object> params, @PathVariable Long catelogId) {
        PageUtils page = attrGroupService.queryPage(params, catelogId);

        return R.ok().put("page", page);
    }

    /**
     * 根据分组id查询对应分组的全部属性
     *
     * @param attrGroupId
     * @return
     */
    @GetMapping("/{attrGroupId}/attr/relation")
    public R attrRelation(@PathVariable Long attrGroupId) {
        List<AttrEntity> entityList = attrService.getAttrRelation(attrGroupId);
        return R.ok().put("data", entityList);
    }

    /**
     * 删除分组和属性的关联
     *
     * @param attrRelationVOS
     * @return
     */
    @PostMapping("/attr/relation/delete")
    public R deleteAttrRelation(@RequestBody AttrGroupRelationVO[] attrRelationVOS) {
        attrGroupService.deleteAttrRelation(attrRelationVOS);
        return R.ok();
    }

    /**
     * 分页查询当前分类未被其他组关联的参数
     *
     * @param attrGroupId
     * @param params
     * @return
     */
    @GetMapping("/{attrGroupId}/noattr/relation")
    public R attrNoRelation(@PathVariable Long attrGroupId,
                            @RequestParam Map<String, Object> params) {
        PageUtils page = attrService.getAttrNoRelation(attrGroupId, params);
        return R.ok().put("page", page);
    }

    /**
     * 添加关联关系
     *
     * @param vos
     * @return
     */
    @PostMapping("/attr/relation")
    public R addRelation(@RequestBody List<AttrGroupRelationVO> vos) {
        relationService.addRelation(vos);
        return R.ok();
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    // @RequiresPermissions("product:attrgroup:info")
    public R info(@PathVariable("attrGroupId") Long attrGroupId) {
        AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);
        // 给attrGroup设置完整的分组路径
        categoryService.setCatelogPath(attrGroup);
        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("product:attrgroup:save")
    public R save(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("product:attrgroup:update")
    public R update(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("product:attrgroup:delete")
    public R delete(@RequestBody Long[] attrGroupIds) {
        attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

}
