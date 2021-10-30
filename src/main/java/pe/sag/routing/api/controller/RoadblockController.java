package pe.sag.routing.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.sag.routing.api.response.RestResponse;
import pe.sag.routing.core.model.Roadblock;
import pe.sag.routing.core.service.RoadblockService;
import pe.sag.routing.data.parser.RoadblockParser;
import pe.sag.routing.shared.dto.RoadblockDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/roadblock")
public class RoadblockController {
    private final RoadblockService roadblockService;

    public RoadblockController(RoadblockService roadblockService) {
        this.roadblockService = roadblockService;
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

    @PostMapping
    protected ResponseEntity<?> saveMany(@RequestBody List<RoadblockDto> roadblocksDto) {
        List<Roadblock> roadblocks = roadblocksDto.stream().map(RoadblockParser::fromDto).collect(Collectors.toList());
        roadblocks = roadblockService.saveMany(roadblocks);
        RestResponse response = new RestResponse(HttpStatus.OK, roadblocks);
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }
}
