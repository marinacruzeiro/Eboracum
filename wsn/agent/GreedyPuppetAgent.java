package eboracum.wsn.agent;


import eboracum.wsn.agent.central.GreedyCentralizedLoadBalancer;
import eboracum.wsn.network.node.WirelessNode;
import eboracum.wsn.network.node.sensor.ControlledWSNNode;
import eboracum.wsn.network.node.sensor.mobile.ControlledDRMobileWSNNode;

public class GreedyPuppetAgent implements BasicAgent{

	public GreedyCentralizedLoadBalancer masterOfPuppets;
	public WirelessNode myNode;	

	public boolean eventSensed(String tempEvent){
		try {
			//Esse system.out.println tava comentado:
		        //System.out.println(myNode.getDirector());
		        // esse eu botei:
		        //System.out.println(tempEvent);
		        //esse eu também botei:
		        //System.out.println(myNode.getDirector().getModelTime());
			this.masterOfPuppets.eventSensed(tempEvent, myNode.getDirector().getModelTime(), this);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public void setNode(ControlledWSNNode myNode){
		this.myNode = myNode;
	}
	
	public void setNode(ControlledDRMobileWSNNode myNode) {
		this.myNode = myNode;
	}
}
