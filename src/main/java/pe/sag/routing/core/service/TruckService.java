package pe.sag.routing.core.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.sag.routing.algorithm.Planner;
import pe.sag.routing.core.model.Order;
import pe.sag.routing.core.model.Truck;
import pe.sag.routing.data.parser.TruckParser;
import pe.sag.routing.data.repository.TruckRepository;
import pe.sag.routing.shared.dto.TruckDto;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TruckService {
    @Autowired
    private TruckRepository truckRepository;

    public Truck register(TruckDto truckRequest) {
        Truck truck = TruckParser.fromDto(truckRequest);
        truck.setAvailable(true);
        return truckRepository.save(truck);
    }

    public List<TruckDto> list() {
        return truckRepository.findAll().stream().map(TruckParser::toDto).collect(Collectors.toList());
    }

    public Truck findById(String id) {
        Optional<Truck> ot = truckRepository.findById(id);
        return ot.orElse(null);
    }

    public List<Truck> findByAvailable(boolean available) {
        return truckRepository.findByAvailableOrderByModelDesc(available);
    }
}
