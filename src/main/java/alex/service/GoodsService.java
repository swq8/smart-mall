package alex.service;

import alex.repository.GoodsRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class GoodsService {
    @Resource
    private GoodsRepository goodsRepository;
}
