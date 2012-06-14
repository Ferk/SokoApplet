
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Clase que contiene la lógica del juego.
 * <br/>
 * <a href="../src/Board.java">[Pulse aquí apra ver el código fuente]</a>
 * <hr/>
 * La clase Board es empleada para almacenar el estado del tablero del juego así como para implementar
 * los movimientos del jugador sobre el tablero y los efectos de las colisiones.
 *
 * <h3>Construcción del tablero</h3>
 * La clase Board.java genera el tablero directamente en el constructor de su clase. Recibe como parámetro un String que contiene codificado en caracteres los objetos del tablero (cada carácter representa a un objeto) separando las filas del tablero por saltos de linea.<br/>
 * El tablero se introduce en una matriz de caracteres mientras que la posición del jugador en el tablero se almacena en una variable aparte para poder localizarlo con facilidad.
 * 
 * <h3>Movimiento del jugador y historial de movimientos</h3>
 * La clase Board tiene un método para mover al jugador que es el que se encarga de comprobar si este movimiento es posible (hay espacio libre, o un objeto que puede ser empujado) y realizarlo, moviendo al jugador, y en su caso empujando el objeto.<br/>
 * Cada movimiento es añadido a una lista que almacena un historial de los mvimientos del jugador, de forma que con otro método es posible deshacer movimientos del jugador.
 * 
 * <h3>Fin del nivel</h3>
 * Cuando todas las cajas han sido colocadas en un almacén, el método “hasWon()” de la clase retornará true.
 * 
 * @author Fernando Carmona Varo
 */
public class Board { 

	/* Caracteres que definen los objetos del mapa */
	/** jugador */
    public static final char PLAYER      = '@'; 
    /** muro, todo el mapa debe estar rodeado de muros */
    public static final char WALL        = '#'; 
    /** espacio vacío */
    public static final char EMPTY       = ' '; 
    /**  Caja, objeto que el jugador puede empujar */
    public static final char BOX         = '$'; 
    /**  Meta vacía, sobre la que hay que mover las cajas para ganar */
    public static final char GOAL_EMPTY  = '.'; 
    /**  Meta sobre la que ya hay situada una caja */
    public static final char GOAL_BOX    = '*'; 
    /**  Meta sobre la que está situado el jugador */
    public static final char GOAL_PLAYER = '+'; 
    /* Propiedades del mapa  */
    /**  Límite máximo de longitud X del tablero */
    public int XLim; 
    /**  Límite máximo de longitud Y del tablero */
    public int YLim; 
    /**  Matriz que almacena el estado del tablero */
    private char gBoard[][]; 
    /**  Posición del jugador */
    public int pl_x,  pl_y;  
    /**  historial de movimientos del jugador */
    LinkedList<Integer> moveHistory= new LinkedList<Integer>(); 

    /**
     * Construye el tablero inicial de juego a partir del texto dado      
     * 
     * @param text El string determinará la estructura del mapa, donde cada fila de items correspondera 
     * con una linea del string (la fila acaba al llegar a un '\n'). Los objetos del mapa se corresponden 
     * con las constantes indicadas en la clase.  
     */
    public Board(String text) {
        gBoard = new char[255][255];
        XLim = YLim = 0;
        for (int x = 0, y = 0, i = 0; i < text.length(); i++) {
            char aux = text.charAt(i);
            switch (aux) {
                case '\n':
                    YLim = y + 1;
                    if (XLim < x) {
                        XLim = x;
                    }
                    y++;
                    x = 0; //pasa a la siguiente linea
                    continue;
                case PLAYER: case GOAL_PLAYER:
                    pl_x = x;
                    pl_y = y;
                case GOAL_EMPTY:

                default:
                    gBoard[x][y] = aux;
            }
            x++;
        }
    }

    /**
     * Constructor vacío
     * Construye un mapa simple por defecto, añadido por simple conveniencia
     */
    public Board() {
        this("############\n" +
              "# @     ..#\n" +
              "#    $$ ..#\n" +
              "##### $####\n" +
              "    # $   #\n" +
              "    #     #\n" +              
              "    #######\n");
    }

    /**
     * Devuelve el caracter del objeto ubicado en las coordenadas dadas del tablero
     * 
     * @param x Ordenada X del tablero
     * @param y Coordenada Y del tablero
     * @return Caracter que identifica al objeto
     */
    public char get(int x, int y) {
        return gBoard[x][y];
    }

    /**
     * Devuelve el complementario del objeto dado, con respecto a si es un Objetivo (GOAL) o una casilla normal   
     * 
     * @param obj Objeto del tablero
     * @return Si el objeto es de tipo GOAL devuelve el equivalente de tipo normal 
     *          Si el objeto es de tipo normal devuelve el equivalente de tipo GOAL
     */
    private char switchGoal(char obj) {
        char ret;
        switch (obj) {
            // from obj to goal
            case PLAYER:
                ret = GOAL_PLAYER;
                break;
            case BOX:
                ret = GOAL_BOX;
                break;
            case EMPTY:
                ret = GOAL_EMPTY;
                break;
            // from goal to obj
            case GOAL_PLAYER:
                ret = PLAYER;
                break;
            case GOAL_BOX:
                ret = BOX;
                break;
            default:
                ret = EMPTY;
                break;
        }
        return ret;
    }

    /**
     * Comprueba si la casilla es un objetivo.
     * 
     * @param obj Objeto que se desea comprobar
     * @return Devuelve true si el objeto dado es una meta, false en otro caso
     */
    private boolean isGoal(char obj) {
        return (obj == GOAL_EMPTY || obj == GOAL_PLAYER || obj == GOAL_BOX);
    }

    /**
     * Comprueba si la casilla está vacía.
     * 
     * @param obj Objeto que se desea comprobar
     * @return Devuelve true si el objeto dado es un espacio vacío, false en otro caso
     */
    private boolean isEmpty(char obj) {
        return (obj == GOAL_EMPTY || obj == EMPTY);
    }

    /**
     * Comprueba si el objeto dado puede ser empujado por el jugador.
     * 
     * @param obj Objeto que se desea comprobar
     * @return Devuelve true si el objeto dado es empujable, false en otro caso
     */
    private boolean isPushable(char obj) {
        return (obj == BOX || obj == GOAL_BOX);
    }

    /**
     * Comprueba si se ha ganado el juego.
     * 
     * @return Devuelve true si no quedan cajas que no se hayan colocado ya en un objetivo
     */
    public boolean hasWon() {
        boolean win= true;
        for(int i=0; i<XLim; i++ )
            for(int j=0; j<YLim; j++ ) 
                if(gBoard[i][j]==BOX) {
                    win=false;
                }
        return win;
    }

    /**
     * Mueve un objeto de una casilla del tablero a otra, sobreescribiendo lo que existiese en esa casilla
     * pero manteniendo si es un objetivo/no objetivo o no.
     * 
     * @param x0 ordenada del origen
     * @param y0 coordenada del origen
     * @param x1 ordenada del destino
     * @param y1 coordenada del destino
     */
    private void moveFromTo(int x0, int y0, int x1, int y1) {
        // Si origen y destino son ambos del mismo tipo copiar normalmente
        if (isGoal(gBoard[x1][y1]) == isGoal(gBoard[x0][y0])) {
            gBoard[x1][y1] = gBoard[x0][y0];
        } else { // sino habrá que cambiar el tipo al copiar
            gBoard[x1][y1] = switchGoal(gBoard[x0][y0]);
        }

        if (isGoal(gBoard[x0][y0])) {
            gBoard[x0][y0] = GOAL_EMPTY;
        } else {
            gBoard[x0][y0] = EMPTY;
        }
    }

    /**
     * Mueve el jugador por el tablero
     * 
     * @param dx Incremento en su posición respecto del eje X
     * @param dy Incremento en su posición respecto del eje Y
     * @return Devuelve true si se efectua una operación de empuje 
     */
    public boolean movePlayer(int dx, int dy) {        
        int x = pl_x + dx;
        int y = pl_y + dy;
        boolean pushed=false;
        if (isEmpty(gBoard[x][y])) {
            moveFromTo(pl_x, pl_y, x, y);
            moveHistory.addFirst(pl_x*1000 +pl_y);
            pl_x = x;
            pl_y = y;                        
            System.out.println("pushed: "+moveHistory.getFirst());
        } else {
            if (isPushable(gBoard[x][y]) && isEmpty(gBoard[x + dx][y + dy])) {
                moveFromTo(x, y, x + dx, y + dy);
                moveFromTo(pl_x, pl_y, x, y);
                pushed= true;
                moveHistory.addFirst(1000000+pl_x*1000 +pl_y );
                pl_x = x;
                pl_y = y;              
                System.out.println("pushed: "+moveHistory.getFirst());
            }
        }
        printHistory();
        return pushed;
    }
    


    /**
     * Muestra por System.out el historial de movimientos del jugador
     * 
     * (útil para obtener información de depuración)
     */
    private void printHistory() {
    	Iterator iterator = moveHistory.iterator();
    	System.out.print("hist: ");
        while (iterator.hasNext()) {
          System.out.print(iterator.next()+",");  
        } System.out.print("\n");
    }
    
    /** 
     * Devuelve el número de movimientos realizados por el jugador
     * 
     *  @return movimientos 
     */
    public int moveNumber() {
    	return moveHistory.size();
    }
    
    /**
     * Deshace el último movimiento realizado por el jugador.
     * 
     * Puede ser llamado de forma sucesiva y ir deshaciendo los movimientos guardados en cola.
     */
    public void undoMove() {    	
    	int x, y;
    	if(moveHistory.size()>0) {
    		boolean pushed=false;
    		x= moveHistory.removeFirst();
    		if (x>1000000) {
    			pushed=true;
    			x= x-1000000;
    		}
    		printHistory();
    		System.out.println("unmoved: "+x);
    		y= x%1000;
    		x= x/1000;
    		//System.out.println(pl_x+","+pl_y+"->"+x+","+y);
    		moveFromTo(pl_x, pl_y, x, y);
    		// Deshace el empujon de la casilla siguiente
    		if (pushed) {
        		int xnext= pl_x+(pl_x-x);
        		int ynext= pl_y+(pl_y-y);
    			moveFromTo(xnext, ynext, pl_x, pl_y);
    			System.out.println("unpush:"+xnext+","+ynext+"->"+pl_x+","+pl_y);
    		}
    		// actualiza la posición del jugador
    		pl_x=x;
    		pl_y=y;
    	}
    }
}
