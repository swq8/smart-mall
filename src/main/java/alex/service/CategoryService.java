package alex.service;

import alex.entity.CategoryEntity;
import alex.repository.CategoryRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class CategoryService {

    @Resource
    CategoryRepository categoryRepository;

    public List<CategoryEntity> findAll() {
        Sort sort = Sort.by(Sort.Direction.ASC, "parentId")
                .and(Sort.by(Sort.Direction.ASC, "sort"))
                .and(Sort.by(Sort.Direction.ASC, "id"));
        return categoryRepository.findAll(sort);
    }
}
