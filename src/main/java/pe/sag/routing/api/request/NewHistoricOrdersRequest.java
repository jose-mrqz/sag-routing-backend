package pe.sag.routing.api.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewHistoricOrdersRequest {
    private ArrayList<NewOrderRequest> orderRequests;
    private int speed;
}