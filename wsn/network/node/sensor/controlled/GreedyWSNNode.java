package eboracum.wsn.network.node.sensor.controlled;

import ptolemy.actor.NoTokenException;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import eboracum.wsn.agent.GreedyPuppetAgent;
import eboracum.wsn.network.node.sensor.ControlledWSNNode;
//import eboracum.wsn.agent.central.GreedyCentralizedLoadBalancer; //botei

public class GreedyWSNNode extends ControlledWSNNode{

	private static final long serialVersionUID = 1L;
	public String definedByMasterEvent;

	public GreedyWSNNode(CompositeEntity container, String name)
			throws IllegalActionException, NameDuplicationException {
		super(container, name);
		/*this.myAgent = new GreedyPuppetAgent();
		this.myAgent.setNode(this);
		this.definedByMasterEvent = null;*/
	}
	
	public void initialize() throws IllegalActionException {
		super.initialize();
                
		this.myAgent = new GreedyPuppetAgent();
                this.myAgent.setNode(this);
		
		//this.definedByMasterEvent = null;
	}
	
	protected void sensorNodeAction() throws NoTokenException, IllegalActionException{
	        // Esse print eu que botei:
                System.out.println(this.definedByMasterEvent);
	        if (this.definedByMasterEvent != null) {
			if (!this.definedByMasterEvent.equals("VOID")){
				this.controledSensingManager();
				//Esses prints j? estavam:
				System.out.println(this+"|"+this.tempEvent);
				System.out.println(this.timeControler+"|"+this.getDirector().getModelTime());
			}
			this.definedByMasterEvent = null;
		}
		else {	
		    
			this.sensingManager();
		       
		}
		this.cpuRunManager();
	}
	
	private void controledSensingManager() throws NoTokenException, IllegalActionException{
		this.tempEvent = this.definedByMasterEvent;
		if (!this.tempLastSensingEventTime.equals(this.getDirector().getModelTime())){
    		this.tempLastSensingEventTime = this.getDirector().getModelTime();
    		this.sensedEvents.setExpression(tempEvent.split("_")[0]);
    	}
    	else this.sensedEvents.setExpression(sensedEvents.getExpression()+tempEvent.split("_")[0]);
	}

	protected boolean eventSensedManager(String tempEvent) throws NoTokenException, IllegalActionException{
   		//Esses dois prints eu botei:
	        //System.out.println(tempEvent);
	        this.myAgent.eventSensed(tempEvent);
	        System.out.println(this.myAgent.eventSensed(tempEvent));
   		//return false;
	        return true;
   	}
	
}
