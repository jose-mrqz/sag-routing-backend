package pe.sag.routing.algorithm;

import java.io.IOException;

public class DataGenerator {
    public static void main(String[] args) throws IOException {
        for (int i = 1; i <= 2; i++) {
            OrderGenerator orderGenerator = new OrderGenerator(i, "incOrder32", 3);
            orderGenerator.generateOrders();
        }
    }
}
