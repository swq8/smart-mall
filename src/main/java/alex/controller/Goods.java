package alex.controller;

import alex.cache.CategoryCache;
import alex.cache.GoodsCache;
import alex.cache.GoodsSpecCache;
import alex.entity.GoodsEntity;
import alex.lib.Helper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping(path = "goods")
public class Goods {

    @GetMapping(path = "{goodsId:\\d+}.html")
    public ModelAndView getIndex(
            @PathVariable long goodsId,
            HttpServletRequest request){
        GoodsEntity goodsEntity = GoodsCache.getGoodsEntity(goodsId);
        String[] goodsImgs = goodsEntity.getImgs().split(",");
        String specItems = null;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            specItems = objectMapper.writeValueAsString(GoodsSpecCache.getGoodsSpecEntities(goodsId));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        if (specItems == null || specItems.length() < 10) {
            specItems = "[]";
        }
        ModelAndView modelAndView = Helper.newModelAndView("goods/index", request);
        modelAndView.addObject("categoryPath", CategoryCache.getCategoryPath(goodsEntity.getCateId()));
        modelAndView.addObject("goodsEntity", goodsEntity);
        modelAndView.addObject("goodsImgs", goodsImgs);
        modelAndView.addObject("specItems", specItems);
        return modelAndView;
    }
}
