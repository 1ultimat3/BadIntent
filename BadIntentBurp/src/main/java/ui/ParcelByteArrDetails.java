package ui;


import com.google.gson.internal.LinkedTreeMap;

import javax.swing.*;
import javax.xml.bind.DatatypeConverter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 * represents details view with respect to byte array parcel values
 */
public class ParcelByteArrDetails extends JPanel {

    public JScrollPane scrollable;
    public JTextArea byteArray = new JTextArea();
    public JTextField offset = new JTextField();
    public JLabel offsetLabel = new JLabel("offset");
    public JTextField length = new JTextField();
    public JLabel lengthLabel = new JLabel("length");
    public JButton updateButton = new JButton("Update");
    public JPanel inner;

    public ParcelByteArrDetails(LinkedTreeMap map) {
        setLayout(new GridBagLayout());

        setOffset(((Double) map.get("offset")).intValue());
        setLength(((Double) map.get("len")).intValue());
        setByteArray((ArrayList) map.get("array"));
        scrollable = new JScrollPane(byteArray);
        scrollable.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollable.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        byteArray.setLineWrap(true);
        byteArray.setWrapStyleWord(true);

        inner = new JPanel();
        inner.setLayout(new BorderLayout());
        inner.add(scrollable);

        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println(getByteArrayList().toString());
                map.put("offset", getOffset());
                map.put("len", getLength());
                map.put("array", getByteArrayList());
            }
        });

        //building GUI
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        add(offsetLabel, c);
        c.gridx = 1;
        add(offset, c);
        c.gridx = 3;
        add(lengthLabel);
        c.gridx = 4;
        add(length, c);
        c.gridx = 6;
        c.anchor = GridBagConstraints.LINE_END;
        add(updateButton, c);
        c.anchor = GridBagConstraints.LINE_START;
        c.gridy = 1;
        c.gridx = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 7;
        c.gridheight = 3;
        c.fill = GridBagConstraints.BOTH;
        add(inner, c);
    }

    public void setOffset(int offset) {
        this.offset.setText(Integer.toString(offset));
    }

    public double getOffset(){
        return Double.parseDouble(this.offset.getText());
    }

    public void setLength(int length) {
        this.length.setText(Integer.toString(length));
    }

    public double getLength(){
        return Double.parseDouble(this.length.getText());
    }

    public void setByteArray(ArrayList byteArray) {
        StringBuilder builder = new StringBuilder();
        if (byteArray != null) {
            for (Object element : byteArray){
                builder.append(String.format("%02X ", ((Double) element).byteValue()));
                builder.append(" ");
            }
        }
        this.byteArray.setText(builder.toString());
    }

    public ArrayList getByteArrayList(){
        String hexStr = this.byteArray.getText().replace(" ", "");
        byte[] bytes = DatatypeConverter.parseHexBinary(hexStr);
        ArrayList doubleArrayList = new ArrayList();
        for (byte byteVal : bytes){
            doubleArrayList.add((double) byteVal);
        }
        return doubleArrayList;
    }
}
