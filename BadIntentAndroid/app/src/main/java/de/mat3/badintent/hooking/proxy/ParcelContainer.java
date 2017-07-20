package de.mat3.badintent.hooking.proxy;

import android.os.IBinder;
import android.os.Parcel;

import java.io.FileDescriptor;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.mat3.badintent.hooking.proxy.dao.NotSerializable;
import de.mat3.badintent.hooking.proxy.dao.ParcelOperation;

/**
 * This class is supposed to save serialized Parcel data
 *
 * The lifecycle for the container is as follows
 * (however, please note that parcels are reused and can therefore be reset to NEW/DONE):
 * 1. In case the transaction is intercepted:
 *  NEW,DONE --> SENT --> RECEIVED --> REPLY --> DONE
 * 2. Transaction is not intercepted
 *  NEW,DONE --> BYPASS --> BYPASS --> DONE
 * 3. In a error situation
 *  * --> ERROR --> ERROR --> DONE
 */
public class ParcelContainer {

    private static final String TAG = "BadIntentPContainer";

    public enum State { NEW, SENT, RECEIVED, REPLY, ERROR, DONE, BYPASS};

    protected List<ParcelOperation> operationList = new LinkedList<>();
    protected Map<String, IBinder> binderProxyMap = new HashMap<>();

    @NotSerializable protected Parcel parcel;
    private int parcelID;
    private State state;
    private Parcel reply;

    public ParcelContainer(Parcel parcel) {
        this.parcel = parcel;
        this.parcelID = ParcelIdGenerator.getNextParcelId();
        state = State.NEW;
    }

    public void addString(String arg) {
        ParcelOperation operation = new ParcelOperation(ParcelOperation.ParcelType.STRING, arg);
        operationList.add(operation);
    }

    public void addInt(Integer arg) {
        ParcelOperation operation = new ParcelOperation(ParcelOperation.ParcelType.INTEGER, arg);
        operationList.add(operation);
    }

    public void addLong(Long arg) {
        ParcelOperation operation = new ParcelOperation(ParcelOperation.ParcelType.LONG, arg);
        operationList.add(operation);
    }

    public void addFloat(Float arg) {
        ParcelOperation operation = new ParcelOperation(ParcelOperation.ParcelType.FLOAT, arg);
        operationList.add(operation);
    }

    public void addDouble(Double arg) {
        ParcelOperation operation = new ParcelOperation(ParcelOperation.ParcelType.DOUBLE, arg);
        operationList.add(operation);
    }

    public void addByteArray(byte[] arg, int offset, int len) {
        ParcelOperation.ArrayValue arrayValue = new ParcelOperation.ArrayValue(arg, offset, len);
        ParcelOperation operation = new ParcelOperation(ParcelOperation.ParcelType.BYTE_ARRAY, arrayValue);
        operationList.add(operation);
    }

    public void addBlob(byte[] arg, int offset, int len) {
        ParcelOperation.ArrayValue arrayValue = new ParcelOperation.ArrayValue(arg, offset, len);
        ParcelOperation operation = new ParcelOperation(ParcelOperation.ParcelType.BLOB, arrayValue);
        operationList.add(operation);
    }

    public void addFileDescriptor(FileDescriptor arg) {
        ParcelOperation operation = new ParcelOperation(ParcelOperation.ParcelType.FILE_DESCRIPTOR, arg);
        operationList.add(operation);
    }

    public void addStrongBinder(IBinder arg) {
        String binderArg = null;
        if (arg != null){
            binderArg = arg.toString();
        }
        ParcelOperation operation = new ParcelOperation(ParcelOperation.ParcelType.STRONG_BINDER, binderArg);
        operationList.add(operation);
    }

    public void addInterfaceToken(String arg) {
        ParcelOperation operation = new ParcelOperation(ParcelOperation.ParcelType.INTERFACE_TOKEN, arg);
        operationList.add(operation);
    }

    public void addDataPosition(int arg) {
        ParcelOperation operation = new ParcelOperation(ParcelOperation.ParcelType.DATA_POSITION, arg);
        operationList.add(operation);
    }

    public void addDataCapacity(int arg) {
        ParcelOperation operation = new ParcelOperation(ParcelOperation.ParcelType.DATA_CAPACITY, arg);
        operationList.add(operation);
    }

    public void addDataSize(int arg) {
        ParcelOperation operation = new ParcelOperation(ParcelOperation.ParcelType.DATA_SIZE, arg);
        operationList.add(operation);
    }

    public void addPushAllowFds(boolean arg) {
        ParcelOperation operation = new ParcelOperation(ParcelOperation.ParcelType.PUSH_ALLOW_FDS, arg);
        operationList.add(operation);
    }

    public void addAppendFrom(Parcel arg, int arg1, int arg2) {
        ParcelContainer otherParcel = ParcelAgent.Instance.getOrCreateParcelContainer(arg);
        ParcelOperation.AppendFromValue appendFromValue = new ParcelOperation.AppendFromValue(otherParcel.getParcelID(), arg1, arg2);
        ParcelOperation operation = new ParcelOperation(ParcelOperation.ParcelType.APPEND_FROM, appendFromValue);
        operationList.add(operation);
    }

    public void setReply(Parcel reply) {
        this.reply = reply;
    }

    public Parcel getReply() {
        return reply;
    }

    public int getParcelID() {
        return parcelID;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        // Log.d(TAG, "ParcelId: " + parcelID + " setting state: " + state.toString());
        this.state = state;
    }

    public Parcel getParcel() {
        return parcel;
    }

    public List<ParcelOperation> getOperationList() {
        return operationList;
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer("ParcelContainer state: " + state.toString() + " #Parcels" + operationList.size());
        return buf.toString();
    }
}
