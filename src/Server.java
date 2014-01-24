import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

public class Server extends UnicastRemoteObject implements Server_itf {

	private static final long serialVersionUID = 1L;
	private static int port;
	private static String url;
	private static Server server;
	private static HashMap<String,Integer> nameTable  = new HashMap<String,Integer> ();
	private static HashMap<Integer, ServerObject> objectsTable  = new HashMap<Integer,ServerObject> ();
	private IdGenerator id;
	
	public Server() throws RemoteException {
		super();
	    setId(new IdGenerator(0));
	    objectsTable  = new HashMap<Integer,ServerObject> ();
	    nameTable  = new HashMap<String,Integer> ();
	}
	
	// Returns the object's id 
	public int lookup(String name) {
		if (nameTable.get(name) == null)
			return -1;
		else
			return nameTable.get(name);
	}
	
	// Linking the object's name with its id
	public void register(String name, int id)  {
		nameTable.put(name,id);
		System.out.println(name +" added to the name table with id = " +id);
	}
	
	// returns the id of the created object
	public int create(Object o) {
		int id = this.getId().nextId(); // incrementation of the id
		ServerObject servObj = new ServerObject(id,o,server); // creation of the ServerObject associated with this id and object
		objectsTable.put(id, servObj); // linking the id with the associated ServerObject
		System.out.println(servObj.toString()+ "added to the objects table with id ="+id);
		return id;
	}
	
	public Object lock_read(int id, Client_itf client)  {

		ServerObject so = null;								    	
		so = objectsTable.get(id);
		return so.lock_read(client);
		
	}
	
	public Object lock_write(int id, Client_itf client) {
		ServerObject so = null;								    	
		so = objectsTable.get(id);
		return so.lock_write(client);
		
	}
	
	// The 3 methods below transfer the calls froms the serverObject to the Client
	public void invalidate_reader (int id, Client_itf client){ 

		try {
			client.invalidate_reader(id);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
	}
	
	public Object invalidate_writer ( int id , Client_itf client){
		
		Object o = null;
		try {
			 o = client.invalidate_writer(id);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return o;
		
	}
	
	public Object reduce_lock ( int id ,Client_itf client ){
		Object o = null;
		try {
			o = client.reduce_lock(id);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return o;
	}
	
	
	public static void main(String[] args) {
		
		port = 1099;
		// Creating name registry
		try {
			Registry registry = LocateRegistry.createRegistry(port);
			// Creating the server
			server = new Server();
			// Calculating the server's url
			url = "//"+InetAddress.getLocalHost().getHostName()+":"+port+"/server";
			System.out.println("Url : " +url+", server =" +server.toString());
			Naming.rebind(url, server);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.err.println("Server running");
	}

	// Getters and Stetters
	public IdGenerator getId() {
		return id;
	}

	public void setId(IdGenerator id) {
		this.id = id;
	}
	
	public static HashMap<String, Integer> getNameTable() {
		return nameTable;
	}

	public static void setNameTable(HashMap<String, Integer> nameTable) {
		Server.nameTable = nameTable;
	}

	public static HashMap<Integer, ServerObject> getObjectsTable() {
		return objectsTable;
	}

	public static void setObjectsTable(HashMap<Integer, ServerObject> objectsTable) {
		Server.objectsTable = objectsTable;
	}
	
}
