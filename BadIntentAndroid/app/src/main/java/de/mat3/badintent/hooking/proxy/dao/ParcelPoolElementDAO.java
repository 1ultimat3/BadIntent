package de.mat3.badintent.hooking.proxy.dao;


import java.util.LinkedList;
import java.util.List;

import de.mat3.badintent.hooking.proxy.ParcelContainer;

public class ParcelPoolElementDAO {
    public int parcelID;
    public List<ParcelOperationDAO> operations = new LinkedList<>();
    public ParcelContainer.State state;
    public int transactionCode;
    public int transactionFlags;
}
