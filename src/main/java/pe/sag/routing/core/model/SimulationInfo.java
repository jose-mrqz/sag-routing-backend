package pe.sag.routing.core.model;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SimulationInfo {
    private LocalDateTime startDateReal;
    private LocalDateTime startDateTransformed;
}

