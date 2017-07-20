package dao;

import java.util.LinkedList;
import java.util.List;


public class ParcelPoolElementDAO {
    public int parcelID;
    public List<ParcelOperationDAO> operations = new LinkedList<>();
    public String state;
    public int transactionCode;
    public int transactionFlags;
}
