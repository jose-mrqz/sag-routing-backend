package pe.sag.routing.aStar;

import pe.sag.routing.algorithm.Node;
import pe.sag.routing.algorithm.Order;
import pe.sag.routing.core.service.RoadblockService;

import java.time.LocalDateTime;

public class AStarMain {
    public static void main(String[] args){
        AStar aStar = new AStar();
        LocalDateTime startDate = LocalDateTime.of(2021,11,2,10,50,0);
        Node nodeStart = new Node(5,5,1);
        Node nodeGoal = new Node(30,35,2);
        aStar.run(startDate,nodeStart,nodeGoal);
    }
}
