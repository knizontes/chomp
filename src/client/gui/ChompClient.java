package client.gui;

import java.awt.Color;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import java.awt.BorderLayout;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import java.awt.Button;
import java.awt.Font;
import javax.swing.SwingConstants;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.rmi.RemoteException;
import javax.swing.JPopupMenu;
import java.awt.Component;
import java.awt.TextField;
import java.awt.Dimension;
import javax.swing.JDesktopPane;

import client.ClientEngine;

public class ChompClient {

	private JFrame frame;
    private ClientEngine ce;
    
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ChompClient window = new ChompClient();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public ChompClient() {
		
		
		try {
			//cambiare a localhost a file di configuarazione
			ce = new ClientEngine("localhost",true);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		initialize();
	    
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		
		String title = "Chomp Client";
		
		frame = new JFrame();
		frame.setName(title);
		frame.setTitle(title);
		frame.setDefaultLookAndFeelDecorated(true);
				
		JLabel labelTitle = new JLabel("Chomp Client");
		labelTitle.setForeground(Color.BLUE);
		labelTitle.setBackground(Color.LIGHT_GRAY);
		labelTitle.setVerticalAlignment(SwingConstants.BOTTOM);
		labelTitle.setSize(247, 50);
		labelTitle.setLocation(206, 25);
		labelTitle.setFont(new Font("Bitstream Charter", Font.BOLD | Font.ITALIC, 25));
		
		frame.getContentPane().add(labelTitle);
		
		frame.setBounds(100, 100, 592, 410);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		/*  LOGIN  */
		Button buttonLogin = new Button("Log In");
		buttonLogin.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) 
			{
			    new LoginWindow(frame);
			    frame.setVisible(false);
			}
		});
		buttonLogin.setFont(new Font("Bitstream Charter", Font.BOLD | Font.ITALIC, 25));
		buttonLogin.setForeground(new Color(119, 136, 153));
		buttonLogin.setBounds(80, 116, 440, 60);
		frame.getContentPane().add(buttonLogin);
		
		
		
		
		/*  EXIT  */
		Button buttonExit = new Button("Exit");
		buttonExit.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
			  System.exit(0);
			}		
		});
		buttonExit.setFont(new Font("Bitstream Charter", Font.BOLD | Font.ITALIC, 25));
		buttonExit.setBounds(80, 203, 440, 60);
		buttonExit.setForeground(new Color(255, 0, 0));
		frame.getContentPane().add(buttonExit);
		
	}
	
}
