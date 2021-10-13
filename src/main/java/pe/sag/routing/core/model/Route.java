package pe.sag.routing.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import pe.sag.routing.algorithm.Pair;

import java.time.LocalDateTime;
import java.util.List;

@Document
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Route {
    @Id
    private String _id;
    @Indexed(unique = true)
    private Truck truck;
    private List<Order> orders;
    private List<Pair<Integer,Integer>> nodes;
    private double distance;
    private double fuelConsumed;
    private double deliveredGLP;
    private boolean active = true;
    private LocalDateTime startDate;
    private LocalDateTime finishDate;
}
