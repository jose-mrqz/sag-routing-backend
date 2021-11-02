package pe.sag.routing.data.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import pe.sag.routing.core.model.Breakdown;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BreakdownRepository extends MongoRepository<Breakdown, String> {
    List<Breakdown> findAllByStartDateBeforeAndEndDateAfter(LocalDateTime start, LocalDateTime end);
}
