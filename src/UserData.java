
public class UserData {
	private int taId, pageId, lsn;
	private String data;
	
	public UserData(int taId, int pageId, int lsn, String data) {
		super();
		this.taId = taId;
		this.pageId = pageId;
		this.lsn = lsn;
		this.data = data;
	}

	/**
	 * @return the Transaction ID
	 */
	public int getTaId() {
		return taId;
	}
	
	/**
	 * @return the Page ID
	 */
	public int getPageId() {
		return pageId;
	}

	@Override
	public String toString() {
		return String.valueOf(pageId) + "," + String.valueOf(lsn) + "," + data;
	}
}
