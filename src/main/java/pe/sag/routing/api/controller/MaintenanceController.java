package pe.sag.routing.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.sag.routing.api.request.ManyMaintenanceRequest;
import pe.sag.routing.api.response.RestResponse;
import pe.sag.routing.core.model.Maintenance;
import pe.sag.routing.core.service.MaintenanceService;
import pe.sag.routing.shared.dto.MaintenanceDto;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/maintenance")
public class MaintenanceController {
    private final MaintenanceService maintenanceService;

    public MaintenanceController(MaintenanceService maintenanceService) {
        this.maintenanceService = maintenanceService;
    }

    @PostMapping
    public ResponseEntity<?> registerMany(@RequestBody ManyMaintenanceRequest request) {
        //eliminar mantenimientos existentes?

        List<Maintenance> maintenances = new ArrayList<>();
        for(MaintenanceDto m : request.getMaintenances()){
            Maintenance maintenance = Maintenance.builder()
                    .truckCode(m.getTruckCode())
                    .startDate(m.getStartDate())
                    .endDate(m.getStartDate().plusDays(1))
                    .preventive(true)
                    .finished(false)
                    .build();
            maintenances.add(maintenance);
            /*//Para dentro de 2 meses
            Maintenance maintenance2 = Maintenance.builder()
                    .truckCode(m.getTruckCode())
                    .startDate(m.getStartDate().plusMonths(2))
                    .endDate(m.getStartDate().plusDays(1).plusMonths(2))
                    .preventive(true)
                    .finished(false)
                    .build();
            maintenances.add(maintenance2);
            //Para dentro de 4 meses
            Maintenance maintenance3 = Maintenance.builder()
                    .truckCode(m.getTruckCode())
                    .startDate(m.getStartDate().plusMonths(4))
                    .endDate(m.getStartDate().plusDays(1).plusMonths(4))
                    .preventive(true)
                    .finished(false)
                    .build();
            maintenances.add(maintenance3);*/
        }
        List<Maintenance> maintenancesRegistered = maintenanceService.registerMany(maintenances);

        RestResponse response;
        if (maintenancesRegistered != null) response = new RestResponse(HttpStatus.OK, "Nuevos mantenimientos preventivos agregados correctamente.", maintenancesRegistered);
        else response = new RestResponse(HttpStatus.BAD_REQUEST, "Error al agregar mantenimientos preventivos.");
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }

    @GetMapping
    public ResponseEntity<?> list() {
        RestResponse response = new RestResponse(HttpStatus.OK, maintenanceService.list());
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }
}

