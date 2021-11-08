package pe.sag.routing.core.service;

import org.springframework.stereotype.Service;
import pe.sag.routing.core.model.Maintenance;
import pe.sag.routing.data.parser.MaintenanceParser;
import pe.sag.routing.data.repository.MaintenanceRepository;
import pe.sag.routing.shared.dto.MaintenanceDto;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MaintenanceService {
    private final MaintenanceRepository maintenanceRepository;

    public MaintenanceService(MaintenanceRepository maintenanceRepository) {
        this.maintenanceRepository = maintenanceRepository;
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

    public Maintenance save(Maintenance maintenance) {
        return maintenanceRepository.save(maintenance);
    }
}
