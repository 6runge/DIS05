import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

public class PersistenceManager {

	static final private PersistenceManager instance;
	
	private Hashtable<Integer, UserData> buffer = new Hashtable<Integer, UserData>();
	private int taCounter  = 0, //used for Transaction IDs
			    logCounter = 0; //used for log IDs
	private LinkedList<Integer> activeTas    = new LinkedList<Integer>(),
	                            committedTas = new LinkedList<Integer>();
	
	static {
		try {
			instance = new PersistenceManager();
		}
		catch (Throwable e) {
			throw new RuntimeException(e.getMessage());
		}
	}
	//private constructor defeats instantiation
	private PersistenceManager() {
	}
	
	/**
	 * @return the singleton instance of PersistenceManager
	 */
	
	static public PersistenceManager getInstance() {
		return instance;
	}
	
	/**
	 * Begin a new transaction. ID is valid until commit.
	 * @return the ID of the transaction
	 */
	public int beginTransaction(){
		taCounter++;
		activeTas.add(taCounter);
		return taCounter;
	}
	
	/**
	 * Commit a Transaction.
	 * @param taId the ID of the transaction to be committed
	 */
	public void commit(int taId){
		if (activeTas.contains(taId)) {
			activeTas.remove(taId);
			committedTas.add(taId);
		}
		else {
			System.out.println("invalid Transaction ID " + taId);
		}
	}
	
	/**
	 * Write data to a page.
	 * @param taId
	 * @param pageId
	 * @param data
	 */
	public void write(int taId, int pageId, String data) {
		buffer.put(pageId, new UserData(taId, pageId, logCounter, data));
		log(taId, pageId, data);
		
		if (buffer.size() > 5) {
			Iterator<Map.Entry<Integer, UserData>> it = buffer.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<Integer, UserData> entry = it.next();
				UserData d = entry.getValue();
				if (committedTas.contains(d.getTaId())){ //has entry been committed?
					writeToPersistentStorage(d);
					it.remove();
				}
			}
		}
	}

	private void writeToPersistentStorage(UserData d) {
		String fileName = "data" + File.separator + String.valueOf(d.getPageId());
		try {
			FileWriter writer = new FileWriter(fileName);
			writer.write(d.toString());
			writer.close();
		} catch (IOException e) {
			System.out.println("Persisting data failed due to a write error.");
			System.out.println(e.toString());
		} 
		
	}

	private void log(int taId, int pageId, String data) {
		String fileName = "logs" + File.separator + String.valueOf(logCounter);
		String logData = String.valueOf(logCounter) + "," + String.valueOf(taId) + "," +
				String.valueOf(pageId) + "," + data;
		logCounter++;
		try {
			FileWriter writer = new FileWriter(fileName);
			writer.write(logData);
			writer.close();
		} catch (IOException e) {
			System.out.println("Logging failed due to a write error.");
			System.out.println(e.toString());
		} 
	}
}