import java.util.Hashtable;

public class PersistenceManager {

	static final private PersistenceManager instance;
	
	private Hashtable buffer;
	
	static {
		try {
			instance = new PersistenceManager();
		}
		catch (Throwable e) {
			throw new RuntimeException(e.getMessage());
		}
	}
	
	private PersistenceManager() {
		buffer = new Hashtable();
	}
	
	static public PersistenceManager getInstance() {
		return instance;
	}
}