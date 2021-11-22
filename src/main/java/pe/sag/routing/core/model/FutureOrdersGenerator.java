package pe.sag.routing.core.model;

import pe.sag.routing.algorithm.Pair;
import pe.sag.routing.shared.dto.OrderDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FutureOrdersGenerator {
    public ArrayList<OrderDto> futureOrders;
    public ArrayList<Integer> distributionOrders;
    public ArrayList<ArrayList<Integer>> distributionTrucks;
    public List<Integer> distributionSlacks;

    public FutureOrdersGenerator() {
        this.futureOrders = new ArrayList<>();
        this.distributionOrders = new ArrayList<>();
        this.distributionSlacks = new ArrayList<>();
    }

    public ArrayList<OrderDto> generateFutureOrders(int maxGLP, LocalDateTime orderDate){
        getOrdersDistribution(maxGLP);
        generateDistributionSlacks();
        //Generar variacion de horas
        int totalOrders = distributionOrders.stream().mapToInt(x->x).sum();
        long timeToAdd = 24*60*60/totalOrders;

        for (ArrayList<Integer> demandGLPPerTruck : distributionTrucks) { //5 tipos
            for (Integer demandGLP : demandGLPPerTruck) { //por cada tipo
                int totalDemand = 0;

                //Generar coordenadas
                Pair<Integer, Integer> pair = getRandomCoordinates();
                int x = pair.getX(), y = pair.getY();

                //Generar horas limite
                int slack = getRandomSlack();

                OrderDto orderDto = OrderDto.builder()
                        .x(x)
                        .y(y)
                        .demandGLP(demandGLP)
                        .totalDemand(totalDemand)
                        .registrationDate(orderDate)
                        .deadlineDate(orderDate.plusHours(slack))
                        .build();

                futureOrders.add(orderDto);
                orderDate = orderDate.plusSeconds(timeToAdd);
            }
        }
        return futureOrders;
    }

    //proporciones por camiones cisterna
    public void getOrdersDistribution(int maxGLP) {
        List<Integer> distributionOrdersActual;
        List<Integer> distributionOrdersBefore = List.of(1,2,4,4,10); //T+, TA, TB, TC, TD
        ArrayList<ArrayList<Integer>> distributionTrucksActual;
        ArrayList<ArrayList<Integer>> distributionTrucksBefore = generateDistributionTrucks(distributionOrdersBefore);
        int k = 2;
        for(int i=0;i<10000;i++){
            distributionOrdersActual = List.of(k,k*2,k*4,k*4,k*10);
            distributionTrucksActual = generateDistributionTrucks(distributionOrdersActual);
            if(getTotalGLP(distributionTrucksActual) > maxGLP) break;
            distributionTrucksBefore = new ArrayList<>(distributionTrucksActual);
            distributionOrdersBefore = new ArrayList<>(distributionOrdersActual);
            k++;
        }
        distributionOrders.addAll(distributionOrdersBefore);
        distributionTrucks = new ArrayList<>(distributionTrucksBefore);
    }

    public ArrayList<ArrayList<Integer>> generateDistributionTrucks(List<Integer> actualDistribution){
        ArrayList<ArrayList<Integer>> distributionTrucksActual = new ArrayList<>(List.of(new ArrayList<>(),new ArrayList<>(),new ArrayList<>(),new ArrayList<>(),new ArrayList<>()));
        for(int i=0;i<5;i++) {
            ArrayList<Integer> glpi = new ArrayList<>();
            for (int j = 0; j < actualDistribution.get(i); j++) {
                int demandGLP = getRandomDemand(i);
                glpi.add(demandGLP);
            }
            distributionTrucksActual.set(i, glpi);
        }
        return distributionTrucksActual;
    }

    public double getTotalGLP(ArrayList<ArrayList<Integer>> distributionTrucksActual){
        int totalGLP = 0;
        for (ArrayList<Integer> demandGLPPerTruck : distributionTrucksActual) { //5 tipos
            for (Integer demandGLP : demandGLPPerTruck) { //por cada tipo
                totalGLP += demandGLP;
            }
        }
        return totalGLP;
    }

    public int getRandomDemand(int i) {
        switch (i){
            case 0:
                return ThreadLocalRandom.current().nextInt(25, 35);
            case 1:
                return ThreadLocalRandom.current().nextInt(15, 25);
            case 2:
                return ThreadLocalRandom.current().nextInt(10, 15);
            case 3:
                return ThreadLocalRandom.current().nextInt(5, 10);
            case 4:
                return ThreadLocalRandom.current().nextInt(1, 5);
            default:
                return 0;
        }
    }

    public Pair<Integer, Integer> getRandomCoordinates() {
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
        coords.setX(x);
        coords.setY(y);
        return coords;
    }

    public void generateDistributionSlacks(){
        int totalOrders = distributionOrders.stream().mapToInt(x->x).sum();
        this.distributionSlacks = Stream.of(totalOrders*0.1,totalOrders*0.4,totalOrders*0.4,totalOrders*0.1).map(Double::intValue).collect(Collectors.toList());

        //si por el redondeo (truncado), no se llega al total de pedidos, se debe completar
        int difference = totalOrders - distributionSlacks.stream().mapToInt(x->x).sum();
        if(difference > 0){
            for(int i=0; i < difference;i++){
                int distribution = ThreadLocalRandom.current().nextInt(0, 4);
                distributionSlacks.set(distribution,distributionSlacks.get(distribution)+1);
            }
        }
        else if(difference < 0){
            difference *= -1;
            for(int i=0; i < difference;i++){
                int distribution = ThreadLocalRandom.current().nextInt(0, 4);
                distributionSlacks.set(distribution,distributionSlacks.get(distribution)-1);
            }
        }
    }

    public int getRandomSlack() {
        int slack, distribution;

        //Obtener numero aleatorio entre 0 y 3 para generar slack segun rango
        do {
            distribution = ThreadLocalRandom.current().nextInt(0, 4);
        } while (distributionSlacks.get(distribution) <= 0);

        switch (distribution){
            case 0:
                slack = 4;
                break;
            case 1:
                slack = ThreadLocalRandom.current().nextInt(5, 12);
                break;
            case 2:
                slack = ThreadLocalRandom.current().nextInt(12, 18);
                break;
            case 3:
                slack = ThreadLocalRandom.current().nextInt(18, 36);
                break;
            default:
                return 0;
        }
        //Quitar el rango de slack ya asignado
        distributionSlacks.set(distribution,distributionSlacks.get(distribution)-1);
        return slack;
    }
}

