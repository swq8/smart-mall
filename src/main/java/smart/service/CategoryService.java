package smart.service;

import jakarta.annotation.Resource;
import smart.cache.CategoryCache;
import smart.entity.CategoryEntity;
import smart.repository.CategoryRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import smart.util.DbUtils;
import smart.dto.IdDto;

import java.util.List;

@Service
public class CategoryService {

    @Resource
    CategoryRepository categoryRepository;

    public String delete(IdDto idDto) {
        if (categoryRepository.countByParentId(idDto.getId()) > 0) {
            return "请先删除子分类";
        }
        CategoryEntity categoryEntity = new CategoryEntity();
        categoryEntity.setId(idDto.getId());
        DbUtils.delete(categoryEntity);
        CategoryCache.update();
        return null;
    }

    public List<CategoryEntity> findAll() {
        Sort sort = Sort.by(Sort.Direction.ASC, "parentId")
                .and(Sort.by(Sort.Direction.ASC, "orderNum"))
                .and(Sort.by(Sort.Direction.ASC, "id"));
        return categoryRepository.findAll(sort);
    }

    public List<CategoryEntity> query() {
        return findAll();
    }

    public String save(CategoryEntity categoryEntity) {
        if (categoryEntity.getId() == null) {
            DbUtils.insert(categoryEntity);
        } else {
            DbUtils.update(categoryEntity);
        }
        CategoryCache.update();
        return null;
    }
}
