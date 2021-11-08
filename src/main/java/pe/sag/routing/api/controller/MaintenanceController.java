package pe.sag.routing.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.sag.routing.api.request.SimulationInputRequest;
import pe.sag.routing.api.response.RestResponse;
import pe.sag.routing.core.model.Maintenance;
import pe.sag.routing.core.model.Order;
import pe.sag.routing.core.service.MaintenanceService;
import pe.sag.routing.shared.dto.MaintenanceDto;
import pe.sag.routing.shared.dto.OrderDto;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/maintenance")
public class MaintenanceController {
    private final MaintenanceService maintenanceService;

    public MaintenanceController(MaintenanceService maintenanceService) {
        this.maintenanceService = maintenanceService;
    }

    /*@PostMapping
    public ResponseEntity<?> registerMany(@RequestBody List<MaintenanceDto> maintenancesDto) {
        ArrayList<OrderDto> ordersDto = new ArrayList<>();
        for(SimulationInputRequest.SimulationOrder r : request.getOrders()){
            OrderDto orderDto = OrderDto.builder()
                    .x(r.getX())
                    .y(r.getY())
                    .demandGLP(r.getDemandGLP())
                    .totalDemand(r.getDemandGLP())
                    .registrationDate(r.getDate())
                    .deadlineDate(r.getDate().plusHours(r.getSlack()))
                    .build();
            if(!orderDto.inRoadblocks(roadblocks)){
                ordersDto.add(orderDto);
            }
        }

        if(ordersDto.size()==0){
            RestResponse response = new RestResponse(HttpStatus.OK, "Todos los pedidos se encuentran bloqueados.");
            return ResponseEntity
                    .status(response.getStatus())
                    .body(response);
        }

        List<Order> ordersRegistered = orderService.registerAll(ordersDto,false);

        RestResponse response;
        if (maintenance == null) response = new RestResponse(HttpStatus.OK, "Error al agregar nueva planta.");
        else response = new RestResponse(HttpStatus.OK, "Nueva planta agregada correctamente.", maintenanceDto);
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }*/

    @GetMapping
    public ResponseEntity<?> list() {
        RestResponse response = new RestResponse(HttpStatus.OK, maintenanceService.list());
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }
}

