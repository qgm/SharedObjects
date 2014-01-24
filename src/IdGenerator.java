import java.io.Serializable;

public class IdGenerator implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private int nextId;
	
	public IdGenerator(int id) {
		this.nextId = id;
	}

	public int toInt() {
		return nextId;
	}
	
    public synchronized int nextId() {
            return nextId++;
    }
  }