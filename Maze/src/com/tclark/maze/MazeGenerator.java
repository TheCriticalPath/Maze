package com.tclark.maze;
import java.util.Collections;
import java.util.Arrays;
 
/*
 * recursive backtracking algorithm
 */
public class MazeGenerator {
	private final int x;
	private final int y;
	private  int[][] maze;
	private  int sx, sy;
	private int ex;
	private int ey;
	
	public MazeGenerator(int x, int y) {
		this.x = x;
		this.y = y;
		maze = new int[this.x][this.y];
		sx = 0; sy=0;
		ex = -1; ey = -1;
		generateMaze(0, 0);
	}
 
// Replace this method with a graphical view. 
	public void display() {
		for (int i = 0; i < y; i++) {
			// draw the north edge
			for (int j = 0; j < x; j++) {
					System.out.print((maze[j][i] & 1) == 0 ? "+---" : "+   ");
			}
			System.out.println("+");
			// draw the west edge
			for (int j = 0; j < x; j++) {
				if (i == sy && j == sx )
					System.out.print((maze[j][i] & 8) == 0 ? "| S " : "  S ");
				else if (i == ey && j == ex )
					System.out.print((maze[j][i] & 8) == 0 ? "| E " : "  E ");
				else
					System.out.print((maze[j][i] & 8) == 0 ? "|   " : "    ");
			}
			System.out.println("|");
		}
		// draw the bottom line
		for (int j = 0; j < x; j++) {
			System.out.print("+---");
		}
		System.out.println("+");
	}
 
	private void generateMaze(int cx, int cy) {
		DIR[] dirs = DIR.values();
		Collections.shuffle(Arrays.asList(dirs));
		for (DIR dir : dirs) {
			int nx = cx + dir.dx;
			int ny = cy + dir.dy;
			if (between(nx, x) && between(ny, y)
					&& (maze[nx][ny] == 0)) {
				maze[cx][cy] |= dir.bit;
				maze[nx][ny] |= dir.opposite.bit;
				generateMaze(nx, ny);
			}
		}
		if (ex == -1 && ey == -1){
			ex = cx;
			ey = cy;
		}
	}
 
	private static boolean between(int v, int upper) {
		return (v >= 0) && (v < upper);
	}
 
	private enum DIR {
		N(1, 0, -1), S(2, 0, 1), E(4, 1, 0), W(8, -1, 0);
		private final int bit;
		private final int dx;
		private final int dy;
		private DIR opposite;
 
		// use the static initializer to resolve forward references
		static {
			N.opposite = S;
			S.opposite = N;
			E.opposite = W;
			W.opposite = E;
		}
 
		private DIR(int bit, int dx, int dy) {
			this.bit = bit;
			this.dx = dx;
			this.dy = dy;
		}
	};
 
	public static void main(String[] args) {
		int x = args.length >= 1 ? (Integer.parseInt(args[0])) : 16;
		int y = args.length == 2 ? (Integer.parseInt(args[1])) : 16;
		MazeGenerator maze = new MazeGenerator(x, y);
		maze.display();
	}
 
}