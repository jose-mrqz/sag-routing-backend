package pe.sag.routing.core.service;

import org.springframework.stereotype.Service;
import pe.sag.routing.core.model.Depot;
import pe.sag.routing.core.scheduler.DepotScheduler;
import pe.sag.routing.data.parser.DepotParser;
import pe.sag.routing.data.repository.DepotRepository;
import pe.sag.routing.shared.dto.DepotDto;
import pe.sag.routing.shared.util.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DepotService {
    private final DepotRepository depotRepository;
    private final DepotScheduler depotScheduler;

    public DepotService(DepotRepository depotRepository, DepotScheduler depotScheduler) {
        this.depotRepository = depotRepository;
        this.depotScheduler = depotScheduler;
    }

    public Depot register(DepotDto depotRequest) {
        Depot depot = DepotParser.fromDto(depotRequest);
        return depotRepository.save(depot);
    }

    public List<DepotDto> list() {
        return depotRepository.findAll().stream().map(DepotParser::toDto).collect(Collectors.toList());
    }

    public List<Depot> getAll() {
        return depotRepository.findAll();
    }

    public Depot save(Depot depot) {
        return depotRepository.save(depot);
    }

    public void scheduleStatusChange(double x, double glpConsumed, LocalDateTime dateTime) {
        String id;
        List<Depot> depots = depotRepository.findAll();
        if(x == depots.get(0).getX()) id = depots.get(0).get_id();
        else id = depots.get(1).get_id();

        depotScheduler.scheduleStatusChange(id, glpConsumed, dateTime);
    }
}
