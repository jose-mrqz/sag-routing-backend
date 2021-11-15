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
import java.util.*;

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

    public void scheduleStatusChange(String id, TruckStatus status, LocalDateTime now, String idMaintenance) {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Optional<Truck> truckOptional = truckRepository.findById(id);
                if (truckOptional.isPresent()) {
                    Truck t = truckOptional.get();
                    if(idMaintenance.equals("")){
                        t.setStatus(status.toString());
                        truckRepository.save(t);
                    }
                    else{
                        Maintenance maintenance = maintenanceRepository.findBy_id(idMaintenance);
                        //Antes: MANTENIMIENTO, Despues: DISPONIBLE -> flujo principal
                        if(t.getStatus().equals(TruckStatus.MANTENIMIENTO.toString()) && status.equals(TruckStatus.DISPONIBLE)){
                            if(!maintenance.isFinished()){
                                maintenance.setFinished(true);
                                maintenanceRepository.save(maintenance);

                                t.setStatus(status.toString());
                                truckRepository.save(t);
                            }
                        }
                        //Antes: AVERIADO, Despues: MANTENIMIENTO -> camion averiado cuando le toca mantenimiento preventivo
                        else if( t.getStatus().equals(TruckStatus.AVERIADO.toString()) && status.equals(TruckStatus.MANTENIMIENTO) ){
                            maintenance.setFinished(true);
                            maintenanceRepository.save(maintenance);
                        }
                        //Antes: MANTENIMIENTO, Despues: MANTENIMIENTO -> camion en mantenimiento cuando
                        //le toca mantenimiento preventivo
                        else if(t.getStatus().equals(TruckStatus.MANTENIMIENTO.toString()) && status.equals(TruckStatus.MANTENIMIENTO) ){
                            maintenance.setFinished(true);
                            maintenanceRepository.save(maintenance);
                        }
                        else{
                            if(!maintenance.isFinished()){
                                t.setStatus(status.toString());
                                truckRepository.save(t);
                            }
                        }
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
