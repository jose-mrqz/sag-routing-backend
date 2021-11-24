package pe.sag.routing.core.model;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Document
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class FuelConsume {
    private LocalDateTime date;
    private double fuelConsumed;


    public String getDateString(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return date.format(formatter);
    }


}

