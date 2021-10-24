package pe.sag.routing.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.sag.routing.core.model.TruckModel;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TruckDto {
    @NotBlank
    private String code;
    @NotBlank
    private TruckModel model;
    @NotBlank
    private boolean available;
    @NotBlank
    private boolean monitoring;
}
