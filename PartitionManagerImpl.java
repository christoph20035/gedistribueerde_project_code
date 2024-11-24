import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;


public class PartitionManagerImpl extends UnicastRemoteObject implements PartitionManager {
    private List<BulletinBoard> partitions=null;
    int maxIndex;
    int bulletinBoardSize;

    protected PartitionManagerImpl() throws RemoteException, NoSuchAlgorithmException {
        super();
    }

    @Override
    public void startServers(int numPartitions, int bulletinBoardSize, int portNumber) throws RemoteException {
        this.partitions = new ArrayList<>();
        maxIndex = numPartitions*bulletinBoardSize;
        this.bulletinBoardSize = bulletinBoardSize;

        for(int i=0; i<numPartitions; i++){
            try{
                BulletinBoardImpl partition = new BulletinBoardImpl();
                partition.setBulletinBoardSize(bulletinBoardSize);
                portNumber++;
                Registry registry = LocateRegistry.createRegistry(portNumber);
                String name = "partition" + i;
                registry.rebind(name, partition); // Register each partition
                partitions.add(partition);
            }
            catch(Exception e){
                e.printStackTrace();
            }

        }

    }


    @Override
    public void add(int index, byte[] data, byte[] tag) throws RemoteException {
        Tulpe_partition_index tuple = getPartition(index);
        if(tuple!=null){
            BulletinBoard partition = tuple.partition;
            partition.add(tuple.index, data, tag);
        }
    }

    @Override
    public byte[] get(int index, byte[] tag) throws RemoteException {
        Tulpe_partition_index tuple = getPartition(index);
        if(tuple!=null){
            BulletinBoard partition = tuple.partition;
            return partition.get(tuple.index, tag);
        }
        return null;
    }

    private Tulpe_partition_index getPartition(int index) {
        if(index >= this.maxIndex){
            return null;
        }
        int partitionIndex = (int) Math.floor(index/this.bulletinBoardSize);
        index = index - this.bulletinBoardSize*partitionIndex;
        return new Tulpe_partition_index(index, partitions.get(partitionIndex));
    }

    private static class Tulpe_partition_index{
        int index;
        BulletinBoard partition;

        public  Tulpe_partition_index(int index, BulletinBoard partition){
            this.index = index;
            this.partition = partition;
        }
    }

}