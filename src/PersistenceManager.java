import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class PersistenceManager {

	static final private PersistenceManager instance;
	
	private Hashtable<Integer, Page> buffer = new Hashtable<Integer, Page>();
	private int taCounter  = 0, //used for Transaction IDs
			    logCounter = 0; //used for log IDs
	private  Hashtable<Integer, LinkedList<Integer>> activeTas    = new Hashtable<Integer, LinkedList<Integer>>(),
	                                                 committedTas = new Hashtable<Integer, LinkedList<Integer>>();
	
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
	public synchronized int beginTransaction(){
		taCounter++;
		activeTas.put(taCounter, new LinkedList<Integer>());
		String logEntry = logCounter + "," + taCounter + ",BOT";
		log(logEntry);
		return taCounter;
	}

	/**
	 * Commit a Transaction.
	 * @param taId the ID of the transaction to be committed
	 */
	public synchronized void commit(int taId){
		if (activeTas.containsKey(taId)) {
			LinkedList<Integer> pageIds = activeTas.remove(taId);
			committedTas.put(taId, pageIds);
            String logEntry = logCounter + "," + taId + ",Commit";
			log(logEntry);
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
	public synchronized void write(int taId, int pageId, String data) {
		//check if taId is valid and add pageId to the list of pages corresponding to taId
		if (activeTas.containsKey(taId)) {
			List<Integer> pageIds = activeTas.get(taId);
			if (!pageIds.contains(pageId)) {
				pageIds.add(pageId);
			}
		}
		//add page to buffer
		buffer.put(pageId, new Page(taId, pageId, logCounter, data));
		//log
		String logEntry = logCounter + "," + taId + "," + pageId + "," + "Modification: " + data;
		log(logEntry);
		//check if buffer is full and write to persistent storage if necessary
		if (buffer.size() > 5) {
			Object[] pIds = buffer.keySet().toArray();
            for (Object pId : pIds) {
                if (correspondsToCommittedTa((int) pId)) {            	
            		writeToPersistentStorage(buffer.remove(pId));
                }

            }
		}
	}

	private boolean correspondsToCommittedTa(int pId) {
		for (int taId : committedTas.keySet()) {
			if (committedTas.get(taId).contains(pId)) {
				return true;
			}
		}
		return false;
	}

	private void writeToPersistentStorage(Page d) {
		String fileName = "data" + File.separator + d.getPageId() + ".txt";
		try {
			FileWriter writer = new FileWriter(fileName);
			writer.write(d.toString());
			writer.close();
		} catch (IOException e) {
			System.out.println("Persisting data failed due to a write error.");
			System.out.println(e.toString());
		} 
		
	}

	private void log(String data) {
		logCounter++;
		try (FileWriter writer = new FileWriter("log.txt", true)){
			writer.write(data + System.lineSeparator());
		} catch (IOException e) {
			System.out.println("Logging failed due to a write error.");
			System.out.println(e.toString());
		} 
	}
}