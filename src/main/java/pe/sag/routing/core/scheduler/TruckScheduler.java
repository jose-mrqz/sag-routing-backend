package pe.sag.routing.core.scheduler;

import lombok.Data;
import org.springframework.stereotype.Component;
import pe.sag.routing.core.model.Truck;
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
    private static HashMap<String, Timer> timerRecord = new HashMap<>();

    public TruckScheduler(TruckRepository truckRepository) {
        this.truckRepository = truckRepository;
    }

    public void scheduleStatusChange(String id, TruckStatus status, LocalDateTime now) {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Optional<Truck> truckOptional = truckRepository.findById(id);
                if (truckOptional.isPresent()) {
                    Truck t = truckOptional.get();
                    t.setStatus(status.toString());
                    truckRepository.save(t);
                }
                timer.cancel();
            }
        };
        long wait = Duration.between(LocalDateTime.now(), now).toMillis();
        timer.schedule(task, wait, Long.MAX_VALUE);
        timerRecord.put(id, timer);
    }
}
