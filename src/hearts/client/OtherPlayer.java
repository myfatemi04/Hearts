package hearts.client;

public class OtherPlayer {
	public long id = 0L;
	public String name = "Unnamed 0";
	public OtherPlayer(long id) {
		this.id = id;
		this.name = "Unnamed " + id;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return this.name;
	}
}
