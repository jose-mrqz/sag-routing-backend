package pe.sag.routing.api.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ManyOrdersRequest {
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Order {
        private int x;
        private int y;
        private double demandGLP;
        private int slack;
    }

    private List<Order> orders;
}
