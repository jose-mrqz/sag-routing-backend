package pe.sag.routing.core.service;

import org.springframework.stereotype.Service;
import pe.sag.routing.core.model.Depot;
import pe.sag.routing.data.parser.DepotParser;
import pe.sag.routing.data.repository.DepotRepository;
import pe.sag.routing.shared.dto.DepotDto;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DepotService {
    private final DepotRepository depotRepository;

    public DepotService(DepotRepository depotRepository) {
        this.depotRepository = depotRepository;
    }

    public Depot register(DepotDto depotRequest) {
        Depot depot = DepotParser.fromDto(depotRequest);
        return depotRepository.save(depot);
    }

    public List<DepotDto> list() {
        return depotRepository.findAll().stream().map(DepotParser::toDto).collect(Collectors.toList());
    }

    public Depot save(Depot depot) {
        return depotRepository.save(depot);
    }
}
