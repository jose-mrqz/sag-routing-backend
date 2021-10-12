package pe.sag.routing.algorithm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Queue;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Route {
    ArrayList<Node> tour;
    Queue<LocalDateTime> times;
    int totalTourDistance;
    double totalFuelConsumption;
}
