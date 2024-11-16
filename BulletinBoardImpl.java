import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Set;

public class BulletinBoardImpl extends UnicastRemoteObject implements BulletinBoard{
    private static Set<Tuple>[] B;


    protected BulletinBoardImpl() throws RemoteException {
        super();
    }

    @Override
    public void add() throws RemoteException{

    }

    @Override
    public void get() throws RemoteException{

    }
}
