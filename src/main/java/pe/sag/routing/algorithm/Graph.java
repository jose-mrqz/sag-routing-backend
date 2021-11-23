package pe.sag.routing.algorithm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import pe.sag.routing.api.controller.RouteController;
import pe.sag.routing.core.model.SimulationHelper;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

    public Graph(List<Truck> trucks, List<Order> orders, List<Depot> depots) {
        this.nTruck = trucks.size();
        this.trucks = trucks.toArray(Truck[]::new);
        this.nNode = orders.size()+3;

        // depots - add availableGlp
        nodes = new Node[nNode];
        nodes[0] = new Depot("PORONGAZO PRINCIPAL", true, MDX, MDY, 0);

        if (depots == null) {
            SimulationHelper sh = RouteController.simulationHelper;
            if (sh.getDepots().size() == 0) {
                sh.getDepots().add(new Depot("PORONGAZO NORTE",false, D1X, D1Y, 1));
                sh.getDepots().add(new Depot("PORONGAZO SUR", false, D2X, D2Y, 2));
            }
            nodes[1] = sh.getDepots().get(0);
            nodes[2] = sh.getDepots().get(1);
        } else {
            nodes[1] = depots.get(0);
            nodes[2] = depots.get(1);
        }

        for (int i = 0; i < orders.size(); i++) {
            nodes[i+3] = orders.get(i);
            nodes[i+3].idx = i+3;
        }

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
