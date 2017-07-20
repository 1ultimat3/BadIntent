package ui;

import com.google.gson.internal.LinkedTreeMap;
import dao.ParcelOperationDAO;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;
import java.awt.*;
import java.util.*;
import java.util.List;


public class ParcelPanel extends JScrollPane {

    public JPanel panel = new JPanel();
    protected JPanel details;

    protected List<ParcelOperationDAO> parcelOperations = new LinkedList<>();
    protected boolean hasBeenEdited = false;
    private JTable table;

    public ParcelPanel() {
        panel.setLayout(new BorderLayout());
        setVerticalScrollBar(createVerticalScrollBar());
        setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        add(panel);
        setViewportView(panel);
    }

    public void setParcelOperations(List<ParcelOperationDAO> parcelOperations) {
        this.parcelOperations = parcelOperations;
    }

    public void setDetails(JPanel details) {
        this.details = details;
    }

    public void rebuild() {
        panel.removeAll();
        Object[] columnNames = {"Operation", "Value"};
        Object[][] data = new Object[parcelOperations.size()][];
        for (int i = 0; i < parcelOperations.size(); i++){
            ParcelOperationDAO parcelOperationDAO = parcelOperations.get(i);
            data[i] = new Object[]{parcelOperationDAO.parcelType.toString(), parcelOperationDAO.value};
        }
        table = new JTable(data, columnNames);
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
            public void valueChanged(ListSelectionEvent event) {
                //make sure that JTable index matches parcelOperations index
                int selectedRow = table.getSelectedRow();
                String operation = (String) table.getValueAt(selectedRow, 0);
                Object value = parcelOperations.get(selectedRow).value;
                details.removeAll(); //clear view first
                if (operation.equals("BYTE_ARRAY") || operation.equals("BLOB")){
                    details.add(new ParcelByteArrDetails((LinkedTreeMap) value));
                } else {
                    details.removeAll();
                }
                details.revalidate();
                details.repaint();
            }
        });
        panel.add(table.getTableHeader(), BorderLayout.PAGE_START);
        panel.add(table, BorderLayout.CENTER);
        panel.add(table);
        table.getColumnModel().getColumn(0).setWidth(5);
        table.getColumnModel().getColumn(1).setWidth(95);
        revalidate();
        repaint();
    }

    public List<ParcelOperationDAO> retrievedParcelOperations(){
        hasBeenEdited = false;
        List<ParcelOperationDAO> ops = new LinkedList<>();
        TableModel model = table.getModel();
        if (model.getRowCount() != parcelOperations.size()) {
            hasBeenEdited = true;
        }
        for (int i = 0; i < model.getRowCount(); i++){
            String parcelOperation = (String) model.getValueAt(i, 0);
            Object parcelValue = model.getValueAt(i, 1);
            String originalOperation = parcelOperations.get(i).parcelType;
            Object originalValue = parcelOperations.get(i).value;
            if (parcelValue == null && originalValue == null) {
                ops.add(parcelOperations.get(i));
            } else if (parcelValue == null || originalValue == null) {
                hasBeenEdited = true;
                ops.add(new ParcelOperationDAO(parcelOperation, parcelValue));
            } else if (!originalOperation.equals(parcelOperation) || !originalValue.equals(parcelValue)) {
                hasBeenEdited = true;
                ops.add(new ParcelOperationDAO(parcelOperation, parcelValue));
            } else {
                ops.add(parcelOperations.get(i));
            }
        }
        return ops;
    }


    public boolean isModified() {
        return hasBeenEdited;
    }

    public byte[] getSelectedData() {
        int row = table.getSelectedRow();
        int column = table.getSelectedColumn();
        return table.getValueAt(row, column).toString().getBytes();
    }
}
