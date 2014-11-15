package main_pack.ring.common;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;

/**
 * 
 * Utility class per la gestione delle informazioni scambiate sull'anello tramite datagrammi UDP.
 *
 */
public class RingCommon {
	
	public static RingPacket getPingPacket(DatagramPacket packet) throws IOException, ClassNotFoundException{
		RingPacket pp;
		byte [] receivedData= packet.getData();
		byte [] data;
		int dim=0;
		// getting the four 4 bytes of payload corresponding to the field data_size 
		for (int i=0; i<4;++i)
			dim+=((int)((int)receivedData[i]& 0xff)<<((3-i)*8));
//		System.out.println("------->declared data dimension:"+dim+ " real dimension:"+receivedData.length);

		
		data = new byte[dim];
		for (int i=0; i<dim;++i)
			data[i]=receivedData[i+4];
		
		
		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		ObjectInputStream ois = new ObjectInputStream(bais);
		pp = (RingPacket) ois.readObject();
		return pp;
	}
	
	
	/*If the src ip is the same as declared in the packet checkIP returns true */
	public static Boolean checkIp(DatagramPacket packet, RingPacket pp){
		InetAddress ip_address = packet.getAddress();
		String ip=ip_address.getHostAddress();
		return (ip.equals(pp.getSrc()));		
	}
	
	/*Set the buffer to 0*/
	public static void clearBuffer(byte [] buffer){
		for(int i=0; i<buffer.length;++i)
			buffer[i]=0;
	}
	
	/*return a byte array which contains the dimension of the payload and the payload bytestream*/
	public static byte[] dataFormatting(byte [] data){
		byte [] retval = new byte [data.length+4];
		int dim = data.length;
		for (int i = 0; i < 4; i++) {
			int offset = (3 - i) * 8;
			retval[i] = (byte) ((dim >>> offset) & 0xFF);
		}
		for (int i=4; i<retval.length;++i){
			retval[i]=data[i-4];
		}
		return retval;
	}
	
	/*returns an array of 4 bytes which holds the value of the parameter int*/
	private byte[] intToByteArray(int value) {
		byte[] b = new byte[4];
		for (int i = 0; i < 4; i++) {
			int offset = (b.length - 1 - i) * 8;
			b[i] = (byte) ((value >>> offset) & 0xFF);
		}
		return b;
	}
}
