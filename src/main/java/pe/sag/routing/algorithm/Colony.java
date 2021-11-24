package pe.sag.routing.algorithm;

import lombok.*;
import pe.sag.routing.aStar.AStar;
import pe.sag.routing.core.model.Roadblock;

import java.time.Duration;
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
    private static final int ITERATOR = 100;
    private Double threshold;
    private Double[][] pheromoneMatrix;
    private Double[][] ethaMatrix;
    private Random rand;

    public List<Route> solutionRoutes = null;
    public List<Order> solutionOrders = null;
    public List<Order> orderList = null;
    public List<Depot> solutionDepots = null;
    public List<Roadblock> roadblocks = null;
    public double bestSolutionQuality;

    public Colony(List<Order> orders, List<Truck> trucks, List<Depot> depots, List<Roadblock> roadblocks) {
        super(trucks, orders, depots);
        this.orderList = orders;
        this.roadblocks = roadblocks;
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
                    ethaMatrix[i][j] += Q / (Duration.between(((Order)nodes[j]).twOpen, ((Order)nodes[j]).twClose).toHours()/10.0) + Q * (((Order) nodes[j]).totalDemand/5);
                else ethaMatrix[i][j] += 0.85;
                ethaMatrix[j][i] = Q / (distanceMatrix[j][i]+1);
                if (nodes[i] instanceof Order)
                    ethaMatrix[j][i] += Q / (Duration.between(((Order)nodes[i]).twOpen, ((Order)nodes[i]).twClose).toHours()/10.0) + Q * (((Order) nodes[i]).totalDemand/5);
                else ethaMatrix[j][i] += 0.85;
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

    public void updatePheroMatrix(int visited) {
        for (int i = 0; i < nTruck; i++) {
            int tourDistance = calculateTourDistance(trucks[i].tour, 0);
            for (int j = 0; j < trucks[i].tour.size(); j++) {
                if (j+1 != trucks[i].tour.size()) {
                    int currIdx = trucks[i].tour.get(j).idx;
                    int nextIdx = trucks[i].tour.get(j+1).idx;
                    pheromoneMatrix[currIdx][nextIdx] =
                            EVAP_RATE * pheromoneMatrix[currIdx][nextIdx] + Q / tourDistance + (Q * visited);
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

    private int validateRoutes() {
//        AStar astar = new AStar();
//        List<Route> routes = parseRoutes();
//        List<Route> validatedRoutes = astar.run(routes, orderList, roadblocks);
        int totalDistance = 0;
        List<Route> validatedRoutes = parseRoutes();
        for (Route r : validatedRoutes) {
            r.generatePath();
            totalDistance += r.getPath().size();
            if (r.getPath().size() <= 1) return -1;
            Pair<Integer, Integer> lastNode = r.getPath().get(r.getPath().size()-1);
            Pair<Integer, Integer> firstNode = r.getPath().get(0);
            if (firstNode.getX() != 12 || firstNode.getY() != 8) return -1;
            if (lastNode.getX() != 12 || lastNode.getY() != 8) return -1;
        }
        solutionRoutes = validatedRoutes;
        return totalDistance;
    }

    public void run()  {
        double bestSolution = Double.MIN_VALUE;
        double bestCost = Double.MAX_VALUE;
        for(int i = 0; i < ITERATOR; i++) {
            solve();
            int attendedCustomers = 0;
            double totalGLP = 0.0;
            double totalConsumption = 0.0;
            for (Truck t: trucks) {
                attendedCustomers += t.attendedCustomers;
                totalGLP += t.totalDelivered;
                totalConsumption += t.totalFuelConsumption;
            }
            int visited = 0;
            for (int k = 3; k < nodes.length; k++) {
                if (((Order)nodes[k]).visited) visited++;
            }
            updatePheroMatrix(visited);
//            double quality = visited + 1/totalConsumption + 1/totalGLP;
            double quality = visited;
            int total = validateRoutes();
            if (quality > bestSolution || (quality == bestSolution && total < bestCost)) {
                if (total != -1) {
                    bestSolution = quality;
                    bestCost = total;
                    saveBestSolution();
//                    if (i >= 50 && bestSolution > 30) {
//                        resetStep();
//                        break;
//                    }
                }
            }
            updateThreshold();
            resetStep();
        }
        this.bestSolutionQuality = bestSolution;
    }

    private List<Route> parseRoutes() {
        List<Route> solutionRoutes = new ArrayList<>();
        for (Truck t : trucks) {
            int depIdx = 0;
            int arrIdx = 0;
            int fuelIdx = 0;
            int glpIdx = 0;
            double routeConsumption = 0.0;
            double routeDelivered = 0.0;
            double consumption;
            ArrayList<NodeInfo> routeNodes = new ArrayList<>();
            LocalDateTime startTime = null;
            LocalDateTime endTime = null;
            if (t.tour.size() == 1) continue;
            int i = 0;
            while (i < t.tour.size()) {
                Node n = t.tour.get(i);
                if (n.idx == 0) {
                    if (routeNodes.isEmpty()) { //start of new route
                        startTime = t.departureRegistry.get(depIdx++);
                        routeConsumption = 0.0;
                        routeDelivered = 0.0;
                        i++;
                    } else { //end of current route
                        routeConsumption += t.fuelConsumption.get(fuelIdx++);
                        endTime = t.arrivalRegistry.get(arrIdx++);
                        Route route = new Route(t._id, t.code, startTime, endTime, routeNodes, routeConsumption, routeDelivered);
                        solutionRoutes.add(route);
                        routeNodes = new ArrayList<>();
                        if (i == t.tour.size()-1) break;
                    }
                } else {
                    consumption = t.fuelConsumption.get(fuelIdx++);
                    routeConsumption += consumption;
                    if (n instanceof Depot) {
                        Depot depot = (Depot)n;
                        DepotInfo depotInfo = new DepotInfo(n.x, n.y, t.glpRegistry.get(glpIdx++), t.arrivalRegistry.get(arrIdx++));
                        if (depot.id != null) depotInfo.setId(depot.id);
                        routeNodes.add(depotInfo);
                    }
                    else {
                        double delivered = t.glpRegistry.get(glpIdx++);
                        routeDelivered += delivered;
                        routeNodes.add(new OrderInfo(n.x, n.y, ((Order)n)._id, ((Order)n).deliveryTime, delivered,
                                t.arrivalRegistry.get(arrIdx++)));
                    }
                    i++;
                }
            }
        }
        return solutionRoutes;
    }

    private void saveBestSolution() {
        solutionOrders = new ArrayList<>();
        solutionDepots = List.of(new Depot((Depot)nodes[1]), new Depot((Depot)nodes[2]));
        for (Node node : nodes) {
            if (node instanceof Order) {
                Order order = (Order)node;
                solutionOrders.add(new Order(order));
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
                trucks[truckIdx].addNode(nodes[0], distanceMatrix, nodes);
            ArrayList<Pair<Integer, Integer>> feasibleEdges = new ArrayList<>();
            while (feasibleEdges.isEmpty() && (trucks[truckIdx].nowTime).isBefore(lastOrder) && !trucks[truckIdx].finished) {
                onlyDepot = true;
                for (int nodeIdx = 1; nodeIdx < nNode; nodeIdx++) {
                    if (nodes[nodeIdx] instanceof Order && ((Order)nodes[nodeIdx]).visited) continue;
//                    if (trucks[truckIdx].evaluateNode(nodes[nodeIdx], distanceMatrix)) {
                    if (trucks[truckIdx].evaluateNode(nodes[nodeIdx], distanceMatrix, nodes)) {
                        if (onlyDepot && nodes[nodeIdx] instanceof Order) onlyDepot = false;
                        feasibleEdges.add(new Pair<>(trucks[truckIdx].nowIdx, nodeIdx));
                    }
                }
                if (onlyDepot) feasibleEdges.clear();
                if (feasibleEdges.isEmpty()) {
                    if (trucks[truckIdx].nowIdx != 0) trucks[truckIdx].addNode(nodes[0], distanceMatrix, nodes); //return to main depot
                    else trucks[truckIdx].nowTime = trucks[truckIdx].nowTime.plusMinutes(15); // wait at main depot
                }
            }

            if (feasibleEdges.isEmpty()) {
                if (truckIdx + 1 < nTruck) { // check if there are other available trucks
                    if (trucks[truckIdx].nowIdx != 0) // current truck didn't returned to main plaint
                        trucks[truckIdx].addNode(nodes[0], distanceMatrix, nodes);
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
                trucks[truckIdx].addNode(nodes[nextNodeIdx], distanceMatrix, nodes);
            }
        }
        if (trucks[truckIdx].nowIdx != 0) {
            // in case the vehicle did not return back to the depot
            trucks[truckIdx].addNode(nodes[0], distanceMatrix, nodes);
        }
   }
}