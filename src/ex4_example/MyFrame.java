package ex4_example;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileSystemView;

import Robot.Game;

//https://docs.oracle.com/javase/tutorial/uiswing/components/menu.html
public class MyFrame extends JFrame{

	
	public MyFrame() {

		initUI();
	}

	private void initUI() {

		GameBoard board = new GameBoard();
		add(board);

		JMenuBar menuBar;
		JMenu menu, submenu;
		JMenuItem menuItem;
		JRadioButtonMenuItem rbMenuItem;
		JCheckBoxMenuItem cbMenuItem;

		//Create the menu bar.
		menuBar = new JMenuBar();

		JButton btnOpen = new JButton("Open");
		btnOpen.setIcon(new ImageIcon("data/open.png"));
		menuBar.add(btnOpen);
		
		JButton btnSave = new JButton("Save");
		btnSave.setIcon(new ImageIcon("data/save.png"));
		menuBar.add(btnSave);
		
		JButton btnRun = new JButton("Run");
		btnRun.setIcon(new ImageIcon("data/run.png"));
		menuBar.add(btnRun);
		
		JButton btnPause = new JButton("Pause");
		btnPause.setIcon(new ImageIcon("data/pause.png"));
		menuBar.add(btnPause);

//		btnOpen.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent arg0) {
//				JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
//				int returnValue = jfc.showOpenDialog(null);
//	
//				if (returnValue == JFileChooser.APPROVE_OPTION) {
//					File selectedFile = jfc.getSelectedFile();
//					board.game = new Game();
//				}
//			}         
//	    });  
//		
//		btnSave.addActionListener(new ActionListener() {
//	        
//			@Override
//			public void actionPerformed(ActionEvent arg0) {
//				board.game.writeCsv();
//			}         
//		}); 
//		
//		btnRun.addActionListener(new ActionListener() {
//	        
//			@Override
//			public void actionPerformed(ActionEvent arg0) {
//				ShortestPathAlgo spa = new ShortestPathAlgo(board.game);
//				spa.findPaths();
//				board.running = true;
//			}         
//		});  
//		
//		btnPause.addActionListener(new ActionListener() {
//	        
//			@Override
//			public void actionPerformed(ActionEvent arg0) {
//				board.running = false;
//			}         
//		}); 
		
		setJMenuBar(menuBar);

		setResizable(false);
		pack();

		setTitle("Packman Game");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
	}    

	public static void main(String[] args) {

		EventQueue.invokeLater(() -> {
			JFrame ex = new MyFrame();
			ex.setVisible(true);
		});
	}

}
