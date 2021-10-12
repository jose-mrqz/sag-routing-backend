package pe.sag.routing.algorithm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Route {
    String truckId;
    ArrayList<Node> nodes;
    ArrayList<LocalDateTime> times;
    int totalTourDistance;
    double totalFuelConsumption;
    LocalDateTime startDate;
    LocalDateTime finishDate;
}
