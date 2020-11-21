package alex.service;

import alex.entity.BrandEntity;
import alex.repository.BrandRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class BrandService {

    @Resource
    BrandRepository brandRepository;

    public List<BrandEntity> findAll() {
        Sort sort = Sort.by(Sort.Direction.ASC, "name");
        return brandRepository.findAll(sort);
    }
}
