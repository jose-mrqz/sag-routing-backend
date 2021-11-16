package pe.sag.routing.data.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import pe.sag.routing.core.model.Order;
import pe.sag.routing.shared.util.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends MongoRepository<Order,String> {
    Optional<Order> findBy_id(String id);
    Optional<Order> findByCode(int code);
    List<Order> findByStatusAndMonitoringOrderByRegistrationDateAsc(OrderStatus status, boolean monitoring);
    Optional<Order> findFirstByOrderByCodeDesc();
    List<Order> findFirst2000ByStatusAndMonitoringOrderByRegistrationDateAscDeadlineDateAsc(OrderStatus status, boolean monitoring);
    void deleteByMonitoring(boolean monitoring);
    int deleteByCode(int code);

    List<Order> findByMonitoringAndRegistrationDateBetweenOrderByCodeAsc(boolean monitoring, LocalDateTime registrationDate1, LocalDateTime registrationDate2);
    List<Order> findByStatusAndMonitoringAndRegistrationDateBetweenOrderByCodeAsc(OrderStatus status, boolean monitoring, LocalDateTime registrationDate1, LocalDateTime registrationDate2);
}
