package ca.athabascau.sccori.client;

import static nextapp.echo2.app.Alignment.*;
import ca.athabascau.sccori.agent.AgentFactory;
import ca.athabascau.sccori.comm.Simulation;
import ca.athabascau.sccori.comm.TurnAdapter;
import ca.athabascau.sccori.data.TurnEvent;
import echopointng.PushButton;
import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.PasswordField;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.TextField;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.layout.GridLayoutData;
import nextapp.echo2.app.list.DefaultListModel;

/**
 * <code>
 * |--------------------------------------------|
 * |             |------------|                 |
 * | User name : |[TextField] |                 |
 * |             |------------|                 |
 * |             |------------|                 |
 * | Password  : |[Password]  |                 |
 * |             |------------|                 |
 * |             |--------------------|       | |
 * |             |Login : [PushButton]|       | |
 * |             |--------------------|       | |
 * |--------------------------------------------|
 * |                |-------------------------| |
 * | Meeting room : |           [SelectField] | |
 * |                |-------------------------| |
 * |--------------------------------------------|
 * |                |-------------------------| |
 * | Select Agent : |           [SelectField] | |
 * |                |-------------------------| |
 * |--------------------------------------------|
 * | Information about the game.                |
 * | i.e. turn played so far, current inv.      |
 * |                                            |
 * |--------------------------------------------|
 * </code>
 * 
 * <P>
 * The normal login stages are:
 * <UL>
 * <LI>User enters user name and password and select the Login button
 * <LI>Agent logs into the SCCORI site. Enable the meeting room field.
 * <LI>User selects a room.
 * </UL>
 */
public class AgentPanel extends Row {
    private static final Extent PX_150 = new Extent(150, Extent.PX);

    private Simulation theSimulation;

    /**
     * Check if a simulation is already created for this index
     */
    public AgentPanel() {
        final Grid theGrid = new Grid(6);
        add(theGrid);

        GridLayoutData theDefaultLabelLayoutData = new GridLayoutData();
        theDefaultLabelLayoutData.setAlignment(new Alignment(RIGHT, DEFAULT));

        // Add the user name label
        Label label = new Label("User Name:");
        label.setLayoutData(theDefaultLabelLayoutData);
        theGrid.add(label);
        // Add user name textfield
        final TextField theUserNameTextField = new TextField();        
        theUserNameTextField.setWidth(PX_150);
        theUserNameTextField.setFocusTraversalIndex(1);
        theGrid.add(theUserNameTextField);

        // Add the login button
        final PushButton theLoginButton = new PushButton("Login");
        theLoginButton.setFocusTraversalIndex(2);
        GridLayoutData theButtonLayoutData = new GridLayoutData();
        theButtonLayoutData.setAlignment(new Alignment(CENTER, DEFAULT));
        theButtonLayoutData.setRowSpan(2);
        theLoginButton.setLayoutData(theButtonLayoutData);
        theGrid.add(theLoginButton);

        // Add the Meeting room selection
        label = new Label("Select Meeting Room:");
        label.setLayoutData(theDefaultLabelLayoutData);
        theGrid.add(label);
        // Add the Meeting room select field
        final DefaultListModel theMeetingListModel = new DefaultListModel();
        final SelectField theMeetingSelectField = new SelectField(
                theMeetingListModel);
        theMeetingSelectField.setFocusTraversalIndex(1);
        theMeetingSelectField.setWidth(PX_150);
        theMeetingSelectField.setEnabled(false);
        theGrid.add(theMeetingSelectField);

        // Add the start button
        final PushButton theStartButton = new PushButton("Start Playing");
        theStartButton.setFocusTraversalIndex(2);
        theStartButton.setEnabled(false);
        theGrid.add(theStartButton);

        // Add the password label
        label = new Label("Password:");
        label.setLayoutData(theDefaultLabelLayoutData);
        theGrid.add(label);
        // Add the password textfield
        final PasswordField thePasswordField = new PasswordField();
        thePasswordField.setFocusTraversalIndex(1);
        thePasswordField.setWidth(PX_150);
        theGrid.add(thePasswordField);

        // Add the agent selection
        label = new Label("Select Agent:");
        label.setLayoutData(theDefaultLabelLayoutData);
        theGrid.add(label);

        // Agent Select Field
        final SelectField theAgentSelectField = new SelectField(AgentFactory
                .getAvailableAgents());
        theAgentSelectField.setFocusTraversalIndex(1);
        theAgentSelectField.setWidth(PX_150);
        theAgentSelectField.setEnabled(false);
        theGrid.add(theAgentSelectField);

        // Add the Kill button
        final PushButton theKillButton = new PushButton("Kill This Agent");
        theKillButton.setFocusTraversalIndex(2);
        theKillButton.setEnabled(false);
        theGrid.add(theKillButton);

        // Add login action
        ActionListener theActionListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    theSimulation = SimulationPool.getInstance()
                            .createSimulation(theUserNameTextField.getText(),
                                    thePasswordField.getText());
                    // If this gets here then it is a valid username/password
                    // AND there is enough room for this agent
                    // Disable the login buttons
                    theUserNameTextField.setEnabled(false);
                    thePasswordField.setEnabled(false);
                    theLoginButton.setEnabled(false);
                    theKillButton.setEnabled(true);

                    // Add the room selection
                    theMeetingListModel.removeAll();
                    for (String aMeeting : theSimulation.getAvailableMeeting()) {
                        theMeetingListModel.add(aMeeting);
                    }

                    theMeetingSelectField.setEnabled(true);
                    theAgentSelectField.setEnabled(true);

                    SimulationPool.getInstance().fireTableUpdate();
                } catch (Exception exp) {
                    exp.printStackTrace();
                }
            }
        };
        theLoginButton.addActionListener(theActionListener);
        theUserNameTextField.addActionListener(theActionListener);
        thePasswordField.addActionListener(theActionListener);

        // Add listeners to enable the Start playing button
        ActionListener theMeetingAndAgentListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // If both fields have been selected
                if (theMeetingSelectField.getSelectedIndex() != -1
                        && theAgentSelectField.getSelectedIndex() != -1) {
                    theStartButton.setEnabled(true);
                }
            }
        };

        theMeetingSelectField.addActionListener(theMeetingAndAgentListener);
        theAgentSelectField.addActionListener(theMeetingAndAgentListener);

        // Add logic to start the simulation
        theStartButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                // Get the agent, and set it to the Simulation
                try {
                    theSimulation.joinMeeting(theMeetingSelectField
                            .getSelectedItem().toString());

                    theMeetingSelectField.setEnabled(false);
                    theAgentSelectField.setEnabled(false);
                    theStartButton.setEnabled(false);
                    theKillButton.setEnabled(true);

                    theSimulation.setAgent(AgentFactory.getAgent(
                            theAgentSelectField.getSelectedItem().toString(),
                            theSimulation));
                    // Add a refresh to update the table whenever
                    // something happens
                    theSimulation.addMyTurnListener(new TurnAdapter() {
                        @Override
                        public void executeTurn(TurnEvent e) {
                            SimulationPool.getInstance().fireTableUpdate();
                        }
                        /**
                         * Simulation ending is the same as killing 
                         * the agent, so lets use the same logic.  
                         */
                        @Override
                        public void simulationHasEnded() {
                            theKillButton.doAction();
                        }
                    });

                    SimulationPool.getInstance().fireTableUpdate();
                } catch (Exception exp2) {
                    exp2.printStackTrace();
                }
            }
        });

        // Add logic to Kill the agent
        theKillButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                theSimulation.destroy();
                SimulationPool.getInstance().removeSimulation(theSimulation);
                theUserNameTextField.setText("");
                thePasswordField.setText("");

                // Reset all values
                theMeetingSelectField.setSelectedIndex(-1);
                theAgentSelectField.setSelectedIndex(-1);
                theUserNameTextField.setEnabled(true);
                thePasswordField.setEnabled(true);
                theLoginButton.setEnabled(true);
                theStartButton.setEnabled(false);
                theKillButton.setEnabled(false);

                SimulationPool.getInstance().fireTableUpdate();
            }
        });
    }
}