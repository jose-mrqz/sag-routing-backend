package pe.sag.routing.api.controller;

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
import pe.sag.routing.shared.util.enums.TruckStatus;

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
        String code;
        if (lastNumber < 10) code = request.getCode() + "0" + lastNumber;
        else code = request.getCode() + lastNumber;
        //String code = request.getCode() + lastNumber;

        TruckDto truckDto = new TruckDto(code, truckModel, true, TruckStatus.DISPONIBLE.toString());
        Truck truck1 = truckService.register(truckDto, true);
        Truck truck2 = truckService.register(truckDto, false);
        RestResponse response;
        if (truck1 == null || truck2 == null) response = new RestResponse(HttpStatus.BAD_REQUEST, "Error al agregar nuevo camion.");
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

    @PostMapping(path = "/delete")
    protected ResponseEntity<?> delete(@RequestBody NewTruckRequest request) {
        RestResponse response;
        if(request.getCode() == null){
            response = new RestResponse(HttpStatus.BAD_REQUEST, "Error por no ingresar codigo de camion.");
            return ResponseEntity
                    .status(response.getStatus())
                    .body(response);
        }

        int count = truckService.deleteByCode(request.getCode());
        if (count == 2) response = new RestResponse(HttpStatus.OK, "Camion eliminado correctamente.");
        else if (count == -1) response = new RestResponse(HttpStatus.BAD_REQUEST, "Error al eliminar camion: esta actualmente en recorrido.");
        else response = new RestResponse(HttpStatus.BAD_REQUEST, "Error al eliminar camion.");
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }
}

