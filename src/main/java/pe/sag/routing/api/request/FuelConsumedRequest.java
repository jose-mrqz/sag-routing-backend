package pe.sag.routing.api.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@NoArgsConstructor
@AllArgsConstructor
@Data

public class FuelConsumedRequest {
    public LocalDateTime startDate;
    public LocalDateTime endDate;
}
