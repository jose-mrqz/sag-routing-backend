package pe.sag.routing.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Roadblock {
    int x;
    int y;
    LocalDateTime startDate;
    LocalDateTime endDate;
}
