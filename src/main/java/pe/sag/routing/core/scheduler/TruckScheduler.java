package pe.sag.routing.core.scheduler;

import lombok.Data;
import org.springframework.stereotype.Component;
import pe.sag.routing.core.model.Maintenance;
import pe.sag.routing.core.model.Truck;
import pe.sag.routing.core.service.MaintenanceService;
import pe.sag.routing.data.repository.MaintenanceRepository;
import pe.sag.routing.data.repository.TruckRepository;
import pe.sag.routing.shared.util.enums.TruckStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

@Component
@Data
public class TruckScheduler {
    public final TruckRepository truckRepository;
    public final MaintenanceRepository maintenanceRepository;
    private static HashMap<String, Timer> timerRecord = new HashMap<>();

    public TruckScheduler(TruckRepository truckRepository, MaintenanceRepository maintenanceRepository) {
        this.truckRepository = truckRepository;
        this.maintenanceRepository = maintenanceRepository;
    }

    public void scheduleStatusChange(String id, TruckStatus status, LocalDateTime now) {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Optional<Truck> truckOptional = truckRepository.findById(id);
                if (truckOptional.isPresent()) {
                    Truck t = truckOptional.get();
                    if(t.getStatus().equals(TruckStatus.MANTENIMIENTO.toString()) && status.equals(TruckStatus.DISPONIBLE)){
                        Maintenance maintenance = maintenanceRepository.findAllByTruckCodeOrderByStartDateAsc(t.getCode()).get(0);
                        maintenance.setFinished(true);
                        maintenanceRepository.save(maintenance);

                        t.setStatus(status.toString());
                        truckRepository.save(t);
                    }
                    else if(t.getStatus().equals(TruckStatus.AVERIADO.toString()) && status.equals(TruckStatus.MANTENIMIENTO)){
                        Maintenance maintenance = maintenanceRepository.findAllByTruckCodeOrderByStartDateAsc(t.getCode()).get(0);
                        maintenance.setFinished(true);
                        maintenanceRepository.save(maintenance);
                        //revisar el hashmap y cancelar el timer de regreso a DISPONIBLE
                    }
                    else{
                        t.setStatus(status.toString());
                        truckRepository.save(t);
                    }
                }
                timer.cancel();
            }
        };
        long wait = Duration.between(LocalDateTime.now(), now).toMillis();
        timer.schedule(task, wait, Long.MAX_VALUE);
        timerRecord.put(id, timer);
    }
}
