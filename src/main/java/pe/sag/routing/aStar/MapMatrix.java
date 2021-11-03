package pe.sag.routing.aStar;

import pe.sag.routing.core.model.Roadblock;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MapMatrix {
    //Parametros para revision de bloqueos a tiempo real
    public static final int NUMBER_NODES_EVALUATION = 10; //esto equivale a 12 min
    public static final long TIME_PER_CICLE = 12;

    int[][] matrix;
    int cordX,cordY;
    int sizeX;
    int sizeY;
    NodeList startNode,goalNode;
    ListStructure openList;
    ListStructure closeList;
    ListStructure solutionList;

    public MapMatrix( int sizeX, int sizeY ){
        openList = new ListStructure();
        closeList = new ListStructure();
        solutionList = new ListStructure ();

        this.sizeX = sizeX;
        this.sizeY = sizeY;
        matrix = new int[sizeX][sizeY];
        initializedMatrix();

        startNode = null;
        goalNode = null;

    }

    ///////METODO PARA ESTABLECER LAS PAREDES EL EL LABERINTO///////////////////
    public void setRoadblocks( int x, int y ){
        this.matrix[x][y] = 1;
    }

    ///////METODO PARA ESTABLECER EL INICIO Y LA META///////////////////////////
    public void setInitialNodes( int x_ini, int y_ini, int x_fin, int y_fin){
        startNode = new NodeList(x_ini,y_ini);
        goalNode = new NodeList(x_fin,y_fin);
        matrix[x_ini][y_ini] = 8;
        matrix[x_fin][y_fin] = 9;
    }

    ///////METODO QUE INICIALIZA LOS VALORES DE LA MATRIZ///////////////////////
    public void initializedMatrix(){
        for(int i = 0; i < sizeX; i++){
            for(int j = 0; j < sizeY; j++){
                matrix[i][j] = 0;
            }
        }
    }

    //////METODO QUE EMPIEZA EL ALGORITMO Y REGRESA EL CAMINO FINAL/////////////
    public ListStructure getSolutionList(List<Roadblock> roadblocks, LocalDateTime startDate, LocalDateTime limitDate){
        //Inicializar atributos antes de algoritmo
        openList = new ListStructure();
        closeList = new ListStructure();
        solutionList = new ListStructure ();

        //Iniciar algoritmo
        startAStar( startNode, roadblocks, startDate);

        //Creacion de lista
        NodeList extra2 = solutionList.first;
        ListStructure solution = new ListStructure(startDate, limitDate);

        while( extra2 != null ){
            extra2.next = null;
            solution.addNode(extra2);
            extra2 = extra2.father;
        }
        solution.calcularDistanceTime();
        if(solution.first != null) solution.delete(goalNode);

        return solution;
    }///////////////////////////////////////////////////////////////////////////

    ///////METODO QUE REALIZA EL ALGORITMO (EN FORMA GENERAL)///////////////////
    public void startAStar( NodeList inicial, List<Roadblock> roadblocks, LocalDateTime startDate ){
        openList.addNode( inicial );
        NodeList actual = openList.deleteLeastCost();
        int numberNodes = 0;
        int contCiclos = 0;
        LocalDateTime fechaMin1 = LocalDateTime.of(startDate.toLocalDate(),startDate.toLocalTime());
        LocalDateTime fechaMin2 = LocalDateTime.of(startDate.toLocalDate(),startDate.toLocalTime());

        while( !inOpenList( goalNode )){
            NodeList extra = actual;
            extra.next = null;
            closeList.addNode( extra );

            //si el actual es igual al destino
            if( ( ( actual.cordX == goalNode.cordX ) && ( actual.cordY == goalNode.cordY )  ) )//|| openList.isEmpty() )
                break;//terminamos

            else{
                //obtengo adyacentes
                int x = 0, y = 0,w = 0,w2 = 0;
                for( int i = 0; i < 3; i++ ){
                    for( int j = 0; j < 3; j++ ){
                        //verificamos que no sea el de referencia
                        if(i == 1 && j == 1){
                            continue;
                        }
                        //valores de i
                        if(i == 0 )
                            w = -1;
                        else if(i == 1)
                            w = 0;
                        else if(i == 2)
                            w = 1;

                        //valores de j
                        if(j == 0 )
                            w2 = -1;
                        else if(j == 1)
                            w2 = 0;
                        else if(j == 2)
                            w2 = 1;

                        //asignamos los x y y correspondientes
                        x = actual.cordX + w;
                        y = actual.cordY + w2;


                        //los asignamos a un nodo el cual es el adyacente
                        NodeList adyacente = new NodeList( x, y );
                        // si no es transitable o si estan el la lista cerrada
                        if( ( !( (x >= 0) && (y >= 0) && (x < sizeX) && (y < sizeY) && (matrix[x][y] != 1) ) )  || inCloseList( adyacente ) ){
                            continue;//lo ignoro
                        }

                        //consideramos los casos para avance diagonal donde rosa
                        NodeList extra3;
                        if( ( adyacente.cordX != actual.cordX ) && ( adyacente.cordY != actual.cordY ) ){
                            ////QUITANDO EL CONTINUE Y DESCOMENTANDO LO DE ABAJO ACEPTAMOS CAMINOS DIAGONALES///////////////////////////
                            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
                            continue;
	                            /*
	                            if( !(adyacente.cordX-1 < 0) ){// -1 0
	                                extra3 = new nodo( adyacente.cordX-1, adyacente.cordY );
	                                if( ( matrix[adyacente.cordX-1][adyacente.cordY] == 1 ) && distancia( extra3, actual ) ==10 ){
	                                    continue;
	                                }
	                            }
	                            if( !(adyacente.cordX+1 >= tamanio) ){// +1 0
	                                extra3 = new nodo( adyacente.cordX+1, adyacente.cordY );
	                                if( ( matrix[adyacente.cordX+1][adyacente.cordY] == 1 ) && distancia( extra3, actual ) ==10 ){
	                                    continue;
	                                }
	                            }

	                            if( !(adyacente.cordY+1 >= tamanio) ){// 0 +1
	                                extra3 = new nodo( adyacente.cordX, adyacente.cordY+1 );
	                                if( ( matrix[adyacente.cordX][adyacente.cordY+1] == 1 ) && distancia( extra3, actual ) ==10 ){
	                                    continue;
	                                }
	                            }

	                            if( !(adyacente.cordY-1 < 0) ){// 0 -1
	                                extra3 = new nodo( adyacente.cordX, adyacente.cordY-1 );
	                                if( ( matrix[adyacente.cordX][adyacente.cordY-1] == 1 ) && distancia( extra3, actual ) ==10 ){
	                                    continue;
	                                }
	                            }
	                             */
                        }

                        //comprobamos que no este en la openList ni en la cerrada
                        if( !inOpenList(adyacente) && !inCloseList(adyacente) ){
                            adyacente = setValores( actual, adyacente );
                            adyacente.father = actual;
                            openList.addNode( adyacente );
                        }

                        else if( inOpenList( adyacente ) ){
                            if( setValores( actual, adyacente ).costF < actual.costF ){
                                adyacente = setValores( actual, adyacente );
                                adyacente.father = actual;
                            }
                        }

                        else if( !inOpenList( adyacente ) ){
                            if( inCloseList( adyacente ) )
                                ;//lo ignoramos
                        }
                    }//fin del primer for
                }//fin del segundo for

            }//fin del else

            ///////evaluar cuantos nodos se han recorrdo y actualizar bloqueos
            numberNodes++;
            if(numberNodes == NUMBER_NODES_EVALUATION || contCiclos == 0) {
                fechaMin1 = LocalDateTime.of(fechaMin2.toLocalDate(),fechaMin2.toLocalTime());
                fechaMin2 = fechaMin2.plusMinutes(TIME_PER_CICLE);
                //Reiniciar mapa
                initializedMatrix();
                //Obtener bloqueos dentro de rango [timeMin1;timeMin2]
                ArrayList<Roadblock> asignedRoadblocks = asignRoadblocks(roadblocks,fechaMin1,fechaMin2);
                //Pintar bloqueos en mapa
                for(Roadblock rb : asignedRoadblocks) {
                    setRoadblocks(rb.getX(), rb.getY());
                }
                //System.out.println(contCiclos);
                //imprimeMatriz();
                numberNodes = 0;
                contCiclos++;
            }

            actual = openList.deleteLeastCost();

            //preguntamos si ya encontro el solutionList para mostrarlo
            if( ( actual.cordX == goalNode.cordX ) &&  ( actual.cordY == goalNode.cordY ) ){
                NodeList extra2 = actual;

                while( extra2 != null ){
                    extra2.next = null;
                    solutionList.addNode(extra2);
                    extra2 = extra2.father;
                }
            }
        }//fin del for
    }

    ///////METODO QUE RECOBE UN NODO ACTUAL Y UNO ADYACENTE Y LE PONE///////////
    ///////SUS VALORES RESPECTIVOS DE F,G Y H Y LO REGRESA//////////////////////
    public NodeList setValores( NodeList actual, NodeList adyacente ){
        //si es el mismo
        if( ( actual.cordX == adyacente.cordX ) && ( actual.cordY == adyacente.cordY ) )
            return actual;
        else if( ( actual.cordX == adyacente.cordX ) || ( actual.cordY == adyacente.cordY ) )
            adyacente.costG = 10;
        else
            adyacente.costG = 14;

        adyacente.costH = distancia( adyacente, goalNode );
        adyacente.costF = adyacente.costG + adyacente.costH;

        return adyacente;
    }

    //////METODO QUE CALCULA LA DISTANCIA MANHATTAN/////////////////////////////
    public int distancia(NodeList a, NodeList b){
        int distance = Math.abs(b.cordX-a.cordX) + Math.abs(b.cordY-a.cordY);
        return distance*10;
    }

    ///////METODO QUE VERIFICA SI UN NODO ESTA EN LA LISTA DE CERRADOS//////////
    ///////Y REGRESA UN VERDADERO O FALSO SEGUN SEA EL CASO/////////////////////
    public boolean inCloseList( NodeList nodin ){
        NodeList aux = closeList.first;

        //la lista esta vacia
        if(aux == null)
            return false;

        while( aux != null ){
            if( ( nodin.cordX == aux.cordX ) &&  ( nodin.cordY == aux.cordY ) )
                return true;

            aux = aux.next;
        }
        return nodin.visited;
    }

    ///////METODO QUE VERIFICA SI UN NODO ESTA EN LA LISTA DE ABIERTOS//////////
    ///////Y REGRESA UN VERDADERO O FALSO SEGUN SEA EL CASO/////////////////////
    public boolean inOpenList( NodeList nodin ){
        if( !openList.isEmpty() )
            return false;
        NodeList aux = openList.first;

        //la lista esta vacia
        if(aux == null)
            return false;

        while( aux != null ){
            if( ( nodin.cordX == aux.cordX ) &&  ( nodin.cordY == aux.cordY ) )
                return true;

            aux = aux.next;
        }
        return false;

    }

    //////METODO PARA IMPRIMIR LA MATRIZ////////////////////////////////////////
    public void printMap(){
        System.out.println(" ");
        for(int j = sizeY-1; j >= 0; j--){
            for(int i = 0; i < sizeX; i++){
                System.out.print(" "+matrix[i][j] );
            }
            System.out.println(" ");
        }
    }

    public void paintRoute(ListStructure ruta) {
        NodeList aux2 = ruta.first;
        while ( aux2 != null ){
            matrix[aux2.cordX][aux2.cordY] = 2;
            aux2 = aux2.next;
        }
    }

    public ArrayList<Roadblock> asignRoadblocks(List<Roadblock> roadblocks,LocalDateTime fechaMin1,LocalDateTime fechaMin2){
        ArrayList<Roadblock> asignBlocks = new ArrayList<>();

        for(Roadblock rb : roadblocks) {
            if(rb.validateDates(fechaMin1,fechaMin2)){
                asignBlocks.add(rb);
            }
        }
        return asignBlocks;
    }
}
