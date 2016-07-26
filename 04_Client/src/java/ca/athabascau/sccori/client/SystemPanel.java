package ca.athabascau.sccori.client;

import java.util.LinkedList;
import java.util.TreeMap;

import ca.athabascau.sccori.comm.Simulation;
import ca.athabascau.sccori.data.SimulationInfo;
import nextapp.echo2.app.Border;
import nextapp.echo2.app.Color;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Table;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.layout.TableLayoutData;
import nextapp.echo2.app.table.DefaultTableModel;
import nextapp.echo2.app.table.TableCellRenderer;
import echopointng.PushButton;

public class SystemPanel extends Table {
    private static final String[] TABLE_COLUMN_NAME = { "User name  ",
            "Meeting name   ", "Agent Type        ", "Role        ", "Cycle",
            "Order Placed", "Unit Cost", "Total Cost", "Waiting for about", "" };

    private static final TreeMap<Long, String> WAIT_TIME_NAME = new TreeMap<Long, String>();

    private DefaultTableModel theTableModel;

    // Index the components so that we don't need to continuously
    // create them every time the table is refreshed
    private final LinkedList<Component[]> theTableComponents = new LinkedList<Component[]>();

    static {
        WAIT_TIME_NAME.put(1l, "a minute");
        WAIT_TIME_NAME.put(3l, "a few minutes");
        WAIT_TIME_NAME.put(20l, "ten minutes");
        WAIT_TIME_NAME.put(40l, "half an hour");
        WAIT_TIME_NAME.put(90l, "one hour");
        WAIT_TIME_NAME.put(3 * 60l, "two hours");
        WAIT_TIME_NAME.put(7 * 60l, "six hours");
        WAIT_TIME_NAME.put(12l * 60l, "half a day");
        WAIT_TIME_NAME.put(24l * 60l, "a day");
        WAIT_TIME_NAME.put(2l * 24l * 60, "a few days");
        WAIT_TIME_NAME.put(Long.MAX_VALUE, "way too long!");
    }

    {
        Color theBackgroundColor = new Color(238,238,238);
        boolean backgoundSwitch = true;
        for (int i = 0; i < SimulationPool.MAX_AGENT_ALLOWED; i++) {
            final int theIndex = i;
            PushButton thePushButton = new PushButton("RIP");
            thePushButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    SimulationPool.getInstance()
                            .removeSimulation(
                                    SimulationPool.getInstance()
                                            .getSimulations()[theIndex]);
                    SimulationPool.getInstance().fireTableUpdate();
                }
            });

            theTableComponents.add(new Component[] { new Label(), new Label(),
                    new Label(), new Label(), new Label(), new Label(),
                    new Label(), new Label(), new Label(), thePushButton });

            if (backgoundSwitch) {
                for (Component aComponent : theTableComponents.getLast()) {
                    TableLayoutData theLayoutData = new TableLayoutData();
                    if (aComponent instanceof Label) {
                        aComponent.setBackground(theBackgroundColor);
                    }
                    
                    theLayoutData.setBackground(aComponent.getBackground());
                    aComponent.setLayoutData(theLayoutData);
                }
            }

            backgoundSwitch = !backgoundSwitch;
        }
    }

    public SystemPanel() {
        super(TABLE_COLUMN_NAME.length, SimulationPool.MAX_AGENT_ALLOWED);
        theTableModel = (DefaultTableModel) getModel();

        setBorder(new Border(2, Color.LIGHTGRAY, Border.STYLE_GROOVE));

        guiInit();
    }

    public void refreshTable() {
        int row = 0;
        for (Simulation aSimulation : SimulationPool.getInstance()
                .getSimulations()) {
            // "User name",
            theTableModel.setValueAt(aSimulation.getUserName(), 0, row);

            // "Meeting name"
            theTableModel.setValueAt(aSimulation.getSelectedMeeting(), 1, row);

            if (aSimulation.getAgent() == null) {
                // "Agent Type",
                theTableModel.setValueAt("", 2, row);
            } else {
                theTableModel.setValueAt(aSimulation.getAgent().toString(), 2,
                        row);
            }

            // Only display the cycle number after the simulation has started
            if (aSimulation.getSimulationInfo().getCurrentCycleNumber() == 0) {
                // "Role",
                theTableModel.setValueAt("", 3, row);
                // "Cycle",
                theTableModel.setValueAt("", 4, row);
                // "Order Placed",
                theTableModel.setValueAt("", 5, row);
                // "Unit Cost",
                theTableModel.setValueAt("", 6, row);
                // "Total Cost",
                theTableModel.setValueAt("", 7, row);
            } else {
                switch (aSimulation.getSimulationInfo().getAgentRole()) {
                case SimulationInfo.RETAILER:
                    theTableModel.setValueAt("Retailer", 3, row);
                    break;
                case SimulationInfo.WHOLESALER:
                    theTableModel.setValueAt("Wholesaler", 3, row);
                    break;
                case SimulationInfo.DISTRIBUTOR:
                    theTableModel.setValueAt("Distributor", 3, row);
                    break;
                case SimulationInfo.MANUFACTURER:
                    theTableModel.setValueAt("Manufacturer", 3, row);
                    break;
                }
                theTableModel.setValueAt(aSimulation.getSimulationInfo()
                        .getCurrentCycleNumber(), 4, row);
                // "Order Placed",
                theTableModel.setValueAt(aSimulation.getSimulationInfo()
                        .getCurrentTurnEvent().getAmountToOrder(), 5, row);
                // "Unit Cost",
                theTableModel.setValueAt(aSimulation.getSimulationInfo()
                        .getMyCurrentTurnInfo().getUnitCost(), 6, row);
                // "Total Cost",
                theTableModel.setValueAt(aSimulation.getSimulationInfo()
                        .getMyCurrentTurnInfo().getTotalCost(), 7, row);

            }
            // "Wait time",
            long theNumberOfMinutesWaited = (System.currentTimeMillis() - aSimulation
                    .getWaitTime().getTime()) / 60000;
            for (Long aWaitTime : WAIT_TIME_NAME.keySet()) {
                if (theNumberOfMinutesWaited < aWaitTime) {
                    theTableModel.setValueAt(WAIT_TIME_NAME.get(aWaitTime), 8,
                            row);
                    break;
                }
            }

            // "" The RIP button -- Enable it if it has been 60 minutes
            if (theNumberOfMinutesWaited > 60) {
                theTableModel.setValueAt("enabled", 9, row);
            } else {
                theTableModel.setValueAt("disabled", 9, row);
            }

            row++;
        }

        // Fill the rest of the table with empty fields
        for (; row < SimulationPool.MAX_AGENT_ALLOWED; row++) {
            theTableModel.setValueAt("", 0, row);
            theTableModel.setValueAt("", 1, row);
            theTableModel.setValueAt("", 2, row);
            theTableModel.setValueAt("", 3, row);
            theTableModel.setValueAt("", 4, row);
            theTableModel.setValueAt("", 5, row);
            theTableModel.setValueAt("", 6, row);
            theTableModel.setValueAt("", 7, row);
            theTableModel.setValueAt("", 8, row);
            theTableModel.setValueAt("", 9, row);
        }
    }

    private void guiInit() {
        setHeaderVisible(true);

        theTableModel.setColumnCount(TABLE_COLUMN_NAME.length);
        for (int i = 0; i < TABLE_COLUMN_NAME.length; i++) {
            theTableModel.setColumnName(i, TABLE_COLUMN_NAME[i]);
        }
        setWidth(new Extent(800, Extent.PX));

        setDefaultRenderer(Object.class, new TableCellRenderer() {
            public Component getTableCellRendererComponent(Table table,
                    Object value, int column, int row) {
                // Get the component from the index
                Component theComponent = theTableComponents.get(row)[column];

                String theValue;
                if (value == null) {
                    theValue = "";
                } else {
                    theValue = value.toString();
                }

                if (theComponent instanceof Label) {
                    ((Label) theComponent).setText(theValue);
                }

                if (theComponent instanceof PushButton) {
                    if (theValue.equals("") || !hasBeenWaitingTooLong(theValue)) {
                        ((PushButton) theComponent).setEnabled(false);
                    } else {
                        ((PushButton) theComponent).setEnabled(true);
                    }
                }

                return theComponent;
            }
        });
    }

    private boolean hasBeenWaitingTooLong(String aTimeValue) {
        // Been waiting too long if it has been waiting for more then one hour
        return aTimeValue.equals("enabled");
    }
}