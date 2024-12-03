package smart.repository;

import smart.entity.SpecEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpecRepository extends JpaRepository<SpecEntity, Long> {

    List<SpecEntity> findAllByOrderByName();
    SpecEntity findFirstByNameAndNote(String name, String note);
    SpecEntity findFirstByNameAndNoteAndIdIsNot(String name, String note, Long id);
}
