package pe.sag.routing.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import pe.sag.routing.shared.util.enums.TruckStatus;

import java.time.LocalDateTime;

@Document
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Truck {
    @Id
    private String _id;
    @Indexed(unique = true)
    private String code;
    private TruckModel model;
    private LocalDateTime lastRouteEndTime = null;
    private boolean monitoring;
    private boolean active = true;
    private String status = TruckStatus.DISPONIBLE.toString();

    public boolean isTruckAvailable(){
        return status.equals(TruckStatus.DISPONIBLE.toString()) || status.equals(TruckStatus.RUTA.toString());
    }

    public double getModelCapacity() {
        return model.getCapacity();
    }
}
