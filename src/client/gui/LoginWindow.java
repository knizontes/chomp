package client.gui;

import java.awt.Button;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JButton;

import main_pack.data.Traveler;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class LoginWindow {

	private JFrame mainFrame;
	private JFrame frameLogin;
	private JTextField textNome;
	private JTextField textCognome;
	private JTextField textCf;
	private JTextField textInfo;
	private JTextField textPasswd;

	private Traveler traveler;
	/**
	 * Launch the application.
	 */
//	public static void main(String[] args) {
//		EventQueue.invokeLater(new Runnable() {
//			public void run() {
//				try {
//					LoginWindow window = new LoginWindow();
//					window.frameLogin.setVisible(true);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		});
//	}

	/**
	 * Create the application.
	 */
	public LoginWindow(JFrame main) {
		this.mainFrame = main;
		this.traveler = new Traveler();
		initialize();
	}

	public JFrame getMainFrame() {
		return mainFrame;
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frameLogin = new JFrame();
		frameLogin.setBounds(100, 100, 450, 300);
		frameLogin.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    
	    frameLogin.setVisible(true); 
	    frameLogin.setTitle("Chomp Client - Log In");
	    
	    frameLogin.getContentPane().setLayout(null);
	    

	    
	    Button invia = new Button("Invia");
	    invia.addMouseListener(new MouseAdapter() {
	    	@Override
	    	public void mouseClicked(MouseEvent e) {
	    		traveler.setNome(textNome.getText());
	    		traveler.setCognome(textCognome.getText());
	    		traveler.setCF(textCf.getText());
	    		traveler.setInfo(textInfo.getText());
	    		traveler.setPwdHash(textPasswd.getText());
	    		RMIWindow rmiw = new RMIWindow(frameLogin, traveler);
	    		frameLogin.setVisible(false);
	    		rmiw.getFrameLogin().setVisible(true);
	    	}
	    });
	    invia.setBounds(291, 300, 70, 50);
	    
	    Button exitPannel = new Button("exit");  
	    exitPannel.setBounds(354, 300, 70, 50);
	    
	    frameLogin.getContentPane().add(invia);
	    frameLogin.getContentPane().add(exitPannel);
	    
	    textNome = new JTextField();
	    textNome.setBounds(229, 52, 114, 19);
	    frameLogin.getContentPane().add(textNome);
	    textNome.setColumns(10);
	    
	    textCognome = new JTextField();
	    textCognome.setBounds(229, 98, 114, 19);
	    frameLogin.getContentPane().add(textCognome);
	    textCognome.setColumns(10);
	    
	    textCf = new JTextField();
	    textCf.setBounds(229, 147, 114, 19);
	    frameLogin.getContentPane().add(textCf);
	    textCf.setColumns(10);
	    
	    textInfo = new JTextField();
	    textInfo.setBounds(229, 200, 114, 19);
	    frameLogin.getContentPane().add(textInfo);
	    textInfo.setColumns(10);
	    
	    textPasswd = new JTextField();
	    textPasswd.setBounds(229, 250, 114, 19);
	    frameLogin.getContentPane().add(textPasswd);
	    textPasswd.setColumns(10);
	    
	    JLabel lblNome = new JLabel("Nome");
	    lblNome.setBounds(63, 54, 70, 15);
	    frameLogin.getContentPane().add(lblNome);
	    
	    JLabel lblCognome = new JLabel("Cognome");
	    lblCognome.setBounds(63, 100, 70, 15);
	    frameLogin.getContentPane().add(lblCognome);
	    
	    JLabel lblCodiceFiscale = new JLabel("Codice fiscale");
	    lblCodiceFiscale.setBounds(66, 149, 133, 15);
	    frameLogin.getContentPane().add(lblCodiceFiscale);
	    
	    JLabel lblInfoViaggiatore = new JLabel("Info viaggiatore");
	    lblInfoViaggiatore.setBounds(66, 200, 133, 15);
	    frameLogin.getContentPane().add(lblInfoViaggiatore);
	    
	    JLabel lblPassword = new JLabel("Password");
	    lblPassword.setBounds(66, 250, 133, 15);
	    frameLogin.getContentPane().add(lblPassword);
	    
//	    JButton btnNewButton = new JButton("temp button");
//	    btnNewButton.setBounds(126, 226, 117, 25);
//	    frameLogin.getContentPane().add(btnNewButton);

	}
}
