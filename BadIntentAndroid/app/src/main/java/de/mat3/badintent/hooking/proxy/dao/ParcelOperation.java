package de.mat3.badintent.hooking.proxy.dao;

import android.support.annotation.NonNull;

import com.google.gson.internal.LinkedTreeMap;

import java.util.ArrayList;

/**
 * A parcel operation consists of a type and value.
 */
public class ParcelOperation {


    public enum ParcelType {
        STRING,
        LONG,
        FLOAT,
        DOUBLE,
        BYTE_ARRAY,
        BLOB,
        FILE_DESCRIPTOR,
        STRONG_BINDER,
        INTEGER,

        DATA_POSITION,
        DATA_CAPACITY,
        DATA_SIZE,

        PUSH_ALLOW_FDS,

        APPEND_FROM,
        INTERFACE_TOKEN

    }

    public static class ArrayValue {
        public byte[] array;
        public int offset;
        public int len;

        public ArrayValue(byte[] array, int offset, int len) {
            this.len = len;
            this.array = array;
            this.offset = offset;
        }

        public ArrayValue(ArrayList array, int offset, int len) {
            this.len = len;
            if (array != null) {
                byte[] result = new byte[array.size()];
                for (int i = 0; i < array.size(); i++) {
                    result[i] = (byte) ((Double) array.get(i)).intValue();
                }
                this.array = result;
            }
            this.offset = offset;
        }

        @NonNull
        public static ArrayValue parseArrayValue(LinkedTreeMap receivedStructure) {
            return new ArrayValue(
                    (ArrayList) receivedStructure.get("array"),
                    ((Double) receivedStructure.get("len")).intValue(),
                    ((Double) receivedStructure.get("offset")).intValue()
            );
        }
    }

    public static class AppendFromValue {
        public int parcelId;
        public int offset;
        public int length;

        public AppendFromValue(int parcelId, int offset, int length) {
            this.parcelId = parcelId;
            this.offset = offset;
            this.length = length;
        }

        @NonNull
        public static AppendFromValue parseAppendFromValue(LinkedTreeMap receivedStructure) {
            return new AppendFromValue(
                    ((Double) receivedStructure.get("parcelId")).intValue(),
                    ((Double) receivedStructure.get("offset")).intValue(),
                    ((Double) receivedStructure.get("length")).intValue()
            );
        }
    }

    public ParcelType operationType;
    public Object operationValue;

    public ParcelOperation(ParcelType operationType, Object operationValue) {
        this.operationType = operationType;
        this.operationValue = operationValue;
    }

    @Override
    public String toString() {
        return "Operation: " + operationType.name() + " Value: " + operationValue;
    }


}
