package client.gui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.BoxLayout;

import main_pack.data.Traveler;
//import net.miginfocom.swing.MigLayout;
import javax.swing.JDesktopPane;

public class RMIWindow {

	private JFrame frameLogin;
	private JFrame frameRMI;
	private Traveler traveler;

	
	
	public Traveler getTraveler() {
		return traveler;
	}

	
	/**
	 * Create the application.
	 * @param frameLogin 
	 */
	/*public RMIWindow() {
		this.traveler = new Traveler();
		initialize();
	}*/

	public RMIWindow(JFrame frameLogin, Traveler t) 
	{
		this.frameLogin = frameLogin;
		this.traveler = t;
		initialize();
	}

	
	public JFrame getFrameLogin()
	{
		return this.frameLogin;
	}
	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		
		this.getFrameLogin().setVisible(false);
		
		frameRMI = new JFrame();
		frameRMI.setTitle("Chomp Client - Operations");
		frameRMI.setBounds(100, 100, 600, 500);
		frameRMI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frameRMI.getContentPane().setLayout(null);
		
		JButton btnPost = new JButton("POST");
		btnPost.setBounds(90, 55, 274, 35);
		frameRMI.getContentPane().add(btnPost);
		
		JButton btnGet = new JButton("GET");
		btnGet.setBounds(90, 92, 274, 35);
		frameRMI.getContentPane().add(btnGet);
		
		JButton btnRemove = new JButton("REMOVE");
		btnRemove.setBounds(90, 129, 274, 35);
		frameRMI.getContentPane().add(btnRemove);
		
		JButton btnExit = new JButton("Exit");
		btnExit.setBounds(305, 226, 70, 50);
		frameRMI.getContentPane().add(btnExit);
	}

	/**
	 * Launch the application.
	 */
	
//	public static void main(String[] args) {
//		EventQueue.invokeLater(new Runnable() {
//			public void run() {
//				try {
//					Traveler prova = null;
//					RMIWindow window = new RMIWindow(prova);
//					window.frameRMI.setVisible(true);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		});
//	}







}
