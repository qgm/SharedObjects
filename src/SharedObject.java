import java.io.*;

public class SharedObject implements Serializable, SharedObject_itf {
	
	private static final long serialVersionUID = 1L;
	
	int id;
	Object obj;
	int lock; // 0 = NL : no local lock
	          // 1 = RLC : read lock cached (not taken)
			  // 2 = WLC : write lock cached
			  // 3 = RLT : read lock taken
			  // 4 = WLT : write lock taken
	  		  // 5 = RLT_WLC : read lock taken and write lock cached
	
	public SharedObject(Object o,int id) {
		this.obj = o;
		this.lock = 0; // initialize to NL
		this.id = id;
	}

	// invoked by the user program on the client node
	public void lock_read() {
		
		synchronized (this) {
			
			if (lock == 0) { // if there is no lock, NL
				lock = 3; // take the lock -> RLT
				System.out.println("SharedObj : NL -> RLT on object" +id);
				obj = Client.lock_read(id); // call the server
			}
			
			if (lock==1) { // if RLC
				System.out.println("SharedObj : RLC -> RLT on object" +id);
				lock = 3; // idem but there is no need to call the server
			}
			
			if (lock==2) { // if WLC
				lock = 5; // take the lock already cached -> RLT_WLC, no call to the server
				System.out.println("SharedObj : WLC -> RLT_WLC on object" +id);
			}
		
			// the others cases lead to nothing
		}
		
	}

	
	// invoked by the user program on the client node
	public void lock_write() {
		
		synchronized (this) {
			
			if (lock == 0) { // if NL
				System.out.println("SharedObj : NL -> WLT on object" +id);
				lock = 4; // take the lock -> WLT
				obj = Client.lock_write(id); // and call the server 
			}
			
			if ( lock == 1 ) { // if RLC
				System.out.println("SharedObj : RLC -> WLT on object" +id);
			    lock = 4;
			    // no need to call the server
			}
			
			if (lock == 2) { // if WLC
				System.out.println("SharedObj : WLC -> WLT on object" +id);
				lock = 4; // take the lock, no need to call the server
			}
			
			if (lock == 3) { // if RLT
				System.out.println("SharedObj : stays at RLT on object" +id);
				obj = Client.lock_write(id); // cant take the lock, call the server
			}
		}
	}

	
	// invoked by the user program on the client node
	public synchronized void unlock() {
		
		if (lock == 3) { // if RLT
			System.out.println("SharedObj : RLT -> RLC on object" +id);
			lock = 1; // -> RLC
		}
		
		if ((lock == 4)||(lock == 5)) { // if WLT or RLC_WLT
			System.out.println("SharedObj : WLT|RLC_WLT -> WLC on object" +id);
			lock = 2; // -> WLC
		}
		
		try{
			notify(); 
		} catch(Exception e){
			System.out.println("synchronization error @unlock");
			e.printStackTrace();
		}
		
	}


	// callback invoked remotely by the server
	public synchronized Object reduce_lock() {

		while(lock == 4){ // while WLT: waiting for the client finish to write
			try {
				wait();
			} catch (InterruptedException e) {
				System.out.println("synchronization error @reduce_lock");
				e.printStackTrace();
			}
		}
		
		if (lock == 2){
			System.out.println("SharedObj : WLC -> RLC on object" +id);
			lock = 1; // if the client keeps the lock cached without using it (WLC), we take it (RLC)
		}
		
		else if (lock == 5){ // idem if RLT_WLC then RLT
			System.out.println("SharedObj : RLT_WLC -> RLT on object" +id);
			lock = 3; 
		}
		
		return obj;
		
	}

	
	// callback invoked remotely by the server
	public synchronized void invalidate_reader() {
		
		while(lock == 3){ // while RLT: waiting for the client finish to read
			try {
				wait();
			} catch (InterruptedException e) {
				System.out.println("synchronization error @invalidate_reader");
				e.printStackTrace();
			}
		}
		
		if (lock == 1) {
			System.out.println("SharedObj : RLC -> NL on object" +id);
			lock = 0; // taking back the lock if he keeps it (if RLC)
		}
	}

	
	public synchronized Object invalidate_writer() {
		
		while(lock == 4){ // while WLT: waiting for the client finish to write
			try {
				wait();
			} catch (InterruptedException e) {
				System.out.println("synchronization error @invalidate_writer");
				e.printStackTrace();
			}
		}
		
		if (lock == 2) {
			System.out.println("SharedObj : WLC -> NL on object" +id);
			lock = 0; // taking back the lock if he keeps it (if WLC)
		}
		
		return this.obj;
	}


	// Getters and Setters
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Object getObj() {
		return obj;
	}

	public void setObj(Object obj) {
		this.obj = obj;
	}

	public int getLock() {
		return lock;
	}

	public void setLock(int lock) {
		this.lock = lock;
	}

}