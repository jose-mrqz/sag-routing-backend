package pe.sag.routing.algorithm;

import lombok.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Colony extends Graph {
    private static final Double INIT_PHERO = 1.0;
    private static final Double EVAP_RATE = 0.85;
    private static final Double ALPHA = 3.0;
    private static final Double BETA = 5.0;
    private static final Double Q = 1.0;
    private static final Double DECAY_RATE = 0.95;
    private static final int INF = Integer.MAX_VALUE;
    private static final int ITERATOR = 10000;
    private Double threshold;
    private Double[][] pheromoneMatrix;
    private Double[][] ethaMatrix;
    private Random rand;

    public List<Route> solutionRoutes = null;
    public double bestSolutionQuality;

    public Colony(List<Order> orders, List<Truck> trucks) {
        super(trucks, orders);
        this.rand = new Random();
        this.threshold = 0.5;
        this.pheromoneMatrix = new Double[nNode][nNode];
        this.ethaMatrix = new Double[nNode][nNode];
        this.lastOrder = orders.get(orders.size()-1).twClose;

        // init pheromone/heuristic matrix
        for(int i = 0; i < nNode; i++) {
            for (int j = i+1; j < nNode; j++) {
                pheromoneMatrix[i][j] =  INIT_PHERO;
                pheromoneMatrix[j][i] = INIT_PHERO;
                ethaMatrix[i][j] = Q / (distanceMatrix[i][j]+1);
                if (nodes[j] instanceof Order)
                    ethaMatrix[i][j] += Q / Duration.between(((Order)nodes[j]).twOpen, ((Order)nodes[j]).twClose).toHours();
                ethaMatrix[j][i] = Q / (distanceMatrix[j][i]+1);
                if (nodes[i] instanceof Order)
                    ethaMatrix[j][i] += Q / Duration.between(((Order)nodes[i]).twOpen, ((Order)nodes[i]).twClose).toHours();
            }
        }
    }

    public Double calculateProbability(int nowNodeIdx, int nextNodeIdx) {
        double ETAij = Math.pow(ethaMatrix[nowNodeIdx][nextNodeIdx], BETA);
        double TAUij = Math.pow(pheromoneMatrix[nowNodeIdx][nextNodeIdx], ALPHA);
        return ETAij * TAUij;
    }

    public void updateThreshold() {
        this.threshold *= DECAY_RATE;
    }

    public void updatePheroMatrix() {
        for (int i = 0; i < nTruck; i++) {
            int tourDistance = calculateTourDistance(trucks[i].tour, 0);
            for (int j = 0; j < trucks[i].tour.size(); j++) {
                if (j+1 != trucks[i].tour.size()) {
                    int currIdx = trucks[i].tour.get(j).idx;
                    int nextIdx = trucks[i].tour.get(j+1).idx;
                    pheromoneMatrix[currIdx][nextIdx] =
                            EVAP_RATE * pheromoneMatrix[currIdx][nextIdx] + Q / tourDistance;
                }
            }
        }
    }

    public void resetStep() {
        for (int i = 0; i < nTruck; i++)
            trucks[i].reset();
        for (int i = 0; i < nNode; i++)
            nodes[i].reset();
    }

    public void run()  {
        double bestSolution = Double.MIN_VALUE;
        for(int i = 0; i < ITERATOR; i++) {
            solve();
            updatePheroMatrix();
            int attendedCustomers = 0;
            double totalGLP = 0.0;
            double totalConsumption = 0.0;
            for (Truck t: trucks) {
                attendedCustomers += t.attendedCustomers;
                totalGLP += t.totalDelivered;
                totalConsumption += t.totalFuelConsumption;
            }
            double quality = (totalGLP * attendedCustomers) / totalConsumption;
            if (quality > bestSolution) {
                bestSolution = quality;
                saveBestSolution2();
            }
            updateThreshold();
            resetStep();
        }
        this.bestSolutionQuality = bestSolution;
    }

    private void saveBestSolution() {
        solutionRoutes = new ArrayList<>();
        for (Truck t : trucks) {
            ArrayList<Node> tour = new ArrayList<>();
            for (Node n : t.tour) {
                if (n instanceof Depot) {
                    Depot dp = (Depot) n;
                    Depot d = new Depot(dp.isMain, dp.x, dp.y, dp.idx);
                    d.remainingGlp = (HashMap<LocalDate, Double>) dp.remainingGlp.clone();
                    tour.add(d);
                } else {
                    Order op = (Order) n;
                    Order o = new Order(op._id, op.x, op.y, op.idx, op.demand, op.twOpen, op.twClose);
                    o.visited = op.visited;
                    o.totalDemand = op.totalDemand;
                    o.deliveryTime = op.deliveryTime;
                    tour.add(o);
                }
            }
            ArrayList<LocalDateTime> tourTimes = new ArrayList<>(t.timeRegistry);
            tourTimes.add(t.finishDate);
            Route route = Route.builder()
                    .truckId(t._id)
                    .nodes(new ArrayList<>(tour))
                    .totalFuelConsumption(t.totalFuelConsumption)
                    .totalTourDistance(calculateTourDistance(t.tour, 0))
                    .totalDelivered(t.totalDelivered)
                    .times(tourTimes)
                    .startDate(t.startDate)
                    .finishDate(t.finishDate)
                    .build();
            solutionRoutes.add(route);
        }
    }

    private void saveBestSolution2() {
        solutionRoutes = new ArrayList<>();
        for (Truck t : trucks) {
            int depIdx = 0;
            int arrIdx = 0;
            int fuelIdx = 0;
            int glpIdx = 0;
            double routeConsumption = 0.0;
            double routeDelivered = 0.0;
            ArrayList<NodeInfo> routeNodes = new ArrayList<>();
            LocalDateTime startTime = null;
            LocalDateTime endTime = null;
            if (t.tour.size() == 1) continue;
            int i = 0;
            while (i != t.tour.size()) {
                Node n = t.tour.get(i);
                if (n.idx == 0) {  //start of new route
                    if (routeNodes.isEmpty()) {
                        startTime = t.departureRegistry.get(depIdx++);
                        i++;
                    } else {
                        endTime = t.departureRegistry.get(depIdx++);
                        routeNodes.clear();
                        Route route = new Route(t._id, startTime, endTime, routeNodes);
                        solutionRoutes.add(route);
                    }
                } else {
                    if (n instanceof Depot)
                        routeNodes.add(new DepotInfo(n.x, n.y, t.fuelConsumption.get(fuelIdx++),
                                t.glpRegistry.get(glpIdx++), t.arrivalRegistry.get(arrIdx++)));
                    else routeNodes.add(new OrderInfo(n.x, n.y, t.fuelConsumption.get(fuelIdx++),
                            ((Order)n)._id, ((Order)n).deliveryTime,t.glpRegistry.get(glpIdx++), t.arrivalRegistry.get(arrIdx++)));
                    i++;
                }
            }
        }
    }

    public double getRandom() {
        return ThreadLocalRandom.current().nextDouble(0.0, 1.0);
    }

    public void solve()  {
        int truckIdx = 0;
        boolean onlyDepot;
        while (!isAllVisited()) {
            if (trucks[truckIdx].tour.isEmpty()) // add main depot as starting point
                trucks[truckIdx].addNode(nodes[0], distanceMatrix);
            ArrayList<Pair<Integer, Integer>> feasibleEdges = new ArrayList<>();
            while (feasibleEdges.isEmpty() && (trucks[truckIdx].nowTime).isBefore(lastOrder)) {
                onlyDepot = true;
                for (int nodeIdx = 1; nodeIdx < nNode; nodeIdx++) {
                    if (nodes[nodeIdx] instanceof Order && ((Order)nodes[nodeIdx]).visited) continue;
                    if (trucks[truckIdx].evaluateNode(nodes[nodeIdx], distanceMatrix)) {
                        if (onlyDepot && nodes[nodeIdx] instanceof Order) onlyDepot = false;
                        feasibleEdges.add(new Pair<>(trucks[truckIdx].nowIdx, nodeIdx));
                    }
                }
                if (onlyDepot) feasibleEdges.clear();
                if (feasibleEdges.isEmpty()) {
                    if (trucks[truckIdx].nowIdx != 0) trucks[truckIdx].addNode(nodes[0], distanceMatrix); //return to main depot
                    else trucks[truckIdx].nowTime = trucks[truckIdx].nowTime.plusMinutes(30); // wait at main depot
                }
            }

            if (feasibleEdges.isEmpty()) {
                if (truckIdx + 1 < nTruck) { // check if there are other available trucks
                    if (trucks[truckIdx].nowIdx != 0) // current truck didn't returned to main plaint
                        trucks[truckIdx].addNode(nodes[0], distanceMatrix);
                    truckIdx += 1;
                } else {
                    break; // collapse
                };
            } else {
                int nextNodeIdx;
                if (getRandom() < threshold) {
                    // choose randomly next node to prevent local optimization
                    nextNodeIdx = feasibleEdges.get(Math.abs(rand.nextInt()) % feasibleEdges.size()).y;
                } else {                     
                    // follow pheromone trail and heuristic to choose next node
                    ArrayList<Double> ups = new ArrayList<>();
                    ArrayList<Double> probs = new ArrayList<>();
                    ArrayList<Double> cumulativeSum = new ArrayList<>();
                    double sum = 0.0;
                    for (Pair<Integer, Integer> feasibleEdge : feasibleEdges){
                        double up = calculateProbability(feasibleEdge.x, feasibleEdge.y);
                        sum += up;
                        ups.add(up);
                    }
                    for (Double up : ups){
                        probs.add(up / sum);
                    }
                    cumulativeSum.add(probs.get(0));
                    for (int i = 0; i < probs.size()-1; i++) {
                        probs.set(i+1, probs.get(i+1) + probs.get(i));
                        cumulativeSum.add(probs.get(i+1));
                    }
                    int bestIdx = 0;
                    double bestV = INF;
                    double r = getRandom();
                    for (int x = 0; x < cumulativeSum.size(); x++) {
                        if (r <= cumulativeSum.get(x)) {
                            double candidateV = cumulativeSum.get(x);
                            if (candidateV < bestV) {
                                bestIdx = x;
                                bestV = candidateV;
                            }
                        }
                    }
                    if ((int)bestV == INF)
                        break;
                    nextNodeIdx = feasibleEdges.get(bestIdx).y;
                }
                trucks[truckIdx].addNode(nodes[nextNodeIdx], distanceMatrix);
            }
        }
        if (trucks[truckIdx].nowIdx != 0) {
            // in case the vehicle did not return back to the depot
            trucks[truckIdx].addNode(nodes[0], distanceMatrix);
        }
   }
}