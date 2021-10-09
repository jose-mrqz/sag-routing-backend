package pe.sag.routing.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.sag.routing.api.response.RestResponse;
import pe.sag.routing.core.model.Truck;
import pe.sag.routing.core.service.TruckService;
import pe.sag.routing.shared.dto.TruckDto;

@RestController
@RequestMapping("/truck")
public class TruckController {
    @Autowired
    private TruckService truckService;

    @PostMapping
    public ResponseEntity<?> register(@RequestBody TruckDto truckDto) {
        Truck truck = truckService.register(truckDto);
        RestResponse response;
        if (truck == null) response = new RestResponse(HttpStatus.OK, "Error al agregar nuevo camion.");
        else response = new RestResponse(HttpStatus.OK, "Nuevo camion agregado correctamente.", truckDto);
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }

    @GetMapping
    public ResponseEntity<?> list() {
        RestResponse response = new RestResponse(HttpStatus.OK, truckService.list());
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }
}

