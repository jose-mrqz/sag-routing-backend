package pe.sag.routing.core.model;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import pe.sag.routing.shared.dto.RouteDto;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static java.time.temporal.ChronoUnit.NANOS;

@Document
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class OrderLost {
    private String truckCode;
    private String positionTruck;
    private String ubication;
    private double demandGLP;
    private LocalDateTime deliveryDate;

    public OrderLost(RouteDto route, LocalDateTime timeEnd, int speed, SimulationInfo simulationInfo) {
        truckCode = route.getTruckCode();

        LocalDateTime startDate = route.getStartDate();

        LocalDateTime startDateTransf = transformDate(simulationInfo,speed,startDate);

        Duration duration = Duration.between(startDateTransf,timeEnd);
        long secTme = duration.toSeconds();

        double distancia = (50.0*secTme)/(3600*speed);
        int dist = (int)distancia;

        RouteDto.Node ubic = route.getRoute().get(dist);
        positionTruck = '(' + Integer.toString(ubic.getX()) + " , " + Integer.toString(ubic.getY()) + ')';

        //siquiente ruta
        int cont = 0;
         for (RouteDto.Order order: route.getOrders()){
             if(order.getIndexRoute()  > dist){
                 cont = 1;
                 ubication = '(' + Integer.toString(order.getX()) + " , " + Integer.toString(order.getY()) + ')';
                 demandGLP = order.getDelivered();
                 deliveryDate = order.getDeliveryDate();
                 break;
             }
         }
         if(cont == 0){
             ubication = "planta principal";
             demandGLP = 0;
             deliveryDate = null;
         }

    }

    public String getDeliveryDateString(){

        if(deliveryDate ==null){
            return "-";
        }else{
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return deliveryDate.format(formatter);
        }

    }
    public LocalDateTime transformDate(SimulationInfo simulationInfo, int speed, LocalDateTime dateToConvert){
        LocalDateTime simulationStartReal = simulationInfo.getStartDateReal();
        LocalDateTime simulationStartTransform = simulationInfo.getStartDateTransformed();

        long differenceTransformReal = NANOS.between(simulationStartReal, simulationStartTransform);
        dateToConvert = dateToConvert.plusNanos(differenceTransformReal);

        long amountNanos = NANOS.between(simulationStartTransform, dateToConvert);
        amountNanos /= speed;
        LocalDateTime transformedDate = LocalDateTime.of(simulationStartTransform.toLocalDate(),simulationStartTransform.toLocalTime());
        transformedDate = transformedDate.plusNanos(amountNanos);
        return transformedDate;
    }
}
