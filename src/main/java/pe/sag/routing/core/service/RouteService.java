package pe.sag.routing.core.service;

import org.springframework.stereotype.Service;
import pe.sag.routing.core.model.Route;
import pe.sag.routing.data.parser.RouteParser;
import pe.sag.routing.data.repository.RouteRepository;
import pe.sag.routing.shared.dto.RouteDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RouteService {
    private final RouteRepository routeRepository;

    public RouteService(RouteRepository routeRepository) {
        this.routeRepository = routeRepository;
    }

    public Route register(Route route) {
        return routeRepository.save(route);
    }

    public Route edit(Route route) {
        return routeRepository.save(route);
    }

    public List<RouteDto> list() {
        return routeRepository.findAll().stream().map(RouteParser::toDto).collect(Collectors.toList());
    }

    public List<Route> getActiveRoutes(boolean monitoring) {
        return routeRepository.findAllByStartDateIsAfterAndFinishDateIsBeforeAndMonitoring(LocalDateTime.now(), LocalDateTime.now(), monitoring);
    }
}

