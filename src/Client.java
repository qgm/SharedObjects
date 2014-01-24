import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.lang.reflect.Constructor;
import java.net.*;

public class Client extends UnicastRemoteObject implements Client_itf {

	private static final long serialVersionUID = 1L;
	private static Server_itf server;
	private static Client client;
	private static HashMap<Integer,SharedObject>  objectsTable;
	
	protected Client() throws RemoteException {
		super();
	}

	///////////////////////////////////////////////////
	//         Interface to be used by applications
	///////////////////////////////////////////////////
	
	// initialization of the client layer
	public static void init() {
		int port = 1099; 
		try {
		// calculating the server's url
		String url = "//"+InetAddress.getLocalHost().getHostName()+":"+port+"/server";
		// retrieving the server on the name registry
		server = (Server_itf) Naming.lookup(url);
		// initializing the client
		client = new Client();
		} catch (Exception exc) { 
			exc.printStackTrace();
		}
	}
	
	// lookup in the name server
	public static SharedObject lookup(String name) {
		int id = -1;
		try {
			id = server.lookup(name);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		if (id==-1) {
			// id not found in the registry
			System.err.println("this id cannot be found in the name table");
			return null;
		} else {
			// id found
			/*ServerObject servObj = ((Server) server).getObjectsTable().get(id);
			SharedObject so = generateStub(servObj.getObject(),id);*/
			SharedObject so = new SharedObject(null,id);
			Object o = lock_read(id); // getting a copy of the object linked with id
			so = generateStub(o,id);
			objectsTable.put(id, so);
			return so;
		}
	}		
	
	// binding in the name server
	public static void register(String name, SharedObject_itf so) {
		try {
			server.register(name,((SharedObject) so).getId());
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	// creation of a shared object
	public static SharedObject create(Object o) {
		int id = -1;
		try {
			id = server.create(o);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		SharedObject so = generateStub(o,id);
		objectsTable.put(id,so);
		return so;
	}
	
	public static SharedObject generateStub(Object o, int id) {
		SharedObject so = null;
		// Generation of the stub associated to the type of the Object c
		StubGenerator sg = new StubGenerator(o.getClass().getName());
		sg.writeStub();
		sg.generateFile();
		sg.compileStub();
		// Create an instance of this stub
		try {
			System.out.println(sg.getClassName());
			Class<?> stubClass = Class.forName(sg.getClassName()+"_stub");
			Constructor<?> constructor = stubClass.getConstructor(Object.class,int.class);
			so = (SharedObject) constructor.newInstance(o, id);
		} catch (Exception e ) {
			e.printStackTrace();
		}
		return so;
	}
	
	
    /////////////////////////////////////////////////////////////
    //    Interface to be used by the consistency protocol
    ////////////////////////////////////////////////////////////

	 /////////////////////////////////////////////////////////////
    //    Interface to be used by the consistency protocol
    ////////////////////////////////////////////////////////////

	// request a read lock from the server
	public static Object lock_read(int id) {
		Object o = null;
		try{
			o = server.lock_read(id,client);
		} catch (Exception e){
			System.out.println("connexion error @lock_read");
			e.printStackTrace();
		}
		return o;
	}
	// request a write lock from the server
	public static Object lock_write (int id) {
		Object o = null;
		try{
		o = server.lock_write(id,client);
		}
		catch (Exception e){
			System.out.println("connexion error @lock_write");
			e.printStackTrace();
		}
		
		return o;
	}

	// receive a lock reduction request from the server
	public Object reduce_lock(int id) throws java.rmi.RemoteException {
		SharedObject so = null;
		so = objectsTable.get(id); 
		return so.reduce_lock();	
	}


	// receive a reader invalidation request from the server
	public void invalidate_reader(int id) throws java.rmi.RemoteException {
		
		SharedObject so = null;
		so = objectsTable.get(id); 
		so.invalidate_reader();
	
	}

	// receive a writer invalidation request from the server
	public Object invalidate_writer(int id) throws java.rmi.RemoteException {
		
		SharedObject so;
		so = objectsTable.get(id); 
		return so.invalidate_writer();
	
	}
}   
