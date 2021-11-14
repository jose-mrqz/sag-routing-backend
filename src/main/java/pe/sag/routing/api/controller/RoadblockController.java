package pe.sag.routing.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.sag.routing.aStar.AStar;
import pe.sag.routing.aStar.ListStructure;
import pe.sag.routing.algorithm.*;
import pe.sag.routing.api.request.SimulationRequest;
import pe.sag.routing.api.response.RestResponse;
import pe.sag.routing.core.model.Roadblock;
import pe.sag.routing.core.model.SimulationInfo;
import pe.sag.routing.core.service.RoadblockService;
import pe.sag.routing.data.parser.RoadblockParser;
import pe.sag.routing.data.repository.SimulationInfoRepository;
import pe.sag.routing.shared.dto.RoadblockDto;
import pe.sag.routing.shared.dto.RouteDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/roadblock")
public class RoadblockController {
    private final RoadblockService roadblockService;
    private final SimulationInfoRepository simulationInfoRepository;

    public RoadblockController(RoadblockService roadblockService, SimulationInfoRepository simulationInfoRepository) {
        this.roadblockService = roadblockService;
        this.simulationInfoRepository = simulationInfoRepository;
    }


    @GetMapping
    protected ResponseEntity<?> getActive() {
        List<Roadblock> roadblocks = roadblockService.findByRange(LocalDateTime.now(), LocalDateTime.now());
        List<RoadblockDto> roadblocksDto = roadblocks.stream().map(RoadblockParser::toDto).collect(Collectors.toList());
        RestResponse response = new RestResponse(HttpStatus.OK, roadblocksDto);
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }

    @GetMapping(path = "/all")
    protected ResponseEntity<?> getAll() {
        List<Roadblock> roadblocks = roadblockService.findAll();
        List<RoadblockDto> roadblocksDto = roadblocks.stream().map(RoadblockParser::toDto).collect(Collectors.toList());
        RestResponse response = new RestResponse(HttpStatus.OK, roadblocksDto);
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }

    @PostMapping(path = "/simulation")
    protected ResponseEntity<?> getAllSimulation(@RequestBody SimulationRequest request) {
        List<SimulationInfo> listSimulationInfo = simulationInfoRepository.findAll();
        if (listSimulationInfo.size() == 0) {
            RestResponse response = new RestResponse(HttpStatus.OK, "Error por no registrar SimulationInfo");
            return ResponseEntity
                    .status(response.getStatus())
                    .body(response);
        }
        List<Roadblock> roadblocks = roadblockService.findSimulation();
        //List<Roadblock> roadblocks = roadblockService.findByDateAfter(listSimulationInfo.get(0).getStartDateTransformed());
        List<Roadblock> transformedRoadblocks = new ArrayList<>();
        for(Roadblock rb : roadblocks){
            Roadblock rbt = roadblockService.transformRoadblock(rb,listSimulationInfo.get(0), request.getSpeed());
            transformedRoadblocks.add(rbt);
        }
        List<RoadblockDto> roadblocksDto = transformedRoadblocks.stream().map(RoadblockParser::toDto).collect(Collectors.toList());
        RestResponse response = new RestResponse(HttpStatus.OK, roadblocksDto);
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }

    @PostMapping
    protected ResponseEntity<?> saveMany(@RequestBody List<RoadblockDto> roadblocksDto) {
        List<Roadblock> roadblocks = roadblocksDto.stream().map(RoadblockParser::fromDto).collect(Collectors.toList());
        roadblocks = roadblockService.saveMany(roadblocks);
        RestResponse response = new RestResponse(HttpStatus.OK, roadblocks);
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }

    @PostMapping(path = "/astar1")
    protected ResponseEntity<?> astar1() {
        AStar aStar = new AStar();
        LocalDateTime startDate = LocalDateTime.now();
        Node nodeStart = new Node(14,23,1);
        Node nodeGoal = new Node(12,8,2);
        List<Roadblock> roadblocks = roadblockService.findAll();
        List<Pair<Integer,Integer>> solutionList = aStar.run(startDate,nodeStart,nodeGoal,roadblocks);

        RestResponse response = new RestResponse(HttpStatus.OK, solutionList);
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }

    @PostMapping(path = "/astar2")
    protected ResponseEntity<?> astar2() {
        AStar aStar = new AStar();
        List<Roadblock> roadblocks = roadblockService.findAll();
        List<Route> routes = new ArrayList<>();

        ArrayList<NodeInfo> nodesInfo = new ArrayList<>();
        OrderInfo orderInfo = new OrderInfo(40,35,"6181658d7378f13a04c4dae5",LocalDateTime.of(2021,11,1,8,23,24),
                12,LocalDateTime.of(2021,11,1,8,23,24));
        nodesInfo.add(orderInfo);
        Route route = new Route("6178bc8205cbc421b0a7bc18", "TB2", LocalDateTime.of(2021,11,1,7,9),
                LocalDateTime.of(2021,11,1,9,47,48), nodesInfo, 5.373333333333333, 12.0);

        routes.add(route);
        //Route route = new Route(truckId=6178bc8205cbc421b0a7bc18, truckCode=TB2, nodesInfo=[OrderInfo(id=6181658d7378f13a04c4dae6, deliveryDate=2021-05-01T11:05:48, deliveredGlp=15.0)], path=null, totalFuelConsumption=4.9833333333333325, totalDelivered=15.0, startDate=2021-05-01T09:47:48, finishDate=2021-05-01T12:33:48));

        List<Order> orders = new ArrayList<>();

        Order order = new Order("181658d7378f13a04c4dae5", 40, 35, 0, 12.0, LocalDateTime.parse("2021-11-01T07:09"), LocalDateTime.parse("2021-11-01T11:09"));
        order.setDeliveryTime(LocalDateTime.parse("2021-11-01T08:23:24"));
        order.setVisited(true);
        order.setUnloadTime(10);
        //Order order = new Order(_id=6181658d7378f13a04c4dae6, demand=0.0, totalDemand=15.0, twOpen=2021-05-01T07:09:10, twClose=2021-05-01T11:09:10, deliveryTime=2021-05-01T11:05:48, visited=true, unloadTime=10);
        orders.add(order);

        List<Route> routesSolution = aStar.run(routes, orders, roadblocks);

        RestResponse response = new RestResponse(HttpStatus.OK, routesSolution);
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }
}
