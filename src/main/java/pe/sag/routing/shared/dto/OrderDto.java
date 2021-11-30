package pe.sag.routing.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.sag.routing.core.model.Roadblock;
import pe.sag.routing.core.model.SimulationInfo;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderDto {
    private int code;
    @NotBlank
    private double demandGLP;
    private double totalDemand;
    @NotBlank
    private int x;
    @NotBlank
    private int y;
    private LocalDateTime registrationDate;
    private LocalDateTime deadlineDate;
    private LocalDateTime deliveryDate;
    private String status;
    private boolean monitoring;

    public boolean inRoadblocks(List<Roadblock> roadblocks) {
        for (Roadblock r : roadblocks) {
            if (r.getX() == x && r.getY() == y) {
                if ( !( deadlineDate.isBefore(r.getStartDate()) || registrationDate.isAfter(r.getEndDate()) ) )
                    return true;
            }
        }
        return false;
    }

}
