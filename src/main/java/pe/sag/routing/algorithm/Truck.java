package pe.sag.routing.algorithm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Data
@AllArgsConstructor
@Builder
public class Truck {
    String _id;
    double capacity;
    int nowIdx;
    double nowLoad;
    double speed;
    double fuel;
    double weight;
    double tareWeight;
    int attendedCustomers;
    double totalFuelConsumption;
    double totalDelivered;
    ArrayList<Node> tour;
    ArrayList<LocalDateTime> timeRegistry;
    LocalDateTime nowTime;
    LocalDateTime startDate;
    LocalDateTime finishDate;

    private LocalDateTime startingDate;

    // general parameters
    private static final Double AVG_SPEED = 50.0;
    private static final Double CONSUMPTION_RATE = 150.0;
    private static final Double MAX_FUEL = 25.0;
    private static final Double GLP_WEIGHT = 0.5;

    public Truck(String _id, double capacity, double tareWeight, int nowIdx, LocalDateTime startingDate) {
        this._id = _id;
        this.startingDate = startingDate;
        this.capacity = capacity;
        this.nowIdx = nowIdx;
        nowLoad = capacity;
        nowTime = startingDate;
        speed = AVG_SPEED;
        fuel = MAX_FUEL;
        this.tareWeight = tareWeight;
        weight = tareWeight + capacity * GLP_WEIGHT;
        tour = new ArrayList<>();
        totalFuelConsumption = 0.0;
        attendedCustomers = 0;
        totalDelivered = 0.0;
        timeRegistry = new ArrayList<>();
        startDate = null;
        finishDate = null;
    }

    // a* stub
    private int calculateTravelTime(int[][] matrix, int i, int j) {
        return (int)(60*matrix[i][j]/speed);
    }

    private double calculateFuelConsumption(int i, int j, int[][] matrix, double weight) {
        return matrix[i][j] * weight / CONSUMPTION_RATE;
    }

    private boolean okTime(Order o, int travelTime) {
        LocalDateTime arrivalTime = nowTime.plusMinutes(travelTime);
        return !arrivalTime.isAfter(o.twClose);
    }

    private boolean okCapacity(Node n, int travelTime) {
        if (n instanceof Order) {
            return ((Order)n).demand > capacity && nowLoad > capacity/4 || nowLoad >= ((Order)n).demand;
        } else {
            double remainingGlp = ((Depot)n).getAvailableGLp(nowTime.plusMinutes(travelTime).toLocalDate());
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

    public boolean evaluateNode(Node n, int[][] matrix) {
        if (n instanceof  Order) return evaluateOrder((Order)n, matrix);
        else return evaluateDepot((Depot)n, matrix);
    }

    public void visitOrder(Order order) {
        double aux = nowLoad;
        if (nowLoad > order.demand) {
            totalDelivered += order.demand;
            nowLoad -= order.demand;
            weight -= order.demand * GLP_WEIGHT;
        } else {
            totalDelivered += nowLoad;
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
        } else {
            nowLoad += availableGlp;
            weight += availableGlp * GLP_WEIGHT;
        }
        depot.handleVisit(nowTime, missingGlp);
        fuel = MAX_FUEL;
    }

    public void addNode(Node n, int[][] matrix) {
        int travelTime = calculateTravelTime(matrix, nowIdx, n.idx);
        if (!tour.isEmpty()) {
            if (tour.get(tour.size()-1) instanceof Depot) {
                timeRegistry.add(nowTime);
                if (startDate == null) startDate = nowTime;
            }
            if (n instanceof Order) timeRegistry.add(nowTime.plusMinutes(travelTime));
        }

        double consumption = calculateFuelConsumption(nowIdx, n.idx, matrix, this.weight);
        nowTime = nowTime.plusMinutes(travelTime);
        fuel -= consumption;
        totalFuelConsumption += consumption;

        if (n instanceof Order) visitOrder((Order)n);
        else visitDepot((Depot)n);

        tour.add(n);
        nowIdx = n.idx;
        if (finishDate == null || nowTime.isAfter(finishDate)) finishDate = nowTime;
    }

    public void reset() {
        tour.clear();
        timeRegistry.clear();
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
