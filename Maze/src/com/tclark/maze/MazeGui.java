package com.tclark.maze;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
@SuppressWarnings("serial")
public class MazeGui extends JFrame implements ActionListener, TaskListener{
	private static final Dimension DEFAULT_WINDOW_SIZE = new Dimension(800,600);
	private static final Dimension MINIMUM_WINDOW_SIZE = new Dimension(400,300);
	
	private JLayeredPane _layeredPane = new JLayeredPane();
	
	private JMenuBar _mbMenu;
	private JMenu _mFile;
	private JMenu _mGame;
	private JMenu _mHelp;
	private JMenuItem _miFileOptions;
	private JMenuItem _miFileSave;
	private JMenuItem _miGameRun;
	private JMenuItem _miGameStop;
	private JMenuItem _miGameReset;
	private JMenuItem _miGameExit;
	private JMenuItem _miHelpAbout;
	
	private Integer _iRows = 20;
	private Integer _iColumns = 20;
	private String FILEPATH = "c:\\temp\\maze";
	private String FILENAME = "maze";
	private final String FILEEXT = "png";
	
	private Maze _Maze;
	MazeSolver _MazeSolver; 
	private Thread _thMaze;
	/**
	 * @param args
	 */
	public static void main(String[] args){
		JFrame maze = new MazeGui();
		maze.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		maze.setTitle("Maze Generator and Solver");
		maze.setSize(DEFAULT_WINDOW_SIZE);
		maze.setMinimumSize(MINIMUM_WINDOW_SIZE);
		maze.setLocation((Toolkit.getDefaultToolkit().getScreenSize().width-maze.getWidth())/2,
				         (Toolkit.getDefaultToolkit().getScreenSize().height-maze.getHeight())/2);
		maze.setVisible(true);
		
	}
	public MazeGui(){
		_mbMenu = new JMenuBar();
		setJMenuBar(_mbMenu);
		_mFile = new JMenu("File");
		_mbMenu.add(_mFile);
		_mGame = new JMenu("Game");
		_mbMenu.add(_mGame);
		/*_mGame.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent ae){
				if(_Maze.isRunning()){
					_miGameRun.setEnabled(false);
					_miGameStop.setEnabled(true);
				}else{
					_miGameRun.setEnabled(true);
					_miGameStop.setEnabled(false);
				}
			}
		});*/
		_mHelp = new JMenu("Help");
		_mbMenu.add(_mHelp);
		_miFileSave = new JMenuItem("Save Image");
		_miFileSave.addActionListener(this);
		_mFile.add(_miFileSave);
		_miFileOptions = new JMenuItem("Options");
		_miFileOptions.addActionListener(this);
		_mFile.add(_miFileOptions);
		_miGameRun = new JMenuItem("Run");
		_miGameRun.addActionListener(this);
		_miGameStop = new JMenuItem("Stop");
		_miGameStop.addActionListener(this);
		_miGameReset = new JMenuItem("Reset");
		_miGameReset.addActionListener(this);
		_miGameExit = new JMenuItem("Exit");
		_miGameExit.addActionListener(this);
		_mGame.add(_miGameRun);
		_mGame.add(_miGameStop);
		_mGame.add(new JSeparator());
		_mGame.add(_miGameReset);
		_mGame.add(new JSeparator());
		_mGame.add(_miGameExit);
		_miHelpAbout = new JMenuItem("About");
		_miHelpAbout.addActionListener(this);
		_mHelp.add(_miHelpAbout);
		
		_Maze = new Maze();
		_MazeSolver = new MazeSolver(_Maze);
		add(_Maze);
		
	}
	public void setMazeRunning(boolean isRunning){
		if(isRunning){
			_Maze.resetMaze(this._iColumns, this._iRows);
			_MazeSolver.reset();
			_miGameRun.setEnabled(false);
			_miGameStop.setEnabled(true);
			_Maze.setRunning(true);
			_Maze.addListener(this);
			_thMaze = new Thread(_Maze);
			_thMaze.start();
			
		}else{
			_miGameRun.setEnabled(true);
			_miGameStop.setEnabled(false);
			_Maze.setRunning(false);
			_Maze.removeListener(this);
			_thMaze.interrupt();
		}
	}
	public void actionPerformed(ActionEvent ae){
		JLabel lblTemp;
		if (ae.getSource().equals(this._miGameExit)){
			System.exit(0);
		}else if (ae.getSource().equals(this._miFileOptions)){
			final JFrame frmOptions = new JFrame();
			frmOptions.setTitle("Options");
			frmOptions.setSize(300,160);
			frmOptions.setLocation((Toolkit.getDefaultToolkit().getScreenSize().width - frmOptions.getWidth())/2,
					               (Toolkit.getDefaultToolkit().getScreenSize().height - frmOptions.getHeight())/2);
			frmOptions.setResizable(false);
			JPanel pnlOptions = new JPanel();
			pnlOptions.setLayout(new GridLayout(4,2));
			pnlOptions.setOpaque(false);
			frmOptions.add(pnlOptions);
			lblTemp = new JLabel("Number of Columns:");
			pnlOptions.add(lblTemp);
			
			Integer[] columnOptions = {5,10,20,30,40,50,60,70,80,90,100};
			final JComboBox<Integer> cbColumns = new JComboBox<Integer>(columnOptions);
			pnlOptions.add(cbColumns);
			cbColumns.setSelectedItem(this._iColumns);
			cbColumns.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent ae){
					_iColumns = (Integer)cbColumns.getSelectedItem();
				}
			});			
			pnlOptions.add(new JLabel("Number of Rows:"));
			Integer[] rowOptions = {5,10,20,30,40,50,60,70,80,90,100};
			final JComboBox<Integer> cbRows = new JComboBox<Integer>(rowOptions);
			pnlOptions.add(cbRows);
			cbRows.setSelectedItem(this._iRows);
			cbRows.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent ae){
					_iRows = (Integer)cbRows.getSelectedItem();
				}
			});
			
			pnlOptions.add(new JLabel("Output Folder for images:"));
			//JTextField txtField = new JTextField();
			JTextField txtField = new JTextField();
			txtField.setText(FILEPATH);
			txtField.setPreferredSize(new Dimension(115,20));
			JPanel pnlFileChooser = new JPanel(new FlowLayout());
			pnlFileChooser.add(txtField);
			//pnlOptions.add(txtField);
			JButton btnFileChooser = new JButton("...");
			btnFileChooser.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent ae){
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY);
					fileChooser.setCurrentDirectory(new File(FILEPATH));
					fileChooser.setVisible(true);
					fileChooser.showDialog(null, "Select Folder");
					FILEPATH = fileChooser.getSelectedFile().getPath();
					txtField.setText(FILEPATH);
				}
			});			
			pnlFileChooser.add(btnFileChooser);
			btnFileChooser.setPreferredSize(new Dimension(19,19));
			pnlOptions.add(pnlFileChooser);
			pnlOptions.add(new JLabel());
			JButton btnOk = new JButton();
			btnOk.setText("Okay");
			btnOk.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent ae){
					frmOptions.dispose();
				}
			});
			pnlOptions.add(btnOk);
			frmOptions.setVisible(true);
		}else if(ae.getSource().equals(this._miGameRun)){
			setMazeRunning(true);
		}else if(ae.getSource().equals(this._miGameReset)){
			this._Maze.resetMaze(this._iColumns, this._iRows);
		}else if(ae.getSource().equals(this._miGameStop)){
			setMazeRunning(false);
		}else if(ae.getSource().equals(this._miFileSave)){
			BufferedImage bi = new BufferedImage(_Maze.getWidth(), _Maze.getHeight(),
					                             BufferedImage.TYPE_INT_RGB);
			Graphics2D g2d = bi.createGraphics();
			g2d.setBackground(Color.WHITE);
			_Maze.paint(g2d);
			try {
				int lcv = 1;
				File out = new File(String.format("%s\\%s_%d.%s", new Object[]{this.FILEPATH, this.FILENAME,lcv,this.FILEEXT}));
				while(out.exists()){
					lcv++;
				    out = new File(String.format("%s\\%s_%d.%s", new Object[]{this.FILEPATH, this.FILENAME,lcv,this.FILEEXT}));
				}
				ImageIO.write(bi, "png",out);
				
				out = new File(String.format("%s\\%s_%d.%s", new Object[]{this.FILEPATH, this.FILENAME,lcv,"txt"}));
				Files.write(out.toPath(), _Maze.get_MazeString().getBytes(),StandardOpenOption.CREATE);
			} catch (IOException e) {
				
				e.printStackTrace();
			}
			
		};
	}
	@Override
	public void threadComplete(Runnable runner) {
		_miGameRun.setEnabled(true);
		_miGameStop.setEnabled(false);
		_Maze.setRunning(false);
		//add(_Maze);
		_MazeSolver = new MazeSolver(_Maze);
		_layeredPane = this.getLayeredPane();
		_layeredPane.setPreferredSize(MazeGui.DEFAULT_WINDOW_SIZE);
		_layeredPane.setOpaque(false);
		_layeredPane.add(_MazeSolver,new Integer(1));
		_layeredPane.setVisible(true);
		_MazeSolver.setOpaque(false);
		_MazeSolver.setVisible(true);
		_MazeSolver.setBounds(0,0,(int)this.getWidth(),(int)this.getHeight());
		Thread t = new Thread(_MazeSolver);
		t.start();
		
	}
}
