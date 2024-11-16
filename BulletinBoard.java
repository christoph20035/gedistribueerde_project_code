import java.rmi.Remote;
import java.rmi.RemoteException;

public interface BulletinBoard extends Remote {
    void add() throws RemoteException;
    void get() throws RemoteException;
}
