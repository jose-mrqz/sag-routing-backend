package pe.sag.routing.data.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import pe.sag.routing.core.model.Maintenance;

import java.util.List;

@Repository
public interface MaintenanceRepository extends MongoRepository<Maintenance, String> {
    List<Maintenance> findAllByTruckCodeOrderByStartDateAsc(String truckCode);
}
