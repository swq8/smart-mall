package smart.service;

import smart.repository.GoodsSpecRepository;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

@Service
public class GoodsSpecService {
    @Resource
    GoodsSpecRepository goodsSpecRepository;

    long deleteByGoodsId(long goodsId, long[] notIn) {
        return 0;
    }
}
