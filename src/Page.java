
public class Page {
	private int pageId, lsn;
	private String data;
	
	public Page(int taId, int pageId, int lsn, String data) {
		super();
		this.pageId = pageId;
		this.lsn = lsn;
		this.data = data;
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
