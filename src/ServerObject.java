import java.util.ArrayList;

public class ServerObject {

	private int id;
	private int lock;
	private Object object;
	private Server server;
	
	private ArrayList<Client_itf> readersTable;
	private Client_itf writer;
	
	
	public ServerObject(int id, Object o,Server s) {
		this.id = id;
		this.object = o;
		this.server = s;
		this.lock = 0; // 0 = NL : no local lock
				  	   // 1 = RL : read lock 
				       // 2 = WL : write lock
		this.readersTable = new ArrayList<Client_itf> ();
		
	}
	
	// Calls propagated by the server
	public synchronized Object lock_read(Client_itf client)  {
		
		if (lock == 2){  // if WL
			this.object = server.reduce_lock(id, writer); // need to reduce
			readersTable.add(writer); // former reader becomes writer
		}
		
		System.out.println("ServerObj : _ -> RL on object" +id);
		lock = 1; // -> RL
		writer = null; // no more readers
		readersTable.add(client);
		
		return object;
		
	}
	
	public Object lock_write(Client_itf client) {
		
		if (lock == 1){	// if RL
			for (Client_itf clt : readersTable){ 							
				server.invalidate_reader(id, clt); // all the readers are invalidate
			}	
			readersTable.clear(); // the array of readers becomes empty	
		}
		
		if (lock == 2) { // if WL
			this.object = server.invalidate_writer(id, writer); // the former writer is invalidate
		}
		
		System.out.println("ServerObj : _ -> WL on object" +id);
		lock = 2; // -> WL 
		writer = client; // client becomes new writer
		return object;
	
	}
	
	// Getters and Setters
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Object getObject() {
		return object;
	}

	public void setObject(Object object) {
		this.object = object;
	}
}