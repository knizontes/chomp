package client;

import java.util.ArrayList;

import main_pack.data.IndexTableName;
import main_pack.data.Traveler;

/**
 * 
 * Classe che compone l'associazione tra un {@link Traveler} e la lista (ArrayList) di {@link IndexTableName} che lo coinvolgono.
 * Usata in fase di popolazione del sistema per scopi di testing.
 *
 */
public class InfoTrav {

	public Traveler t;
	public ArrayList<IndexTableName> alitn;
	
	public InfoTrav(){}
	
	public InfoTrav(Traveler t, ArrayList<IndexTableName> alitn)
	{
		this.t = t;
		this.alitn = alitn;
	}

	public Traveler getT() {
		return t;
	}

	public void setT(Traveler t) {
		this.t = t;
	}

	public ArrayList<IndexTableName> getAlitn() {
		return alitn;
	}

	public void setAlitn(ArrayList<IndexTableName> alitn) {
		this.alitn = alitn;
	}
	
	
	
}
