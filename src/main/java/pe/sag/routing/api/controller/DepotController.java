package pe.sag.routing.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.sag.routing.api.response.RestResponse;
import pe.sag.routing.core.model.Depot;
import pe.sag.routing.core.service.DepotService;
import pe.sag.routing.shared.dto.DepotDto;

@RestController
@RequestMapping("/depot")
public class DepotController {
    private final DepotService depotService;

    public DepotController(DepotService depotService) {
        this.depotService = depotService;
    }

    @PostMapping
    public ResponseEntity<?> register(@RequestBody DepotDto depotDto) {
        depotDto.setCurrentGlp(depotDto.getGlpCapacity());
        Depot depot = depotService.register(depotDto);
        RestResponse response;
        if (depot == null) response = new RestResponse(HttpStatus.OK, "Error al agregar nueva planta.");
        else response = new RestResponse(HttpStatus.OK, "Nueva planta agregada correctamente.", depotDto);
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }

    @GetMapping
    public ResponseEntity<?> list() {
        RestResponse response = new RestResponse(HttpStatus.OK, depotService.list());
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }
}

