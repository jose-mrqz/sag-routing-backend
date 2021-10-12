package pe.sag.routing.data.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import pe.sag.routing.core.model.Route;

import java.util.Optional;

public interface RouteRepository extends MongoRepository<Route,String> {
    //Metodo para devolver ruta?
}
