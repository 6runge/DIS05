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
			doTransaction();
		}
	}

	//perform a transaction with up to 20 write operations
	private void doTransaction() {
		int numberOfWrites = ThreadLocalRandom.current().nextInt(20); 
		
		int taId =perMan.beginTransaction();
		idle();
		for (int i = 0; i < numberOfWrites; i++) {
			int pageId = ThreadLocalRandom.current().nextInt(10) + (this.clientId - 1) * 10; //each client works on a different set pages
			String data = "I am client number " + this.clientId + ". My favorite special number in the whole world has to be " 
			              + ThreadLocalRandom.current().nextInt() + "."; 
			perMan.write(taId, pageId, data);
			idle();
		}
		perMan.commit(taId);
	}

	//wait for up to a few seconds
	private void idle() {
		try{
			Thread.sleep(ThreadLocalRandom.current().nextLong(3000));
		}catch(InterruptedException e){
			return;
		}		
	}

}
