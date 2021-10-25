package pe.sag.routing.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.sag.routing.api.request.NewOrderRequest;
import pe.sag.routing.api.response.RestResponse;
import pe.sag.routing.core.model.Order;
import pe.sag.routing.core.model.SimulationInfo;
import pe.sag.routing.core.service.OrderService;
import pe.sag.routing.data.parser.OrderParser;
import pe.sag.routing.data.repository.SimulationInfoRepository;
import pe.sag.routing.shared.dto.OrderDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private OrderService orderService;
    private SimulationInfoRepository simulationInfoRepository;

    @PostMapping
    public ResponseEntity<?> register(@RequestBody NewOrderRequest request) throws IllegalAccessException {
        OrderDto orderDto = OrderDto.builder()
                .x(request.getX())
                .y(request.getY())
                .demandGLP(request.getDemandGLP())
                .registrationDate(LocalDateTime.now())
                .deadlineDate(LocalDateTime.now().plusHours(request.getSlack()))
                .build();
        Order order = orderService.register(orderDto, true);
        RestResponse response;
        if (order == null) response = new RestResponse(HttpStatus.OK, "Error al agregar nuevo pedido.");
        else response = new RestResponse(HttpStatus.OK, "Nuevo pedido agregado correctamente.", order);
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }

    @PostMapping(path = "/historic")
    public ResponseEntity<?> insertHistoricOrders(@RequestBody List<NewOrderRequest> request) throws IllegalAccessException {
        ArrayList<Order> orders = new ArrayList<>();
        ArrayList<Integer> codeOrders = new ArrayList<>();
        boolean responseOK = true;
        for(NewOrderRequest r : request){
            OrderDto orderDto = OrderDto.builder()
                    .x(r.getX())
                    .y(r.getY())
                    .demandGLP(r.getDemandGLP())
                    .registrationDate(LocalDateTime.now())
                    .deadlineDate(LocalDateTime.now().plusHours(r.getSlack()))
                    .build();
            Order order = orderService.register(orderDto, false);
            if (order == null) {
                responseOK = false;
                break;
            }
            codeOrders.add(order.getCode());
            orders.add(order);
        }

        //guardar informacion de simulacion - startDate
        SimulationInfo simulationInfo = new SimulationInfo();
        simulationInfo.setStartDate(LocalDateTime.now());
        simulationInfo.setCodeOrders(codeOrders);//
        simulationInfoRepository.save(simulationInfo);

        RestResponse response;
        if (responseOK) response = new RestResponse(HttpStatus.OK, "Nuevos pedidos agregados correctamente.", orders);
        else response = new RestResponse(HttpStatus.OK, "Error al agregar pedidos.");
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }

    @GetMapping
    public ResponseEntity<?> list() {
        RestResponse response = new RestResponse(HttpStatus.OK, orderService.list());
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }

    @GetMapping("/{code}")
    protected ResponseEntity<?> getByCode(@PathVariable String code) throws IllegalAccessException {
        Order order = orderService.findByCode(code);
        RestResponse response = new RestResponse(HttpStatus.OK, OrderParser.toDto(order));
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }
}
