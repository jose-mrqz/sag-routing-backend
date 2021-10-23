package pe.sag.routing.core.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@EqualsAndHashCode(callSuper = true)
@Document
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Depot extends Node {
    @Id
    private String _id;
    @Indexed(unique = true)
    private String name;
    private double capacityGLP;
    private double currentGLP;
    private boolean active = true;
}
