package com.tclark.maze;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Base64;
import java.util.Stack;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class Maze extends JPanel implements Runnable{
	/**
	 * The maze is drawn with an extra row on the top and bottom, and an extra column on the left and right.
	 * This simplifies the creation of the maze.  Because we want to maintain a "visit" count for standard cells
	 * We need a default visit count for the border cells.
	 */
	private final static int BORDER_VISITED = 16;
	/** 
	 * Value representing the North or Up direction
	 */
	private final static int NORTH = 1;
	/** 
	 * Value representing the East or Right direction
	 */
	private final static int EAST = 2;
	/** 
	 * Value representing the South or Down direction
	 */
	private final static int SOUTH = 4;
	/** 
	 * Value representing the West or Left direction
	 */
	private final static int WEST = 8;
	/**
	 * 
	 */
	private boolean isRunning;
	/**
	 * A Position is a location in the maze and the list of moves not attempted.
	 * @author tclark
	 */
	private class Position {
		/**
		 * The {@link Point} that describes the x and y location of a square in the Maze
		 */
		public Point point;  
		/**
		 * The list of moves that have not been attempted at this location.
		 */
		public ArrayList<Integer> moves;
		/**
		 * @param point x,y position of this point
		 * @param moves the moves not yet attempted at this location
		 */
		public Position(Point point, ArrayList<Integer> moves){
			this.point = point;
			this.moves = moves;
		}
	} 
	/**
	 * A Last In, First Out storage of the cells visited in a given path
	 */
	private Stack<Position> _stkCells;
	public int get_cellHeight() {
		return _cellHeight;
	}
	public int get_cellWidth() {
		return _cellWidth;
	}
	/**
	 * The height of a square in pixels for graphical display
	 */
	private int _cellHeight;
	/**
	 * The width of a cell in pixels for graphical display
	 */
	private int _cellWidth;
	byte[] _encodedMazeBytes;
	byte[] _decodedMazeBytes;
	public void set_encodedMazeBytes(byte[] encodedMazeBytes){
		_encodedMazeBytes = encodedMazeBytes;
	}
	/**
	 * The amount of white space on the left and right side of the maze in pixels
	 */
	private int _borderX = 10;
	
	public int get_borderY() {
		return _borderY;
	}
	public int get_borderX( ) {
		return _borderX;
	}
	/**
	 * The amount of white space on the top and bottom of the maze in pixels
	 */
	private int _borderY = 10;
	/**
	 * Half of the thickness of the maze wall in pixels
	 */
	private int _halfWallThickness = 2;
	/**
	 * The number of columns in the maze
	 */
	private int _numColumns;
	/**
	 * The number of rows in the maze
	 */
	private int _numRows;
	/**
	 * 
	 */
	private Point _startCell = new Point(0,0);
	private Point _endCell = new Point(0,0);
	/**
	 * This is a 2-Dimensional array.  Each x,y position in the array will hold the value
	 *  true if a wall is present in the North direction or false if the wall is down.
	 */
	private boolean[][] _northWalls;
	/**
	 * This is a 2-Dimensional array.  Each x,y position in the array will hold the value
	 * true if a wall is present in the East direction or false if the wall is down.
	 */
	private boolean[][] _eastWalls;
	/**
	 * This is a 2-Dimensional array.  Each x,y position in the array will hold the value
	 * true if a wall is present in the South direction or false if the wall is down.
	 */
	private boolean[][] _southWalls;
	/**
	 * This is a 2-Dimensional array.  Each x,y position in the array will hold the value
	 * true if a wall is present in the West direction or false if the wall is down.
	 */	
	private boolean[][] _westWalls;
	/**
	 * This is a 2-Dimensional array.  Each x,y position in the array will hold the number

	 * of times the cell has been visited.
	 */
	private int[][] _visitCount;
	public int get_visitCount(int x,int y) {
		return _visitCount[x][y];
	}
	public void increment_visitCount(int x, int y) {
		this._visitCount[x][y]++;
	}
	/**
	 * This is a 2-Dimensional array.  Each x,y position in the array will hold a true/false 
	 * value indicating if the cell has been visited.
	 */
	private boolean[][] _visited;
	/**
	 * Task Listeners
	 */
	private java.util.List<TaskListener> listeners = Collections.synchronizedList( new ArrayList<TaskListener>() );

	public String get_MazeString(){
		StringBuilder sb = new StringBuilder();
		Integer cell = 0;
		for(int x = 0; x < _numColumns+2; x++){
			for(int y = 0; y < _numRows+2;y++){
				cell = 0;
				if(this._startCell.x == x && this._startCell.y == y){
					cell += 32;
				}
				if(this._endCell.x == x && this._endCell.y == y){
					cell += 16;
				}
				if(this._northWalls[x][y]){
					cell += NORTH;
				}
				if(this._eastWalls[x][y]){
					cell += EAST;
				}
				if(this._southWalls[x][y]){
					cell += SOUTH;
				}
				if(this._westWalls[x][y]){
					cell += WEST;
				}
				sb.append(cell.toString());
				if (y < _numColumns + 1){
					sb.append(",");
				}
			}
			if ( x < _numColumns + 1){
				sb.append('.');
			}
		}
		Base64.Encoder e = Base64.getEncoder();
		return e.encodeToString(sb.toString().getBytes());
	}
	public void build_MazeFromBytes(){
		Base64.Decoder d = Base64.getDecoder();
		String decodedMazeString="";
		
		_decodedMazeBytes = d.decode(_encodedMazeBytes);
		try {
			decodedMazeString = new String(_decodedMazeBytes,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String[] rows;
		String[] cols;
		rows = decodedMazeString.split("\\.");
		cols = rows[0].split(",");
		resetMaze(rows.length-2,cols.length-2);
		for(int x = 0; x < rows.length; x++){
			cols = rows[x].split(",");
			for(int y = 0; y < cols.length; y++){
				int val = new Integer(cols[y]);  
				if(val - 32 >= 0){
					this._startCell = new Point(x,y);
					val -= 32;
				}
				if(val - 16 >= 0){
					this._endCell = new Point(x,y);
					val -= 16;
				}
				if (val - WEST >= 0){
					_westWalls[x][y] = true;
					val -= WEST;
				}else{
					_westWalls[x][y] = false;
				}
				if (val - SOUTH >= 0){
					_southWalls[x][y] = true;
					val -= SOUTH;
				}else{
					_southWalls[x][y] = false;
				}
				
				if (val - EAST >= 0){
					_eastWalls[x][y] = true;
					val -= EAST;
				}else{
					_eastWalls[x][y] = false;
				}
				
				if (val - NORTH == 0){
					_northWalls[x][y] = true;
					val -= NORTH;
				}else{
					_northWalls[x][y] = false;
				}
				repaint();
				try{
					Thread.sleep(5);//1000/1000);
				}catch(InterruptedException ie){
					isRunning = false;
				}
			}
		}
	}
	public int get_numColumns() {
		return _numColumns;
	}
	public int get_numRows() {
		return _numRows;
	}
	/**
	 * Adds a listener to this object. 
	 * @param listener Adds a new listener to this object. 
	 */
	public void addListener( TaskListener listener ){
		listeners.add(listener);
	}
	/**
	 * Removes a particular listener from this object, or does nothing if the listener
	 * is not registered. 
	 * @param listener The listener to remove. 
	 */
	public void removeListener(TaskListener listener){
		listeners.remove(listener);
	}
	/**
	 * Notifies all listeners that the thread has completed.
	 */
	private final void notifyListeners() {
		synchronized ( listeners ){
			for (TaskListener listener : listeners) {
				listener.threadComplete(this);
			}
		}
	}
	/**
	 * @return the isRunning
	 */
	public boolean isRunning() {
		return isRunning;
	}
	/**
	 * @param isRunning the isRunning to set
	 */
	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}
	/**
	 * Create the Maze object.
	 * Prepare the stack of actively used cells
	 */
	public Maze(){
		this._stkCells = new Stack<Position>();
	}
	/**
	 * 
	 * @param x The number of columns in the maze
	 * @param y The number of rows in the maze
	 * Here we prepare the maze to be recreated.
	 */
	public void resetMaze(int x, int y){
		this.setBackground(Color.WHITE);
		_startCell = new Point(0,0);
		_endCell = new Point(0,0);
		prepMaze(x,y);
		this.initialize();
		this._stkCells.clear();
		repaint();
	}
	/**
	 * 
	 * @param x The number of columns in the maze
	 * @param y The number of rows in the maze
	 * Here we store the maze dimensions and calculate the pixel dimensions of each cell.
	 */
	private void prepMaze(int x, int y){
		_numColumns = x;
		_numRows = y;
		_cellWidth = (this.getWidth() - (2*this._borderX))/(x+2);
		_cellHeight = (this.getHeight() - (2*this._borderY))/(y+2);
	}

	@Override
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		for(int x = 1; x < _numColumns+1; x++){
			for(int y = 1; y < _numRows+1;y++){
				if(_northWalls[x][y] && _eastWalls[x][y] && _southWalls[x][y] && _westWalls[x][y]){
					g.fillRect((x*this._cellWidth)+this._borderX, 
							(y*this._cellHeight)+this._borderY,
							this._cellWidth,this._cellHeight);
				}
				if(!_northWalls[x][y] || !_eastWalls[x][y] || !_southWalls[x][y] || !_westWalls[x][y]){

					g.clearRect((x*this._cellWidth)+this._borderX, 
							(y*this._cellHeight)+this._borderY,
							this._cellWidth,this._cellHeight);
				}

				if(_northWalls[x][y]){
					g.fillRect((x*this._cellWidth)+this._borderX,
							((y*this._cellHeight)+this._borderY) - _halfWallThickness,
							this._cellWidth, 
							_halfWallThickness*2);
				}
				if(_eastWalls[x][y]){
					g.fillRect((((x+1)*this._cellWidth)+this._borderX) - _halfWallThickness,
							(y*this._cellHeight)+this._borderY,
							_halfWallThickness*2,
							this._cellHeight
							);
				}
				if(_southWalls[x][y]){
					g.fillRect((x)*this._cellWidth+this._borderX,
							(((y+1)*this._cellHeight)+this._borderY) - _halfWallThickness,
							this._cellWidth,
							_halfWallThickness*2);
				}
				if(_westWalls[x][y]){
					g.fillRect((((x)*this._cellWidth)+this._borderX) - _halfWallThickness,
							(y)*this._cellHeight+this._borderY,
							_halfWallThickness*2,
							this._cellHeight
							);
				}
			}
		}
		if (_startCell.x != 0){
			g.setColor(Color.GREEN);	
			g.fill3DRect(((_startCell.x*this._cellWidth)+this._borderX)+_cellWidth/4,
					((_startCell.y*this._cellHeight)+this._borderY)+_cellHeight/4,
					_cellWidth/2, _cellHeight/2, false);			
			g.setColor(Color.RED);	
			g.fill3DRect(((_endCell.x*this._cellWidth)+this._borderX)+_cellWidth/4,
					((_endCell.y*this._cellHeight)+this._borderY)+_cellHeight/4,
					_cellWidth/2, _cellHeight/2, false);			}

	}
	/**
	 * 
	 * @return A randomized list of the possible directions N,S,E,W
	 */
	private ArrayList<Integer> getArrayList(){
		ArrayList<Integer> al = new ArrayList<Integer>();
		al.add(NORTH);
		al.add(EAST);
		al.add(SOUTH);
		al.add(WEST);
		Collections.shuffle(al);
		return al;
	}
	
	/**
	 * Build the maze
	 */
	private void generate() {
		// We are starting a 1,1.  The array starts at 0,0 but that is part of the border.
		int x = 1, y = 1;
		Point point = new Point(x,y);
		ArrayList<Integer> directionList = getArrayList();
		//Create the position object for 1,1 and add it to the Stack
		Position position = new Position(point,directionList);
		this._stkCells.add(position);

		//Increase the count of this cells visits;
		//While this cell has neighbors with Visit Count = 0;
		while (!this._stkCells.isEmpty() && isRunning){
			// Get the position off the top of the Stack.
			position = this._stkCells.pop();
			x = (int) position.point.getX();
			y = (int) position.point.getY();
			directionList = position.moves;
			// Mark this cell visited and increase it's visit count.
			_visited[x][y] = true;
			_visitCount[x][y]++;

			// Do this until the code inside the loop says to break.
			while(true && isRunning){
				//Check to see if this cell has any open moves.
				if(_visited[x][y+1] == false ||      // Look up 1
						_visited[x][y-1] == false || // Look down 1
						_visited[x-1][y] == false || // Look left 1
						_visited[x+1][y] == false )  // Look right 1					
				{
					// Get the next direction to check
					int i = directionList.get(0);
					//If the direction says to go North and the North cell has not been visited.
					if(i == NORTH && !_visited[x][y-1]){
						//Knock down North Wall of this cell
						_northWalls[x][y] = false;      
						//Knock down South Wall of North Neighbor cell
						_southWalls[x][y-1] = false;    
						//Remove move the direction from the list.  We have already been North so 
						//we won't need to try it again. 
						directionList.remove((Integer)i);
						//If this position has more directions that it can go, put it back on the stack.
						if (!directionList.isEmpty()){
							position.moves = directionList;
							this._stkCells.push(position);
						}
						//Push the cell to the North on the stack.  Next time through the loop.
						//This is where we will start.
						this._stkCells.push(new Position(new Point(x,y-1),this.getArrayList()));
						//Stop processing and jump out side of the loop While(true).
						break;
					}
					//If the direction says to go East and the East cell has not been visited.
					if(i == EAST && !_visited[x+1][y]){ 
						//Knock down East Wall of this cell
						_eastWalls[x][y] = false;       
						//Knock down West Wall of East Neighbor cell
						_westWalls[x+1][y] = false;     
						//Remove move the direction from the list.  We have already been East so 
						//we won't need to try it again. 
						directionList.remove((Integer)i);
						//If this position has more directions that it can go, put it back on the stack.
						if (!directionList.isEmpty()){
							position.moves = directionList;
							this._stkCells.push(position);
						}
						//Push the cell to the East on the stack.  Next time through the loop.
						//This is where we will start.
						this._stkCells.push(new Position(new Point(x+1,y),this.getArrayList()));
						//Stop processing and jump out side of the loop While(true).
						break;
					}
					//If the direction says to go South and the South cell has not been visited.
					if(i == SOUTH && !_visited[x][y+1]){
						//Knock down South Wall of this cell
						_southWalls[x][y] = false;      
						//Knock down North Wall of South Neighbor cell
						_northWalls[x][y+1] = false;    
						//Remove the direction from the list.  We have already been South so 
						//we won't need to try it again. 
						directionList.remove((Integer)i);
						//If this position has more directions that it can go, put it back on the stack.
						if (!directionList.isEmpty()){
							position.moves = directionList;
							this._stkCells.push(position);
						}
						//Push the cell to the South on the stack.  Next time through the loop.
						//This is where we will start.
						this._stkCells.push(new Position(new Point(x,y+1),this.getArrayList()));
						//Stop processing and jump out side of the loop While(true).
						break;
					}
					//If the direction says to go West and the West cell has not been visited.
					if(i == WEST && !_visited[x-1][y]){ 
						//Knock down West Wall of this cell
						_westWalls[x][y] = false;       
						//Knock down East Wall of West Neighbor cell
						_eastWalls[x-1][y] = false;     
						//Remove move the direction from the list.  We have already been South so 
						//we won't need to try it again. 
						directionList.remove((Integer)i);
						//If this position has more directions that it can go, put it back on the stack.
						if (!directionList.isEmpty()){
							position.moves = directionList;
							this._stkCells.push(position);
						}
						//Push the cell to the East on the stack.  Next time through the loop.
						//This is where we will start.
						this._stkCells.push(new Position(new Point(x-1,y),this.getArrayList()));
						//Stop processing and jump out side of the loop While(true).
						break;
					}
					//If we made it here, the direction specified has already been visited.  
					//Remove it from the stack
					directionList.remove((Integer)i);
				}else{
					//There are no open cells neighboring this one.
					break;
				}
				repaint();
				try{
					Thread.sleep(5);//1000/1000);
				}catch(InterruptedException ie){
					isRunning = false;
				}
			}// end if
		}// end While Stack
	}
	/**
	 * Initialize the maze.t
	 */
	private void initialize(){
		initializeBorder();
		initializeWalls();
		initializeVisitCount();
	}
	public void initializeVisitCount(){
		_visitCount = new int[_numColumns+2][_numRows+2];
		for(int x = 0; x < _numColumns+2;x++){
			for(int y = 0; y < _numRows+2;y++){
				_visitCount[x][y] = 0;
			}
		}		
	}
	/**
	 * Walls will be set for the maze and the extra rows and columns in the border.
	 */
	private void initializeWalls() {
		_northWalls = new boolean[_numColumns+2][_numRows+2];
		_southWalls = new boolean[_numColumns+2][_numRows+2];
		_eastWalls = new boolean[_numColumns+2][_numRows+2];
		_westWalls = new boolean[_numColumns+2][_numRows+2];
		for(int x = 0; x < _numColumns+2;x++){
			for(int y = 0; y < _numRows+2;y++){
				_northWalls[x][y] = true;
				_southWalls[x][y] = true;
				_eastWalls[x][y] = true;
				_westWalls[x][y] = true;

			}
		}

	}

	/**
	 * Create the maze larger than needed by 2 rows and 2 columns.
	 * 1 row extra on top and bottom.
	 * 1 column extra left and right.	 
	 */
	private void initializeBorder() {
		_visited = new boolean[_numColumns+2][_numRows+2];
		_visitCount = new int[_numColumns+2][_numRows+2];
		for(int x = 0; x < _numColumns + 2; x++){
			_visited[x][0] = true;
			_visited[x][_numRows+1] = true;
			_visitCount[x][0] = BORDER_VISITED;
			_visitCount[x][_numRows+1] = BORDER_VISITED;
		}
		for(int y = 0; y < _numRows+2; y++){
			_visited[0][y] = true;
			_visited[_numColumns+1][y] = true;
			_visitCount[0][y] = BORDER_VISITED;
			_visitCount[_numColumns+1][y] = BORDER_VISITED;
		}		
	}
	private Point getEndPoint(){
		Point goodEndPoint = null;
		int counter = 0;
		int offsetY = 0;
		int offsetX = 0;
		//A good end point should be surrounded by 3 walls.
		while(offsetY < _numRows || offsetX < _numColumns)
		{
			//Check the Right Edge first.		
			for (int y = _numRows-offsetY; y > 0;y--){
				counter = 0;
				counter += ( this._northWalls[_numColumns-offsetX][y] ) ? 1:0;
				counter += ( this._southWalls[_numColumns-offsetX][y] ) ? 1:0;
				counter += ( this._eastWalls[_numColumns-offsetX][y] ) ? 1:0;
				counter += ( this._westWalls[_numColumns-offsetX][y] ) ? 1:0;
				if (counter >= 3){
					goodEndPoint = new Point(_numColumns-offsetX,y);
					break;
				}
			}
			//Check the Bottom Edge
			if (goodEndPoint == null){
				for (int x = _numColumns-offsetX; x > 0;x--){
					counter = 0;
					counter += ( this._northWalls[x][_numRows-offsetY] ) ? 1:0;
					counter += ( this._southWalls[x][_numRows-offsetY] ) ? 1:0;
					counter += ( this._eastWalls[x][_numRows-offsetY] ) ? 1:0;
					counter += ( this._westWalls[x][_numRows-offsetY] ) ? 1:0;
					if (counter >= 3){
						goodEndPoint = new Point(x,_numRows-offsetY);
						break;
					}
				}		
			}
			if(goodEndPoint != null){
				break;
			}else{
				offsetX++;
				offsetY++;
			}
		}
		return goodEndPoint;
	}
	/**
	 * Build the Maze
	 */
	public void run(){
		if(_encodedMazeBytes == null){
			generate();
			_startCell = new Point(1,1);
			_endCell = getEndPoint();
		}else{
			this.build_MazeFromBytes();
		}
		repaint();
		this.notifyListeners();
	}
}
