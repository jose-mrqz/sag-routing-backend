package pe.sag.routing.core.model;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import pe.sag.routing.shared.dto.RouteDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

    public OrderLost(RouteDto route, long timeSec) {
        truckCode = route.getTruckCode();

        LocalDateTime startDate = route.getStartDate();

        double distancia = 50*timeSec/3600;
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
}
