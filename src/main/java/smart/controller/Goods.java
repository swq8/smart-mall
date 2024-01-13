package smart.controller;

import jakarta.annotation.Resource;
import smart.cache.CategoryCache;
import smart.cache.GoodsCache;
import smart.cache.SystemCache;
import smart.entity.CategoryEntity;
import smart.entity.GoodsEntity;
import smart.util.Helper;
import smart.util.Json;
import smart.repository.GoodsRepository;
import smart.repository.GoodsSpecRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import java.util.List;

@Controller
@RequestMapping(path = "goods")
@Transactional
public class Goods {

    @Resource
    GoodsRepository goodsRepository;

    @Resource
    GoodsSpecRepository goodsSpecRepository;

    /**
     * 商品页面
     *
     * @param goodsIdStr 商品ID
     * @param request http request
     * @return 商品页面
     */
    @GetMapping(path = "{goodsIdStr:\\S+}.html")
    public ModelAndView getIndex(
            @PathVariable String goodsIdStr,
            HttpServletRequest request) {
        long goodsId = Helper.parseNumber(goodsIdStr, Long.class);
        GoodsEntity goodsEntity = goodsRepository.findById(goodsId).orElse(null);
        if (goodsEntity == null) {
            return Helper.msgPage("商品不存在", "", request);
        }
        String specItems = Json.stringify(goodsSpecRepository.findByGoodsId(goodsId));
        if (specItems == null || specItems.length() < 10) {
            specItems = "[]";
        }
        // 商品详情内容适配移动端,元素移除宽高值
        if (Helper.isMobileRequest(request)) {
            String des = goodsEntity.getDes();
            des = Helper.stringReplaceAll(des, "\\s+width=('|\")?\\d+('|\")?\\s*", " ");
            des = Helper.stringReplaceAll(des, "\\s+height=('|\")?\\d+('|\")?\\s*", " ");
            goodsEntity.setDes(des);
        }
        List<CategoryEntity> categoryPath = CategoryCache.getCategoryPath(goodsEntity.getCateId());
        ModelAndView modelAndView = Helper.newModelAndView("goods/index", request);
        modelAndView.addObject("categoryPath", categoryPath);
        modelAndView.addObject("goodsEntity", goodsEntity);
        modelAndView.addObject("goodsImgs", goodsEntity.getImgsObj());
        modelAndView.addObject("recommendGoods", GoodsCache.getRecommendGoodsList(categoryPath.get(0).getId()));
        modelAndView.addObject("specItems", specItems);
        modelAndView.addObject("title", goodsEntity.getName() + " - " + SystemCache.getSiteName());
        return modelAndView;
    }
}
