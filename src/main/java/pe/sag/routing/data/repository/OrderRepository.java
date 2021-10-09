package pe.sag.routing.data.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import pe.sag.routing.core.model.Order;

import java.util.Optional;

public interface OrderRepository extends MongoRepository<Order,String> {
    Optional<Order> findByCode(String code);
}
