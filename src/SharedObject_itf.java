import java.rmi.Remote;

public interface SharedObject_itf extends Remote {
	
	public void lock_read();
	public void lock_write();
	public void unlock();

}