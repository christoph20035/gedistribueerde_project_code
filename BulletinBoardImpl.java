import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class BulletinBoardImpl extends UnicastRemoteObject implements BulletinBoard {
    private static int BULLETIN_BOARD_SIZE = -1;
    private static Set<Tuple>[] B;

    MessageDigest digest = MessageDigest.getInstance("SHA-256");


    protected BulletinBoardImpl(int BULLETIN_BOARD_SIZE) throws RemoteException, NoSuchAlgorithmException {
        super();
    }
    public static void setBulletinBoardSize(int size) {
        BULLETIN_BOARD_SIZE = size;
        B = new Set[BULLETIN_BOARD_SIZE];
        for (int i = 0; i < B.length; i++) {
            B[i] = new HashSet<Tuple>();
        }
    }

    @Override
    public void add(int index, byte[] data, byte[] tag) throws RemoteException {
        assert(BULLETIN_BOARD_SIZE != -1);
        B[index].add(new Tuple(data, tag));
    }

    @Override
    public byte[] get(int index, byte[] tag) throws RemoteException {
        byte[] t = digest.digest(tag);
        for(Tuple tuple: B[index]){
            if(Arrays.equals(t, tuple.getTag())){
                B[index].remove(tuple);
                return tuple.getData();
            }
        }
        return null;
    }
}
