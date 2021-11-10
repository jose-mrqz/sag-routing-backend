package pe.sag.routing.core.service;

import org.springframework.stereotype.Service;
import pe.sag.routing.core.model.Maintenance;
import pe.sag.routing.core.model.Truck;
import pe.sag.routing.core.scheduler.TruckScheduler;
import pe.sag.routing.data.parser.MaintenanceParser;
import pe.sag.routing.data.repository.MaintenanceRepository;
import pe.sag.routing.shared.dto.MaintenanceDto;
import pe.sag.routing.shared.util.enums.TruckStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MaintenanceService {
    private final MaintenanceRepository maintenanceRepository;
    private final TruckService truckService;
    private final TruckScheduler truckScheduler;

    public MaintenanceService(MaintenanceRepository maintenanceRepository, TruckService truckService, TruckScheduler truckScheduler) {
        this.maintenanceRepository = maintenanceRepository;
        this.truckService = truckService;
        this.truckScheduler = truckScheduler;
    }

    public Maintenance register(MaintenanceDto maintenanceRequest) {
        Maintenance maintenance = MaintenanceParser.fromDto(maintenanceRequest);
        return maintenanceRepository.save(maintenance);
    }

    public List<MaintenanceDto> list() {
        return maintenanceRepository.findAll().stream().map(MaintenanceParser::toDto).collect(Collectors.toList());
    }

    public List<Maintenance> getAll() {
        return maintenanceRepository.findAll();
    }

    public List<Maintenance> registerMany(List<Maintenance> maintenances) {
        for(Maintenance m : maintenances){
            Truck truck = truckService.findByCodeAndMonitoring(m.getTruckCode(), true);
            truckScheduler.scheduleStatusChange(truck.get_id(), TruckStatus.MANTENIMIENTO, m.getStartDate());
            truckScheduler.scheduleStatusChange(truck.get_id(), TruckStatus.DISPONIBLE, m.getEndDate());
        }
        return maintenanceRepository.saveAll(maintenances);
    }


}
