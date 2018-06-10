public class Main { 
	
	public static void main (String[] args) {
		Client[] clients = {new Client(1), new Client(2), new Client(3), new Client(4), new Client(5)};
		
		for (Client c : clients) { //clients start doing their thing
			c.start();
		}
		
		try {
			Thread.sleep(10000);
			} catch (InterruptedException e) {} //wait for a while

		for (Client c : clients) { //everybody stops what they're doing
			c.interrupt();
		}
		
	}

}
