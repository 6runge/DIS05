import java.util.concurrent.ThreadLocalRandom;

public class Client extends Thread{
	private int clientId;
	private PersistenceManager perMan;

	public Client(int clientId) {
		super();
		this.clientId = clientId;
		this.perMan = PersistenceManager.getInstance();
	}

	/**
	 * @return the clientId
	 */
	public int getClientId() {
		return clientId;
	}

    @Override
	public void run() { 
		while(true){
			try {
				doTransaction();
			} catch (InterruptedException e) {
				return;
			}
		}
	}

	//perform a transaction with up to 20 write operations
	private void doTransaction() throws InterruptedException{
		int numberOfWrites = ThreadLocalRandom.current().nextInt(10); 
		
		int taId =perMan.beginTransaction();
		Thread.sleep(ThreadLocalRandom.current().nextLong(2000));
		for (int i = 0; i < numberOfWrites; i++) {
			int pageId = ThreadLocalRandom.current().nextInt(10) + (this.clientId - 1) * 10; //each client works on a different set pages
			String data = "I am client number " + this.clientId + ". My favorite special number in the whole world has to be " 
			              + ThreadLocalRandom.current().nextInt() + "."; 
			perMan.write(taId, pageId, data);
			Thread.sleep(ThreadLocalRandom.current().nextLong(3000));
		}
		perMan.commit(taId);
	}
}
