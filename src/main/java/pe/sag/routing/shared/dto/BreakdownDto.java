package pe.sag.routing.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BreakdownDto {
    private int x;
    private int y;
    private String truckCode;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
