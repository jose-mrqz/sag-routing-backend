package pe.sag.routing.algorithm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Graph {
    int nTruck;
    int nNode;
    LocalDateTime lastOrder;
    Truck[] trucks;
    Node[] nodes;
    int[][] distanceMatrix;

    private static final int MDX = 12;
    private static final int MDY = 8;
    private static final int D1X = 42;
    private static final int D1Y = 42;
    private static final int D2X = 63;
    private static final int D2Y = 3;

    public Graph(Truck[] trucks) {
        this.nTruck = trucks.length;
        this.trucks = trucks;
    }

    public void resetGraph(Order[] orders, LocalDateTime lastOrder) {
        this.nNode = orders.length+3;
        this.lastOrder = lastOrder;

        // depots
        nodes[0] = new Depot(true);
        nodes[1] = new Depot(false);
        nodes[2] = new Depot(false);

        if (nNode-3 >= 0) System.arraycopy(orders, 0, nodes, 3, nNode-3);

        distanceMatrix = new int[nNode][nNode];
        calculateNodeDistance();
    }

    public void calculateNodeDistance() {
        for (int i = 0; i < nNode; i++) {
            for (int j = i+1; j < nNode; j++) {
                distanceMatrix[i][j] = nodes[i].calculateDistance(nodes[j]);
                distanceMatrix[j][i] = distanceMatrix[i][j];
            }
        }
    }

    public Boolean isAllVisited() {
        for (int i = 1; i < nNode; i++) {
            if (nodes[i] instanceof Depot) continue;
            if (!((Order)nodes[i]).visited) return false;
        }
        return true;
    }

    public int calculateTourDistance(ArrayList<Node> tour, int tourDistance) {
        if (tour.size() > 2) {
            for (int i = 0; i < tour.size()-1; i++) {
                tourDistance += distanceMatrix[tour.get(i).idx][tour.get(i+1).idx];
            }
        }
        return tourDistance;
    }

    @SneakyThrows
    public void showEachTour() throws IOException {
        int totalTourDistance = 0;
        double totalFuelConsumption = 0.0;
        double totalGLP = 0.0;
        HashMap<Integer, Integer> visited = new HashMap<>();
        String projectPath = System.getProperty("user.dir");
        FileWriter fileWriter = new FileWriter(projectPath + "/best.txt", false);
        PrintWriter printWriter = new PrintWriter(fileWriter);

        printWriter.println("total visited customer: " + (visited.size()) + "/" + (nNode-3));
        printWriter.println("total distance: " + totalTourDistance + "km");
        printWriter.println("total fuel consumption: " + totalFuelConsumption + "gal");
        printWriter.println("total GLP delivered: " + totalGLP + "m3");
        printWriter.close();
    }
}
