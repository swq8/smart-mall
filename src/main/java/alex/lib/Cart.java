package alex.lib;

import alex.Application;
import alex.authentication.UserToken;
import alex.cache.GoodsCache;
import alex.cache.GoodsSpecCache;
import alex.entity.GoodsSpecEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Cart {
    public static String KEY = "cart";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpServletRequest request;
    private final UserToken userToken;
    private List<Item> items;

    public Cart(HttpServletRequest request) {
        this.request = request;
        String json;
        userToken = (UserToken) request.getAttribute(UserToken.KEY);
        if (userToken == null) {
            json = (String) request.getSession().getAttribute(KEY);
        } else {
            json = Application.REDIS_TEMPLATE.opsForValue().get(KEY + ":" + userToken.getId());
        }
        if (json == null || json.length() < 10) {
            items = new LinkedList<>();
        } else {
            try {
                items = objectMapper.readValue(json, new TypeReference<>() {
                });
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        if (!check()) {
            save();
        }
    }

    public void add(long goodsId, long specId, long num) {
        if (goodsId <= 0 || specId < 0 || num <= 0) {
            return;
        }
        for (Item item : items) {
            if (item.goodsId == goodsId && item.specId == specId) {
                item.num += num;
                save();
                return;
            }

        }
        var item = new Item(goodsId, specId, num);
        items.add(0, item);
        save();
    }

    /**
     * Check goods availability
     *
     * @return all goods availability
     */
    public boolean check() {
        int size = items.size();

        items.removeIf(item -> {
            var goodsEntity = GoodsCache.getGoodsEntity(item.getGoodsId());
            if (goodsEntity == null) {
                return true;
            }
            item.setGoodsImg(goodsEntity.getImgs().split(",")[0]);
            item.setGoodsName(goodsEntity.getName());
            item.setGoodsWeight(goodsEntity.getWeight());
            item.setGoodsPrice(goodsEntity.getPrice());
            if (item.specId == 0) {
                return false;
            }
            var specEntities = GoodsSpecCache.getGoodsSpecEntities(item.getGoodsId());
            if (specEntities == null) {
                return true;
            }
            GoodsSpecEntity specEntity = null;
            for (GoodsSpecEntity entity : specEntities) {
                if (entity.getId() == item.getSpecId()) {
                    specEntity = entity;
                    break;
                }
            }
            if (specEntity == null) {
                return true;
            } else {
                item.setGoodsPrice(specEntity.getPrice());
                item.setGoodsWeight(specEntity.getWeight());
                item.setSpecDes(specEntity.getDes());
                return false;
            }

        });
        return size == items.size();
    }

    public void clear() {
        items.clear();
        save();
    }

    public void del(long goodsId, long specId) {
        if (goodsId <= 0 || specId < 0) {
            return;
        }
        for (int i = 0; i < items.size(); i++) {
            var item = items.get(i);
            if (item.goodsId == goodsId && item.specId == specId) {
                items.remove(i);
                break;
            }
        }
        save();
    }

    public List<Item> getItems() {
        return items;
    }


    void save() {
        check();
        String json;
        List<Map<Object, Object>> list = new LinkedList<>();
        items.forEach(item -> {
            Map<Object, Object> map = Map.of(
                    "goodsId", item.getGoodsId(),
                    "specId", item.getSpecId(),
                    "num", item.getNum(),
                    "selected", item.isSelected()
            );
            list.add(map);
        });
        try {
            json = objectMapper.writeValueAsString(list);
            if (userToken == null) {
                request.getSession().setAttribute(KEY, json);
            } else {
                Application.REDIS_TEMPLATE.opsForValue().set(KEY + ":" + userToken.getId(), json);
            }

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public void setSelected(long goodsId, long specId, boolean selected) {
        if (goodsId <= 0 || specId < 0) {
            return;
        }
        for (Item item : items) {
            if (item.goodsId == goodsId && item.specId == specId) {
                if (item.isSelected() != selected) {
                    item.setSelected(selected);
                    save();
                }
                return;
            }
        }
    }

    public void setSelectedAll(boolean selected) {
        items.forEach(item -> item.setSelected(selected));
        save();
    }

    public void sub(long goodsId, long specId, long num) {
        if (goodsId <= 0 || specId < 0 || num <= 0) {
            return;
        }
        for (int i = 0; i < items.size(); i++) {
            var item = items.get(i);
            if (item.goodsId == goodsId && item.specId == specId) {
                if (item.num > num) {
                    item.num -= num;
                } else {
                    items.remove(i);
                }
                save();
                return;
            }
        }
    }

    /**
     * 选中商品数量合计
     *
     * @return 选中商品数量合计
     */
    public long sumNum() {
        long l = 0;
        for (var item : items) {
            if (item.isSelected()) {
                l += item.num;
            }
        }
        return l;
    }

    /**
     * 选中商品价格合计
     *
     * @return 选中商品价格合计
     */
    public long sumPrice() {
        long l = 0;
        for (var item : items) {
            if (item.isSelected()) {
                l += item.goodsPrice * item.num;
            }
        }
        return l;
    }

    /**
     * 选中商品重量合计
     *
     * @return 选中商品重量合计
     */
    public long sumWeight() {
        long l = 0;
        for (var item : items) {
            if (item.isSelected()) {
                l += item.goodsWeight * item.num;
            }

        }
        return l;
    }

    public static class Item {
        private long goodsId;

        private String goodsImg;

        private String goodsName;

        private long goodsPrice;

        private long goodsWeight;

        private long specId;

        private boolean selected = true;
        private String specDes;
        private long num;

        public Item() {
        }

        public Item(long goodsId, long specId, long num) {
            this.goodsId = goodsId;
            this.specId = specId;
            this.num = num;
        }

        public long getGoodsId() {
            return goodsId;
        }

        public String getGoodsImg() {
            return goodsImg;
        }

        public void setGoodsImg(String goodsImg) {
            this.goodsImg = goodsImg;
        }

        public String getGoodsName() {
            return goodsName;
        }

        public void setGoodsName(String goodsName) {
            this.goodsName = goodsName;
        }

        public long getGoodsPrice() {
            return goodsPrice;
        }

        public void setGoodsPrice(long goodsPrice) {
            this.goodsPrice = goodsPrice;
        }

        public long getGoodsWeight() {
            return goodsWeight;
        }

        public void setGoodsWeight(long goodsWeight) {
            this.goodsWeight = goodsWeight;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public long getSpecId() {
            return specId;
        }

        public String getSpecDes() {
            return specDes;
        }

        public void setSpecDes(String specDes) {
            this.specDes = specDes;
        }

        public long getNum() {
            return num;
        }

        public void setNum(long num) {
            this.num = num;
        }
    }
}
