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

    public boolean validateDates(LocalDateTime startDateMin,LocalDateTime endDateMin){
        //[startDateMin;endDateMin] y [startDate;endDate]
        return !endDateMin.plusSeconds(60).isBefore(startDate) && !endDate.plusSeconds(60).isBefore(startDateMin);
    }

    public void printRoadblock(){
        System.out.print(startDate);
        System.out.print(endDate);
        System.out.print(x + "," + y + "");
        System.out.print("\n");
    }
}
