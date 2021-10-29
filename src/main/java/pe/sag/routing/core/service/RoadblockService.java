package pe.sag.routing.core.service;

import org.springframework.stereotype.Service;
import pe.sag.routing.core.model.Roadblock;
import pe.sag.routing.data.repository.RoadblockRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RoadblockService {
    private final RoadblockRepository roadblockRepository;

    public RoadblockService(RoadblockRepository roadblockRepository) {
        this.roadblockRepository = roadblockRepository;
    }

    public List<Roadblock> findAll() {
        return roadblockRepository.findAll();
    }

    public List<Roadblock> findByDateTime(LocalDateTime now) {
        return roadblockRepository.findByStartDateBeforeAndEndDateAfter(now, now);
    }

    public List<Roadblock> findActive() {
        return findByDateTime(LocalDateTime.now());
    }

    public List<Roadblock> findByRange(LocalDateTime startDate, LocalDateTime endDate) {
        return roadblockRepository.findByStartDateBeforeAndEndDateAfter(startDate, endDate);
    }
}
