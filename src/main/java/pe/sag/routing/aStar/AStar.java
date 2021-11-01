package pe.sag.routing.aStar;

import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import pe.sag.routing.algorithm.Node;
import pe.sag.routing.algorithm.Pair;
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
        ListStructure solutionList = map.getSolutionList(roadblocks, startDate);
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
}

