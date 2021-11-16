package pe.sag.routing.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.sag.routing.api.request.ListOrderRequest;
import pe.sag.routing.api.request.ManyOrdersRequest;
import pe.sag.routing.api.request.NewOrderRequest;
import pe.sag.routing.api.request.SimulationInputRequest;
import pe.sag.routing.api.response.RestResponse;
import pe.sag.routing.core.model.Order;
import pe.sag.routing.core.model.Roadblock;
import pe.sag.routing.core.model.SimulationInfo;
import pe.sag.routing.core.model.User;
import pe.sag.routing.core.service.OrderService;
import pe.sag.routing.core.service.RoadblockService;
import pe.sag.routing.data.parser.OrderParser;
import pe.sag.routing.data.parser.RoadblockParser;
import pe.sag.routing.data.repository.SimulationInfoRepository;
import pe.sag.routing.shared.dto.OrderDto;
import pe.sag.routing.shared.dto.UserDto;
import pe.sag.routing.shared.util.SimulationData;
import pe.sag.routing.shared.util.enums.OrderStatus;
import pe.sag.routing.shared.util.enums.TruckStatus;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/order")
public class OrderController {
    private final OrderService orderService;
    private final RoadblockService roadblockService;
    private final SimulationInfoRepository simulationInfoRepository;
    private final RouteController routeController;

    public OrderController(OrderService orderService, RoadblockService roadblockService, SimulationInfoRepository simulationInfoRepository, RouteController routeController) {
        this.orderService = orderService;
        this.roadblockService = roadblockService;
        this.simulationInfoRepository = simulationInfoRepository;
        this.routeController = routeController;
    }

    @PostMapping
    public ResponseEntity<?> register(@RequestBody NewOrderRequest request) throws IllegalAccessException {
        OrderDto orderDto = OrderDto.builder()
                .x(request.getX())
                .y(request.getY())
                .demandGLP(request.getDemandGLP())
                .totalDemand(request.getDemandGLP())
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

    @PostMapping("/many")
    public ResponseEntity<?> registerMany(@RequestBody ManyOrdersRequest request) throws IllegalAccessException {
        int registered = 0;
        for (ManyOrdersRequest.Order req : request.getOrders()) {
            OrderDto orderDto = OrderDto.builder()
                    .x(req.getX())
                    .y(req.getY())
                    .demandGLP(req.getDemandGLP())
                    .totalDemand(req.getDemandGLP())
                    .registrationDate(LocalDateTime.now())
                    .deadlineDate(LocalDateTime.now().plusHours(req.getSlack()))
                    .build();
            orderService.register(orderDto, true);
            registered++;
        }
        RestResponse response = new RestResponse(HttpStatus.OK, "Se registraron " + registered + " pedidos.");
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }

    @PostMapping(path = "/historic")
    public ResponseEntity<?> insertHistoricOrders(@RequestBody SimulationInputRequest request) throws IllegalAccessException {
        orderService.deleteByMonitoring(false);
        roadblockService.deleteByMonitoring(false);

        List<Roadblock> roadblocks = request.getRoadblocks().stream().map(RoadblockParser::fromDto).collect(Collectors.toList());
        for (Roadblock r : roadblocks) {
            r.setMonitoring(false);
        }
        roadblockService.saveMany(roadblocks);

        //fijar fecha muy menor
        LocalDateTime startDateReal = LocalDateTime.of(2100,1,1,1,0,0);
        ArrayList<OrderDto> ordersDto = new ArrayList<>();
        int inserted = 0;
        for(SimulationInputRequest.SimulationOrder r : request.getOrders()){
            OrderDto orderDto = OrderDto.builder()
                    .x(r.getX())
                    .y(r.getY())
                    .demandGLP(r.getDemandGLP())
                    .totalDemand(r.getDemandGLP())
                    .registrationDate(r.getDate())
                    .deadlineDate(r.getDate().plusHours(r.getSlack()))
                    .build();
            //Revisar si nodo de pedido se encuentra bloqueado
            if(!orderDto.inRoadblocks(roadblocks)){
                ordersDto.add(orderDto);
                inserted++;
            }
        }

        RouteController.simulationData = SimulationData.builder()
                .nOrders(inserted)
                .nScheduled(0)
                .message("Simulacion iniciada con " + inserted +  " pedidos.")
                .finished(false)
                .build();

        if (ordersDto.size() == 0) {
            RestResponse response = new RestResponse(HttpStatus.OK, "Todos los pedidos se encuentran bloqueados.");
            return ResponseEntity
                    .status(response.getStatus())
                    .body(response);
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
        simulationInfo.setStartDateTransformed(LocalDateTime.now()); //considerar margen, preguntar a renzo
        simulationInfoRepository.save(simulationInfo);

        //correr algoritmo
        ResponseEntity<?> responseEntity = routeController.scheduleRoutesSimulation(startDateReal);//probar response
        if(responseEntity.getStatusCode() != HttpStatus.OK){
            return responseEntity;
        }

        boolean responseOK = ordersRegistered.size() != 0;

        RestResponse response;
        if (responseOK) response = new RestResponse(HttpStatus.OK, "Nuevos pedidos agregados correctamente.", simulationInfo);
        else response = new RestResponse(HttpStatus.OK, "Error al agregar pedidos.");
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }

    @PostMapping(path = "/list")
    public ResponseEntity<?> list(@RequestBody ListOrderRequest request) {
        if(request.getFilter() == null){
            RestResponse response = new RestResponse(HttpStatus.OK, "Se debe ingresar el tipo de filtro.");
            return ResponseEntity
                    .status(response.getStatus())
                    .body(response);
        }
        List<OrderDto> orderDtos = orderService.list(request.getFilter(), request.getStartDate(), request.getEndDate());
        RestResponse response = new RestResponse(HttpStatus.OK, orderDtos);
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }

    @GetMapping("/{code}")
    protected ResponseEntity<?> getByCode(@PathVariable int code) throws IllegalAccessException {
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

    @PostMapping(path = "/edit")
    protected ResponseEntity<?> edit(@RequestBody NewOrderRequest request) {
        RestResponse response;
        if(request.getCode()<=0){
            response = new RestResponse(HttpStatus.OK, "Error al editar pedido: no se ha ingresado codigo de pedido.");
            return ResponseEntity
                    .status(response.getStatus())
                    .body(response);
        }

        Order order = orderService.findByCode(request.getCode());
        if(order == null){
            response = new RestResponse(HttpStatus.OK, "Error al editar pedido: pedido no encontrado.");
            return ResponseEntity
                    .status(response.getStatus())
                    .body(response);
        }
        if(!order.getStatus().equals(OrderStatus.PENDIENTE)){
            response = new RestResponse(HttpStatus.OK, "Error al editar pedido: ya ha sido programado o atendido.");
            return ResponseEntity
                    .status(response.getStatus())
                    .body(response);
        }

        Order orderEdited = orderService.edit(order, request);
        if (orderEdited != null) response = new RestResponse(HttpStatus.OK, "Pedido editado correctamente.", orderEdited);
        else response = new RestResponse(HttpStatus.OK, "Error al editar pedido.");
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }

    @PostMapping(path = "/delete")
    protected ResponseEntity<?> delete(@RequestBody NewOrderRequest request) {
        RestResponse response;
        if(request.getCode()<=0){
            response = new RestResponse(HttpStatus.OK, "Error al eliminar pedido: no se ha ingresado codigo de pedido.");
            return ResponseEntity
                    .status(response.getStatus())
                    .body(response);
        }

        Order order = orderService.findByCode(request.getCode());
        if(order == null){
            response = new RestResponse(HttpStatus.OK, "Error al eliminar pedido: pedido no encontrado.");
            return ResponseEntity
                    .status(response.getStatus())
                    .body(response);
        }
        if(!order.getStatus().equals(OrderStatus.PENDIENTE)){
            response = new RestResponse(HttpStatus.OK, "Error al eliminar pedido: ya ha sido programado o atendido.");
            return ResponseEntity
                    .status(response.getStatus())
                    .body(response);
        }

        int count = orderService.deleteByCode(order.getCode());
        if (count == 1) response = new RestResponse(HttpStatus.OK, "Pedido eliminado correctamente.");
        else response = new RestResponse(HttpStatus.OK, "Error al eliminar pedido.");
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }
}
