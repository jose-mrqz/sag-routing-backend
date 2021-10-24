package pe.sag.routing.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.sag.routing.api.request.NewTruckRequest;
import pe.sag.routing.api.response.RestResponse;
import pe.sag.routing.core.model.Truck;
import pe.sag.routing.core.model.TruckModel;
import pe.sag.routing.core.service.TruckModelService;
import pe.sag.routing.core.service.TruckService;
import pe.sag.routing.shared.dto.TruckDto;

@RestController
@RequestMapping("/truck")
public class TruckController {
    private final TruckService truckService;
    private final TruckModelService truckModelService;

    public TruckController(TruckService truckService, TruckModelService truckModelService) {
        this.truckService = truckService;
        this.truckModelService = truckModelService;
    }

    @PostMapping
    public ResponseEntity<?> register(@RequestBody NewTruckRequest request) {
        TruckModel truckModel = truckModelService.getByCode(request.getCode());
        int lastNumber = truckService.getLastCodeByModel(truckModel);
        if (lastNumber == -1) lastNumber = 1;
        else lastNumber++;
        String code = request.getCode() + lastNumber;
        TruckDto truckDto = new TruckDto(code, truckModel, true, true);
        Truck truck1 = truckService.register(truckDto, true);
        Truck truck2 = truckService.register(truckDto, false);
        RestResponse response;
        if (truck1 == null && truck2 == null) response = new RestResponse(HttpStatus.OK, "Error al agregar nuevo camion.");
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

