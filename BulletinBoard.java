import java.rmi.Remote;
import java.rmi.RemoteException;

public interface BulletinBoard extends Remote {
    void add(int index, byte[] data, byte[] tag) throws RemoteException;
    void get(int index, byte[] tag) throws RemoteException;
}
