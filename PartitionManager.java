import java.rmi.Remote;
import java.rmi.RemoteException;

public interface PartitionManager extends Remote {
    void add(int index, byte[] data, byte[] tag) throws RemoteException;
    byte[] get(int index, byte[] tag) throws RemoteException;
    void startServers(int numPartitions, int bulletinBoardSize, int portNumber) throws RemoteException;
}

