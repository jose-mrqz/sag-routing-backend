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
    @NotBlank
    private double totalDemand;
    @NotBlank
    private int x;
    @NotBlank
    private int y;
    @NotBlank
    private LocalDateTime registrationDate;
    @NotBlank
    private LocalDateTime deadlineDate;
    private LocalDateTime deliveryDate;
    @NotBlank
    private String status;
    @NotBlank
    private boolean monitoring;

    public boolean inRoadblocks(List<Roadblock> roadblocks){
        for (Roadblock r : roadblocks) {
            if(r.getX() == x && r.getY() == y) return true;
        }
        return false;
    }
}
