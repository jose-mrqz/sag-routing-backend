package pe.sag.routing.api.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewOrderRequest {
    private int code;
    @NotBlank
    private int x;
    @NotBlank
    private int y;
    @NotBlank
    private double demandGLP;
    @NotBlank
    private int slack;
}
