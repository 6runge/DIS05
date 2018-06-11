import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

public class RecoveryTool {
	private ArrayList<Integer> winners;
	private ArrayList<String[]> log;

	public void restart() {
		analyze();
		redo();
	}

	private void analyze() {
        //read log
		log = new ArrayList<String[]>();
		try (BufferedReader reader = new BufferedReader(new FileReader("log.txt"))){
			String line;
			while ((line = reader.readLine()) != null)
			{
				log.add(line.split(","));
			}
		} catch (Exception e){
			System.out.println("error while tying to read log.txt");
			e.printStackTrace();
		}
		//determine winners (all committed TAs, since we have no checkpoints)
		winners= new ArrayList<Integer>();
		for (String[] logEntry : log) {
			if (logEntry[logEntry.length - 1].equals("Commit")) {
				winners.add(Integer.parseInt(logEntry[1]));
			}
		}	
	}

	private void redo() {
		//find relevant pages and for each for each relevant page the LSN corresponding to its most recent modification
		Hashtable<Integer, Integer> relevantPages = new Hashtable<Integer, Integer>();
		for (String[] logEntry : log) {
			int taId = Integer.parseInt(logEntry[1]);
			if (winners.contains(taId) && logEntry[logEntry.length - 1].startsWith("Modification")) { //Does entry correspond to winner TA && is a modification?
				int pId = Integer.parseInt(logEntry[2]);
				if (relevantPages.containsKey(pId)) { //Is page corresponding to entry already listed as relevant?
					int lsn = Integer.parseInt(logEntry[0]);
					if (relevantPages.get(pId) < lsn) { //Is new lsn more recent than the one already listed?
						relevantPages.put(pId, lsn);
					}
				}
				else { //if page is relevant and not yet listed
					relevantPages.put(pId, Integer.parseInt(logEntry[0]));
				}
			}
		}
		//compare LSNs of relevant pages to LSNs of latest log entry corresponding to the respective page
		File[] allPages = new File("data").listFiles();
		for (File page : allPages) {
			int p = Integer.parseInt(page.getName());
			if (relevantPages.containsKey(p)){ //Is the page relevant?
				try (BufferedReader reader = new BufferedReader(new FileReader(page.getAbsolutePath()))){
					String[] pContent = reader.readLine().split(",");
					int pLsn = Integer.parseInt(pContent[1]),
						lLsn = relevantPages.get(p);
					if (pLsn < lLsn) { //Is the record stale?
						//redo modification from log
						String filename = "data" + File.separator + p;
						String mod = "";
						for (String[] logEntry : log) {
							if (logEntry[0].equals(pContent[0])) { //find log entry corresponding to latest modification to page
								mod = logEntry[3];
								break;
							}
						}
						String newRecord = p + "," + lLsn + "," + mod;
	                    writeRecord(filename, newRecord);
						relevantPages.remove(p);
					}
				} catch (Exception e) {
					System.out.println("redo failed due to a read error.");
					e.printStackTrace();
				}
			}
		}
		//now do the pages that had'nt been persisted
		for (int pId : relevantPages.keySet()) {
			String filename = "data" + File.separator + pId;
			String mod  = ""; 
			int lLsn = relevantPages.get(pId);
			for (String[] logEntry : log) {
				if (Integer.parseInt(logEntry[0]) == relevantPages.get(pId)) { //find log entry corresponding to latest modification to page
					mod = logEntry[3];
					System.out.println(mod);
					//break;
				}
				String newRecord = pId + "," + lLsn + "," + mod;
				writeRecord(filename, newRecord);						
			}
		}
	}
	
	private void writeRecord(String filename, String record) {
		try {
			FileWriter writer = new FileWriter(filename);
			writer.write(record);
			writer.close();
		} catch (IOException e) {
			System.out.println("redo failed due to a write error.");
			System.out.println("couldn't write " + record + " to " + filename);
			System.out.println(e.toString());
		} 	
	}
}
