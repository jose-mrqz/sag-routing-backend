package pe.sag.routing.core.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.sag.routing.core.model.Truck;
import pe.sag.routing.data.parser.TruckParser;
import pe.sag.routing.data.repository.TruckRepository;
import pe.sag.routing.shared.dto.TruckDto;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

@Service
public class TruckService {
    @Autowired
    private TruckRepository truckRepository;

    public Truck register(TruckDto truckRequest) {
        Truck truck = TruckParser.fromDto(truckRequest);
        truck.setAvailable(true);
        return truckRepository.save(truck);
    }

    public Truck updateAvailable(Truck truck, boolean available) {
        truck.setAvailable(available);
        return truckRepository.save(truck);
    }

    public List<TruckDto> list() {
        return truckRepository.findAll().stream().map(TruckParser::toDto).collect(Collectors.toList());
    }

    public Truck findById(String id) {
        Optional<Truck> ot = truckRepository.findById(id);
        return ot.orElse(null);
    }

    public List<Truck> findByAvailable(boolean available) {
        return truckRepository.findByAvailableOrderByModelDesc(available);
    }

    public void scheduleStatusChange(Truck truck, boolean b, LocalDateTime endTime) {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Optional<Truck> truckOptional = truckRepository.findById(truck.get_id());
                if (truckOptional.isPresent()) {
                    Truck t = truckOptional.get();
                    t.setAvailable(b);
                    truckRepository.save(t);
                }
                timer.cancel();
            }
        };
        long wait = Duration.between(LocalDateTime.now(), endTime).toMillis();
        timer.schedule(task, wait, Long.MAX_VALUE);
    }
}
