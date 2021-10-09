package pe.sag.routing.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderDto {
    @NotBlank
    private String code;
    @NotBlank
    private double demandGLP;
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
}
