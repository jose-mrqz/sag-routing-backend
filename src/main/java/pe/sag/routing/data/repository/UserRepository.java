package pe.sag.routing.data.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import pe.sag.routing.core.model.User;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User,String> {
    Optional<User> findByCode(String code);
    Optional<User> findByUsername(String username);
    Optional<User> findFirstByOrderByCodeDesc();
    int deleteByCode(String code);
}
