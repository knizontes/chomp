package client;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import main_pack.data.Traveler;


public class testServerino {

	private Socket socket;
	private int PORT = 20000;
	
	public testServerino()
	{
		try {
			socket = new Socket("localhost", PORT);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public void sendTraveler(Traveler[] array)
	{
		try 
		{
			//collego lo stream in uscita alla socket
			OutputStream os = this.socket.getOutputStream();

			ObjectOutputStream oos = new ObjectOutputStream(os);

			oos.writeObject(array);

			oos.close();
			os.close();
			socket.close();
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}
	
	
    public static void main(String[] args) 
    {
		Traveler t1 = new Traveler("aaaaaaa", "mkljojolj", "asdsadasdas");
		Traveler t2 = new Traveler("bbbbbbb", "dasflj", "dfjhgu");
	    Traveler[] _array = new Traveler[2];
	    _array[0] = t1;
	    _array[1] = t2;
	    
		
		testServerino ts = new testServerino();
		ts.sendTraveler(_array);
		
		testServerino ts2 = new testServerino();
		ts2.sendTraveler(_array);
		
		testServerino ts3 = new testServerino();
		ts3.sendTraveler(_array);
		
	}
}
