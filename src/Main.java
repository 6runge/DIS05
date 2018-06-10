import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main { 
	
	public static void main (String[] args) {
		Client[] clients = {new Client(1), new Client(2), new Client(3), new Client(4), new Client(5)};
		try {
			Files.deleteIfExists(Paths.get("log.txt"));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		for (Client c : clients) { //clients start doing their thing
			c.start();
		}
		
		for (int i = 10; i > 0; i--)
		try {
			System.out.println(i);
			Thread.sleep(1000);
			} catch (InterruptedException e) {} //wait for a while

		for (Client c : clients) { //everybody stops what they're doing
			c.interrupt();
		}
		
	}

}
