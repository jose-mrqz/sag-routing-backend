package pe.sag.routing.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.sag.routing.api.response.RestResponse;
import pe.sag.routing.core.model.TruckModel;
import pe.sag.routing.core.service.TruckModelService;
import pe.sag.routing.shared.dto.TruckModelDto;

@RestController
@RequestMapping("/truckModel")
public class TruckModelController {
    @Autowired
    private TruckModelService truckModelService;

    @PostMapping
    public ResponseEntity<?> register(@RequestBody TruckModelDto truckModelDto) {
        TruckModel truckModel = truckModelService.register(truckModelDto);
        RestResponse response;
        if (truckModel == null) response = new RestResponse(HttpStatus.OK, "Error al agregar nuevo tipo de camion.");
        else response = new RestResponse(HttpStatus.OK, "Nuevo tipo de camion agregado correctamente.", truckModelDto);
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }

    @GetMapping
    public ResponseEntity<?> list() {
        RestResponse response = new RestResponse(HttpStatus.OK, truckModelService.list());
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }
}
