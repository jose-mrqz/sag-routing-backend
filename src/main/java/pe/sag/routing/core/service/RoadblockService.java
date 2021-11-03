package pe.sag.routing.core.service;

import org.springframework.stereotype.Service;
import pe.sag.routing.core.model.Roadblock;
import pe.sag.routing.core.model.SimulationInfo;
import pe.sag.routing.data.repository.RoadblockRepository;
import pe.sag.routing.shared.dto.RouteDto;

import java.time.LocalDateTime;
import java.util.List;

import static java.time.temporal.ChronoUnit.NANOS;

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
        return roadblockRepository.findByStartDateBeforeAndEndDateAfterAndMonitoring(now, now, true);
    }

    public List<Roadblock> findActive() {
        return findByDateTime(LocalDateTime.now());
    }

    public List<Roadblock> findSimulation() {
        return roadblockRepository.findAllByMonitoring(false);
    }

    public List<Roadblock> findByRange(LocalDateTime startDate, LocalDateTime endDate) {
        return roadblockRepository.findByStartDateBeforeAndEndDateAfterAndMonitoring(startDate, endDate, true);
    }

    public List<Roadblock> findByDateAfter(LocalDateTime endDate) {
        return roadblockRepository.findByEndDateAfter(endDate);
    }

    public List<Roadblock> saveMany(List<Roadblock> roadblocks) {
        return roadblockRepository.saveAll(roadblocks);
    }

    public List<Roadblock> saveManySimulation(List<Roadblock> roadblocks) {
        return roadblockRepository.saveAll(roadblocks);
    }

    public LocalDateTime transformDate(SimulationInfo simulationInfo, int speed, LocalDateTime dateToConvert){
        LocalDateTime simulationStartReal = simulationInfo.getStartDateReal();
        LocalDateTime simulationStartTransform = simulationInfo.getStartDateTransformed();

        long differenceTransformReal = NANOS.between(simulationStartReal, simulationStartTransform);
        dateToConvert = dateToConvert.plusNanos(differenceTransformReal);

        long amountNanos = NANOS.between(simulationStartTransform, dateToConvert);
        amountNanos /= speed;
        LocalDateTime transformedDate = LocalDateTime.of(simulationStartTransform.toLocalDate(),simulationStartTransform.toLocalTime());
        transformedDate = transformedDate.plusNanos(amountNanos);
        return transformedDate;
    }

    public Roadblock transformRoadblock(Roadblock roadblock, SimulationInfo simulationInfo, int speed){
        Roadblock transformedRoadblock = Roadblock.builder()
                .x(roadblock.getX())
                .y(roadblock.getY())
                .startDate(roadblock.getStartDate())
                .endDate(roadblock.getEndDate())
                .monitoring(true)
                .build();

        transformedRoadblock.setStartDate(transformDate(simulationInfo,speed,roadblock.getStartDate()));
        transformedRoadblock.setEndDate(transformDate(simulationInfo,speed,roadblock.getEndDate()));

        return transformedRoadblock;
    }

    public void deleteByMonitoring(boolean b) {
        roadblockRepository.deleteAllByMonitoring(b);
    }
}