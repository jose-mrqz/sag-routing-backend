package pe.sag.routing.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Breakdown {
    @Id
    private String _id;
    private int x;
    private int y;
    private String truckCode;
    private String routeId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    public Breakdown(Breakdown breakdown) {
        this._id = breakdown._id;
        this.x = breakdown.x;
        this.y = breakdown.y;
        this.truckCode = breakdown.truckCode;
        this.routeId = breakdown.routeId;
        this.startDate = breakdown.getStartDate();
        this.endDate = breakdown.getEndDate();
    }
}
