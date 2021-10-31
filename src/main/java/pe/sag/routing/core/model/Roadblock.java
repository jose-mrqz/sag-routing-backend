package pe.sag.routing.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Roadblock {
    int x;
    int y;
    LocalDateTime startDate;
    LocalDateTime endDate;
    boolean monitoring = true;

    public boolean validateDates(LocalDateTime dateMin1,LocalDateTime dateMin2){
        //fechaMin1 dentro de [fecha1;fecha2] (despreciar fechaMin2 dentro de ese rango)
        return dateMin1.isAfter(startDate) && dateMin1.isBefore(endDate);
    }

    public void printRoadblock(){
        System.out.print(startDate);
        System.out.print(endDate);
        System.out.print(x + "," + y + "");
        System.out.print("\n");
    }
}
