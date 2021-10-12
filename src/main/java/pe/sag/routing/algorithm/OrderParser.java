package pe.sag.routing.algorithm;

import pe.sag.routing.core.model.Order;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Scanner;


public class OrderParser {
    public double smallestOrder;
    public LocalDateTime startingDate;
    public ArrayList<Object[]> params;
    public LocalDateTime lastOrder;

    public OrderParser() {
        this.smallestOrder = Integer.MAX_VALUE;
        this.params = new ArrayList<>();
        this.startingDate = LocalDateTime.MAX;
        this.lastOrder = LocalDateTime.MIN;
    }

    public void readFile(String filePath) throws FileNotFoundException {
        File file = new File(filePath);
        Scanner reader = new Scanner(file);
        int i = 1;
        while (reader.hasNextLine()) {
            String line = reader.nextLine();
            String[] data = line.split(",");
            String[] date = data[0].split(":");
            LocalDateTime twOpen = LocalDateTime.of(2020, 10,
                    Integer.parseInt(date[0]), Integer.parseInt(date[1]), Integer.parseInt(date[2]));
            if (twOpen.isBefore(startingDate)) startingDate = twOpen;
            int x = Integer.parseInt(data[1]);
            int y = Integer.parseInt(data[2]);
            double demand = Double.parseDouble(data[3]);
            LocalDateTime twClose = twOpen.plusHours(Integer.parseInt(data[4]));
            Object[] args = new Object[6];
            if (demand < smallestOrder) smallestOrder = demand;
            if (twClose.isAfter(lastOrder)) lastOrder = twClose;
            args[0] = i++;
            args[1] = x;
            args[2] = y;
            args[3] = demand;
            args[4] = twOpen;
            args[5] = twClose;
            params.add(args);
        }
        startingDate = LocalDateTime.of(startingDate.getYear(), startingDate.getMonth(),
                startingDate.getDayOfMonth(), 0, 0);
        reader.close();
    }

    public Object[][] getParams() {
        Object[][] args = new Object[params.size()][5];
        int i = 0;
        for (Object[] record : params) {
            args[i] = record;
            i++;
        }
        return args;
    }
}
