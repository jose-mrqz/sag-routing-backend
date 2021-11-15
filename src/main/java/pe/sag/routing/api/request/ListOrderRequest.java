package pe.sag.routing.api.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ListOrderRequest {
    private String filter;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
