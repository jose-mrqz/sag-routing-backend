package pe.sag.routing.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.sag.routing.api.response.RestResponse;
import pe.sag.routing.core.model.Roadblock;
import pe.sag.routing.data.repository.RoadblockRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController("/roadblock")
public class RoadblockController {
    private RoadblockRepository roadblockRepository;

    public RoadblockController(RoadblockRepository roadblockRepository) {
        this.roadblockRepository = roadblockRepository;
    }

    @GetMapping
    protected ResponseEntity<?> getActive(@RequestParam Optional<LocalDateTime> startDate,
                                          @RequestParam Optional<LocalDateTime> endDate) {
        LocalDateTime start = startDate.orElse(LocalDateTime.now());
        LocalDateTime end = endDate.orElse(LocalDateTime.now());
        List<Roadblock> roadblocks = roadblockRepository.findByStartDateBeforeAndEndDateAfter(start, end);
        RestResponse response = new RestResponse(HttpStatus.OK, roadblocks);
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }
}
