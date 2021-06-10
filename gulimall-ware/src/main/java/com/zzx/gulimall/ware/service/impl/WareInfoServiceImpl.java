package com.zzx.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzx.common.utils.PageUtils;
import com.zzx.common.utils.Query;
import com.zzx.common.utils.R;
import com.zzx.gulimall.ware.dao.WareInfoDao;
import com.zzx.gulimall.ware.entity.WareInfoEntity;
import com.zzx.gulimall.ware.feign.MemberFeignService;
import com.zzx.gulimall.ware.service.WareInfoService;
import com.zzx.gulimall.ware.vo.FareVO;
import com.zzx.gulimall.ware.vo.MemberAddressVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Map;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {

    @Autowired
    private MemberFeignService memberFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareInfoEntity> wrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.eq("id", key)
                    .or().like("name", key)
                    .or().like("address", key)
                    .or().like("areacode", key);
        }

        IPage<WareInfoEntity> page = this.page(new Query<WareInfoEntity>().getPage(params), wrapper);

        return new PageUtils(page);
    }

    /**
     * 根据地址查询运费
     *
     * @param addrId
     * @return
     */
    @Override
    public FareVO getFare(Long addrId) {
        FareVO fareVO = new FareVO();
        R r = memberFeignService.addrInfo(addrId);
        MemberAddressVO data = r.getData("memberReceiveAddress", new TypeReference<MemberAddressVO>() {
        });
        fareVO.setAddress(data);
        if(data!=null){
            String phone = data.getPhone();
            String substring = phone.substring(phone.length() - 1);
            fareVO.setFare(new BigDecimal(substring));
            return fareVO;
        }
        return null;
    }

}