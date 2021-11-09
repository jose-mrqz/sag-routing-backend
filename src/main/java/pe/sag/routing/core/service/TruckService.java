package pe.sag.routing.core.service;

import org.springframework.stereotype.Service;
import pe.sag.routing.core.model.Truck;
import pe.sag.routing.core.model.TruckModel;
import pe.sag.routing.core.scheduler.TruckScheduler;
import pe.sag.routing.data.parser.TruckParser;
import pe.sag.routing.data.repository.TruckRepository;
import pe.sag.routing.shared.dto.TruckDto;
import pe.sag.routing.shared.util.enums.TruckStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TruckService {
    private final TruckRepository truckRepository;
    private final TruckScheduler truckScheduler;
    private final RouteService routeService;

    public TruckService(TruckRepository truckRepository, TruckScheduler truckScheduler, RouteService routeService) {
        this.truckRepository = truckRepository;
        this.truckScheduler = truckScheduler;
        this.routeService = routeService;
    }

    public Truck register(TruckDto truckRequest, boolean monitoring) {
        Truck truck = TruckParser.fromDto(truckRequest);
        truck.setMonitoring(monitoring);
        truck.setStatus(TruckStatus.DISPONIBLE.toString());
        return truckRepository.save(truck);
    }

    public void updateStatus(Truck truck, String status) {
        truck.setStatus(status);
        truckRepository.save(truck);
    }

    public void updateAvailablesSimulation() {
        List<Truck> trucks = truckRepository.findByMonitoring(false);
        for(Truck t : trucks){
            if(!t.getStatus().equals(TruckStatus.DISPONIBLE.toString())){
                updateStatus(t,TruckStatus.DISPONIBLE.toString());
            }
        }
    }

    public List<TruckDto> list() {
        List<Truck> trucks = truckRepository.findByMonitoring(true);
        for (Truck t : trucks) {
            if (t.getStatus().equals(TruckStatus.DISPONIBLE.toString()) && routeService.getCurrentByTruckId(t.get_id(), true) != null)
                t.setStatus(TruckStatus.RUTA.toString());
        }
        return trucks.stream().map(TruckParser::toDto).collect(Collectors.toList());
    }

    public Truck findById(String id) {
        Optional<Truck> ot = truckRepository.findById(id);
        return ot.orElse(null);
    }

    public void scheduleStatusChange(Truck truck, boolean b, LocalDateTime endTime) {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Optional<Truck> truckOptional = truckRepository.findById(truck.get_id());
                if (truckOptional.isPresent()) {
                    Truck t = truckOptional.get();
                    //t.setAvailable(b);
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

    public List<Truck> findByCode(String code) {
        return truckRepository.findByCode(code);
    }

    public Truck findByCodeAndMonitoring(String code, boolean monitoring) {
        Optional<Truck> truckOptional = truckRepository.findByCodeAndMonitoring(code, monitoring);
        return truckOptional.orElse(null);
    }
    public void registerBreakdown(Truck truck, LocalDateTime now) {
        truck.setStatus(TruckStatus.AVERIADO.toString());
        truckRepository.save(truck);
        truckScheduler.scheduleStatusChange(truck.get_id(), TruckStatus.MANTENIMIENTO, now.plusMinutes(60));
        truckScheduler.scheduleStatusChange(truck.get_id(), TruckStatus.DISPONIBLE, now.plusMinutes(60).plusHours(48));
    }

    public List<Truck> findByMonitoringAndStatus(boolean monitoring, TruckStatus status) {
        return truckRepository.findByMonitoringAndStatusOrderByModelDesc(monitoring, status.toString());
    }

    public int deleteByCode(String code) {
        List<Truck> trucks = findByCode(code);
        for(Truck t : trucks){
            if(!t.getStatus().equals(TruckStatus.DISPONIBLE.toString())){
                return -1;
            }
        }
        return truckRepository.deleteByCode(code);
    }
}
