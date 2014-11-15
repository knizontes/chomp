package main_pack.ring.data;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 
 * Utility Class per la manipolazione dell'ip
 *
 */
public class Ip {
	short []ip;
	
	public Ip(short ip3, short ip2, short ip1, short ip0){
		ip= new short [4];
		ip[0]=ip3;
		ip[1]=ip2;
		ip[2]=ip1;
		ip[3]=ip0;
	}
	
	public Ip(){
		ip= new short [4];
	}
	
	public Ip(String ip){
		this.ip= new short [4];
		ipFromString(ip);
	}
	
	public int ipFromString(String str){
		if (checkIp(str))
			return 1;
		for (int i=0; i<4; ++i)
			ip[i]=-1;
		return -1;
	}
	
	
	
	private Boolean checkIp(String str){
		int dots=0;
		int dotPos=-1;
		int dotNum=0;
		String temp;
		for (int i=0; i<str.length();++i){
			if ((!(Character.isDigit(str.charAt(i))))&&(str.charAt(i)!='.'))
				return false;
			if (str.charAt(i)=='.'){
				ip[dotNum]=Short.parseShort(str.substring(dotPos+1,i));
//				System.out.println(ip[ipPos]);
				++dotNum;
				dotPos=i;
			}
			if(dotNum>3)
				return false;
			
		}
		if (dotNum!=3)
			return false;
		ip[dotNum]=Short.parseShort(str.substring(dotPos+1));
		return true;
	}
	
	public String ipToString(){
		return ip[0]+"."+ip[1]+"."+ip[2]+"."+ip[3];
	}
	
	public InetAddress ipToInetAddress() throws UnknownHostException{
		return InetAddress.getByName(ipToString());
	}
	
	public static void main(String [] args){
		String str = new String("192.168.1.1");
		Ip ip = new Ip();
		ip.ipFromString(str);
		System.out.println(ip.ipToString());
	}
	
}
