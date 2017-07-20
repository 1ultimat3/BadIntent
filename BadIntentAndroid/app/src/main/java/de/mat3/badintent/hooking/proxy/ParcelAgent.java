package de.mat3.badintent.hooking.proxy;


import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

import java.io.FileDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

import de.mat3.badintent.hooking.proxy.dao.ParcelOperation;

/**
 * This singleton is responsible to create a bridge between Android Parcels/Transactions and the app-specific RestAPI.
 * Note: Parcel hooks are aware if operations are created by ParcelAgent or another class.
 */
public enum ParcelAgent {

    //Singleton
    Instance;

    //storage of parcel operations and meta data
    protected Map<Parcel, ParcelContainer> parcelPool = new ConcurrentHashMap<>();
    //strong binders are stored here
    protected Map<String, IBinder> strongBinderPool = new ConcurrentHashMap<>();

    private static final String TAG = "BadIntentAgent";


    protected ParcelContainer getOrCreateParcelContainer(Parcel parcel){
        if (!parcelPool.containsKey(parcel)) {
            ParcelContainer container = new ParcelContainer(parcel);
            parcelPool.put(parcel, container);
        }
        return parcelPool.get(parcel);
    }

    public void interruptString(Parcel parcel, String arg) {
        ParcelContainer container = getOrCreateParcelContainer(parcel);
        waitForPreconditions(container);
        container.addString(arg);
    }

    public void interruptInteger(Parcel parcel, Integer arg) {
        ParcelContainer container = getOrCreateParcelContainer(parcel);
        waitForPreconditions(container);
        container.addInt(arg);
    }

    public void interruptLong(Parcel parcel, Long arg) {
        ParcelContainer container = getOrCreateParcelContainer(parcel);
        waitForPreconditions(container);
        container.addLong(arg);
    }

    public void interruptFloat(Parcel parcel, Float arg) {
        ParcelContainer container = getOrCreateParcelContainer(parcel);
        waitForPreconditions(container);
        container.addFloat(arg);
    }

    public void interruptDouble(Parcel parcel, Double arg) {
        ParcelContainer container = getOrCreateParcelContainer(parcel);
        waitForPreconditions(container);
        container.addDouble(arg);
    }

    public void interruptByteArray(Parcel parcel, byte[] arg, int offset, int len) {
        ParcelContainer container = getOrCreateParcelContainer(parcel);
        waitForPreconditions(container);
        container.addByteArray(arg, offset, len);
    }

    public void interruptBlob(Parcel parcel, byte[] arg, int offset, int len) {
        ParcelContainer container = getOrCreateParcelContainer(parcel);
        waitForPreconditions(container);
        container.addBlob(arg, offset, len);
    }

    public void interruptFileDescriptor(Parcel parcel, FileDescriptor arg) {
        ParcelContainer container = getOrCreateParcelContainer(parcel);
        waitForPreconditions(container);
        container.addFileDescriptor(arg);
    }

    public void interruptStrongBinder(Parcel parcel, IBinder arg) {
        ParcelContainer container = getOrCreateParcelContainer(parcel);
        waitForPreconditions(container);
        if (arg != null) {
            strongBinderPool.put(arg.toString(), arg);
        }
        container.addStrongBinder(arg);
    }

    public void interruptInterfaceToken(Parcel parcel, String arg) {
        ParcelContainer container = getOrCreateParcelContainer(parcel);
        waitForPreconditions(container);
        container.addInterfaceToken(arg);
    }

    public void interruptDataPosition(Parcel parcel, int arg) {
        ParcelContainer container = getOrCreateParcelContainer(parcel);
        waitForPreconditions(container);
        container.addDataPosition(arg);
    }

    public void interruptDataCapacity(Parcel parcel, int arg) {
        ParcelContainer container = getOrCreateParcelContainer(parcel);
        waitForPreconditions(container);
        container.addDataCapacity(arg);
    }

    public void interruptDataSize(Parcel parcel, int arg) {
        ParcelContainer container = getOrCreateParcelContainer(parcel);
        waitForPreconditions(container);
        container.addDataSize(arg);
    }

    public void interruptPushAllowFds(Parcel parcel, boolean arg) {
        ParcelContainer container = getOrCreateParcelContainer(parcel);
        waitForPreconditions(container);
        container.addPushAllowFds(arg);
    }

    public void interruptAppendFrom(Parcel parcel, Parcel arg, int arg1, int arg2) {
        ParcelContainer container = getOrCreateParcelContainer(parcel);
        waitForPreconditions(container);
        container.addAppendFrom(arg, arg1, arg2);
    }

    private void waitForPreconditions(ParcelContainer container) {
        waitForState(container, ParcelContainer.State.NEW, ParcelContainer.State.DONE);
    }

    public void writeInterfaceToken(Parcel parcel, String arg) {
        parcel.writeInterfaceToken(arg);
    }

    public void writeStrongBinder(Parcel parcel, IBinder arg) {
        parcel.writeStrongBinder(arg);
    }

    public void writeByteArray(Parcel parcel, byte[] arg, int offset, int len) {
        parcel.writeByteArray(arg, offset, len);
    }

    public void writeBlob(Parcel parcel, byte[] arg, int offset, int len) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method writeBlob = Parcel.class.getDeclaredMethod("writeBlob", new Class[] { byte[].class, int.class, int.class});
        writeBlob.invoke(parcel, new Object[]{arg, offset, len});
    }

    public void writeString(Parcel parcel, String arg) {
        parcel.writeString(arg);
    }

    public void writeInt(Parcel parcel, Integer arg) {
        parcel.writeInt(arg);
    }

    public void writeLong(Parcel parcel, Long arg) {
        parcel.writeLong(arg);
    }

    public void writeFloat(Parcel parcel, Float arg) {
        parcel.writeFloat(arg);
    }

    public void writeDouble(Parcel parcel, Double arg) {
        parcel.writeDouble(arg);
    }

    public void writeFileDescriptor(Parcel parcel, FileDescriptor arg) {
        parcel.writeFileDescriptor(arg);
    }

    public Map<Parcel, ParcelContainer> getParcelPool() {
        return parcelPool;
    }


    public void writeDataPosition(Parcel parcel, int arg) {
        parcel.setDataPosition(arg);
    }

    public void writeDataCapacity(Parcel parcel, int arg) {
        parcel.setDataCapacity(arg);
    }

    public void writeDataSize(Parcel parcel, int arg) {
        parcel.setDataSize(arg);
    }

    public void writePushAllowFds(Parcel parcel, boolean arg) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method writeBlob = Parcel.class.getDeclaredMethod("pushAllowFds", new Class[] { boolean.class});
        writeBlob.invoke(parcel, new Object[]{arg});
    }

    public void writeAppendFrom(Parcel parcel, Parcel arg, int arg1, int arg2) {
        parcel.appendFrom(arg, arg1, arg2);
    }

    /**
     * Looks up a parcel by the parcelId which is currently the hashcode of the parcel object.
     * @param parcelId
     * @return ParcelContainer form the parcel pool
     * @throws NoSuchElementException
     */
    public ParcelContainer getParcelContainerById(int parcelId) throws NoSuchElementException {
        for (Map.Entry<Parcel, ParcelContainer> poolEntry : parcelPool.entrySet()){
            if (poolEntry.getValue().getParcelID() == parcelId)
                return poolEntry.getValue();
        }
        throw new NoSuchElementException();
    }

    public ParcelContainer getParcelContainer(Parcel data) {
        return parcelPool.get(data);
    }

    public boolean isInterruptRequest(int parcelId) {
        for (Map.Entry<Parcel, ParcelContainer> poolEntry : parcelPool.entrySet()){
            if (poolEntry.getValue().getParcelID() == parcelId)
                return poolEntry.getValue().getState() == ParcelContainer.State.SENT;
        }
        return false;
    }

    /**
     * Write operations from container to native binder
     * @param container
     */
    public void materialize(ParcelContainer container, Parcel parcel) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        for (ParcelOperation operation : container.operationList){
            Object value = operation.operationValue;
            //Log.d(TAG, "materializing: " + operation.operationType);
            //Log.d(TAG, " value: " + value);
            switch (operation.operationType){
                case BLOB:
                    ParcelOperation.ArrayValue val = (ParcelOperation.ArrayValue) value;
                    writeBlob(parcel, val.array, val.offset, val.len);
                    break;
                case BYTE_ARRAY:
                    val = (ParcelOperation.ArrayValue) value;
                    writeByteArray(parcel, val.array, val.offset, val.len);
                    break;
                case DOUBLE:
                    writeDouble(parcel, (Double) value);
                    break;
                case FILE_DESCRIPTOR:
                    writeFileDescriptor(parcel, (FileDescriptor) value);
                    break;
                case FLOAT:
                    writeFloat(parcel, (Float) value);
                    break;
                case INTEGER:
                    writeInt(parcel, (Integer) value);
                    break;
                case INTERFACE_TOKEN:
                    writeInterfaceToken(parcel, (String) value);
                    break;
                case LONG:
                    writeLong(parcel, (Long) value);
                    break;
                case STRING:
                    writeString(parcel, (String) value);
                    break;
                case STRONG_BINDER:
                    IBinder binder = null;
                    if (value != null) {
                        binder = strongBinderPool.get(value);
                    }
                    writeStrongBinder(parcel, binder);
                    break;
                case APPEND_FROM:
                    ParcelOperation.AppendFromValue appendFromValue = (ParcelOperation.AppendFromValue) value;
                    Parcel otherParcel = getParcelContainerById(appendFromValue.parcelId).parcel;
                    writeAppendFrom(parcel, otherParcel, appendFromValue.offset, appendFromValue.length);
                    break;
                case DATA_CAPACITY:
                    writeDataCapacity(parcel, (int) value);
                    break;
                case DATA_POSITION:
                    writeDataPosition(parcel, (int) value);
                    break;
                case DATA_SIZE:
                    writeDataSize(parcel, (int) value);
                    break;
                case PUSH_ALLOW_FDS:
                    writePushAllowFds(parcel, (boolean) value);
                    break;
                default:
                    Log.w(TAG, "Missing Implementation for Operation: " + operation.operationType);
            }
        }
    }

    /**
     * This method waits until the container, which corresponds to the passed parcel
     * moves its state to any of the expected ones.
     * @param container
     * @param states one of the expected states has to be met
     */
    public void waitForState(ParcelContainer container, ParcelContainer.State... states) {

        try {
            while (true) {
                if (states.length == 1) {
                    //Log.d(TAG, data.hashCode() + " current " + getParcelContainerById(data.hashCode()).getState().toString() + " waiting for: " + states[0].toString());

                } else {
                    // Log.d(TAG, data.hashCode() + " current " + getParcelContainerById(data.hashCode()).getState().toString() + " waiting for any:" + Arrays.asList(states).toString());
                }
                for (ParcelContainer.State state : states) {
                    if (container.getState().equals(state)) {
                        return;
                    }
                }
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            Log.w(TAG, "interrupted waiting for state(s)");
        }
    }


    public void addBinder(IBinder binderProxy, ParcelContainer container) {
        try {
            container.binderProxyMap.put(binderProxy.getInterfaceDescriptor(), binderProxy);
        } catch (RemoteException e) {
            Log.e(TAG, "could not retrieve interface descriptor!");
        }
    }

    public Parcel createNewParcelBaseOnContainer(ParcelContainer data) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Parcel newParcel = Parcel.obtain();
        materialize(data, newParcel);
        return newParcel;
    }

    public Parcel obtainParcel() {
        return Parcel.obtain();
    }

    public Parcel copyParcel(Parcel reply) {
        Parcel copy = Parcel.obtain();
        copy.appendFrom(reply, 0, reply.dataSize());
        return copy;
    }

    public boolean transact(IBinder binderProxy, int code, Parcel send, Parcel receive, int flags) throws RemoteException {
        return binderProxy.transact(code, send, receive, flags);
    }


    public void updateState(ParcelContainer container, ParcelContainer.State state) {
        container.setState(state);
    }
}
