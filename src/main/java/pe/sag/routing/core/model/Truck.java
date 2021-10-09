package pe.sag.routing.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

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
    private boolean available = true;
    private boolean active = true;
}
