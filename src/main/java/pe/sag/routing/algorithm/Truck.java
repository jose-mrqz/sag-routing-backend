package pe.sag.routing.algorithm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import pe.sag.routing.aStar.AStar;
import pe.sag.routing.core.model.Roadblock;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class Truck {
    String _id = null;
    String code = null;
    double capacity;
    int nowIdx;
    double nowLoad;
    double speed;
    double fuel;
    double weight;
    double tareWeight;
    ArrayList<Node> tour;
    LocalDateTime nowTime;
    LocalDateTime startDate;
    LocalDateTime finishDate;

    private LocalDateTime startingDate;
    private Node currentNode = null;
    private Node destination = null;

    ArrayList<LocalDateTime> departureRegistry;
    ArrayList<LocalDateTime> arrivalRegistry;
    ArrayList<Double> fuelConsumption;
    ArrayList<Double> glpRegistry;

    int attendedCustomers;
    double totalFuelConsumption;
    double totalDelivered;

    private static final Double AVG_SPEED = 50.0;
    private static final Double CONSUMPTION_RATE = 150.0;
    private static final Double MAX_FUEL = 25.0;
    private static final Double GLP_WEIGHT = 0.5;

    public Truck(String _id, String code, double capacity, double tareWeight, int nowIdx, LocalDateTime startingDate) {
        this._id = _id;
        this.code = code;
        this.startingDate = startingDate;
        this.capacity = capacity;
        this.nowIdx = nowIdx;
        nowLoad = capacity;
        nowTime = startingDate;
        speed = AVG_SPEED;
        fuel = MAX_FUEL;
        this.tareWeight = tareWeight;
        weight = tareWeight + capacity * GLP_WEIGHT;
        totalFuelConsumption = 0.0;
        attendedCustomers = 0;
        totalDelivered = 0.0;
        startDate = null;
        finishDate = null;
        tour = new ArrayList<>();
        departureRegistry = new ArrayList<>();
        arrivalRegistry = new ArrayList<>();
        fuelConsumption = new ArrayList<>();
        glpRegistry = new ArrayList<>();
    }

    // a* stub
    private int calculateTravelTime(int[][] matrix, int i, int j) {
        AStar aStar = new AStar();
        LocalDateTime startDate = LocalDateTime.now();
        Node nodeStart = currentNode;
        Node nodeGoal = destination;
        List<Roadblock> roadblocks = new ArrayList<>();
//        List<Pair<Integer,Integer>> solutionList = aStar.run(startDate,nodeStart,nodeGoal,roadblocks);
//        return (int)(3600*solutionList.size()/speed);
        return (int)(3600*matrix[i][j]/speed);
    }

    private double calculateFuelConsumption(int i, int j, int[][] matrix, double weight) {
        return matrix[i][j] * weight / CONSUMPTION_RATE;
    }

    private boolean okTime(Order o, int travelTime) {
        LocalDateTime arrivalTime = nowTime.plusSeconds(travelTime);
        return !arrivalTime.isAfter(o.twClose.plusMinutes(o.unloadTime));
    }

    private boolean okCapacity(Node n, int travelTime) {
        if (n instanceof Order) {
            return ((Order)n).demand > capacity && nowLoad > capacity/4 || nowLoad >= ((Order)n).demand;
        } else {
            double remainingGlp = ((Depot)n).getAvailableGLp(nowTime.plusSeconds(travelTime).toLocalDate());
            return remainingGlp >= capacity/4 && nowLoad <= capacity/4;
        }
    }

    private boolean okFuel(Node n, int[][] matrix) {
        double consumption = calculateFuelConsumption(nowIdx, n.idx, matrix, this.weight);
        if (consumption > fuel) return false;
        if (n instanceof Order) {
            Order order = (Order)n;
            if (order.demand >= nowLoad) {
                consumption += calculateFuelConsumption(n.idx, 0, matrix, tareWeight);
            }
            else {
                double aux = this.weight - order.demand * GLP_WEIGHT;
                consumption += calculateFuelConsumption(n.idx, 0, matrix, aux);
            }
            return consumption <= fuel;
        } else return true;
    }

    private boolean evaluateOrder(Order o, int[][] matrix) {
        if (nowTime.isBefore(o.twOpen)) return false;
        int travelTime = calculateTravelTime(matrix, nowIdx, o.idx);
        return (okTime(o, travelTime) && okCapacity(o, travelTime) && okFuel(o, matrix));
    }

    private boolean evaluateDepot(Depot d, int[][] matrix) {
        int travelTime = calculateTravelTime(matrix, nowIdx, d.idx);
        return (okCapacity(d, travelTime) && okFuel(d, matrix));
    }

    public boolean evaluateNode(Node n, int[][] matrix, Node[] nodes) {
        currentNode = nodes[nowIdx];
        destination = n;
        if (n instanceof  Order) return evaluateOrder((Order)n, matrix);
        else return evaluateDepot((Depot)n, matrix);
    }

    public void visitOrder(Order order) {
        double aux = nowLoad;
        if (nowLoad > order.demand) {
            totalDelivered += order.demand;
            glpRegistry.add(order.demand);
            nowLoad -= order.demand;
            weight -= order.demand * GLP_WEIGHT;
        } else {
            totalDelivered += nowLoad;
            glpRegistry.add(nowLoad);
            nowLoad = 0.0;
            weight = tareWeight;
        }
        order.handleVisit(nowTime, aux);
        attendedCustomers += 1;
    }

    private void visitDepot(Depot depot) {
        double missingGlp = capacity - nowLoad;
        double availableGlp = depot.getAvailableGLp(nowTime.toLocalDate());
        if (availableGlp >= missingGlp) {
            nowLoad = capacity;
            weight +=  missingGlp * GLP_WEIGHT;
            if (depot.idx != 0) glpRegistry.add(missingGlp);
        } else {
            nowLoad += availableGlp;
            weight += availableGlp * GLP_WEIGHT;
            if (depot.idx != 0) glpRegistry.add(availableGlp);
        }
        depot.handleVisit(nowTime, missingGlp);
        fuel = MAX_FUEL;
    }

    public void addNode(Node n, int[][] matrix, Node[] nodes) {
        currentNode = nodes[nowIdx];
        destination = n;
        int travelTime = calculateTravelTime(matrix, nowIdx, n.idx);

        if (nowIdx == 0 && !tour.isEmpty()) {
            departureRegistry.add(nowTime);
        }

        double consumption = calculateFuelConsumption(nowIdx, n.idx, matrix, this.weight);
        fuel -= consumption;
        if (!tour.isEmpty()) {
            fuelConsumption.add(consumption);
        }
        totalFuelConsumption += consumption;
        nowTime = nowTime.plusSeconds(travelTime);

        if (!tour.isEmpty()) {
            arrivalRegistry.add(nowTime);
        }

        if (n instanceof Order) {
            visitOrder((Order)n);
            nowTime = nowTime.plusMinutes(((Order)n).unloadTime);
        }
        else visitDepot((Depot)n);

        tour.add(n);
        nowIdx = n.idx;
        if (finishDate == null || nowTime.isAfter(finishDate)) finishDate = nowTime;
    }

    public void reset() {
        tour.clear();
        departureRegistry.clear();
        glpRegistry.clear();
        fuelConsumption.clear();
        arrivalRegistry.clear();
        nowLoad = capacity;
        fuel = MAX_FUEL;
        weight = tareWeight + capacity * GLP_WEIGHT;
        nowIdx = 0;
        nowTime = startingDate;
        totalFuelConsumption = 0.0;
        totalDelivered = 0.0;
        attendedCustomers = 0;
    }
}
