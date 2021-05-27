package com.zzx.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.zzx.common.to.es.SkuEsModel;
import com.zzx.common.utils.R;
import com.zzx.gulimall.search.config.GulimallElasticSearchConfig;
import com.zzx.gulimall.search.constant.EsConstant;
import com.zzx.gulimall.search.feign.ProductFeignService;
import com.zzx.gulimall.search.service.MallSearchService;
import com.zzx.gulimall.search.vo.AttrResponseVO;
import com.zzx.gulimall.search.vo.SearchParam;
import com.zzx.gulimall.search.vo.SearchResult;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zzx
 * @date 2021-05-24 15:41
 */
@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Autowired
    private RestHighLevelClient client;

    @Autowired
    private ProductFeignService productFeignService;

    /**
     * 到es中检索数据
     *
     * @param param 检索的所有参数
     * @return 返回检索的结果
     */
    @Override
    public SearchResult search(SearchParam param) {
        // 动态构建出查询需要的DSL语句
        SearchRequest searchRequest = buildSearchRequest(param);

        SearchResult result = null;
        try {
            // 执行检索请求
            SearchResponse response = client.search(searchRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);

            // 封装响应数据，构建检索结果
            result = buildSearchResult(response, param);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 构建检索请求的方法
     * 需求：模糊匹配，过滤（按照属性，分类，品牌，价格区间，库存），排序，分页，高亮，聚合分析
     *
     * @param param
     * @return
     */
    private SearchRequest buildSearchRequest(SearchParam param) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        /* 查询：模糊匹配，过滤（按照属性，分类，品牌，价格区间，库存） */
        // 1、构建boolQuery
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        // 1.1、must - 模糊匹配
        if (!StringUtils.isEmpty(param.getKeyword())) {
            boolQuery.must(QueryBuilders.matchQuery("skuTitle", param.getKeyword()));
        }
        // 1.2、bool - filter 按照属性查询
        if (param.getAttrs() != null && param.getAttrs().size() > 0) {
            // attrs=1_5寸:8寸&attrs=2_8G:16G
            for (String attrStr : param.getAttrs()) {
                BoolQueryBuilder nestedBoolQuery = QueryBuilders.boolQuery();
                String[] s = attrStr.split("_");
                // 检索的属性id
                String attrId = s[0];
                // 检索的属性值
                String[] attrValues = s[1].split(":");
                nestedBoolQuery.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                nestedBoolQuery.must(QueryBuilders.termsQuery("attrs.attrValue", attrValues));
                // 每一个都得生成一个nested查询
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", nestedBoolQuery, ScoreMode.None);
                boolQuery.filter(nestedQuery);
            }

        }
        // 1.2、bool - filter 按照三级分类id查询
        if (param.getCatalog3Id() != null) {
            boolQuery.filter(QueryBuilders.termQuery("catalogId", param.getCatalog3Id()));
        }
        // 1.2、bool - filter 按照品牌id查询
        if (param.getBrandId() != null && param.getBrandId().size() > 0) {
            boolQuery.filter(QueryBuilders.termsQuery("brandId", param.getBrandId()));
        }
        // 1.2、bool - filter 按照价格区间查询
        if (!StringUtils.isEmpty(param.getSkuPrice())) {
            // 价格区间格式：1_500/_500/500_
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");
            String[] s = param.getSkuPrice().split("_");
            if (s.length == 2) {
                if (param.getSkuPrice().startsWith("_")) {
                    // "_500"按照"_"分割的结果length也是2
                    rangeQuery.lte(s[1]);
                } else {
                    // 区间
                    rangeQuery.gte(s[0]).lte(s[1]);
                }
            } else if (param.getSkuPrice().endsWith("_")) {
                // 以_结尾，说明是gte
                rangeQuery.gte(s[0]);
            }
            boolQuery.filter(rangeQuery);
        }

        // 1.2、bool - filter 按照库存查询
        if (param.getHasStock() != null) {
            boolQuery.filter(QueryBuilders.termQuery("hasStock", param.getHasStock().equals(1)));
        }
        sourceBuilder.query(boolQuery);

        /* 排序，分页，高亮 */
        // 2.1、排序
        if (!StringUtils.isEmpty(param.getSort())) {
            // 排序字符串规则：sort=hotScore_asc
            String sort = param.getSort();
            String[] s = sort.split("_");
            sourceBuilder.sort(s[0], SortOrder.fromString(s[1]));
        }
        // 2.1、分页
        sourceBuilder.from((param.getPageNum() - 1) * EsConstant.PRODUCT_PAGE_SIZE);
        sourceBuilder.size(EsConstant.PRODUCT_PAGE_SIZE);
        // 2.2、高亮
        if (!StringUtils.isEmpty(param.getKeyword())) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            // 对skuTitle字段的某个检索值进行高亮处理
            highlightBuilder.field("skuTitle");
            highlightBuilder.preTags("<b style='color:red'>");
            highlightBuilder.postTags("</b>");
            sourceBuilder.highlighter(highlightBuilder);
        }

        /* 聚合分析 */
        // 品牌聚合
        TermsAggregationBuilder brandAgg = AggregationBuilders.terms("brand_agg");
        brandAgg.field("brandId").size(50);
        // 品牌聚合的子聚合
        brandAgg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
        brandAgg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));
        sourceBuilder.aggregation(brandAgg);

        // 分类聚合
        TermsAggregationBuilder catalogAgg = AggregationBuilders.terms("catalog_agg").field("catalogId").size(20);
        catalogAgg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));
        sourceBuilder.aggregation(catalogAgg);

        // 属性聚合
        NestedAggregationBuilder attrAgg = AggregationBuilders.nested("attr_agg", "attrs");
        // 聚合出当前的attrId
        TermsAggregationBuilder attrIdAgg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId");
        // 聚合分析出当前attrId对应的名字
        attrIdAgg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        // 聚合分析出当前attrId对应的值
        attrIdAgg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50));
        attrAgg.subAggregation(attrIdAgg);
        sourceBuilder.aggregation(attrAgg);

        SearchRequest searchRequest = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, sourceBuilder);
        return searchRequest;
    }

    /**
     * 构建结果数据
     *
     * @param response
     * @param param
     * @return
     */
    private SearchResult buildSearchResult(SearchResponse response, SearchParam param) {

        // 获取检索命中的记录
        SearchHits hits = response.getHits();

        SearchResult result = new SearchResult();
        List<SkuEsModel> esModels = new ArrayList<>();
        if (hits.getHits() != null && hits.getHits().length > 0) {
            for (SearchHit hit : hits.getHits()) {
                // 获取检索结果中的source，将其封装到SkuEsModel中
                String sourceAsString = hit.getSourceAsString();
                SkuEsModel skuEsModel = JSON.parseObject(sourceAsString, SkuEsModel.class);
                HighlightField skuTitle = hit.getHighlightFields().get("skuTitle");
                if (!StringUtils.isEmpty(param.getKeyword())) {
                    String string = skuTitle.getFragments()[0].string();
                    skuEsModel.setSkuTitle(string);
                }
                esModels.add(skuEsModel);
            }
        }
        // 1、返回查询到的所有商品
        result.setProducts(esModels);
        // 2、当前查询到的商品涉及到的所有属性信息
        ParsedNested attrAgg = response.getAggregations().get("attr_agg");
        ParsedLongTerms attrIdAgg = attrAgg.getAggregations().get("attr_id_agg");
        List<SearchResult.AttrVO> attrVos = new ArrayList<>();
        for (Terms.Bucket bucket : attrIdAgg.getBuckets()) {
            SearchResult.AttrVO attrVo = new SearchResult.AttrVO();
            // 1、得到属性的id
            attrVo.setAttrId(bucket.getKeyAsNumber().longValue());
            // 2、得到属性的名字
            String attrName = ((ParsedStringTerms) bucket.getAggregations().get("attr_name_agg")).getBuckets().get(0).getKeyAsString();
            attrVo.setAttrName(attrName);
            // 3、得到属性的所有值
            List<String> attrValues = ((ParsedStringTerms) bucket.getAggregations().get("attr_value_agg"))
                    .getBuckets().stream().map(item -> ((Terms.Bucket) item).getKeyAsString()).collect(Collectors.toList());
            attrVo.setAttrValue(attrValues);
            attrVos.add(attrVo);
        }
        result.setAttrs(attrVos);

        // 3、当前查询到的商品涉及到的所有品牌信息
        ParsedLongTerms brandAgg = response.getAggregations().get("brand_agg");
        List<SearchResult.BrandVO> brandVos = new ArrayList<>();
        for (Terms.Bucket bucket : brandAgg.getBuckets()) {
            SearchResult.BrandVO brandVo = new SearchResult.BrandVO();
            // 得到品牌id
            brandVo.setBrandId(bucket.getKeyAsNumber().longValue());
            // 得到品牌名字
            String brandName = ((ParsedStringTerms) bucket.getAggregations().get("brand_name_agg")).getBuckets().get(0).getKeyAsString();
            brandVo.setBrandName(brandName);
            // 得到品牌图片
            String brandImg = ((ParsedStringTerms) bucket.getAggregations().get("brand_img_agg")).getBuckets().get(0).getKeyAsString();
            brandVo.setBrandImg(brandImg);
            brandVos.add(brandVo);
        }
        result.setBrands(brandVos);

        // 4、当前查询到的商品涉及到的所有分类信息
        ParsedLongTerms catalogAgg = response.getAggregations().get("catalog_agg");
        List<SearchResult.CatalogVO> catalogVos = new ArrayList<>();
        List<? extends Terms.Bucket> buckets = catalogAgg.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            SearchResult.CatalogVO catalogVo = new SearchResult.CatalogVO();
            // 从分类聚合中获取key就是分类的id
            catalogVo.setCatalogId(bucket.getKeyAsNumber().longValue());
            // 获取分类聚合的子聚合
            ParsedStringTerms catalogNameAgg = bucket.getAggregations().get("catalog_name_agg");
            String catalogName = catalogNameAgg.getBuckets().get(0).getKeyAsString();
            catalogVo.setCatalogName(catalogName);
            catalogVos.add(catalogVo);
        }
        result.setCatalogs(catalogVos);

        // 5、分页信息-当前页码
        result.setPageNum(param.getPageNum());

        // 6、分页信息-总记录数
        long total = hits.getTotalHits().value;
        result.setTotal(total);

        // 7、分页信息-总页码
        int totalPages = (int) total % EsConstant.PRODUCT_PAGE_SIZE == 0 ?
                (int) total / EsConstant.PRODUCT_PAGE_SIZE :
                ((int) total / EsConstant.PRODUCT_PAGE_SIZE + 1);
        result.setTotalPages(totalPages);

        // 8、构建面包屑导航功能
        List<SearchResult.NavVO> navs = new ArrayList<>();
        List<String> attrs = param.getAttrs();
        if (attrs != null && attrs.size() > 0) {
            navs = attrs.stream().map(attr -> {
                SearchResult.NavVO navVO = new SearchResult.NavVO();
                String[] s = attr.split("_");
                navVO.setNavValue(s[1]);
                R r = productFeignService.attrInfo(Long.parseLong(s[0]));
                if (r.getCode() == 0) {
                    AttrResponseVO data = r.getData("attr", new TypeReference<AttrResponseVO>() {
                    });
                    navVO.setNavName(data.getAttrName());
                } else {
                    navVO.setNavName(s[0]);
                }

                // 取消这个面包屑以后，将请求地址url的当前置空
                String encode = null;
                try {
                    encode = URLEncoder.encode(attr, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                String replace = param.get_queryString().replace("&attrs=" + encode, "");
                navVO.setLink("http://search.gulimal.com?" + replace);

                return navVO;
            }).collect(Collectors.toList());
        }
        result.setNavs(navs);

        return result;
    }

}
