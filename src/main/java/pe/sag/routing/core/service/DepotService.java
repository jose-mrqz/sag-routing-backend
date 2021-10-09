package pe.sag.routing.core.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.sag.routing.core.model.Depot;
import pe.sag.routing.data.parser.DepotParser;
import pe.sag.routing.data.repository.DepotRepository;
import pe.sag.routing.shared.dto.DepotDto;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DepotService {
    @Autowired
    private DepotRepository depotRepository;

    public Depot register(DepotDto depotRequest) {
        Depot depot = DepotParser.fromDto(depotRequest);
        return depotRepository.save(depot);
    }

    public List<DepotDto> list() {
        return depotRepository.findAll().stream().map(DepotParser::toDto).collect(Collectors.toList());
    }
}
