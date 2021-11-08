package pe.sag.routing.aStar;

import pe.sag.routing.algorithm.*;
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
        //Imprimir mapa completo
        //map.printMap();

        //Recorrido de rutas y asignacion de nodos (una ruta por camion)
        //System.out.println("Rutas:");
        for (Route route : routes) {
            //route.printRoute();
            LocalDateTime departureDate, limitDate;
            ArrayList<ListStructure> solutionLists = new ArrayList<>();
            NodeList nodeStart, nodeGoal;

            //Agregar planta principal tanto al inicio como al fin de la ruta (nodes)
            ArrayList<NodeInfo> nodes = new ArrayList<>();
            DepotInfo depotInfo = new DepotInfo(12,8,0,route.getStartDate());
            nodes.add(depotInfo);
            nodes.addAll(route.getNodesInfo());
            depotInfo = new DepotInfo(12,8,0,route.getFinishDate());
            nodes.add(depotInfo);

            //Inicializar arrivalDate con fecha inicio de ruta
            LocalDateTime arrivalDate = LocalDateTime.of(route.getStartDate().toLocalDate(),route.getStartDate().toLocalTime());

            for (int j = 0; j < nodes.size()-1; j++) {
                nodeStart = new NodeList(nodes.get(j));
                nodeGoal = new NodeList(nodes.get(j+1));

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

                //Revisar si fecha de llegada de camino se pasa de fecha limite (solamente si no es planta)
                if(!nodeGoal.isDepot() && !solutionList.limitDateOK){
                    //truncar ruta de camion
                    route.setFinishDate(arrivalDate);
                    break;
                }

                //Actualizar fecha salida
                arrivalDate = departureDate.plusSeconds(solutionList.timeSec);
                if(j < nodes.size()-2){
                    NodeInfo node = route.getNodesInfo().get(j);
                    if(node instanceof OrderInfo && node.getArrivalTime() == ((OrderInfo)node).getDeliveryDate()){
                        ((OrderInfo)node).setDeliveryDate(arrivalDate);
                    }
                    route.getNodesInfo().get(j).setArrivalTime(arrivalDate);
                }
                else{
                    route.setFinishDate(arrivalDate);
                }
                //Imprimir camino
                //solutionList.printList();
                //Añadir camino
                solutionLists.add(solutionList);
            }

            //Convertir solutionLists en path de route
            ArrayList<Pair<Integer,Integer>> pairListCompleted = new ArrayList<>();
            for(ListStructure solutionList : solutionLists){
                ArrayList<Pair<Integer,Integer>> pairList = new ArrayList<>();
                //Pintar Ruta
                map.paintRoute(solutionList);
                //Acumular lista de pares
                NodeList aux = solutionList.first;
                while ( aux != null ) {
                    Pair<Integer, Integer> pair = new Pair<>(aux.cordX,aux.cordY);
                    pairList.add(pair);
                    aux = aux.next;
                }
                Collections.reverse(pairList);
                pairListCompleted.addAll(pairList);
            }
            Collections.reverse(pairListCompleted);
            nodeStart = new NodeList(nodes.get(0));
            Pair<Integer, Integer> pair = new Pair<>(nodeStart.cordX,nodeStart.cordY);
            pairListCompleted.add(0,pair);
            route.setPath(pairListCompleted);
        }

        //Imprimir mapa completo
        //map.printMap();

        return routes;
    }
}

