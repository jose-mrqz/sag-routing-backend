package pe.sag.routing.core.service;

import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    private RouteRepository routeRepository;

    public Route register(Route route) {
        return routeRepository.save(route);
    }

    public List<RouteDto> list() {
        return routeRepository.findAll().stream().map(RouteParser::toDto).collect(Collectors.toList());
    }

    public List<Route> getActiveRoutes() {
        return routeRepository.findAllByStartDateIsAfterAndFinishDateIsBefore(LocalDateTime.now(), LocalDateTime.now());
    }
}

