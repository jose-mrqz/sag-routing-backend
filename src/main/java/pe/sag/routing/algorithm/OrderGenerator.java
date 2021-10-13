package pe.sag.routing.algorithm;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class OrderGenerator {
    LocalDateTime initialDate;
    LocalDateTime currentTime;
    int dayInterval;
    int key;
    double capacity;
    String fileName;
    Deque<Object[]> orders;

    public OrderGenerator(int i, String fileName, int dayInterval) {
        this.key = i;
        this.dayInterval = dayInterval;
        this.initialDate = LocalDateTime.of(2020, 10, 1, 0, 0);
        this.currentTime = initialDate;
        this.capacity = 200.0;
        this.fileName = fileName;
        this.orders = new ArrayDeque<>();
    }

    Pair<Integer, Integer> getRandomCoordinates() {
        Pair<Integer, Integer> coords = new Pair<>();
        int x, y;
        while (true) {
            x = ThreadLocalRandom.current().nextInt(1, 70);
            y = ThreadLocalRandom.current().nextInt(1, 50);
            if (x == 10 && y == 8 ||
                x == 40 && y == 45 ||
                x == 60 && y == 5) continue;
            else break;
        }
        coords.x = x;
        coords.y = y;
        return coords;
    }

    int getRandomTw(int low, int high) {
        return ThreadLocalRandom.current().nextInt(low, high+1);
    }

    double getRandomDemand() {
        int demand = ThreadLocalRandom.current().nextInt(1, 26);
        int probs = ThreadLocalRandom.current().nextInt(0, 2);
        if (probs == 0)  return demand + 0.0;
        else {
            if (demand == 25) return demand + 0.0;
            return demand + 0.5;
        }
    }

    LocalDateTime getRandomDate() {
        int hour = ThreadLocalRandom.current().nextInt(0, 4);
        int minute = ThreadLocalRandom.current().nextInt(0, 60);
//        int day = ThreadLocalRandom.current().nextInt(0, dayInterval);
        LocalDateTime newDate = currentTime.plusHours(hour);
        newDate = newDate.plusMinutes(minute);
        return newDate;
    }

    public void generateOrders() throws IOException {
        String projectPath = System.getProperty("user.dir");
        FileWriter fileWriter = new FileWriter(projectPath + "/" + fileName + ".txt", false);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd:HH:mm");
        NumberFormat formatter = new DecimalFormat("#0.0");

        double rate = 0.5;
        for (int i = 0; i < 14; i++) {
            double currentDemand = 0.0;
            double demandLimit = capacity * rate;
            LocalDateTime lastDate;
            while (currentDemand < demandLimit) {
                Object[] data = new Object[6];
                Pair<Integer, Integer> coords = getRandomCoordinates();
                int tw = getRandomTw(4,6);
                double demand = getRandomDemand();
                if (currentDemand + demand > demandLimit)
                    demand = demandLimit - currentDemand;
                currentDemand += demand;
                LocalDateTime day = getRandomDate();
                data[0] = coords.x;
                data[1] = coords.y;
                data[2] = tw;
                data[3] = demand;
                data[4] = day;
                orders.add(data);
            }
            currentTime = currentTime.plusHours(5);
            rate += 0.03;
        }

        for (Object[] data : orders) {
            int x =(int)data[0];
            int y = (int)data[1];
            int tw = (int)data[2];
            double demand = (double)data[3];
            LocalDateTime day = (LocalDateTime)data[4];
            printWriter.print(day.format(format) + ",");
            printWriter.print(x + "," + y + ",");
            printWriter.println(formatter.format(demand) + "," + tw);
        }
        printWriter.close();
    }

}
