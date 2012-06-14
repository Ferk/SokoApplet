

import java.applet.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;



/**
 * Clase principal del applet para el juego, conteniendo la interfaz.
 * <br/>
 * <a href="../src/SokoApplet.java">[Pulse aquí apra ver el código fuente]</a>
 * <hr/>
 * En el desarrollo de esta aplicación se ha separado a conciencia el programa en dos clases,
 * Esta clase sirve como interfaz con el usuario y se encarga de cargar los recursos externos multimedia,
 * archivo de niveles (levelset) y gestión de los eventos de teclado recibidos, mientras que el código 
 * que implementa el comportamiento del juego está en una clase aparte (Board).<br/>
 * 
 * De este modo sería sencillo programar otras interfaces para el mismo juego, portandolo a otros toolkits.<br/><br/>
 *
 * A continuación una screenshot del 
 * <img src="screenshot.png"/>
 *
 * <hr/>
 * El applet que recibe dos parámetros: “levelset” y “theme”. Ambos deben ser URLs (relativas o absolutas) en la que se encuentra el archivo que contiene el pack de niveles y el directorio que contiene el tema de gráficos y sonido.
 * 
 * <h3>El formato de lectura de packs de niveles (levelsets)</h3>
 * Para cargar los niveles se emplea la lectura de un archivo externo de niveles en formato texto. Este formato es el que se usa frecuentemente para compartir niveles por internet compatibles con otras versiones del juego Sokoban. Algunos ejemplos de archivos de niveles compatibles con este applet se pueden, por ejemplo, desde el siguiente enlace: http://users.bentonrea.com/~sasquatch/sokoban/
 * 
 * <h3>El formato de lectura de packs de gráficos y sonido (themes)</h3>
 * En este caso se trata simplemente de un conjunto de ficheros situados en la dirección dada.
 * 
 * <h3>Mostrando el tablero por pantalla</h3>
 * Simplemente se recorren las posiciones del tablero, y mediante llamadas a Board se va viendo que contiene cada casilla y dibujando en pantalla la imagen correspondiente al objeto de esa casilla. Si el objeto no es reconocido o no existe una imagen correcta para este, el espacio quedará vacío.<br/>
 * Existe también un método que redibujará sólo la parte de la pantalla alrededor del jugador, para evitar tener que actualizar todo el Graphics del applet.
 * 
 * <h3>El escuchador de eventos</h3>
 * En lugar de implementar ActionListener, ni crear una clase aparte se ha utilizado el método “processKeyEvent” y activado los eventos de teclado previamente mediante “enableEvents(java.awt.AWTEvent.KEY_EVENT_MASK);”<br/>
 * El escuchador de eventos también comprueba tras cada movimiento si se ha ganado el juego en ese tablero, y si esto ocurre pasa al siguiente nivel, cambiando de tablero.
 *  
 * @author Fernando Carmona Varo
 */
public class SokoApplet extends Applet {
	
	static final long serialVersionUID=0;
	/** Fuente a usar al mostrar información de ayuda */
	static final Font font = new Font("Helvetica", Font.PLAIN, 12);
	/** Fuente a usar al mostrar el título y nivel del juego */
	static final Font fontb = new Font("Helvetica", Font.BOLD, 20);
	/** Tamaño en pixeles de las imagenes para las casillas */
	static final int tSize = 24;
	/** Matriz de imagenes a usar para mostrar el juego */
	Image tiles[] = new Image[256];
	/** Clip de audio a usar */
	AudioClip auCompleted, auPushed;
	/** Tablero del juego actual, que contiene el estado y la lógica de juego */
	Board board;
	/** Contador de niveles */
	int currentLevel;
	/** Lista de niveles del pack de niveles cargado (levelset) */
	LinkedList<String> levels = new LinkedList<String>();	
	
	/**
	 * Función de inicialización del applet
	 * Los parámetros del juego a cargar que deberán indicarse mediante etiquetas html son:
	 * 
	 ** levelset (opcional) *
	 * Dirección URL indicando el archivo en formato texto de niveles Sokoban a cargar 
	 * Si no se especifica se tomará por defecto el fichero  "levels.txt" del directorio en el que se 
	 * esté ejecutando el Applet. 
	 * La ruta puede ser absoluta o relativa.
	 * 
	 ** theme (opcional) *
	 * Dirección URL indicando el directorio dónde se encuentran los archivos de audio e imágenes.
	 * Si no se especifica se tomará por defecto el directorio web en el que se esté ejecutando el applet.
	 * La ruta puede ser absoluta o relativa.
	 */
	@Override
	public void init() {
		super.init();

		// Inicializar parámetros 
		setSize(500, 500);                              				

		// Cargar el contenido gráfico y audio
		
		URL themeUrl;
		try {
			themeUrl = new URL( getDocumentBase(), getParameter("theme") );
		} catch (MalformedURLException e) {
			System.out.println("No se ha indicado un Tema de imagenes/sonidos, o la dirección indicada" +
					" es incorrecta, se usará la URL base del Applet en su defecto.");
			themeUrl= getDocumentBase();
		}				
		tiles[(int) Board.WALL] = getImage(themeUrl, "wall.png");
		tiles[(int) Board.EMPTY] = null;
		tiles[(int) Board.PLAYER] = getImage(themeUrl, "player.png");
		tiles[(int) Board.BOX] = getImage(themeUrl, "box.png");
		tiles[(int) Board.GOAL_EMPTY] = getImage(themeUrl, "goal_empty.png");
		tiles[(int) Board.GOAL_PLAYER] = getImage(themeUrl, "goal_player.png");
		tiles[(int) Board.GOAL_BOX] = getImage(themeUrl, "goal_box.png");
		auCompleted = getAudioClip(themeUrl, "completed.au");
		auPushed = getAudioClip(themeUrl, "pushed.au");


		// load the levels from the levelfile
		String lvlFile= getParameter("levelset");
		if(lvlFile==null) {
			System.out.println("No se ha indicado LevelSet, se usará \"levels.txt\" por defecto.");
			lvlFile="levels.txt";
		}
		try {            
			InputStream in = new URL(this.getCodeBase(), lvlFile).openStream();
			BufferedReader dis = new BufferedReader(new InputStreamReader(in));
			
			for (String line = ""; line != null; line = dis.readLine()) {
				if (line.contains("#")) {
					String levelStr = line+"\n";
					//System.out.println("---\n"+line);
					 while ((line = dis.readLine()).contains("#")) {
						 levelStr += line + "\n";
						 //System.out.println(line);
					 }
					 
					 levels.add(levelStr);
				}
			}
			
			in.close();
		} catch (Exception ex) {
			System.out.println("** Error cargando el archivo de niveles **");
			Logger.getLogger(SokoApplet.class.getName()).log(Level.SEVERE, null, ex);
			levels.add(" @ $.\n");
		}
		currentLevel = -1;
		nextLevel();
		
		requestFocus();
		enableEvents(java.awt.AWTEvent.KEY_EVENT_MASK);		
	}

	
	/**
	 * Pasa al siguiente nivel del juego, actualizando el contador.
	 */
	private void nextLevel() {
		currentLevel++;
		if (currentLevel >= levels.size()) {
			currentLevel--;
		}
		System.out.println("Starting level " + currentLevel + "...");
		restartLevel();
	}
	
	/**
	 * Regresa al anterior nivel del juego, actualizando el contador
	 */
	private void previousLevel() {        
		if(currentLevel > 0) {
			currentLevel--;        
			System.out.println("Starting level " + currentLevel + "...");
		}
		restartLevel();
	}

	/**
	 * Reinicia el nivel actual dado por el contador de niveles y redibuja la pantalla.
	 * 
	 * nextLevel y previousLevel siempre llaman a este método al final, de forma que
	 * tras cambiar el contador siempre se creará un nuevo Tablero de juego con el estado
	 * inicial del juego dado por el String correcpondiente almacenado en la lista de niveles.  
	 */
	private void restartLevel() {
		board = new Board(levels.get(currentLevel));
		repaint();
	}

	
	
	/**
	 *  Función de dibujado en pantalla del mapa de juego
	 */
	@Override
	public void paint(Graphics g) {		
		Dimension d = this.getSize();
		if (d.width * d.height == 0) return; // Por si acaso alguien cambia el tamaño en el html
		int x = 20, y = 20;

		g.setColor(Color.black);
		g.fillRect(0, 0, d.width, d.height);
		g.setFont(fontb);
		g.setColor(Color.red);
		g.drawString("~ Sokoban :: Level " + (currentLevel+1) +  " ~", d.width / 4, y);
		y += 20;
		g.setFont(font);        
		g.drawString("Flechas: movimiento, R: reinicia nivel, +/-: cambia nivel, U: deshacer",x,y);


		x= (d.width-(board.XLim*tSize))/2;
		y= (50+d.height-(board.YLim*tSize))/2;
		for (int i = 0; i < board.XLim; i++) {
			for (int j = 0; j < board.YLim; j++) {
				g.drawImage(tiles[(int) board.get(i, j)], x + i * tSize, y + j * tSize, this);
			}
		}		
	}

	/**
	 * Redibuja sólo la zona de la ventana alrededor del jugador, para evitar
	 * lo máximo posible los parpadeos.
	 * 
	 * @param radius Radio en número de casillas más alla del jugador que será actualizado
	 *             si el parámetro es cero sólo se actualizará la casilla del jugador (no deseable)
	 */	
	private void drawMove(int radius) {
		Dimension d = this.getSize();
		int x= board.pl_x-1;
		if(x<0) x=0;
		x= x*tSize + (d.width-(board.XLim*tSize))/2;
		int y= board.pl_y-1;
		if(y<0) y=0;
		y= y*tSize + (50+d.height-(board.YLim*tSize))/2;
		repaint(x,y,tSize*(1+radius),tSize*(1+radius));
	}


	/**
	 * Función que procesa los eventos de teclado, no es necesario un EventListener
	 * 
	 * @param e Evento de teclado recibido
	 */
	@Override
	protected void processKeyEvent(KeyEvent e) {
		super.processKeyEvent(e);
		if (e.getID() == KeyEvent.KEY_PRESSED) {
			boolean mov=false;
			switch (e.getKeyCode()) {
			case KeyEvent.VK_RIGHT:
			case KeyEvent.VK_L:
				mov= board.movePlayer(+1, 0);                    	
				break;
			case KeyEvent.VK_LEFT:
			case KeyEvent.VK_H:
				mov= board.movePlayer(-1, 0);
				break;
			case KeyEvent.VK_UP:
			case KeyEvent.VK_K:
				mov= board.movePlayer(0, -1);
				break;
			case KeyEvent.VK_DOWN:
			case KeyEvent.VK_J:
				mov= board.movePlayer(0, +1);
				break;

			case KeyEvent.VK_U:
				board.undoMove();
				repaint();
				break;
			case KeyEvent.VK_R:
				restartLevel();
				break;
			case KeyEvent.VK_PLUS:
				nextLevel();
				break;
			case KeyEvent.VK_MINUS:                	
				previousLevel();
				break;
			}            			
			if(mov) {
				auPushed.play();
				drawMove(3);
			} else {
				drawMove(2);
			}
			if (board.hasWon()) {
				auCompleted.play();
				nextLevel();
			}
		}
	}
}