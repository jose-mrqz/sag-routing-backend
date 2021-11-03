package pe.sag.routing.data.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import pe.sag.routing.core.model.Roadblock;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RoadblockRepository extends MongoRepository<Roadblock, String> {
     List<Roadblock> findByStartDateBeforeAndEndDateAfterAndMonitoring(LocalDateTime startDate, LocalDateTime endDate, boolean monitoring);
     List<Roadblock> findByEndDateAfter(LocalDateTime endDate);
    List<Roadblock> findAllByMonitoring(boolean monitoring);
    void deleteAllByMonitoring(boolean b);
    List<Roadblock> findByStartDateBeforeAndEndDateAfter(LocalDateTime startDate, LocalDateTime endDate);
}
