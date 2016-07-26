package ca.athabascau.sccori.client;

import javax.swing.ImageIcon;

import echopointng.ExpandableSection;
import echopointng.PushButton;
import echopointng.TabbedPane;
import echopointng.tabbedpane.DefaultTabModel;
import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.AwtImageReference;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.ContentPane;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.TaskQueueHandle;
import nextapp.echo2.app.Window;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.layout.ColumnLayoutData;
import nextapp.echo2.app.layout.GridLayoutData;

import static nextapp.echo2.app.Alignment.*;

/**
 * This is the main application window.
 * 
 * This is what this should look like <code>
 * --------------------------------------------------------------
 * | |------------------| |-------------------------|
 * | |SCCORI/Agent image| |Add new Agent : [Button] | 
 * | |------------------| |-------------------------|
 * | Current Agent: XX out of maximum YY.
 * |------------------------------------------------|
 * | | Agent1 | Agent2 |                            |
 * | |--------------------------------------------| |
 * | | Agent1 panel : [AgentPanel]                | |
 * | |                                            | |
 * | |                                            | |
 * | |                                            | |
 * | |                                            | |
 * | |--------------------------------------------| |
 * |------------------------------------------------|
 * </code>
 * 
 * @author franck
 * 
 */
public class ClientApplication extends ApplicationInstance {
    private final static String IMAGE_NAME = "icon.png";

    private Window theWindow = new Window();

    private ContentPane theMainPanel = new ContentPane();

    private DefaultTabModel theTabModel = new DefaultTabModel();

    private SystemPanel theSystemPanel = new SystemPanel();

    public ClientApplication() {
        AwtImageReference theImageRef = new AwtImageReference(new ImageIcon(
                getClass().getResource(IMAGE_NAME)).getImage());

        theWindow.setTitle("SCCORI Agents");
        theWindow.setContent(theMainPanel);

        SplitPane theSplitPane = new SplitPane(SplitPane.ORIENTATION_VERTICAL);
        theSplitPane.setSeparatorHeight(new Extent(10, Extent.PX));
        theSplitPane.setSeparatorPosition(new Extent(300, Extent.PX));
        theSplitPane.setResizable(true);
        theMainPanel.add(theSplitPane);

        final Column theColumn = new Column();
        theSplitPane.add(theColumn);

        // Add the image to the panel
        //Label theImageLabel = new Label(theImageRef);
        //theColumn.add(theImageLabel);

        //        
        ColumnLayoutData theSystemPanelLayout = new ColumnLayoutData();
        theSystemPanelLayout.setAlignment(new Alignment(Alignment.CENTER,
                Alignment.CENTER));
        theSystemPanelLayout.setHeight(theSplitPane.getSeparatorPosition());
        theSplitPane.setLayoutData(theSystemPanelLayout);
        theColumn.add(theSystemPanel);

        // Add the agent panel
        final ContentPane theAgentPanel = new ContentPane();
        theTabModel.addTab("Agents", theAgentPanel);
        final Grid theAgentPanelGrid = new Grid(1);
        theAgentPanel.add(theAgentPanelGrid);
        theSplitPane.add(theAgentPanelGrid);

        // Add the Tabbed Pane
        for (int i = 1; i <= 4; i++) {
            ExpandableSection theExpandableSection = new ExpandableSection();
            Grid theExpandableSectionGrid = new Grid(1);
            theExpandableSection.getTitleBar().setTitle("Agent " + i);
            theExpandableSection.add(theExpandableSectionGrid);
            theExpandableSection.getTitleBar().setExpanded(true);
            theExpandableSectionGrid.add(new AgentPanel());
            theAgentPanelGrid.add(theExpandableSection);
        }
    }

    public void updateTable() {
        theSystemPanel.refreshTable();
    }

    /**
     * This is required by ApplicationInstance. It is the Window that will be
     * displayed to the user.
     */
    public Window init() {
        return theWindow;
    }
}