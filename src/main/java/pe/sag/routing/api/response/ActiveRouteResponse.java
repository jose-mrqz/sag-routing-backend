package pe.sag.routing.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.sag.routing.algorithm.Pair;
import pe.sag.routing.shared.dto.OrderDto;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ActiveRouteResponse {
    private LocalDateTime startDate;
    private double velocity;
    private List<OrderDto> orders;
    private List<Pair<Integer,Integer>> route;
}
