package ui;

import dao.AppInfo;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.Collection;


public class ActiveAppView extends JPanel {

    protected Collection<AppInfo> activeApps;

    protected JTable apps;
    protected JScrollPane scrollPane;
    protected JPanel panel = new JPanel();

    public ActiveAppView(Collection<AppInfo> activeApps) {
        this.activeApps = activeApps;

        setLayout(new BorderLayout());
        apps = new JTable();
        apps.setModel(new DefaultTableModel(0, 3));
        TableColumnModel columnModel = apps.getTableHeader().getColumnModel();
        columnModel.getColumn(0).setHeaderValue("Package");
        columnModel.getColumn(1).setHeaderValue("Interface");
        columnModel.getColumn(2).setHeaderValue("Port");

        panel.setLayout(new BorderLayout());
        panel.add(apps.getTableHeader(), BorderLayout.PAGE_START);
        panel.add(apps, BorderLayout.CENTER);
        panel.add(apps);

        scrollPane = new JScrollPane(panel);
        scrollPane.setVerticalScrollBar(scrollPane.createVerticalScrollBar());
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setViewportView(panel);

        add(scrollPane);

    }

    public void rebuild() {
        DefaultTableModel model = (DefaultTableModel) apps.getModel();
        int rowCount = model.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            model.removeRow(0);
        }
        for (AppInfo appInfo : activeApps){
            for (String interfaceToken : appInfo.interfaceTokens){
                model.addRow(new Object[] {appInfo.packageName, interfaceToken, appInfo.restPort});
            }
        }
        revalidate();
        repaint();
    }


}
