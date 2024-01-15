package smart.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import smart.cache.CategoryCache;
import smart.cache.GoodsCache;
import smart.cache.SystemCache;
import smart.entity.CategoryEntity;
import smart.entity.GoodsEntity;
import smart.repository.GoodsRepository;
import smart.repository.GoodsSpecRepository;
import smart.util.DbUtils;
import smart.util.Helper;
import smart.util.Json;

import java.util.List;

@Controller
@RequestMapping(path = "goods")
@Transactional
public class Goods {

    @Resource
    GoodsSpecRepository goodsSpecRepository;

    /**
     * 商品页面
     *
     * @param goodsIdStr 商品ID
     * @param request    http request
     * @return 商品页面
     */
    @GetMapping(path = "{goodsIdStr:\\S+}.html")
    public ModelAndView getIndex(
            @PathVariable String goodsIdStr,
            HttpServletRequest request) {
        long goodsId = Helper.parseNumber(goodsIdStr, Long.class);
        GoodsEntity goodsEntity = DbUtils.findById(goodsId, GoodsEntity.class);
        if (goodsEntity == null) {
            return Helper.msgPage("商品不存在", "", request);
        }
        String specItems = Json.stringify(goodsSpecRepository.findByGoodsId(goodsId));
        if (specItems == null || specItems.length() < 10) {
            specItems = "[]";
        }
        initDes(goodsEntity, request);
        List<CategoryEntity> categoryPath = CategoryCache.getCategoryPath(goodsEntity.getCateId());
        ModelAndView view = Helper.newModelAndView("goods/index", request);

        view.addObject("categoryPath", categoryPath);
        view.addObject("goodsEntity", goodsEntity);
        view.addObject("goodsImgs", goodsEntity.getImgsObj());
        view.addObject("recommendGoods", GoodsCache.getRecommendGoodsList(categoryPath.getFirst().getId()));
        view.addObject("specItems", specItems);
        view.addObject("title", goodsEntity.getName() + " - " + SystemCache.getSiteName());
        return view;
    }

    private void initDes(GoodsEntity entity, HttpServletRequest request) {
        var goodsTemplate = SystemCache.getGoodsTemplate();
        var sb = new StringBuilder();
        if (goodsTemplate.getHeaderEnable()) sb.append(goodsTemplate.getHeader());
        sb.append(entity.getDes());
        if (goodsTemplate.getFooterEnable()) sb.append(goodsTemplate.getFooter());
        String html = sb.toString();
        if (Helper.isMobileRequest(request)) html = adaptMobile(html);
        entity.setDes(html);
    }

    /**
     * 商品详情内容适配移动端,元素移除宽高值
     *
     * @param html html
     * @return html
     */
    private String adaptMobile(String html) {
        html = Helper.stringReplaceAll(html, "\\s+width=('|\")?\\d+('|\")?\\s*", " ");
        return Helper.stringReplaceAll(html, "\\s+height=('|\")?\\d+('|\")?\\s*", " ");
    }
}
