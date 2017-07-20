package ui;

import javax.swing.*;
import java.awt.*;


public class ParcelTab extends JPanel {

    protected JSplitPane splitPane;
    //carries table (op : value)
    protected ParcelPanel overview;
    //carries details of various entries (e.g. byte array)
    protected JPanel details = new JPanel();

    public ParcelTab(ParcelPanel overview) {
        this.overview = overview;
        overview.setDetails(details);
        setLayout(new BorderLayout());
        details.setLayout(new BorderLayout());
        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, overview, details);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(250);
        add(splitPane);
    }

    public byte[] getSelectedData() {
        return overview.getSelectedData();
    }
}
