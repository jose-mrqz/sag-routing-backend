package pe.sag.routing.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Document
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class GlpRefill {
    private LocalDateTime date;
    private String depotName;
    private double glpRefilled;
    private String truckCode;
}

