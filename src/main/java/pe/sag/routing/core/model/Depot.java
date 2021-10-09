package pe.sag.routing.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Depot {
    @Id
    private String _id;
    @Indexed(unique = true)
    private String name;
    private double capacity;
    private double currentGlp;
    private boolean active = true;
}
