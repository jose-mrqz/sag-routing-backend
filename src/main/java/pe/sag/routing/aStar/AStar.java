package pe.sag.routing.aStar;

import pe.sag.routing.algorithm.Node;
import pe.sag.routing.algorithm.Order;
import pe.sag.routing.algorithm.Pair;
import pe.sag.routing.algorithm.Route;
import pe.sag.routing.core.model.Roadblock;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AStar {
    //Dimensiones del mapa
    public static final int SIZE_MAP_X = 70;
    public static final int SIZE_MAP_Y = 50;

    public List<Pair<Integer,Integer>> run(LocalDateTime startDate, Node nodeStart, Node nodeGoal, List<Roadblock> roadblocks) {
        //Creacion de mapa
        MapMatrix map = new MapMatrix(SIZE_MAP_X,SIZE_MAP_Y);

        //Carga de bloqueos en mapa
        //System.out.println("Bloqueos:");
        for(Roadblock rb : roadblocks) {
            //rb.printRoadblock();
            map.setRoadblocks(rb.getX(), rb.getY());
        }
        //System.out.println("");

        //Encontrar ruta optima
        //System.out.println("Start Node: ("+nodeStart.getX()+","+nodeStart.getY()+")");
        //System.out.println("Goal Node: ("+nodeGoal.getX()+","+nodeGoal.getY()+")");
        //Fijado de posiciones de inicio y fin
        map.setInitialNodes(nodeStart.getX(), nodeStart.getY(), nodeGoal.getX(), nodeGoal.getY());
        //Encontrar camino optimo
        ListStructure solutionList = map.getSolutionList(roadblocks, startDate, null);
        //Imprimir camino
        //solutionList.printList();

        //Imprimir map completo
        //map.paintRoute(solutionList);
        //map.printMap();

        List<Pair<Integer,Integer>> pairList = new ArrayList<>();
        NodeList aux = solutionList.first;
        while ( aux != null ) {
            Pair<Integer, Integer> pair = new Pair<>(aux.cordX,aux.cordY);
            pairList.add(pair);
            aux = aux.next;
        }
        Collections.reverse(pairList);
        return pairList;
    }

    public List<Route> run(List<Route> routes, List<Order> orders, List<Roadblock> roadblocks) {
        //Creacion de mapa
        MapMatrix map = new MapMatrix(SIZE_MAP_X,SIZE_MAP_Y);

        //Carga de bloqueos en mapa
        //System.out.println("Bloqueos:");
        for(Roadblock rb : roadblocks) {
            //rb.printRoadblock();
            map.setRoadblocks(rb.getX(), rb.getY());
        }
        //System.out.println("");

        //Recorrido de rutas y asignacion de nodos
        boolean limitDateExcedida = false;
        //System.out.println("Rutas:");
        for (Route route : routes) {
            //route.printRoute();
            LocalDateTime departureDate, limitDate;
            ArrayList<ListStructure> solutionLists = new ArrayList<>();
            for (int j = 0; j < route.getNodesInfo().size() - 1; j++) {
                NodeList nodeStart = new NodeList(route.getNodesInfo().get(j));
                NodeList nodeGoal = new NodeList(route.getNodesInfo().get(j+1));

                //Para departurteDate, revisar si nodeStart no es planta principal/intermedia, es decir, pedido para aumentarle 10min
                departureDate = LocalDateTime.of(nodeStart.arrivalTime.toLocalDate(),nodeStart.arrivalTime.toLocalTime());
                if(!nodeStart.isDepot()){
                    departureDate = departureDate.plusMinutes(10);
                }

                //Para limitDate, revisar si nodeStart no es planta principal/intermedia, es decir, pedido para buscarla en orders
                limitDate = null;
                if(!nodeGoal.isDepot()){
                    for(Order o : orders){
                        if(nodeGoal.cordX == o.getX() && nodeGoal.cordY == o.getY()){
                            limitDate = o.getTwClose();
                            break;
                        }
                    }
                }

                //Fijado de posiciones de inicio y fin
                map.setInitialNodes(nodeStart.cordX, nodeStart.cordY, nodeGoal.cordX, nodeGoal.cordY);
                //Encontrar camino optimo
                ListStructure solutionList = map.getSolutionList(roadblocks, departureDate, limitDate);

                //Revisar si fecha de llegada de camino no se pasa de fecha limite (solamente si no es planta)
                if(!nodeGoal.isDepot() && !solutionList.limitDateOK){
                    System.out.println("Se excede en Ruta: "+route.getTruckId()+", Nodo 1: "+nodeStart.cordX+","+nodeStart.cordY+
                            ", Nodo 2: "+nodeGoal.cordX+","+nodeGoal.cordY);
                    limitDateExcedida = true;
                    break;
                }

                //Actualizar fecha salida
                assert false;
                departureDate = departureDate.plusSeconds(solutionList.timeSec/60);

                //Imprimir camino
                solutionList.printList();
                //Añadir camino
                solutionLists.add(solutionList);
            }

            ArrayList<Pair<Integer,Integer>> pairList = new ArrayList<>();

            for(ListStructure solutionList : solutionLists){
                //Pintar Ruta
                map.paintRoute(solutionList);
                //Acumular lista de pares
                NodeList aux = solutionList.first;
                while ( aux != null ) {
                    Pair<Integer, Integer> pair = new Pair<>(aux.cordX,aux.cordY);
                    pairList.add(pair);
                    aux = aux.next;
                }
            }
            Collections.reverse(pairList);
            route.setPath(pairList);
        }

        //Imprimir mapa completo
        map.printMap();

        //Retornar datos
        if(!limitDateExcedida){
            System.out.println("\n La fecha limite no fue excedida para ningun pedido");
        }
        else {
            System.out.println("\n La fecha limite fue excedida para algun pedido");
        }

        return routes;
    }
}

