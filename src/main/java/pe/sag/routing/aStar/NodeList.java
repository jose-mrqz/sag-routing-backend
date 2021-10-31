package pe.sag.routing.aStar;

import java.time.LocalDateTime;

public class NodeList {
    NodeList father;
    NodeList son;
    NodeList next;
    boolean visited;
    public int cordX;
    public int cordY;
    int costF, costG, costH;
    public LocalDateTime limitDate;


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

    //////METODO PARA ASIGNAR EL NODO PADRE/////////////////////////////////////
    public void asignFather(NodeList father){
        this.father = father;
    }
}
