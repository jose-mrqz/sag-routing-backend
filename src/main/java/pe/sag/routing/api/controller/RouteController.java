package pe.sag.routing.api.controller;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRSaver;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;
import pe.sag.routing.algorithm.DepotInfo;
import pe.sag.routing.algorithm.Pair;
import pe.sag.routing.algorithm.Planner;
import pe.sag.routing.api.request.FuelConsumedRequest;
import pe.sag.routing.api.request.SimulationRequest;
import pe.sag.routing.api.response.RestResponse;
import pe.sag.routing.api.response.SimulationResponse;
import pe.sag.routing.core.model.*;
import pe.sag.routing.core.service.*;
import pe.sag.routing.data.parser.RouteParser;
import pe.sag.routing.data.repository.SimulationInfoRepository;
import pe.sag.routing.shared.dto.RouteDto;
import pe.sag.routing.shared.util.SimulationData;
import pe.sag.routing.shared.util.enums.OrderStatus;
import pe.sag.routing.shared.util.enums.TruckStatus;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;

import java.time.format.DateTimeFormatter;
import java.util.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/route")
public class RouteController {
    private final RouteService routeService;
    private final TruckService truckService;
    private final OrderService orderService;
    private final DepotService depotService;
    private final MaintenanceService maintenanceService;
    private final SimulationInfoRepository simulationInfoRepository;
    private final RoadblockService roadblockService;

    public static Thread simulationThread = null;
    public static SimulationData simulationData = null;
    public static SimulationInfo simulationInfo = null;
    public static SimulationHelper simulationHelper = null;
    public static int simulationSpeed = 1;

    public RouteController(RouteService routeService, TruckService truckService,
                           OrderService orderService, DepotService depotService,
                           MaintenanceService maintenanceService, SimulationInfoRepository simulationInfoRepository,
                           RoadblockService roadblockService) {
        this.routeService = routeService;
        this.truckService = truckService;
        this.orderService = orderService;
        this.depotService = depotService;
        this.maintenanceService = maintenanceService;
        this.simulationInfoRepository = simulationInfoRepository;
        this.roadblockService = roadblockService;
    }

    @GetMapping
    protected ResponseEntity<?> getActive() {
        List<Route> activeRoutes = routeService.getActiveRoutes(LocalDateTime.now(),true);
        List<RouteDto> routesDto = activeRoutes.stream().map(RouteParser::toDto).collect(Collectors.toList());
        RestResponse response = new RestResponse(HttpStatus.OK, routesDto);
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }

    @PostMapping(path = "/simulation")
    protected ResponseEntity<?> getActiveSimulation(@RequestBody SimulationRequest request) {
        List<Route> activeRoutes = routeService.findByMonitoring(false);
        activeRoutes = activeRoutes.stream()
                .sorted(Comparator.comparing(Route::getStartDate))
                .collect(Collectors.toList());
        List<RouteDto> routesDto = activeRoutes.stream().map(RouteParser::toDto).collect(Collectors.toList());
        ArrayList<RouteDto> routesDtoFiltered = new ArrayList<>();
        ArrayList<RouteDto> routesTransformedDto = new ArrayList<>();

        //sacar simulation info de bd
        List<SimulationInfo> listSimulationInfo = simulationInfoRepository.findAll();
        LocalDateTime filterDateReal = null;
        if (listSimulationInfo.size() != 0) {
            SimulationInfo simulationInfo = listSimulationInfo.get(0);

            LocalDateTime filterDateTransformed = LocalDateTime.now();
            filterDateReal = routeService.transformDateReverseSpeed(simulationInfo, request.getSpeed(), filterDateTransformed);
            filterDateReal = routeService.transformDateReverse(simulationInfo, filterDateReal);

            for(RouteDto r : routesDto) {
                RouteDto rt = r.transformRoute(simulationInfo);
                rt = rt.transformRouteSpeed(simulationInfo, request.getSpeed());
                if(true/*r.inDateRange(filterDateReal)*/){
                    routesDtoFiltered.add(r);
                    routesTransformedDto.add(rt);
                }
            }
        }

        LocalDateTime last = LocalDateTime.MIN;
        for (RouteDto route : routesTransformedDto) {
            if (route.getEndDate().isAfter(last)) last = route.getEndDate();
        }
        simulationData.setLastRouteEndTime(last);
        SimulationResponse simulationResponse = new SimulationResponse(simulationData, routesDtoFiltered, routesTransformedDto);
        RestResponse response = new RestResponse(HttpStatus.OK, simulationResponse);
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }

    @PostMapping(path = "/routeTableSimulation")
    protected ResponseEntity<?> getRouteTableSimulation() {
        List<Route> activeRoutes = routeService.findByMonitoring(false);
        activeRoutes.sort(Comparator.comparing(Route::getStartDate));
        List<RouteDto> routesDto = activeRoutes.stream().map(RouteParser::toDto).collect(Collectors.toList());
        ArrayList<RouteDto> routesDtoFiltered = new ArrayList<>();

        //sacar simulation info de bd
        List<SimulationInfo> listSimulationInfo = simulationInfoRepository.findAll();
        LocalDateTime filterDateReal = null;
        if (listSimulationInfo.size() != 0) {
            SimulationInfo simulationInfo = listSimulationInfo.get(0);

            LocalDateTime filterDateTransformed = LocalDateTime.now();
            filterDateReal = routeService.transformDateReverseSpeed(simulationInfo, simulationInfo.getSpeed(), filterDateTransformed);
            filterDateReal = routeService.transformDateReverse(simulationInfo, filterDateReal);

            for(RouteDto r : routesDto) {
                if(true/*r.inDateRange(filterDateReal)*/){
                    routesDtoFiltered.add(r);
                }
            }
        }

        RestResponse response = new RestResponse(HttpStatus.OK, routesDtoFiltered);
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }

    @PostMapping
    public ResponseEntity<?> scheduleRoutes() {
        while (true) {
            List<Roadblock> roadblocks = roadblockService.findActive();
            List<Order> pendingOrders = orderService.getBatchedByStatusMonitoring(OrderStatus.PENDIENTE, true);
            if (pendingOrders.size() == 0) break; //no hay mas pedidos que procesar
            List<Truck> availableTrucks = truckService.findByMonitoringAndStatus(true, TruckStatus.DISPONIBLE);
            List<Depot> depots = depotService.getAll();

            LocalDateTime now = LocalDateTime.now();
            for (int i = 0; i < availableTrucks.size(); i++) {
                Truck truck = availableTrucks.get(i);
                Route lastRoute = routeService.getLastRouteByTruckMonitoring(truck, true);
                if (lastRoute != null) {
                    truck.setLastRouteEndTime(lastRoute.getFinishDate());
                }
                else truck.setLastRouteEndTime(now);
                Maintenance maintenance = maintenanceService.closestMaintenance(truck.getCode());
                if (maintenance != null) {
                    truck.setClosestMaintenanceStart(maintenance.getStartDate());
                }
            }

            //mejorar con formato de error: colapso logistico
            if (pendingOrders.size() != 0 && availableTrucks.size() != 0) {
                Planner planner = new Planner(availableTrucks, pendingOrders, roadblocks, depots);
                planner.setSimulation(false);
                planner.run();
                if (planner.getSolutionRoutes() == null) break; //colapso no se pueden planificar rutas
                List<pe.sag.routing.algorithm.Route> solutionRoutes = planner.getSolutionRoutes();
                List<Pair<String, LocalDateTime>> solutionOrders = planner.getSolutionTimes();

                for (pe.sag.routing.algorithm.Route route : solutionRoutes) {
                    for (pe.sag.routing.algorithm.NodeInfo ni : route.getNodesInfo()) {
                        if (ni instanceof DepotInfo) {
                            DepotInfo di = (DepotInfo)ni;
                            for (Depot depot : depots) {
                                if (depot.get_id().compareTo(di.getId()) == 0)
                                    depot.setCurrentGlp(depot.getCurrentGlp() - di.getRefilledGlp());
                            }
                        }
                    }
                }

                for (Depot depot : depots) {
                    depotService.save(depot);
                }

                for (Order o : pendingOrders) {
                    boolean scheduled = false;
                    for (Pair<String, LocalDateTime> delivery : solutionOrders) {
                        //if(delivery.getY() == null) //muere
                        if (delivery.getX().compareTo(o.get_id()) == 0 && delivery.getY() != null) {
                            scheduled = true;
                            break;
                        }
                    }
                    if (scheduled) orderService.updateStatus(o, OrderStatus.PROGRAMADO);
                }
                for (Pair<String,LocalDateTime> delivery : solutionOrders) {
                    orderService.scheduleStatusChange(delivery.getX(), OrderStatus.ENTREGADO, delivery.getY());
                }

                orderService.registerDeliveryDate(pendingOrders,planner.getSolutionOrders());

                for(pe.sag.routing.algorithm.Route sr : solutionRoutes){
                    Route r = new Route(sr);
                    routeService.save(r);
                    if(r.getDepots().size() > 0){
                        for(Route.Depot d : r.getDepots()){
                            depotService.scheduleStatusChange(d.getX(), d.getRefilledGlp(), d.getRefillDate());
                        }
                    }
                }
            }
        }
        RestResponse response = new RestResponse(HttpStatus.OK, "Algoritmo de Monitoreo realizado correctamente.");
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    private static class SimulationScheduler implements Runnable {
        private final RouteService routeService;
        private final TruckService truckService;
        private final OrderService orderService;
        private final LocalDateTime startDateReal;
        private final SimulationInfo simulationInfo;
        private final RoadblockService roadblockService;

        public SimulationScheduler(RouteService routeService, TruckService truckService, OrderService orderService,
                                   RoadblockService roadblockService, LocalDateTime startDateReal, SimulationInfo simulationInfo) {
            this.routeService = routeService;
            this.truckService = truckService;
            this.orderService = orderService;
            this.startDateReal = startDateReal;
            this.simulationInfo = simulationInfo;
            this.roadblockService = roadblockService;
        }

        @Override
        public void run() {
            SimulationData simulationData = RouteController.simulationData;
            while(true) {
//                int wtf = 0;
                LocalDateTime start = startDateReal;
                LocalDateTime end;
                LocalDateTime aux;
                List<Order> pendingOrders;
                LocalDateTime lastOrder = RouteController.simulationHelper.getLastDate();
                while (true) {
                    end = start.plusMinutes(30);
                    pendingOrders = orderService.getByDateSimulation(start, end);
                    aux = start;
                    start = end;
                    if (pendingOrders.size() != 0 || (end.isAfter(lastOrder) || end.isEqual(lastOrder))) break;
//                    wtf++;
//                    if (wtf == 100) break;
                }
//                List<Order> pendingOrders = orderService.getBatchedByStatusMonitoring(OrderStatus.PENDIENTE, false);
                if (pendingOrders.size() == 0) {
                    RouteController.simulationData.setNScheduled(simulationData.getNOrders());
                    RouteController.simulationData.setMessage("Simulacion terminada con exito.");
                    RouteController.simulationData.setFinished(true);
                    break; //no hay mas pedidos que procesar
                }
                List<Truck> availableTrucks = truckService.findByMonitoringAndStatus(false, TruckStatus.DISPONIBLE);

                List<Roadblock> roadblocks = this.roadblockService.findSimulation();
                for (int i = 0; i < availableTrucks.size(); i++) {
                    Truck truck = availableTrucks.get(i);
                    Route lastRoute = routeService.getLastRouteByTruckMonitoring(truck, false);
                    if (lastRoute != null) {
//                        LocalDateTime endTime = routeService.transformDateReverse(simulationInfo, lastRoute.getFinishDate());
                        LocalDateTime endTime = lastRoute.getFinishDate();
                        truck.setLastRouteEndTime(endTime);
                    }
//                    else truck.setLastRouteEndTime(startDateReal);
                    else truck.setLastRouteEndTime(aux);
                }

                if (pendingOrders.size() != 0 && availableTrucks.size() != 0) {
                    Planner planner = new Planner(availableTrucks, pendingOrders, roadblocks, null);
                    planner.setSimulation(true);
                    planner.setNOrders(pendingOrders.size());
                    planner.run();
                    if (planner.getSolutionRoutes() == null || planner.getSolutionRoutes().isEmpty()) {
                        RouteController.simulationData.setFinished(true);
                        RouteController.simulationData.setNScheduled(RouteController.simulationData.getNScheduled() + planner.getNScheduled());
                        RouteController.simulationData.setMessage("No se pueden planificar mas pedidos.");
                        break; //colapso no se pueden planificar rutas
                    }
                    List<pe.sag.routing.algorithm.Route> solutionRoutes = planner.getSolutionRoutes();
                    List<Pair<String, LocalDateTime>> solutionOrders = planner.getSolutionTimes();

                    for (Order o : pendingOrders) {
                        boolean scheduled = false;
                        for (Pair<String, LocalDateTime> delivery : solutionOrders) {
                            if (delivery.getX().compareTo(o.get_id()) == 0 && delivery.getY() != null) {
                                scheduled = true;
                                break;
                            }
                        }
                        if (scheduled) {
                            orderService.updateStatus(o, OrderStatus.PROGRAMADO);
                        }
                    }

                    RouteController.simulationData.setNScheduled(RouteController.simulationData.getNScheduled() + planner.getNScheduled());

                    if (planner.getNOrders() != planner.getNScheduled()) {
                        RouteController.simulationData.setFinished(true);
                        RouteController.simulationData.setMessage("Primer pedido sin planificar: " + planner.getFirstFailed().getIdx());

                        pe.sag.routing.algorithm.Order order = planner.getFirstFailed();
                        LocalDateTime transformed = routeService.transformDate(RouteController.simulationInfo, order.getTwOpen());
                        transformed = routeService.transformDateSpeed(RouteController.simulationInfo, RouteController.simulationSpeed, transformed);
                        RouteController.simulationData.setOrder(order, transformed);
                        break;
                    }
                    for (pe.sag.routing.algorithm.Route sr : solutionRoutes) {
                        Route r = new Route(sr);
                        r.setMonitoring(false);
                        //r = routeService.transformRoute(r,simulationInfo);
                        routeService.save(r);
                    }
                }
            }
            for (pe.sag.routing.algorithm.Depot d : RouteController.simulationHelper.getDepots()) {
                System.out.println(d.getId());
                d.getRemainingGlp().forEach((key, val) -> System.out.println("\t" + key + " --- " + val));
            }
        }
    }

    @PostMapping(path = "/simulationAlgorithm")
    public ResponseEntity<?> scheduleRoutesSimulation(LocalDateTime startDateReal) {
        if (simulationThread != null && simulationThread.isAlive()) simulationThread.interrupt();
        routeService.deleteByMonitoring(false);
        truckService.updateAvailablesSimulation();
        List<Roadblock> roadblocks = roadblockService.findSimulation();
        List<SimulationInfo> listSimulationInfo = simulationInfoRepository.findAll();
        if (listSimulationInfo.size() == 0) {
            RestResponse response = new RestResponse(HttpStatus.BAD_REQUEST, "Error por no registrar SimulationInfo");
            return ResponseEntity
                    .status(response.getStatus())
                    .body(response);
        }
        SimulationInfo simulationInfo = listSimulationInfo.get(0);
        RouteController.simulationInfo = simulationInfo;

        LocalDateTime start = RouteController.simulationHelper.getStartDate();
        LocalDateTime end;
        List<Order> pendingOrders;
        LocalDateTime lastOrder = RouteController.simulationHelper.getLastDate();
        while (true) {
            end = start.plusMinutes(30);
            pendingOrders = orderService.getByDateSimulation(start, end);
            start = end;
            if (pendingOrders.size() != 0 || (end.isAfter(lastOrder) || end.isEqual(lastOrder))) break;
        }

        List<Truck> availableTrucks = truckService.findByMonitoringAndStatus(false, TruckStatus.DISPONIBLE);
//        List<Order> pendingOrders = orderService.getBatchedByStatusMonitoring(OrderStatus.PENDIENTE, false);

        for (Truck truck : availableTrucks) {
            Route lastRoute = routeService.getLastRouteByTruckMonitoring(truck, false);
            if (lastRoute != null) truck.setLastRouteEndTime(lastRoute.getFinishDate());
            else truck.setLastRouteEndTime(startDateReal);
//            else truck.setLastRouteEndTime(end);
        }

        if (pendingOrders.size() != 0 && availableTrucks.size() != 0) {
            Planner planner = new Planner(availableTrucks, pendingOrders, roadblocks, null);
            planner.setSimulation(true);
            planner.setNOrders(pendingOrders.size());
            planner.run();
            List<pe.sag.routing.algorithm.Route> solutionRoutes = planner.getSolutionRoutes();
            List<Pair<String, LocalDateTime>> solutionOrders = planner.getSolutionTimes();

            for(Order o : pendingOrders){
                boolean scheduled = false;
                for (Pair<String, LocalDateTime> delivery : solutionOrders) {
                    if (delivery.getX().compareTo(o.get_id()) == 0 && delivery.getY() != null) {
                        scheduled = true;
                        break;
                    }
                }
                if (scheduled) {
                    orderService.updateStatus(o, OrderStatus.PROGRAMADO);
                }
            }
            RouteController.simulationData.setNScheduled(RouteController.simulationData.getNScheduled() + planner.getNScheduled());

            if (solutionRoutes != null) {
                for(pe.sag.routing.algorithm.Route sr : solutionRoutes){
                    Route r = new Route(sr);
                    r.setMonitoring(false);
                    //r = routeService.transformRoute(r, simulationInfo);
                    routeService.save(r);
                }
            }

            if (planner.getNOrders() != planner.getNScheduled()) {
                RestResponse response = new RestResponse(HttpStatus.OK, "Pedidos sin planificar primera corrida.");
                simulationData.setFinished(true);
                simulationData.setMessage("Primer pedido sin planificar: " + planner.getFirstFailed().get_id());

                pe.sag.routing.algorithm.Order order = planner.getFirstFailed();
                LocalDateTime transformed = routeService.transformDate(RouteController.simulationInfo, order.getTwOpen());
                transformed = routeService.transformDateSpeed(RouteController.simulationInfo, RouteController.simulationSpeed, transformed);

                RouteController.simulationData.setOrder(order, transformed);
                return ResponseEntity.status(response.getStatus()).body(response);
            }

            Thread thread = new Thread(new SimulationScheduler(routeService, truckService, orderService,
                    roadblockService, startDateReal, simulationInfo));
            simulationThread = thread;
            thread.start();
        }
        else {
            RestResponse response = new RestResponse(HttpStatus.BAD_REQUEST, "Error: no hay pedidos o camiones.");
            return ResponseEntity.status(response.getStatus()).body(response);
        }

        RestResponse response = new RestResponse(HttpStatus.OK, "Algoritmo de Simulacion realizado correctamente.");
        return ResponseEntity.status(response.getStatus()).body(response);
    }


    @PostMapping(path = "/fuelConsumed")
    public ResponseEntity<?> getFuelConsumed(@RequestBody FuelConsumedRequest request) {
        return ResponseEntity
                .status(HttpStatus.I_AM_A_TEAPOT)
                .body(routeService.getFuelConsumedPerDay(request.startDate, request.endDate));
    }

    @PostMapping(path = "/glpRefilled")
    public ResponseEntity<?> getGlpRefilled(@RequestBody FuelConsumedRequest request) {
        return ResponseEntity
                .status(HttpStatus.I_AM_A_TEAPOT)
                .body(routeService.getGlpRefilledPerDay(request.startDate, request.endDate));
    }

    @RequestMapping(path = "/reportOilSpending")
    @ResponseBody
    public void reportOilSpendingIntoDate(@RequestBody FuelConsumedRequest request, HttpServletResponse response) throws  Exception, JRException {

        List<FuelConsume> oilSpending = routeService.getFuelConsumedPerDay(request.getStartDate(), request.getEndDate());


        JRBeanCollectionDataSource beanCollectionDataSource = new JRBeanCollectionDataSource(oilSpending);

        JRDataSource compileReportEmpty = new JREmptyDataSource(1);


        //File file  = ResourceUtils.getFile("classpath:reportes/ReporteConsumoPetroleoDiario.jrxml");
        //JasperReport compileReport = JasperCompileManager.compileReport(new FileInputStream(file.getAbsolutePath()));

        InputStream resource = getClass().getResourceAsStream("/ReporteConsumoPetroleoDiario.jrxml");
        JasperReport compileReport = JasperCompileManager.compileReport(resource);
        JRSaver.saveObject(compileReport, "ReporteConsumoPetroleoDiario.jasper");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String fechaInicial = request.getStartDate().format(formatter);
        String fechaFinal = request.getEndDate().format(formatter);

        HashMap<String,Object> map = new HashMap<>();
        map.put("fechaInicial", fechaInicial);
        map.put("fechaFinal", fechaFinal);
        map.put("dataSetPetroleoDiario", beanCollectionDataSource);

        JasperPrint report = JasperFillManager.fillReport(compileReport, map, compileReportEmpty);

        byte[] data = JasperExportManager.exportReportToPdf(report);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=reporteConsumoPetroleoXdia.pdf");
        response.setHeader("Content-Transfer-Encoding", "binary");

        BufferedOutputStream bos = new BufferedOutputStream(response.getOutputStream());
        bos.write(data,0,data.length);
        bos.close();
        response.flushBuffer();

    }

    @RequestMapping(path = "/reportGLPSpending")
    @ResponseBody
    public void reportGLPSpendingIntoDate(@RequestBody FuelConsumedRequest request, HttpServletResponse response) throws  Exception, JRException {

        List<GlpRefill> glpSpending = routeService.getGlpRefilledPerDay(request.getStartDate(), request.getEndDate());

        JRBeanCollectionDataSource beanCollectionDataSource = new JRBeanCollectionDataSource(glpSpending);

        JRDataSource compileReportEmpty = new JREmptyDataSource(1);

        //File file  = ResourceUtils.getFile("classpath:reportes/ReporteConsumoGLP.jrxml");
        //JasperReport compileReport = JasperCompileManager.compileReport(new FileInputStream(file.getAbsolutePath()));

        InputStream resource = getClass().getResourceAsStream("/ReporteConsumoGLP.jrxml");
        JasperReport compileReport = JasperCompileManager.compileReport(resource);
        JRSaver.saveObject(compileReport, "ReporteConsumoGLP.jasper");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String fechaInicial = request.getStartDate().format(formatter);
        String fechaFinal = request.getEndDate().format(formatter);

        HashMap<String,Object> map = new HashMap<>();
        map.put("fechaInicial", fechaInicial);
        map.put("fechaFinal", fechaFinal);
        map.put("dataSetGLPConsumo", beanCollectionDataSource);

        JasperPrint report = JasperFillManager.fillReport(compileReport, map, compileReportEmpty);

        byte[] data = JasperExportManager.exportReportToPdf(report);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=reporteConsumoGLP.pdf");
        response.setHeader("Content-Transfer-Encoding", "binary");

        BufferedOutputStream bos = new BufferedOutputStream(response.getOutputStream());
        bos.write(data,0,data.length);
        bos.close();
        response.flushBuffer();
    }
}
