package pe.sag.routing.aStar;

import java.time.LocalDateTime;

public class ListStructure {
    public NodeList first, last, aux;
    public int numberNodes;
    public int distanceKm;
    public long timeSec;
    public LocalDateTime departureDate;
    public LocalDateTime arriveDate;
    public LocalDateTime limitDate;
    public boolean limitDateOK;

    ///////CONSTRUCTOR//////////////////////////////////////////////////////////
    public ListStructure(){
        this.first = this.last = null;
        this.numberNodes = 0;
        this.distanceKm = 0;
        this.timeSec = 0;
        this.departureDate = null;
        this.arriveDate = null;
        this.limitDate = null;
        this.limitDateOK = false;
    }

    public ListStructure(LocalDateTime departureDate, LocalDateTime limitDate){
        this.first = this.last = null;
        this.numberNodes = 0;
        this.distanceKm = 0;
        this.timeSec = 0;
        this.departureDate = LocalDateTime.of(departureDate.toLocalDate(),departureDate.toLocalTime());
        this.arriveDate = null;
        if(limitDate != null) this.limitDate = LocalDateTime.of(limitDate.toLocalDate(),limitDate.toLocalTime());
        this.limitDateOK = false;
    }

    ///////METODO PARA AGRGAR NODOS/////////////////////////////////////////////
    public void addNode( NodeList nodin ){
        //para lista vacia
        if( first == null ){
            first = last = nodin;
        }
        //en caso de mas nodos
        else{
            aux = first;
            while( aux.next != null ){
                aux = aux.next;
            }
            aux.next = nodin;
            last = aux.next;
        }
        numberNodes++;
    }

    ///////METODO QUE ELIMINA EL PRIMERO DE LA LISTA Y LO DEVUELVE//////////////
    public NodeList deleteFirst(){
        NodeList temp = first;
        first = first.next;
        numberNodes--;
        return temp;
    }

    ///////METODO QUE ELIMINA EL NODO QUE LE MANDES/////////////////////////////
    public NodeList delete( NodeList nodin ){
        NodeList aux = first;
        if (nodin == null || first == null) return null;
        if( ( nodin.cordX == first.cordX ) &&  ( nodin.cordY == first.cordY ) ){
            return deleteFirst();
        }
        while( aux.next != null ){
            if( ( nodin.cordX == aux.next.cordX ) &&  ( nodin.cordY == aux.next.cordY ) ){
                aux.next = aux.next.next;
            }
            else
                aux = aux.next;
        }
        numberNodes--;
        return nodin;
    }

    ///////METODO QUE BUSCA EN LA LISTA EL NODO CON MENOR COSTO, LO ELIMINA/////
    ///////Y LO REGRESA/////////////////////////////////////////////////////////
    public NodeList deleteLeastCost(){
        NodeList aux = first;
        NodeList least = first;

        while ( aux != null ){
            if( aux.costF < least.costF )
                least = aux;
            else
                aux = aux.next;
        }
        numberNodes--;
        return delete( least );
    }

    ///////METODO QUE CALCULA DISTANCIA RECORRIDA (KM) Y TIEMPO DE RECORRIDO (MIN)//////////////////////////////////////
    public void calcularDistanceTime(){
        //Calcular distanceKm y timeMin
        distanceKm = numberNodes-1;
        timeSec = (long) distanceKm *60*60/50;
        //Calcular fecha llegada
        arriveDate = LocalDateTime.of(departureDate.toLocalDate(),departureDate.toLocalTime());
        arriveDate = arriveDate.plusSeconds(timeSec);
        //Revisar si el pedido llego antes de la fecha limite
        if(limitDate != null) limitDateOK = arriveDate.isBefore(limitDate);
    }

    //////METODO PARA DETERMINAR SI LA LISTA ESTA VACIA/////////////////////////
    public boolean isEmpty(){
        return first == null;
    }

    ///////METODO QUE MUESTRA LA LISTA//////////////////////////////////////////
    public void printList(){
        NodeList aux2 = first;
        String cad = "";
        while ( aux2 != null ) {
            if (aux2.cordX == first.cordX && aux2.cordY == first.cordY) {
                cad = aux2.cordX + "," + aux2.cordY + cad;
            } else {
                cad = aux2.cordX + "," + aux2.cordY + "," + cad;
            }
            aux2 = aux2.next;
        }
        String str = "numberNodes: "+numberNodes+", distanceKm: "+distanceKm+", timeMin: "+timeSec/60+"\n";
        str += "startDate: "+departureDate+", fechaLlegada: "+arriveDate;
        System.out.println(str);
    }
}
