package pe.sag.routing.api.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.sag.routing.shared.dto.MaintenanceDto;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ManyMaintenanceRequest {
    private List<MaintenanceDto> maintenances;
}
