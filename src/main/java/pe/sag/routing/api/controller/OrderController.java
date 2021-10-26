package pe.sag.routing.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.sag.routing.api.request.NewOrderRequest;
import pe.sag.routing.api.request.SimulationInputRequest;
import pe.sag.routing.api.response.RestResponse;
import pe.sag.routing.core.model.Order;
import pe.sag.routing.core.model.SimulationInfo;
import pe.sag.routing.core.service.OrderService;
import pe.sag.routing.data.parser.OrderParser;
import pe.sag.routing.data.repository.SimulationInfoRepository;
import pe.sag.routing.shared.dto.OrderDto;
import pe.sag.routing.shared.util.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/order")
public class OrderController {
    private final OrderService orderService;
    private final SimulationInfoRepository simulationInfoRepository;
    private final RouteController routeController;

    public OrderController(OrderService orderService, SimulationInfoRepository simulationInfoRepository, RouteController routeController) {
        this.orderService = orderService;
        this.simulationInfoRepository = simulationInfoRepository;
        this.routeController = routeController;
    }

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
    public ResponseEntity<?> insertHistoricOrders(@RequestBody SimulationInputRequest request) throws IllegalAccessException {
        orderService.deleteByMonitoring(false);

        //fijar fecha muy menor
        LocalDateTime startDateReal = LocalDateTime.of(2100,1,1,1,0,0);
        ArrayList<OrderDto> ordersDto = new ArrayList<>();
        for(SimulationInputRequest.SimulationOrder r : request.getOrders()){
            OrderDto orderDto = OrderDto.builder()
                    .x(r.getX())
                    .y(r.getY())
                    .demandGLP(r.getDemandGLP())
                    .registrationDate(r.getDate())
                    .deadlineDate(r.getDate().plusHours(r.getSlack()))
                    .build();
            ordersDto.add(orderDto);
        }

        List<Order> ordersRegistered = orderService.registerAll(ordersDto,false);
        for(Order order : ordersRegistered){
            //menor registration date
            if(order.getRegistrationDate().isBefore(startDateReal)){
                startDateReal = LocalDateTime.of(order.getRegistrationDate().toLocalDate(),order.getRegistrationDate().toLocalTime());
            }
        }

        simulationInfoRepository.deleteAll();
        SimulationInfo simulationInfo = new SimulationInfo();
        simulationInfo.setStartDateReal(startDateReal);
        simulationInfo.setSpeed(request.getSpeed());

        //correr algoritmo
        routeController.scheduleRoutesSimulation(startDateReal);

        simulationInfo.setStartDateTransformed(LocalDateTime.now()); //considerar margen, preguntar a renzo
        simulationInfoRepository.save(simulationInfo);

        boolean responseOK = ordersRegistered.size() != 0;

        RestResponse response;
        if (responseOK) response = new RestResponse(HttpStatus.OK, "Nuevos pedidos agregados correctamente.", simulationInfo);
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

    @GetMapping("/batched")
    protected ResponseEntity<?> getBatched() {
        List<Order> orders = orderService.getBatchedByStatusMonitoring(OrderStatus.PENDIENTE, true);
        List<OrderDto> ordersDto = orders.stream().map(OrderParser::toDto).collect(Collectors.toList());
        RestResponse response = new RestResponse(HttpStatus.OK, ordersDto);
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }
}
