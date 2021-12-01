package pe.sag.routing.core.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pe.sag.routing.core.model.Depot;
import pe.sag.routing.core.model.Order;
import pe.sag.routing.data.repository.DepotRepository;
import pe.sag.routing.shared.util.enums.OrderStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Component
public class DepotScheduler {
    private final DepotRepository depotRepository;
    private static HashMap<String, Timer> timerRecord = new HashMap<>();

    public DepotScheduler(DepotRepository depotRepository) {
        this.depotRepository = depotRepository;
    }

    public void scheduleStatusChange(String id, double glpConsumed, LocalDateTime dateTime) {
        if (dateTime == null) return; //error handle
        //Timer currentTimer = timerRecord.getOrDefault(id, null);
        //if (currentTimer != null) currentTimer.cancel();
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Optional<Depot> depotOptional = depotRepository.findById(id);
                if (depotOptional.isPresent()) {
                    Depot d = depotOptional.get();
                    d.setCurrentGlpReal(d.getCurrentGlp() - glpConsumed);
                    depotRepository.save(d);
                }
                timerRecord.remove(id);
                timer.cancel();
            }
        };
        long wait = Duration.between(LocalDateTime.now(), dateTime).toMillis();
        timer.schedule(task, wait, Long.MAX_VALUE);
        timerRecord.put(id, timer);
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void refillDepots() {
        List<Depot> depots = depotRepository.findAll();
        depots.forEach(d -> d.setCurrentGlp(d.getGlpCapacity()));
        depotRepository.saveAll(depots);
    }
}
