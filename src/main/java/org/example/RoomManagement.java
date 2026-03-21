package org.example;

//m, M

import org.intellij.lang.annotations.Flow;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.io.File;

public class RoomManagement extends JFrame {

    private final DatabaseManager db;
    private DefaultTableModel tableModel;
    private JTable roomTable;
    private JLabel statusLabel;

    private static final  String[] COLUMNS ={"ID","Room Name", "Capacity"};

    public RoomManagement(DatabaseManager db){
        this.db = db;
        buildUI();
        refreshTable();
    }

    private void buildUI(){
        setTitle("Room Management");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800,600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10,10));
        tableModel = new DefaultTableModel(COLUMNS,0){
            @Override
            public boolean isCellEditable(int r, int c){
                return false;
            }
        };
        roomTable = new JTable(tableModel);
        roomTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        roomTable.getColumnModel().getColumn(0).setMaxWidth(40);
        add(new JScrollPane(roomTable), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JButton addBtn = new JButton("Add RooM");
        JButton removeBtn = new JButton("Remove room");
        JButton capacityBtn = new JButton("Change capacity");
        JButton refreshBtn = new JButton("Refresh");

        addBtn.addActionListener(e -> onAddRoom());
        removeBtn.addActionListener(e -> onRemoveRoom());
        capacityBtn.addActionListener(e -> onChangeCapacity());
        refreshBtn.addActionListener(e -> refreshTable());

        buttons.add(addBtn);
        buttons.add(removeBtn);
        buttons.add(capacityBtn);
        buttons.add(refreshBtn);
        add(buttons,BorderLayout.SOUTH);

        statusLabel = new JLabel("ready");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(4,8,4,8));
        add(statusLabel, BorderLayout.NORTH);
    }

    private void onAddRoom(){
        JTextField nameField = new JTextField(20);
        JTextField capacityField = new JTextField(5);
        JPanel panel = new JPanel(new GridLayout(2,2,5,5));
        panel.add(new JLabel("Room name:"));
        panel.add(nameField);
        panel.add(new JLabel("capacity"));
        panel.add(capacityField);

        int result = JOptionPane.showConfirmDialog(this,panel,
                "Add Room", JOptionPane.OK_CANCEL_OPTION);
        if(result!= JOptionPane.OK_OPTION) return;
        String name = nameField.getText().trim();
        String capText = capacityField.getText().trim();
        if(name.isEmpty()){
            showError("Room name is empty");
            return;
        }

        int capacity;
        try{
            capacity = Integer.parseInt(capText);
            if(capacity <= 0 ) throw new NumberFormatException();
        }
        catch (NumberFormatException ex){
            showError("Capacity is negative");
            return;
        }
        try {
            db.addRoom(name,capacity);
            refreshTable();
            setStatus("Added room: " + name);
        }
        catch (Exception ex){
            showError("failed to aadd room:"+ex.getMessage());
        }
    }


    private void onRemoveRoom(){
        int row = roomTable.getSelectedRow();
        if(row<0){
            showError("you need to select a room to remove");
            return;
        }
        int roomId = (int) tableModel.getValueAt(row,0);
        String name = (String) tableModel.getValueAt(row,1);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Remove room'" + name +"' and ALL its reservation?",
                "confirm remove",JOptionPane.WARNING_MESSAGE);
        if(confirm!= JOptionPane.YES_OPTION) return;

        try{
            List<String> cancelled = db.removeRoom(roomId);
            JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new File("cancellation_report.txt"));
            int save = fc.showSaveDialog(this);
            if(save == JFileChooser.APPROVE_OPTION){
                CancellationReport.save(
                        fc.getSelectedFile().getAbsolutePath(), name,cancelled);
                        setStatus("Removed"+ name + "report saved to " +
                                fc.getSelectedFile().getName());
            }
            else {
                setStatus("Removed" + name + "report not saved");
            }
            if(cancelled.isEmpty()){
                JOptionPane.showMessageDialog(this,"Room removed");
            }
            else {
                String users = String.join(",",cancelled);
                JOptionPane.showMessageDialog(this,"room removed and cancelled room" +
                        "reservation for:\n"+users,"cancellations",JOptionPane.INFORMATION_MESSAGE);
            }
            refreshTable();
        }
        catch (Exception ex){
            showError("Failed to remove room"+ ex.getMessage());
        }
    }

    private void onChangeCapacity(){
        int row = roomTable.getSelectedRow();
        if(row<0){
            showError("Please select which room");
            return;
        }

        int roomId = (int) tableModel.getValueAt(row,0);
        String roomName = (String) tableModel.getValueAt(row,1);
        int current = (int) tableModel.getValueAt(row,2);
        String input = JOptionPane.showInputDialog(this,
                "new capacity for"+ roomName + " (current: "+ current + "):" );
        if(input == null || input.isBlank()) return;

        int newCapacity;
        try {
            newCapacity = Integer.parseInt(input.trim());
            if(newCapacity <=0 ) throw new NumberFormatException();
        }
        catch (NumberFormatException ex){
            showError("Capacity is negative");
            return;
        }
        try {
            db.updateCapacity(roomId, newCapacity);
            refreshTable();
            setStatus("Updated Capacity of " + roomName + "to" + newCapacity);
        }
        catch (Exception ex){
            showError("Failed to update capacity "+ ex.getMessage());
        }
    }

    private void refreshTable(){
        tableModel.setRowCount(0);
        try {
            for (DatabaseManager.Room room : db.getAllRooms()) {
                tableModel.addRow(new Object[]{
                        room.id(), room.name(), room.capacity()
                });
            }
            setStatus("Loaded" + tableModel.getRowCount() + "rooms");
        }
        catch (Exception ex){
            showError("failed to load rooms"+ex.getMessage());
        }
    }

    private void showError(String mssg){
        JOptionPane.showMessageDialog(this,mssg,"error", JOptionPane.ERROR_MESSAGE);
    }

    private void setStatus(String mssg){
        statusLabel.setText(mssg);
    }


    public static void main(String[] args){
        String dbPath = System.getenv("DbPath");
        if(dbPath == null || dbPath.isBlank()){
            JOptionPane.showMessageDialog(null,
                    "DbPath env varible is not set" +
                            "set it please", "missing stuff",JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        SwingUtilities.invokeLater(() ->{
            DatabaseManager db = new DatabaseManager(dbPath);
            new RoomManagement(db).setVisible(true);
        });

    }
}
