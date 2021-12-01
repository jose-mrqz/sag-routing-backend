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

    //[startDateMin;endDateMin] y [startDate;endDate]
    public boolean validateDates(LocalDateTime startDateMin,LocalDateTime endDateMin){
        int cantSec = 40*60;
        return (startDate.minusSeconds(cantSec).isBefore(endDateMin) && endDateMin.isBefore(endDate.plusSeconds(cantSec)))
                || startDate.minusSeconds(cantSec).isEqual(endDateMin) || endDateMin.isEqual(endDate.plusSeconds(cantSec));

        /*if( ! (endDateMin.isBefore(startDate) || endDate.isBefore(startDateMin) ) ){
            return true;
        }
        int maxsec = 18;//36;
        for(int sec = 1; sec <= maxsec; sec++){
            if( !( endDateMin.plusSeconds(sec).isBefore(startDate) || endDate.plusSeconds(sec).isBefore(startDateMin) ) ){
                return true;
            }
        }
        return false;*/
    }

    public void printRoadblock(){
        System.out.print(startDate);
        System.out.print(endDate);
        System.out.print(x + "," + y + "");
        System.out.print("\n");
    }
}
