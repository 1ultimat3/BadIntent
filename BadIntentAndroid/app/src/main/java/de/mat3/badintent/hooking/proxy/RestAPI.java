package de.mat3.badintent.hooking.proxy;

import android.os.IBinder;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.common.io.BaseEncoding;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.NanoHTTPD;
import org.nanohttpd.protocols.http.response.Response;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import de.mat3.badintent.hooking.proxy.dao.ParcelOperation;
import de.mat3.badintent.hooking.proxy.dao.ParcelOperationDAO;
import de.mat3.badintent.hooking.proxy.dao.ParcelPoolElementDAO;
import de.mat3.badintent.hooking.proxy.dao.SerializationUtils;

import static org.nanohttpd.protocols.http.response.Response.newFixedLengthResponse;

/**
 * REST API in order to interact with the Parcel:
 *  GET     /parcel/list            in order to get all ids in the parcel pool
 *  GET     /parcel/<parcel_id>     retrieves the parcel data
 *  POST    /parcel/<parcel_id>     updates and writes parcel data to Binder
 *  PUT     /parcel/<parcel_id>     creates and writes parcel data to Binder
 *  GET     /info                   received information about the app (currently only package name)
 *  PUT     /log                    log information target (no action)
 */
public class RestAPI extends NanoHTTPD {

    public static final String TAG = "BadIntentRestAPI";

    public RestAPI(String hostname, int port) {
        super(hostname, port);
        Log.i(TAG, "Starting internal Webserver at " + hostname + ":" + port);
    }

    @Override
    public Response serve(IHTTPSession session) {
        String path = session.getUri();

        if (path.startsWith("/parcel/list")) {
            Log.d(TAG, "Serving HTTP Request /parcel/list");
            try {
                List<Integer> parcelIds = new LinkedList<>();
                Map<Parcel, ParcelContainer> parcelPool = ParcelAgent.Instance.getParcelPool();
                Log.d(TAG, "ParcelPool contains #elements: " + parcelPool.size());
                for (ParcelContainer container : parcelPool.values()) {
                    parcelIds.add(container.getParcelID());
                }
                Log.d(TAG, "ParcelPool IDs: " + parcelIds.toString());
                Response response = newFixedLengthResponse(SerializationUtils.gson.toJson(parcelIds));
                return finalize(response);
            } catch (Exception e) {
                Response response = newFixedLengthResponse("ERROR\n" + e.getMessage());
                return finalize(response);
            }

        } else if (path.startsWith("/parcel/")){
            //Log.d(TAG, "Serving HTTP Request /parcel/<parcel_id>");
            //a parcelID is expected /parcel/<parcel_id>
            int parcelId = Integer.parseInt(path.split("/")[2]);
            //Log.d(TAG, "Receiving request regarding " + parcelId);
            switch (session.getMethod()){
                case GET:
                    ParcelContainer container = ParcelAgent.Instance.getParcelContainerById(parcelId);
                    //Log.d(TAG, "#Operations: " + container.operationList.size());
                    ParcelPoolElementDAO parcelPoolElementDAO = convertContainerToDAO(container, 0, 0);
                    String jsonResponse = SerializationUtils.gson.toJson(parcelPoolElementDAO);
                    //Log.d(TAG, "writing out container via HTTP");
                    return finalize(newFixedLengthResponse(jsonResponse));
                case POST:
                    //update and materialize parcel
                    try {
                        Map<String, String> files = new HashMap<String, String>();
                        session.parseBody(files);
                        String postBody = files.get("postData");
                        //Log.d(TAG, "parsing JSON in POST body:" + postBody);
                        ParcelPoolElementDAO receivedPoolElementDAO = SerializationUtils.gson.fromJson(postBody, ParcelPoolElementDAO.class);
                        if (ParcelAgent.Instance.isInterruptRequest(receivedPoolElementDAO.parcelID)) {
                            //Log.d(TAG, "INTERRUPT identified for: " + receivedPoolElementDAO.parcelID);
                            //Log.d(TAG, "Retrieving ParcelContainer for: " + receivedPoolElementDAO.parcelID);
                            ParcelContainer localContainer = ParcelAgent.Instance.getParcelContainerById(receivedPoolElementDAO.parcelID);
                            Iterator<ParcelOperation> localOperations = localContainer.operationList.iterator();
                            Iterator<ParcelOperationDAO> receivedOperations = receivedPoolElementDAO.operations.iterator();
                            //Log.d(TAG, "Taking over changes for " + receivedPoolElementDAO.parcelID);
                            //Take over changes from DAO to container
                            while (localOperations.hasNext() && receivedOperations.hasNext()) {
                                ParcelOperation localOperation = localOperations.next();
                                ParcelOperationDAO receivedOperation = receivedOperations.next();
                                switch (receivedOperation.parcelType) {
                                    case STRONG_BINDER:
                                        localOperation.operationValue = receivedOperation.value;
                                        break;
                                    case BLOB:
                                        LinkedTreeMap receivedStructure = (LinkedTreeMap) receivedOperation.value;
                                        localOperation.operationValue = ParcelOperation.ArrayValue.parseArrayValue(receivedStructure);
                                        break;
                                    case BYTE_ARRAY:
                                        receivedStructure = (LinkedTreeMap) receivedOperation.value;
                                        localOperation.operationValue = ParcelOperation.ArrayValue.parseArrayValue(receivedStructure);
                                        break;
                                    case DOUBLE:
                                        localOperation.operationValue = Double.parseDouble((String) receivedOperation.value);
                                        break;
                                    case FLOAT:
                                        localOperation.operationValue = Float.parseFloat((String) receivedOperation.value);
                                        break;
                                    case INTEGER:
                                        localOperation.operationValue = Integer.parseInt((String) receivedOperation.value);
                                        break;
                                    case LONG:
                                        localOperation.operationValue = Long.parseLong((String) receivedOperation.value);
                                        break;
                                    case DATA_SIZE:
                                        localOperation.operationValue = Integer.parseInt((String) receivedOperation.value);
                                        break;
                                    case DATA_CAPACITY:
                                        localOperation.operationValue = Integer.parseInt((String) receivedOperation.value);
                                        break;
                                    case DATA_POSITION:
                                        localOperation.operationValue = Integer.parseInt((String) receivedOperation.value);
                                        break;
                                    case PUSH_ALLOW_FDS:
                                        localOperation.operationValue = Boolean.parseBoolean((String) receivedOperation.value);
                                        break;
                                    case INTERFACE_TOKEN:
                                        localOperation.operationValue = receivedOperation.value;
                                        break;
                                    case FILE_DESCRIPTOR:
                                        localOperation.operationValue = Integer.parseInt((String) receivedOperation.value);
                                        break;
                                    case APPEND_FROM:
                                        receivedStructure = (LinkedTreeMap) receivedOperation.value;
                                        localOperation.operationValue = ParcelOperation.AppendFromValue.parseAppendFromValue(receivedStructure);
                                        break;
                                    case STRING:
                                        localOperation.operationValue = receivedOperation.value;
                                        break;
                                }
                            }
                            localContainer.setState(ParcelContainer.State.RECEIVED);

                            ParcelAgent.Instance.waitForState(localContainer, ParcelContainer.State.REPLY, ParcelContainer.State.ERROR);

                            Response response = createHttpResponseFromReply(localContainer.getReply());

                            //Need to reset state
                            cleanContainer(localContainer);

                            return finalize(response);
                        } else {
                            Log.d(TAG, "REPEAT identified for: " + receivedPoolElementDAO.parcelID);
                            int code = receivedPoolElementDAO.transactionCode;
                            int flags = receivedPoolElementDAO.transactionFlags;
                            Parcel send = ParcelAgent.Instance.obtainParcel();
                            Parcel receive = ParcelAgent.Instance.obtainParcel();
                            ParcelContainer newContainer = ParcelAgent.Instance.getOrCreateParcelContainer(send);

                            //taking over data
                            container = ParcelAgent.Instance.getParcelContainerById(parcelId);
                            newContainer.binderProxyMap.putAll(container.binderProxyMap);

                            for (ParcelOperationDAO operation : receivedPoolElementDAO.operations) {
                                //Log.d(TAG, "data: " + operation.parcelType + " " + operation.value);
                                switch (operation.parcelType) {
                                    case STRONG_BINDER:
                                        IBinder binder = newContainer.binderProxyMap.get(operation.value);
                                        newContainer.addStrongBinder(binder);
                                        break;
                                    case BLOB:
                                        LinkedTreeMap receivedStructure = (LinkedTreeMap) operation.value;
                                        ParcelOperation.ArrayValue operationValue = ParcelOperation.ArrayValue.parseArrayValue(receivedStructure);
                                        newContainer.addBlob(operationValue.array, operationValue.offset, operationValue.len);
                                        break;
                                    case BYTE_ARRAY:
                                        receivedStructure = (LinkedTreeMap) operation.value;
                                        operationValue = ParcelOperation.ArrayValue.parseArrayValue(receivedStructure);
                                        newContainer.addByteArray(operationValue.array, operationValue.offset, operationValue.len);
                                        break;
                                    case DOUBLE:
                                        newContainer.addDouble(Double.parseDouble((String) operation.value));
                                        break;
                                    case FLOAT:
                                        newContainer.addFloat(Float.parseFloat((String) operation.value));
                                        break;
                                    case INTEGER:
                                        newContainer.addInt(Integer.parseInt((String) operation.value));
                                        break;
                                    case LONG:
                                        newContainer.addLong(Long.parseLong((String) operation.value));
                                        break;
                                    case DATA_SIZE:
                                        newContainer.addDataSize(Integer.parseInt((String) operation.value));
                                        break;
                                    case DATA_CAPACITY:
                                        newContainer.addDataCapacity(Integer.parseInt((String) operation.value));
                                        break;
                                    case DATA_POSITION:
                                        newContainer.addDataPosition(Integer.parseInt((String) operation.value));
                                        break;
                                    case PUSH_ALLOW_FDS:
                                        newContainer.addPushAllowFds(Boolean.parseBoolean((String) operation.value));
                                        break;
                                    case INTERFACE_TOKEN:
                                        newContainer.addInterfaceToken((String) operation.value);
                                        break;
                                    case FILE_DESCRIPTOR:
                                        newContainer.addInt(Integer.parseInt((String) operation.value));
                                        Log.w(TAG, "(new) FileDescriptors are supported in repeat mode. However, value is written out as an integer");
                                        break;
                                    case APPEND_FROM:
                                        receivedStructure = (LinkedTreeMap) operation.value;
                                        ParcelOperation.AppendFromValue operationValueAppendFrom = ParcelOperation.AppendFromValue.parseAppendFromValue(receivedStructure);
                                        Log.w(TAG, "AppendFrom not fully supported in repeat mode.");
                                        newContainer.addAppendFrom(
                                                ParcelAgent.Instance.getParcelPool().get(operationValueAppendFrom.parcelId).parcel,
                                                operationValueAppendFrom.offset,
                                                operationValueAppendFrom.length
                                        );
                                        break;
                                    case STRING:
                                        newContainer.addString((String) operation.value);
                                        break;
                                    default:
                                        Log.w(TAG, "value not added to container: " + operation.value);
                                }
                            }
                            ParcelAgent.Instance.materialize(newContainer, send);

                            try {
                                String interfaceToken = (String) newContainer.operationList.get(0).operationValue;
                                IBinder binderProxy = newContainer.binderProxyMap.get(interfaceToken);
                                boolean result = ParcelAgent.Instance.transact(binderProxy, code, send, receive, flags);
                                Response response = createHttpResponseFromReply(receive);
                                return response;
                            } catch (Exception e) {
                                //show detailed error
                                return finalize(newFixedLengthResponse(e.getMessage()));
                            } finally {
                                //finally set back state to DONE and clear operations
                                cleanContainer(container);
                            }
                        }

                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                        try {
                            ParcelAgent.Instance.getParcelContainerById(parcelId).setState(ParcelContainer.State.ERROR);
                        } catch (NoSuchElementException _) {
                            Log.e(TAG, "Cannot find parcel " + parcelId + " - no state update to ERROR");
                        }
                    }

                    break;
                default:
                    break; //return default error
            }
        } else if (path.startsWith("/info")){
            Map<String, Object> data = new HashMap<>();
            data.put("packageName", AppInformation.Instance.packageName);
            data.put("restPort", Integer.toString(AppInformation.Instance.port));
            data.put("interfaceTokens", AppInformation.Instance.usedInterfaceTokens);
            data.put("interceptedInterfaceTokens", AppInformation.Instance.interceptedInterfaceTokens);
            String metadata = SerializationUtils.createGson().toJson(data);
            return finalize(newFixedLengthResponse(metadata));
        } else if (path.startsWith("/log")) {
            Map<String, String> data = new HashMap<>();
            data.put("status", "OK");
            String metadata = SerializationUtils.createGson().toJson(data);
            return finalize(newFixedLengthResponse(metadata));
        }

        Log.e(TAG, "Serving HTTP Request: no action found!");
        return finalize(newFixedLengthResponse("ERROR"));

    }

    private Response finalize(Response response) {
        response.closeConnection(true);
        return response;
    }

    @NonNull
    protected Response createHttpResponseFromReply(Parcel reply) {
        Gson gson = SerializationUtils.createGson();
        Map<String, Object> data = createResponseMap(reply);
        return finalize(newFixedLengthResponse(gson.toJson(data)));
    }

    @NonNull
    private Map<String, Object> createResponseMap(Parcel reply) {
        Map<String, Object> data = new HashMap<>();
        data.put("message", "reply");
        if (reply != null) {
            try {
                byte[] marshalled = reply.marshall();
                data.put("data", marshalled);
                if (marshalled != null) {
                    String base64 = BaseEncoding.base64().encode(marshalled).replace("/", "");
                    data.put("data_base64", base64);
                }
            } catch (RuntimeException e) {
                data.put("info", "activeObject");
            }
        }
        return data;
    }

    protected void cleanContainer(ParcelContainer localContainer) {
        localContainer.setState(ParcelContainer.State.DONE);
    }

    @NonNull
    public static ParcelPoolElementDAO convertContainerToDAO(ParcelContainer container, int code, int flags) {
        ParcelPoolElementDAO parcelPoolElementDAO = new ParcelPoolElementDAO();
        parcelPoolElementDAO.parcelID = container.getParcelID();
        parcelPoolElementDAO.state = container.getState();
        parcelPoolElementDAO.transactionCode = code;
        parcelPoolElementDAO.transactionFlags = flags;

        for (ParcelOperation operation : container.operationList){
            //Log.d(TAG, " " + operation.operationType + " : " + operation.operationValue);
            ParcelOperationDAO parcelOperationDAO = new ParcelOperationDAO();
            parcelOperationDAO.parcelType = operation.operationType;
            if (operation.operationType.equals(ParcelOperation.ParcelType.BYTE_ARRAY)
                    || operation.operationType.equals(ParcelOperation.ParcelType.BLOB)
                    || operation.operationType.equals(ParcelOperation.ParcelType.APPEND_FROM)) {
                parcelOperationDAO.value = operation.operationValue;
            } else {
                if (operation.operationValue != null) {
                    parcelOperationDAO.value = operation.operationValue.toString();
                }
            }

            parcelPoolElementDAO.operations.add(parcelOperationDAO);
        }
        return parcelPoolElementDAO;
    }

}

