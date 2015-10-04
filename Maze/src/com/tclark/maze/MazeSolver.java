package com.tclark.maze;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import javax.swing.JLayeredPane;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public  class MazeSolver extends JPanel implements Runnable{
	private Maze _maze;
	private int[][] _direction;


	/** 
	 * Value representing the North or Up direction
	 */
	private final static int NORTH = 0;
	/** 
	 * Value representing the East or Right direction
	 */
	private final static int EAST = 1;
	/** 
	 * Value representing the South or Down direction
	 */
	private final static int SOUTH = 2;
	/** 
	 * Value representing the West or Left direction
	 */
	private final static int WEST = 3;

	/**
	 * 
	 */
	private boolean isRunning;
	/**
	 * Task Listeners
	 */
	private java.util.List<TaskListener> listeners = Collections.synchronizedList( new ArrayList<TaskListener>() );

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

	public MazeSolver(Maze maze){
		_maze = maze;
		_direction = new int[_maze.get_numColumns()+2][_maze.get_numRows()+2];
	}
	private void solve(){
		this.reset();
	}
	public void reset(){
		_maze.initializeVisitCount();	
		this.initializeDirectionList();
	}
	private void initializeDirectionList(){
		_direction = new int[_maze.get_numColumns()+2][_maze.get_numRows()+2];
		for(int x = 1; x < _maze.get_numColumns()+1; x++){
			for(int y = 1; y < _maze.get_numRows()+1;y++){
				_maze.increment_visitCount(x,y);
				_direction[x][y] = 0;
				repaint();
			}
		}

	}
	public void run(){
		//solve();
		this.reset();
		testVisitCount();
		repaint();
		this.notifyListeners();
	}
	public void testVisitCount(){
		Random r = new Random();
		for(int x = 1; x < _maze.get_numColumns()+1; x++){
			for(int y = 1; y < _maze.get_numRows()+1;y++){
				_maze.increment_visitCount(x,y);
				_direction[x][y] = r.nextInt(3);
				repaint();
			}
		}
	}	
	@Override
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		for(int x = 1; x < _maze.get_numColumns()+1; x++){
			for(int y = 1; y < _maze.get_numRows()+1;y++){
				if(_maze.get_visitCount(x, y) == 1)
					g.setColor(Color.BLUE);
				if(_maze.get_visitCount(x,y) == 2)
					g.setColor(Color.MAGENTA);
				int row = (int) (_maze.get_borderY() + (y * _maze.get_cellHeight() ) + (1.5* _maze.get_cellHeight()));
				int col = _maze.get_borderX() + (x * _maze.get_cellWidth()) + _maze.get_cellWidth()/2;
				try{
					if(_direction[x][y] == 1 || _direction[x][y] == 3){
						if (x!=_maze.get_numColumns()){
							g.fillRect(col,	row,_maze.get_cellWidth(), _maze.get_cellHeight()/6);			
						}
					}
					if(_direction[x][y] == 2 || _direction[x][y] == 3){
						if(y!=_maze.get_numRows()){
							g.fillRect(col,row,_maze.get_cellWidth()/6, _maze.get_cellHeight());
						}
					}
				}catch (java.lang.ArrayIndexOutOfBoundsException ae){
					//ae.printStackTrace();

				}
			}
		}
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


}
