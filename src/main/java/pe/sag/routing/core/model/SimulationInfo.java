package pe.sag.routing.core.model;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Document
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SimulationInfo {
    private LocalDateTime startDate;
    private ArrayList<Integer> codeOrders;
}

