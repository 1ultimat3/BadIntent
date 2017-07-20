package dao;

public class ParcelOperationDAO {

    public String parcelType;
    public Object value;

    public ParcelOperationDAO(String parcelOperation, Object parcelValue) {
        this.parcelType = parcelOperation;
        this.value = parcelValue;
    }
}
