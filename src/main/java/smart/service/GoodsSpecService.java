package smart.service;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import smart.entity.GoodsSpecEntity;
import smart.repository.GoodsSpecRepository;

import java.util.List;

@Service
public class GoodsSpecService {
    @Resource
    GoodsSpecRepository goodsSpecRepository;

    public List<GoodsSpecEntity> findByGoodsId(Long goodsId) {
        return goodsSpecRepository.findByGoodsId(goodsId);
    }
}
