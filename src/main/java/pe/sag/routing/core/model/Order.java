package pe.sag.routing.core.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import pe.sag.routing.shared.util.enums.OrderStatus;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@EqualsAndHashCode(callSuper = true)
@Document
@Data
@Builder
public class Order extends Node {
    @Id
    private String _id;
    @Indexed(unique = true)
    private int code;
    private double demandGLP;
    private double totalDemand;
    private LocalDateTime registrationDate;
    private LocalDateTime deadlineDate;
    private LocalDateTime deliveryDate;
    private OrderStatus status;
    private boolean monitoring;
    private boolean active = true;

    public Order(String _id, int code, double demandGLP, double totalDemand, LocalDateTime registrationDate, LocalDateTime deadlineDate, LocalDateTime deliveryDate, OrderStatus status, boolean monitoring, boolean active) {
        this._id = _id;
        this.code = code;
        this.demandGLP = demandGLP;
        this.totalDemand = totalDemand;
        this.registrationDate = registrationDate;
        this.deadlineDate = deadlineDate;
        this.deliveryDate = deliveryDate;
        this.status = status;
        this.monitoring = monitoring;
        this.active = active;
    }

    public String getRegistrationDateString(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return registrationDate.format(formatter);
    }

    public String getDeliveryDateString(){

        if(deliveryDate ==null){
            return "-";
        }else{
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return deliveryDate.format(formatter);
        }

    }

    public String getStatusString(){
        return status.toString();
    }

    public String getUbication(){
        return '(' + Integer.toString(this.getX()) + " , " + Integer.toString(this.getY()) + ')';
    }
}
