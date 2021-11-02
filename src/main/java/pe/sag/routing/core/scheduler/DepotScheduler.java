package pe.sag.routing.core.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pe.sag.routing.core.model.Depot;
import pe.sag.routing.data.repository.DepotRepository;

import java.util.List;

@Component
public class DepotScheduler {
    private final DepotRepository depotRepository;

    public DepotScheduler(DepotRepository depotRepository) {
        this.depotRepository = depotRepository;
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void refillDepots() {
        List<Depot> depots = depotRepository.findAll();
        depots.forEach(d -> d.setCurrentGlp(d.getGlpCapacity()));
        depotRepository.saveAll(depots);
    }
}
