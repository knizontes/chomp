package main_pack.metrics;

import main_pack.data.IndexTableManager;
import main_pack.switchRMI.ServerServiceRMI;
import main_pack.utils.Timestamp;

/**
 * 
 * Raccoglitore di metriche basato sul carico del peer in termini di Index Table di cui lo stesso e' responsabile.
 * Il comportamento da switch del peer determina un incremento di tale valore di carico (loadFactor) ad indicare 
 * la criticita' di questo componente rispetto ad un normale funzionamento come generico peer competente per un 
 * insieme di risorse.
 * 
 */
public class MetricsManager implements Runnable{

	
	private IndexTableManager itm;
	private ServerServiceRMI ssr;
	private static final String METRICSMANTAG = "[METRICS MANAGER]";
	private final static int INDEX_TABLE_WEIGTH=1;
	private final static int SWITCH_WEIGHT_RATIO=5;
	private Boolean verbose;
	private long loadFactor=0;
	
	public MetricsManager (IndexTableManager itm,Boolean verbose){
		this.itm=itm;
		this.verbose=verbose;
	}
	
	public void setSwitch(ServerServiceRMI ssr){
		this.ssr=ssr;
	}
	
	
	
	/**
	 * The routine of the {@link MetricsManager}, computes the loadIndex which represent 
	 * the work load insisting on the ChompServer
	 */
	public void run() {
		println("Metrics manager started");
		for(;;){
			try {
				Thread.sleep(1000);
				loadFactor = itm.getIndexTablesCardinality()*INDEX_TABLE_WEIGTH+ switchWeight();
//				itm.printAllTablesNum();
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private int switchWeight() {
		return (ssr.isDnsAddressed()*itm.getIndexTablesCardinality()/SWITCH_WEIGHT_RATIO);
	}

	public long getLoadFactor(){
		return loadFactor;
	}
	
	private String tag(){
		return (Timestamp.now()+"-"+METRICSMANTAG);
	}
	
	private void println(String s){
		if (verbose)
			System.out.println(tag()+s);
	}

	public void resetTables() {
		itm.resetTables();
		loadFactor=0;
	}

	
	
	
}
