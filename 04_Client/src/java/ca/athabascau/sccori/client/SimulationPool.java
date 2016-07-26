package ca.athabascau.sccori.client;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import nextapp.echo2.app.TaskQueueHandle;

import ca.athabascau.sccori.comm.Simulation;

/**
 * This class provides the CLients a central point from which to interact with
 * the simulations.
 */
public class SimulationPool {
    public final static int MAX_AGENT_ALLOWED = 8;

    private static SimulationPool theSimulationPool = new SimulationPool();       

    private List<Simulation> theSimulations = new LinkedList<Simulation>();

    private Map<ClientApplication, TaskQueueHandle> theApplications = new HashMap<ClientApplication, TaskQueueHandle>();

    private Map<ClientApplication, Date> theApplicationCreation = new HashMap<ClientApplication, Date>();

    private SimulationPool() {
        new Thread(new Runnable() {
            public void run() {
                while(true) {
                    try {
                        Thread.sleep(60000);
                        fireTableUpdate();
                    }catch(InterruptedException e) {}                    
                }
            }
        }).start();
    }

    public synchronized int getSize() {
        return theSimulations.size();
    }

    public synchronized Simulation[] getSimulations() {
        return theSimulations.toArray(new Simulation[] {});
    }

    public synchronized Simulation createSimulation(String aUserName,
            String aPassowrd) throws Exception {
        if (theSimulations.size() < MAX_AGENT_ALLOWED) {
            Simulation aSimulation = new Simulation(aUserName, aPassowrd);
            theSimulations.add(aSimulation);
            return aSimulation;
        }
        throw new Exception("Sorry, too many agents");
    }

    public synchronized void removeSimulation(Simulation aSimulation) {
        theSimulations.remove(aSimulation);
        aSimulation.destroy();
    }

    public boolean isMax() {
        return MAX_AGENT_ALLOWED <= getSize();
    }

    public synchronized void addApplication(ClientApplication anApplicationInstance) {
        theApplications.put(anApplicationInstance, anApplicationInstance
                .createTaskQueue());
        theApplicationCreation.put(anApplicationInstance, new Date());
    }
     
    

    public void fireTableUpdate() {
        Date yesterday = new Date(System.currentTimeMillis() - (1000 * 3600 * 24)); 
        loop : for (final ClientApplication anApplication : theApplications.keySet()) {
            if(theApplicationCreation.get(anApplication).before(yesterday)) {
                theApplications.remove(anApplication);
                theApplicationCreation.remove(anApplication);
                continue loop;
            }
            
            anApplication.enqueueTask(theApplications.get(anApplication),
                    new Runnable() {
                        public void run() {
                            anApplication.updateTable();
                        }
                    });
        }
    }

    public static SimulationPool getInstance() {
        return theSimulationPool;
    }
}