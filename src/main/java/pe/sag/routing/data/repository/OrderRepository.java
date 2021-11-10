package pe.sag.routing.data.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import pe.sag.routing.core.model.Order;
import pe.sag.routing.shared.util.enums.OrderStatus;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends MongoRepository<Order,String> {
    Optional<Order> findBy_id(String id);
    Optional<Order> findByCode(int code);
    List<Order> findByStatusAndMonitoringOrderByRegistrationDateAsc(OrderStatus status, boolean monitoring);
    Optional<Order> findFirstByOrderByCodeDesc();
    List<Order> findByMonitoringOrderByCodeAsc(boolean monitoring);
    List<Order> findFirst200ByStatusAndMonitoringOrderByDeadlineDateAscRegistrationDateAsc(OrderStatus status, boolean monitoring);
    void deleteByMonitoring(boolean monitoring);
    int deleteByCode(int code);
}
