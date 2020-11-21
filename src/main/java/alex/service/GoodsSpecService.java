package alex.service;

import alex.entity.GoodsSpecEntity;
import alex.repository.GoodsSpecRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class GoodsSpecService {
    @Resource
    GoodsSpecRepository goodsSpecRepository;

    long deleteByGoodsId(long goodsId, long[] notIn) {
        return 0;
    }
}
