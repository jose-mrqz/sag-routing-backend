package pe.sag.routing.api.controller;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRSaver;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;
import pe.sag.routing.api.request.*;
import pe.sag.routing.api.response.RestResponse;
import pe.sag.routing.core.model.Order;
import pe.sag.routing.core.model.Roadblock;
import pe.sag.routing.core.model.SimulationHelper;
import pe.sag.routing.core.model.SimulationInfo;
import pe.sag.routing.core.service.FileService;
import pe.sag.routing.core.service.OrderService;
import pe.sag.routing.core.service.RoadblockService;
import pe.sag.routing.data.parser.OrderParser;
import pe.sag.routing.data.parser.RoadblockParser;
import pe.sag.routing.data.repository.SimulationInfoRepository;
import pe.sag.routing.shared.dto.OrderDto;
import pe.sag.routing.shared.util.SimulationData;
import pe.sag.routing.shared.util.enums.OrderStatus;

import java.io.FileInputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipOutputStream;

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
        List<Roadblock> roadblocks = roadblockService.findAllByMonitoring(true);
        OrderDto orderDto = OrderDto.builder()
                .x(request.getX())
                .y(request.getY())
                .demandGLP(request.getDemandGLP())
                .totalDemand(request.getDemandGLP())
                .registrationDate(LocalDateTime.now())
                .deadlineDate(LocalDateTime.now().plusHours(request.getSlack()))
                .build();

        Order order;
        //Revisar si nodo de pedido se encuentra bloqueado
        if(!orderDto.inRoadblocks(roadblocks)) order = orderService.register(orderDto, true);
        else order = null;

        RestResponse response;
        if (order == null) response = new RestResponse(HttpStatus.BAD_REQUEST, "Error al agregar nuevo pedido.");
        else response = new RestResponse(HttpStatus.OK, "Nuevo pedido agregado correctamente.", order);
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }

    @PostMapping("/many")
    public ResponseEntity<?> registerMany(@RequestBody ManyOrdersRequest request) throws IllegalAccessException {
        List<Roadblock> roadblocks = roadblockService.findAllByMonitoring(true);
        ArrayList<OrderDto> ordersDto = new ArrayList<>();
        for (ManyOrdersRequest.Order req : request.getOrders()) {
            OrderDto orderDto = OrderDto.builder()
                    .x(req.getX())
                    .y(req.getY())
                    .demandGLP(req.getDemandGLP())
                    .totalDemand(req.getDemandGLP())
                    .registrationDate(LocalDateTime.now())
                    .deadlineDate(LocalDateTime.now().plusHours(req.getSlack()))
                    .build();
            //Revisar si nodo de pedido se encuentra bloqueado
            if(!orderDto.inRoadblocks(roadblocks)){
                ordersDto.add(orderDto);
            }
        }

        if (ordersDto.size() == 0) {
            RestResponse response = new RestResponse(HttpStatus.BAD_REQUEST, "Todos los pedidos se encuentran bloqueados.");
            return ResponseEntity
                    .status(response.getStatus())
                    .body(response);
        }

        List<Order> ordersRegistered = orderService.registerAll(ordersDto,true);

        RestResponse response;
        if (ordersRegistered != null) response = new RestResponse(HttpStatus.OK, "Nuevos pedidos agregados correctamente.", ordersRegistered);
        else response = new RestResponse(HttpStatus.BAD_REQUEST, "Error al agregar pedidos.");
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }

    @PostMapping(path = "/historic")
    public ResponseEntity<?> insertHistoricOrders(@RequestBody SimulationInputRequest request) throws IllegalAccessException {
        orderService.deleteByMonitoring(false);
        roadblockService.deleteByMonitoring(false);
        RouteController.simulationSpeed = request.getSpeed();

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
        RouteController.simulationHelper = new SimulationHelper(request.isColapse());

        if (ordersDto.size() == 0) {
            RestResponse response = new RestResponse(HttpStatus.BAD_REQUEST, "Todos los pedidos se encuentran bloqueados.");
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
        RouteController.simulationSpeed = request.getSpeed();
        ResponseEntity<?> responseEntity = routeController.scheduleRoutesSimulation(startDateReal);//probar response
        if(responseEntity.getStatusCode() != HttpStatus.OK){
            return responseEntity;
        }

        boolean responseOK = ordersRegistered.size() != 0;

        RestResponse response;
        if (responseOK) response = new RestResponse(HttpStatus.OK, "Nuevos pedidos agregados correctamente.", simulationInfo);
        else response = new RestResponse(HttpStatus.BAD_REQUEST, "Error al agregar pedidos.");
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }

    @PostMapping(path = "/list")
    public ResponseEntity<?> list(@RequestBody ListOrderRequest request) {
        if(request.getFilter() == null){
            RestResponse response = new RestResponse(HttpStatus.BAD_REQUEST, "Error al listar pedidos: se debe ingresar el tipo de filtro.");
            return ResponseEntity
                    .status(response.getStatus())
                    .body(response);
        }
        List<OrderDto> orderDtos = orderService.list(request.getFilter(), request.getStartDate(), request.getEndDate());
        RestResponse response = new RestResponse(HttpStatus.OK, "Pedidos listados correctamente.", orderDtos);
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
            response = new RestResponse(HttpStatus.BAD_REQUEST, "Error al editar pedido: no se ha ingresado codigo de pedido.");
            return ResponseEntity
                    .status(response.getStatus())
                    .body(response);
        }

        Order order = orderService.findByCode(request.getCode());
        if(order == null){
            response = new RestResponse(HttpStatus.BAD_REQUEST, "Error al editar pedido: pedido no encontrado.");
            return ResponseEntity
                    .status(response.getStatus())
                    .body(response);
        }
        if(!order.getStatus().equals(OrderStatus.PENDIENTE)){
            response = new RestResponse(HttpStatus.BAD_REQUEST, "Error al editar pedido: ya ha sido programado o entregado.");
            return ResponseEntity
                    .status(response.getStatus())
                    .body(response);
        }
        //Revisar si nodo de pedido se encuentra bloqueado
        List<Roadblock> roadblocks = roadblockService.findAllByMonitoring(true);
        if(order.inRoadblocks(roadblocks)){
            response = new RestResponse(HttpStatus.BAD_REQUEST, "Error al editar pedido: ubicacion de pedido se encuentra bloqueada.");
            return ResponseEntity
                    .status(response.getStatus())
                    .body(response);
        }

        Order orderEdited = orderService.edit(order, request);
        if (orderEdited != null) response = new RestResponse(HttpStatus.OK, "Pedido editado correctamente.", orderEdited);
        else response = new RestResponse(HttpStatus.BAD_REQUEST, "Error al editar pedido.");
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }

    @PostMapping(path = "/delete")
    protected ResponseEntity<?> delete(@RequestBody NewOrderRequest request) {
        RestResponse response;
        if(request.getCode()<=0){
            response = new RestResponse(HttpStatus.BAD_REQUEST, "Error al eliminar pedido: no se ha ingresado codigo de pedido.");
            return ResponseEntity
                    .status(response.getStatus())
                    .body(response);
        }

        Order order = orderService.findByCode(request.getCode());
        if(order == null){
            response = new RestResponse(HttpStatus.BAD_REQUEST, "Error al eliminar pedido: pedido no encontrado.");
            return ResponseEntity
                    .status(response.getStatus())
                    .body(response);
        }
        if(!order.getStatus().equals(OrderStatus.PENDIENTE)){
            response = new RestResponse(HttpStatus.BAD_REQUEST, "Error al eliminar pedido: ya ha sido programado o atendido.");
            return ResponseEntity
                    .status(response.getStatus())
                    .body(response);
        }

        int count = orderService.deleteByCode(order.getCode());
        if (count == 1) response = new RestResponse(HttpStatus.OK, "Pedido eliminado correctamente.");
        else response = new RestResponse(HttpStatus.BAD_REQUEST, "Error al eliminar pedido.");
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }


    @RequestMapping(path = "/future/{months}")
    @ResponseBody
    public void generateFutureOrders(@PathVariable("months") Integer months, HttpServletResponse response) throws IOException {
        LocalDateTime startDate = LocalDateTime.of(2021,11,16,0,0,0);
        LocalDateTime endDate = startDate.plusMonths(months); //minimo 6 meses

        //generar pedidos futuros
        ArrayList<OrderDto> futureOrders = orderService.generateFutureOrders(startDate,endDate);

        //generar un archivo txt por mes a partir de futureOrders
        List<FileWriter> files = orderService.generateFile(futureOrders);

        if (files != null)  {
            String sourceFile = System.getProperty("user.dir") + "/files/orders";
            FileOutputStream fos = new FileOutputStream(System.getProperty("user.dir") + "/files/orders.zip");
            ZipOutputStream zipOut = new ZipOutputStream(fos);
            File fileToZip = new File(sourceFile);

            FileService.zipFile(fileToZip, fileToZip.getName(), zipOut);
            zipOut.close();
            fos.close();

            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=orders.zip");
            response.setHeader("Content-Transfer-Encoding", "binary");

            BufferedOutputStream bos = new BufferedOutputStream(response.getOutputStream());
            FileInputStream fis = new FileInputStream(System.getProperty("user.dir") + "/files/orders.zip");
            int len;
            byte[] buf = new byte[1024];
            while((len = fis.read(buf)) > 0) {
                bos.write(buf,0,len);
            }
            bos.close();
            response.flushBuffer();
        }
    }


    @RequestMapping(path = "/reportOrders")
    @ResponseBody
    public void reportOrdersIntoDate(@RequestBody ListOrderRequest request, HttpServletResponse response ) throws  Exception, JRException {
        //List<OrderDto> ordersDto = orderService.list("todos",request.getStartDate(), request.getEndDate());
        //List<Order> orders = ordersDto.stream().map(OrderParser::fromDto).collect(Collectors.toList());

        List<Order> orders = orderService.findByDateRange(request.getStartDate(), request.getEndDate());
        

        JRBeanCollectionDataSource beanCollectionDataSource = new JRBeanCollectionDataSource(orders);
        //JRBeanArrayDataSource beanCollectionDataSource = new JRBeanArrayDataSource(orders.toArray());

        JRDataSource compileReportEmpty = new JREmptyDataSource(1);
        //JasperReport compileReport = JasperCompileManager.compileReport(new FileInputStream(System.getProperty("user.dir") + "/reportes/ReportePedidos.jrxml"));
//        File file  = ResourceUtils.getFile("classpath:reportes/ReportePedidos.jrxml");
//        File file  = ResourceUtils.getFile("/home/arch/reportes/ReportePedidos.jrxml");
        InputStream resource = getClass().getResourceAsStream("/ReportePedidos.jrxml");
//        URL jarUrl = new URL("jar:file:/home/arch/sag-routing-backend/target/routing-0.0.1-SNAPSHOT.jar!/BOOT-INF/classes!/reportes/ReportePedidos.jrxml");
//        JasperReport compileReport = JasperCompileManager.compileReport(new FileInputStream(file.getAbsolutePath()));
        JasperReport compileReport = JasperCompileManager.compileReport(resource);
        JRSaver.saveObject(compileReport, "ReportePedidos.jasper");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String fechaInicial = request.getStartDate().format(formatter);
        String fechaFinal = request.getEndDate().format(formatter);

        HashMap<String,Object> map = new HashMap<>();
        map.put("fechaInicial", fechaInicial);
        map.put("fechaFinal", fechaFinal);
        map.put("dataSetPedidos", beanCollectionDataSource);

        JasperPrint report = JasperFillManager.fillReport(compileReport, map, compileReportEmpty);
        //JasperExportManager.exportReportToPdfFile(report, "reportePedidos.pdf");
        byte[] data = JasperExportManager.exportReportToPdf(report);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=reportePedidos.pdf");
        response.setHeader("Content-Transfer-Encoding", "binary");

        BufferedOutputStream bos = new BufferedOutputStream(response.getOutputStream());
        bos.write(data,0,data.length);
        bos.close();
        response.flushBuffer();

    }
}
