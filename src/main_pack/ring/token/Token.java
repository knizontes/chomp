package main_pack.ring.token;

import java.io.Serializable;
import java.util.ArrayList;

import main_pack.data.messaging.Request;
import main_pack.ring.Chompeer;

/**
 * 
 * Oggetto serializzabile Token che viene trasmesso sul ping tra i vari {@link Chompeer}
 *
 */
public class Token implements Serializable{
	
	private ArrayList <TokenRecord> records= new ArrayList<TokenRecord>();
	private Boolean migratingChompeer=false;
	private Boolean migrationComplete=false;
	private ArrayList<TokenRequest> requests= new ArrayList<TokenRequest>();
	private long tokenId=0;
	private int migratingChompeerId=-1;
	
	public ArrayList<TokenRecord> getRecords(){
		return records;
	}
	
	public void increaseTokenId(){
		++tokenId;
	}
	
	public TokenRecord getTokenRecord(int index){
		return records.get(index);
	}
	
	public long getTokenId(){
		return tokenId;
	}
	
	public void printRecords(){
		System.out.println("\n***token ["+tokenId+"] records***");
		for (int i=0; i<records.size();++i)
			System.out.println("chompeer id:"+records.get(i).getChompeer_id()+" load factor:"+records.get(i).getLoadFactor());
		
		if (migratingChompeer)
			System.out.println("*** chompeer:"+ migratingChompeerId +" migrating ***");
		else
			System.out.println("*** no migration  ***");
		System.out.println();
	}
	
	/**
	 * Remove a TokenRecord from the token, used when whether a chompeer migrates or has a failure.
	 * @param index the index of the record to remove.
	 * @return -1 if the index is out of bound, 1 otherwise.
	 */
	
	public int removeTokenRecord(int index){
		if ((index<0) ||(index>records.size()))
				return -1;
		records.remove(index);
		return 1;
	}
	
	public int addTokenRecord(TokenRecord tr){
		records.add(tr);
		return records.size()-1;
	}
	
	
	public void resetMigratingChompeer(){
		migratingChompeer=false;
		migratingChompeerId=-1;
	}
	
	
	public Boolean migratingChompeer(){
		return migratingChompeer;
	}
	
	/**
	 * Returns true if a request is carried by the Token, false otherwise.
	 * @return true if a request is carried by the Token, false otherwise.
	 */
	public Boolean reguestPiggybacked(){
		if (requests==null)
			return false;
		return true;
	}
	
	public void addRequest(TokenRequest req){
		requests.add(req);
	}
	
	public ArrayList<TokenRequest> getRequests(){
		return requests;
	}
	
	public void resetRequest(int index){
		requests.remove(index);
	}

	public void setMigratingChompeer(int migratingChompeerId){
		this.migratingChompeerId=migratingChompeerId;
		this.migratingChompeer=true;
	}
	
	public int getMigratingChompeerId(){
		return migratingChompeerId;
	}
	

}
