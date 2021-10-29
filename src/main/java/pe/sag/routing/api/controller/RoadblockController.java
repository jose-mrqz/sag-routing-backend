package pe.sag.routing.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.sag.routing.api.response.RestResponse;
import pe.sag.routing.data.repository.RoadblockRepository;

import java.time.LocalDateTime;

@RestController("/roadblock")
public class RoadblockController {
    private RoadblockRepository roadblockRepository;

    public RoadblockController(RoadblockRepository roadblockRepository) {
        this.roadblockRepository = roadblockRepository;
    }

    @GetMapping
    protected ResponseEntity<?> getActive(@RequestParam LocalDateTime now) {
    }
}
