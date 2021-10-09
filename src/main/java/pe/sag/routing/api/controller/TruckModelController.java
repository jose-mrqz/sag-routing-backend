package pe.sag.routing.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.sag.routing.api.response.RestResponse;
import pe.sag.routing.core.model.TruckModel;
import pe.sag.routing.core.service.TruckModelService;
import pe.sag.routing.shared.dto.TruckModelDto;

import java.util.List;

@RestController
@RequestMapping("/truckModel")
public class TruckModelController {
    @Autowired
    private TruckModelService truckModelService;

    @PostMapping
    public ResponseEntity<?> register(@RequestBody TruckModelDto truckModelDto) {
        TruckModel truckModel = truckModelService.register(truckModelDto);
        RestResponse response;
        if (truckModel == null) response = new RestResponse(HttpStatus.OK, "Error al agregar nuevo tipo.");
        else response = new RestResponse(HttpStatus.OK, "Nuevo tipo agregado correctamente.", truckModelDto);
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }

    @GetMapping
    public ResponseEntity<?> list() {
        List<TruckModel> models = truckModelService.list();
        RestResponse response = RestResponse.builder()
                .status(HttpStatus.OK)
                .payload(models)
                .build();
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }

    @GetMapping(path = "/dto")
    public ResponseEntity<?> listDto() {
        RestResponse response = RestResponse.builder()
                .status(HttpStatus.OK)
                .payload(truckModelService.listDto())
                .build();
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }
}
