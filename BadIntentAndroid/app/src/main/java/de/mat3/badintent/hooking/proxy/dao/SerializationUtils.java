package de.mat3.badintent.hooking.proxy.dao;


import android.os.Parcel;
import android.support.annotation.NonNull;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.mat3.badintent.hooking.proxy.ParcelAgent;
import de.mat3.badintent.hooking.proxy.ParcelContainer;
import de.mat3.badintent.hooking.proxy.RestAPI;

public class SerializationUtils {

    public final static Gson gson = createGson();

    public static String getJson(ParcelContainer container, int code, int flags) {
        ParcelPoolElementDAO parcelPoolElementDAO = RestAPI.convertContainerToDAO(container, code, flags);
        return gson.toJson(parcelPoolElementDAO);
    }

    @NonNull
    public static Gson createGson() {
        return new GsonBuilder().setExclusionStrategies(new ExclusionStrategy() {
                @Override
                public boolean shouldSkipField(FieldAttributes f) {
                    return f.getAnnotation(NotSerializable.class) != null;
                }

                @Override
                public boolean shouldSkipClass(Class<?> clazz) {
                    return false;
                }
            }).create();
    }
}
