package de.mat3.badintent.hooking.proxy;



public class ParcelIdGenerator {

    private static int nextParcelId = 0;

    public synchronized static int getNextParcelId(){
        return nextParcelId++;
    }
}
