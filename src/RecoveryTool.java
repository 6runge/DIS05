import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
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
		//find relevant pages and for each relevant page the LSN corresponding to its most recent modification
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
		//compare LSNs of logs to LSNs of persisted pages
		for (int pId : relevantPages.keySet()) {
			int lLsn = relevantPages.get(pId);
			int recLsn = getRecLsn(pId);
			//System.out.println("lLsn = " + lLsn + "; recLsn = " + recLsn);
			if (lLsn > recLsn) { //redo necessary?
				String filename = "data" + File.separator + pId;
				String mod  = ""; 

				for (String[] logEntry : log) {
					if (Integer.parseInt(logEntry[0]) == relevantPages.get(pId)) { //find log entry corresponding to latest modification to page
						mod = logEntry[3];
						break;
					}
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
		System.out.println("redo: " + record);
	}
	
	private int getRecLsn(int pageId) {
		String filename = "data" + File.separator + pageId;
		int reVa = -1;
		try (BufferedReader reader = new BufferedReader(new FileReader(filename))){
			String[] entry = reader.readLine().split(",");
			reVa = Integer.parseInt(entry[1]);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return reVa;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return reVa;
	}
}
