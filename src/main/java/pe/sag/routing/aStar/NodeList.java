package pe.sag.routing.aStar;

import pe.sag.routing.algorithm.NodeInfo;

import java.time.LocalDateTime;

public class NodeList {
    //Ubicacion de plantas
    public static final int MAIN_DEPOT_X = 12;
    public static final int MAIN_DEPOT_Y = 8;
    public static final int INTERMEDIATE_DEPOT_NORTH_X = 42;
    public static final int INTERMEDIATE_DEPOT_NORTH_Y = 42;
    public static final int INTERMEDIATE_DEPOT_EAST_X = 63;
    public static final int INTERMEDIATE_DEPOT_EAST_Y = 3;

    NodeList father;
    NodeList son;
    NodeList next;
    boolean visited;
    public int cordX;
    public int cordY;
    int costF, costG, costH;
    public LocalDateTime limitDate;
    public LocalDateTime arrivalTime;


    ///////CONSTRUCTOR//////////////////////////////////////////////////////////
    public NodeList(int x,int y){
        this.cordX = x;
        this.cordY = y;
        //this.fechaLimite = new Fecha(-3,0,0);
        visited = false;
        father=null;
    }

    public NodeList(int x,int y, LocalDateTime limitDate){
        this.cordX = x;
        this.cordY = y;
        this.limitDate = limitDate;
        visited = false;
        father = null;
    }

    public NodeList(NodeInfo nodeInfo){
        this.cordX = nodeInfo.getX();
        this.cordY = nodeInfo.getY();
        this.arrivalTime = nodeInfo.getArrivalTime();
        visited = false;
        father = null;
    }

    //////METODO PARA ASIGNAR EL NODO PADRE/////////////////////////////////////
    public void asignFather(NodeList father){
        this.father = father;
    }

    public boolean isDepot(){
        return cordX == MAIN_DEPOT_X && cordY == MAIN_DEPOT_Y ||
                cordX == INTERMEDIATE_DEPOT_NORTH_X && cordY == INTERMEDIATE_DEPOT_NORTH_Y ||
                cordX == INTERMEDIATE_DEPOT_EAST_X && cordY == INTERMEDIATE_DEPOT_EAST_Y;
    }
}
