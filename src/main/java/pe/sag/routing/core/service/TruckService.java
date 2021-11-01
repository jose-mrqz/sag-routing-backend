package pe.sag.routing.core.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.sag.routing.core.model.Truck;
import pe.sag.routing.core.model.TruckModel;
import pe.sag.routing.data.parser.TruckParser;
import pe.sag.routing.data.repository.TruckRepository;
import pe.sag.routing.shared.dto.TruckDto;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TruckService {
    private final TruckRepository truckRepository;

    public TruckService(TruckRepository truckRepository) {
        this.truckRepository = truckRepository;
    }

    public Truck register(TruckDto truckRequest, boolean monitoring) {
        Truck truck = TruckParser.fromDto(truckRequest);
        truck.setMonitoring(monitoring);
        truck.setAvailable(true);
        return truckRepository.save(truck);
    }

    public Truck updateAvailable(Truck truck, boolean available) {
        truck.setAvailable(available);
        return truckRepository.save(truck);
    }

    public void updateAvailablesSimulation() {
        List<Truck> trucks = truckRepository.findByMonitoring(false);
        for(Truck t : trucks){
            if(!t.isAvailable()){
                updateAvailable(t,true);
            }
        }
    }

    public List<TruckDto> list() {
        return truckRepository.findByMonitoring(true).stream().map(TruckParser::toDto).collect(Collectors.toList());
    }

    public Truck findById(String id) {
        Optional<Truck> ot = truckRepository.findById(id);
        return ot.orElse(null);
    }

    public List<Truck> findByAvailableAndMonitoring(boolean available, boolean monitoring) {
        return truckRepository.findByAvailableAndMonitoringOrderByModelDesc(available, monitoring);
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

    public int getLastCodeByModel(TruckModel model) {
        Optional<Truck> truckOptional = truckRepository.findTopByModelOrderByCodeDesc(model);
        if (truckOptional.isPresent()) {
            String code = truckOptional.get().getCode();
            return Integer.parseInt(code.substring(2));
        } else return -1;
    }

    public Truck findByCode(String code) {
        Optional<Truck> truckOptional = truckRepository.findByCode(code);
        return truckOptional.orElse(null);
    }
}
