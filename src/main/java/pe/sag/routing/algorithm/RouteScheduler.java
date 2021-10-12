package pe.sag.routing.algorithm;

import org.springframework.beans.factory.annotation.Autowired;
import pe.sag.routing.core.service.TruckService;

import java.io.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.concurrent.TimeUnit;

public class RouteScheduler {
    public static void main(String[] args) throws IOException, InterruptedException {
        String projectPath = System.getProperty("user.dir");
        File outFile = new File(projectPath + "/out.txt");
        FileOutputStream fos = new FileOutputStream(outFile);
        PrintStream ps = new PrintStream(fos);
        System.setOut(ps);

        String ordersFilePath = projectPath + "/src/main/resources/incOrder31.txt";
        String trucksFilePath = projectPath + "/src/main/resources/truck";

        FileWriter fileWriter = new FileWriter(projectPath + "/quality31.txt", false);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        NumberFormat formatter = new DecimalFormat("#0.000");

        OrderParser orderParser = new OrderParser();
        TruckParser truckParser = new TruckParser();
        orderParser.readFile(ordersFilePath);
        truckParser.readFile(trucksFilePath);


//        Colony colony = new Colony(orderParser.getParams(), truckParser.getParams(),
//                orderParser.smallestOrder, orderParser.startingDate, orderParser.lastOrder);
//        colony.run();


        printWriter.close();
    }

}
