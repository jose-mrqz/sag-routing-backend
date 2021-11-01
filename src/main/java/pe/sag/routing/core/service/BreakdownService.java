package pe.sag.routing.core.service;

import org.springframework.stereotype.Service;
import pe.sag.routing.core.model.Breakdown;
import pe.sag.routing.data.repository.BreakdownRepository;

@Service
public class BreakdownService {
    private final BreakdownRepository breakdownRepository;

    public BreakdownService(BreakdownRepository breakdownRepository) {
        this.breakdownRepository = breakdownRepository;
    }

    public Breakdown save(Breakdown breakdown) {
        return breakdownRepository.save(breakdown);
    }

}
