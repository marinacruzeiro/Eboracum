package eboracum.wsn.network.node.sensor;

import java.util.List;


//import eboracum.wsn.network.node.sensor.cpu.SimpleFIFOBasedCPU;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.NoTokenException;
import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.domains.wireless.kernel.WirelessIOPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
//import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
//import ptolemy.vergil.kernel.attributes.AttributeValueAttribute;
import ptolemy.vergil.kernel.attributes.EllipseAttribute;
import ptolemy.actor.util.Time;

//------------------------------------------------------------------------------------------------

public class WakeUpWSNNode extends  SimpleWSNNode {

    private static final long serialVersionUID = 1L;
    
    // Para as portas de comunicacao do radio:
    protected WirelessIOPort outWU;
    protected WirelessIOPort inWU;
    
    // Para WuR:
    public Parameter sleepCost;
    public Parameter wakeUpSignalCost;
    public Parameter wusProcessingCost;
    
    // Para ACK:
    protected WirelessIOPort outACK;
    protected WirelessIOPort inACK;
    public String receivedACKSignal;
    
    // Para o icone:
    protected EllipseAttribute _circle_wurRadius; 
    public Parameter wurCoverRadius;
    
    public String receivedWUSignal;
    
    protected boolean sleepFlag;
    
    // Para estatisticas:
    public int numberOfReceivedWakeUpMessages;
    public int numberOfSentWakeUpMessages;
    public int numberOfReceivedACKMessages;
    
    // Para testes:
    public String nome;
    public int contador;
    public String evento;
    //--
    
//------------------------------------------------------------------------------------------------
    
    public WakeUpWSNNode(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
        super(container, name);

        sleepCost = new Parameter(this, "SleepCost");
        sleepCost.setExpression("1");
        wakeUpSignalCost = new Parameter(this, "WakeUpSignalCost");
        wakeUpSignalCost.setExpression("1");
        wusProcessingCost = new Parameter(this, "WuSProcessingCost");
        wusProcessingCost.setExpression("1");
        
        // Cria um canal de comunicação só para o envio de sinal de wake up:
        StringParameter wuSignalChannelName = new StringParameter(this,"WUSignalChannelName");
        wuSignalChannelName.setExpression("PowerLossChannel3");
        outWU = new WirelessIOPort(this, "output3", false, true);
        outWU.outsideChannel.setExpression("$WUSignalChannelName");
        inWU = new WirelessIOPort(this, "input3", true, false);
        inWU.outsideChannel.setExpression("$WUSignalChannelName");
        
        // Teste para ACK:
        StringParameter ACKChannelName = new StringParameter(this,"ACKSignalChannelName");
        ACKChannelName.setExpression("PowerLossChannel4");
        outACK = new WirelessIOPort(this, "output4", false, true);
        outACK.outsideChannel.setExpression("$ACKSignalChannelName");
        inACK = new WirelessIOPort(this, "input4", true, false);
        inACK.outsideChannel.setExpression("$ACKSignalChannelName");
        
        //--
        wurCoverRadius = new Parameter(this,"WuRCoverRadius");
        wurCoverRadius.setExpression("WuRCover");        
        
        // Para conectar o raio com o valor dado no PaperSimulation: 
        SingletonParameter hide = new SingletonParameter(wurCoverRadius, "_hide");
        hide.setToken(BooleanToken.TRUE);
        hide.setVisibility(Settable.EXPERT);
        try {
            (new SingletonParameter(outWU, "_hide")).setToken(BooleanToken.TRUE);
            (new SingletonParameter(inWU, "_hide")).setToken(BooleanToken.TRUE);
            // Para ACK:
            (new SingletonParameter(outACK, "_hide")).setToken(BooleanToken.TRUE);
            (new SingletonParameter(inACK, "_hide")).setToken(BooleanToken.TRUE);
            } catch (NameDuplicationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }

//------------------------------------------------------------------------------------------------
    
    public void initialize() throws IllegalActionException {
        super.initialize();
        
        sleepFlag = true;
        
        numberOfReceivedWakeUpMessages = 0;
        numberOfSentWakeUpMessages = 0;
        
        _circle_wurRadius.fillColor.setToken("{0.0, 0.0, 0.5, 0.05}");
        _circle_wurRadius.lineColor.setToken("{0.0, 0.0, 0.0, 0.1}");
        _circle_wurRadius.width.setToken(Double.toString(Double.parseDouble(wurCoverRadius.getValueAsString())*2));
        _circle_wurRadius.height.setToken(Double.toString(Double.parseDouble(wurCoverRadius.getValueAsString())*2));
        
    }      

//------------------------------------------------------------------------------------------------
    
    public void fire() throws NoTokenException, IllegalActionException {        
        super.fire(); 
        
        /*
         * If there is a token representing an acknowledge message in the node's wake up channel
         * then it will verify if it's for itself. If it is, the node will send
         * the message with the event and increase the statistics number.
         */
        //ackChannelManager();
        
        if (inACK.hasToken(0)) {
            this.receivedACKSignal = this.inACK.get(0).toString();
//            System.out.println(this.getName() + " recebeu sinal de ACK: " + this.receivedACKSignal + " em " + this.getDirector().getModelTime());
            String ACKMessage = this.receivedACKSignal.substring(6, this.receivedACKSignal.length()-1);//this.receivedACKSignal.split("=")[1];
//            System.out.println(ACKMessage);
            if (ACKMessage.equals(this.getName())) {
//                System.out.println("Evento no fire ACK: " + this.evento);
                if (this.sendMessageToSink(this.evento)) {
                    this.numberOfReceivedACKMessages++;
                }
            }
        }
        
        
        /*
         * If there is a token representing an event message in the node's main channel
         * then it will use the receiveMessage() method to verify if it is for itself,
         * receive the message and then send it to another node through sendManager() method.  
         */       
        //mainChannelManager();
        
        if (in.hasToken(0)) {
//            System.out.println(this.getName() + " entrou em in.hasToken() em " + this.getDirector().getModelTime());
            if (this.receiveMessage(this.in.get(0).toString())) {
                
                if (this.sendManager(this.receivedMessage)) {

                }
            } 
        }
        
        
    }
    
//------------------------------------------------------------------------------------------------
    
    public boolean postfire() throws IllegalActionException{
        
        this.receivedWUSignal = null;
        this.receivedACKSignal = null;
        
        Boolean flag = super.postfire();
        if (!flag){
            _circle_wurRadius.fillColor.setToken("{0.0, 0.0, 0.5, 0.0}");
            _circle_wurRadius.lineColor.setToken("{1.0, 1.0, 1.0, 0.0}");
            return false;
        }
        else return flag;     

    }

//------------------------------------------------------------------------------------------------
  
    protected void eventDoneManager(List<Object> runReturn) throws NoRoomException, IllegalActionException{
//        System.out.println(runReturn);
        // verify if the event must be send through the network (not ordinary)
        
        if (!this.eventOrdinaryMap.get(((String)runReturn.get(1)).split("_")[0])) {       
           
           this.evento = ((String)runReturn.get(1));
//           System.out.println("Evento no eventDoneManager: " + this.evento);
           sendManager(((String)runReturn.get(1)));
           
           this.numberOfSensoredEvents++; // collect the event for statistics //?
        }

    }
    
//------------------------------------------------------------------------------------------------
    
    protected boolean sendManager(String token) throws NoRoomException, IllegalActionException {
        
        this.sleepFlag = false;
        // Para ACK:
        if (this.sendMessageToNeighbours(token, this.getName(), Double.parseDouble(this.wakeUpSignalCost.getValueAsString()))) {
            return true;
        } else {
            return false;
        }
        
    }
    
//------------------------------------------------------------------------------------------------
/*    
    public void ackChannelManager() throws NumberFormatException, IllegalActionException {
        
        if (inACK.hasToken(0)) {
            this.receivedACKSignal = this.inACK.get(0).toString();
//            System.out.println(this.getName() + " recebeu sinal de ACK: " + this.receivedACKSignal + " em " + this.getDirector().getModelTime());
            String ACKMessage = this.receivedACKSignal.substring(6, this.receivedACKSignal.length()-1);//this.receivedACKSignal.split("=")[1];
//            System.out.println(ACKMessage);
            if (ACKMessage.equals(this.getName())) {
//                System.out.println("Evento no fire ACK: " + this.evento);
                if (this.sendMessageToSink(this.evento)) {
                    this.numberOfReceivedACKMessages++;
                }
            }
        }
        
    }
    
    
//------------------------------------------------------------------------------------------------    
    
    public void mainChannelManager() throws NumberFormatException, IllegalActionException {
        
        if (in.hasToken(0)) {
//          System.out.println(this.getName() + " entrou em in.hasToken() em " + this.getDirector().getModelTime());
          if (this.receiveMessage(this.in.get(0).toString())) {
              
              if (this.sendManager(this.receivedMessage)) {

              }
          } 
      }
        
    }*/
    
//------------------------------------------------------------------------------------------------
    
    public boolean isAwake() throws NumberFormatException, IllegalActionException {
       
       if (!this.sleepFlag) {
           // if sleepFlag is FALSE, the node is awake
           return true;
           
       } else {
           // if sleepFlag is TRUE, the node is sleeping
           return false;
       }
       
   }
       
//------------------------------------------------------------------------------------------------
    
    public void batteryDischarge() throws NumberFormatException, IllegalActionException {
        // Descarrega o custo de sleep ao inves de idle: 
        if (!this.isAwake()) {
            if (!this.timeControler.equals(this.getDirector().getModelTime())){
                //sincronizado com o tempo real
                if (this.synchronizedRealTime.getExpression().equals("true") && 
                                Double.parseDouble(battery.getValueAsString()) >= Double.parseDouble(this.sleepCost.getValueAsString())) { 
                    battery.setExpression(Double.toString((Double.parseDouble(battery.getValueAsString()) - Double.parseDouble(this.sleepCost.getValueAsString()))));
                } else { 
                // assíncrono
//                    System.out.println("Nodo " + this.getName() + " timeController: " + this.timeControler + " model time:" + this.getDirector().getModelTime());
//                   System.out.println("Nodo " + this.getName() + " bateria antes do sleep: " + battery.getValueAsString());
                    battery.setExpression(Double.toString((Double.parseDouble(battery.getValueAsString())-
                                            (Double.parseDouble(this.sleepCost.getValueAsString())*this.getDirector().getModelTime().subtract(this.newTimeControler).getDoubleValue() )   )));
                    
                    if ((Double.parseDouble(battery.getValueAsString())/Double.parseDouble(this.sleepCost.getValueAsString())) > 0) {
                        //this.timeOfDeath = (this.getDirector().getModelTime().add(((Double.parseDouble(battery.getValueAsString())/Double.parseDouble(this.sleepCost.getValueAsString())))));
                    }
//                    System.out.println("Nodo " + this.getName() + " bateria depois do sleep: " + battery.getValueAsString());
                }
            }   
        } else {
            this.idleDischarge();
        }
    }
    
//------------------------------------------------------------------------------------------------    
    
    public void messageManager() throws NumberFormatException, IllegalActionException {
        
//        System.out.println(this.getName() + " entrou no messageManager()");
        
        /*
         * In this class, messageManager() sees if there is a token in the node's
         * wake up channel and if so, it will call the handshakeWakeUp() method to
         * verify if this signal is for itself. If it is, this other method will send
         * an ACK signal for the sender. It also verifies if there's battery enough.  
         */
        
        if (inWU.hasToken(0)) {
//            System.out.println(this.getName() + " entrou em inWU.hasToken()");
            
            if (this.handshakeWakeUp(this.inWU.get(0).toString()) && (Double.parseDouble(battery.getValueAsString()) >= ((Double.parseDouble(this.sleepCost.getValueAsString())) + Double.parseDouble(this.wakeUpSignalCost.getValueAsString()) + Double.parseDouble(this.wusProcessingCost.getValueAsString()) + Double.parseDouble(this.idleEnergyCost.getExpression())) )) {
//                System.out.println("Fez handshake");                

            } else {
//                System.out.println("Não fez handshake e voltou");
            }
            
        } else { // Esse print pode sair eventualmente
//            System.out.println(this.getName() + " não possui token em inWU");
        }
        
   }

//------------------------------------------------------------------------------------------------
    
    public void idleDischarge() throws NumberFormatException, IllegalActionException{
        
//        System.out.println("Entrou no idleDischarge()");
        
            // assíncrono
//            System.out.println("Nodo " + this.getName() + " bateria antes do idle: " + battery.getValueAsString());
            battery.setExpression(Double.toString((Double.parseDouble(battery.getValueAsString())-
                        ((Double.parseDouble(this.idleEnergyCost.getValueAsString()))  ))));
            if ((((Double.parseDouble(battery.getValueAsString())/Double.parseDouble(idleEnergyCost.getExpression()))) > 0))
                this.timeOfDeath = (this.getDirector().getModelTime().add(((Double.parseDouble(battery.getValueAsString())/Double.parseDouble(idleEnergyCost.getExpression())))));
//            System.out.println("Nodo " + this.getName() + " bateria depois do idle: " + battery.getValueAsString());
        
    }   

//------------------------------------------------------------------------------------------------
    
    public boolean receiveMessage(String tempMessage) throws NumberFormatException, IllegalActionException{
        
        tempMessage = tempMessage.substring(2, tempMessage.length()-2);
        
            if (this.getName().equals(tempMessage.split(",")[1].split("=")[1])){
                
                this.numberOfReceivedMessages++;
                
                this.receivedMessage = tempMessage.split(",")[0].split("=")[1];
                
                this.evento = tempMessage.split(",")[0].split("=")[1];
//                System.out.println(this.getName() + " evento no receiveMessage " + this.evento);
                
//                System.out.println(this.getName() + " recebeu mensagem ");
                
                return true;
            } else {
//                System.out.println("Não era para " + this.getName());
                return false;
            }
            
        //}
    }

//------------------------------------------------------------------------------------------------
    
    public boolean handshakeWakeUp(String tempMessage) throws NumberFormatException, IllegalActionException{
        
        /*
         * This method verifies if the Wake up Signal is for the node that heard it. 
         * Firstly it verifies if there's battery enough for processing the address
         * and removes the value it needs to do so.
         * Then, if the address is correct, it sends an acknowledge signal for 
         * the sender node.
         */
        
        
        tempMessage = tempMessage.substring(2, tempMessage.length()-2);
//        System.out.println("No handshake tempMessage: "+ tempMessage);
        
        String corte = tempMessage;
        String nodeName = corte.split("=")[3];
        
//        System.out.println("no handshake sender: "+ nodeName);
        
        this.numberOfReceivedWakeUpMessages++;
        
        // Descarga do valor de processamento para ver se este e o nodo requerido
        if ((Double.parseDouble(battery.getValueAsString()) >= Double.parseDouble(this.wusProcessingCost.getValueAsString())) ){
//            System.out.println("Nodo " + this.getName() + " bateria antes do handshake: " + battery.getValueAsString());
            battery.setExpression(Double.toString( ( Double.parseDouble(battery.getValueAsString()) - Double.parseDouble(this.wusProcessingCost.getValueAsString()) )));
            //this.timeOfDeath = (this.getDirector().getModelTime().add(((Double.parseDouble(battery.getValueAsString())/(Double.parseDouble(this.wusProcessingCost.getValueAsString() ))))));//(this.sleepCost)))));
//            System.out.println("Nodo " + this.getName() + " bateria depois do handshake: " + battery.getValueAsString());
            
            // Processa o handshake
            if (this.getName().equals(tempMessage.split(",")[1].split("=")[1])){
                
//                System.out.println(this.getName() + " fez handshake");
                
                // Envia ACK:
//                System.out.println("Nodo " + this.getName() + " bateria antes do envio de ACK: " + battery.getValueAsString());
                battery.setExpression(Double.toString( ( Double.parseDouble(battery.getValueAsString()) - Double.parseDouble(this.wakeUpSignalCost.getValueAsString()) )));
                //this.timeOfDeath = (this.getDirector().getModelTime().add(((Double.parseDouble(battery.getValueAsString())/(Double.parseDouble(this.wakeUpSignalCost.getValueAsString() ))))));//(this.sleepCost)))));
//                System.out.println("Nodo " + this.getName() + " bateria depois do envio de ACK: " + battery.getValueAsString());
                
                outACK.send(0, new StringToken("{ACK=" + nodeName));                
//                System.out.println(this.getName() + " mandou mensagem de ACK para " + nodeName);
                
                this.sleepFlag = false;
                
                return true;
                    
            } else {
//                System.out.println("Não fez handshake " + this.getName());
                if (in.hasToken(0)) {
                    this.receivedWUSignal = this.in.get(0).toString();
                }
                return false;
            }
            
        } else {
            return false;
        }
    }

//------------------------------------------------------------------------------------------------    
    
    public boolean sendMessageToNeighbours(String token, String nodeName, double neighboursCommCost) throws NoRoomException, IllegalActionException{
        
        /*
         * In this method, firstly it verifies if the message is for the sink.
         * If it is, then it will send directly to sendMessageToSink().
         * If not,  it will verify if there's battery enough to send an Wake Up Signal
         * to wake its neighbour and then send it. 
         */
        
        double wuSignalCost = neighboursCommCost;
        //double commCost = this.eventCommCostMap.get(token.split("_")[0]);
        /*if ((!gateway.getExpression().equals("")&&!gateway.getExpression().equals("END")) && (Double.parseDouble(battery.getValueAsString()) >= commCost)){
            battery.setExpression(Double.toString( ( Double.parseDouble(battery.getValueAsString()) - commCost )));
            if (this.synchronizedRealTime.getExpression().equals("false"))
                    this.timeOfDeath = (this.getDirector().getModelTime().add(((Double.parseDouble(battery.getValueAsString())/Double.parseDouble(idleEnergyCost.getExpression())))));
          */
        
        if (gateway.getExpression().equals("NetworkMainGateway")) {
            
            if (this.isAwake()) {
                this.sleepFlag = true;
                //idleDischarge();
            }
            sendMessageToSink(token);
//            System.out.println("Enviou direto para o NetworkMainGateway");
            return true;
            
        } else {       
        
            if ((Double.parseDouble(battery.getValueAsString()) >= wuSignalCost)){
//                System.out.println("Nodo " + this.getName() + " bateria antes de enviar mensagem WU: " + battery.getValueAsString());
                battery.setExpression(Double.toString( ( Double.parseDouble(battery.getValueAsString()) - wuSignalCost )));
                this.timeOfDeath = (this.getDirector().getModelTime().add(((Double.parseDouble(battery.getValueAsString())/Double.parseDouble(idleEnergyCost.getExpression())))));
                
//                System.out.println("Nodo " + this.getName() + " bateria depois de enviar mensagem WU: " + battery.getValueAsString());
                
                outWU.send(0, new StringToken("{event="+token+",gateway="+gateway.getExpression()+",sender="+ nodeName +"}"));
                
//                System.out.println(this.getName() + " mandou mensagem de WU para " + "{event="+token+",gateway="+gateway.getExpression()+"}");
                
                // teste de tempo
                if (!this.isAwake()) {
                    this.sleepFlag = false;
                } else {
//                    System.out.println("Nodo " + this.getName() + " continua acordado: ");    
                }
                
                               
                this.numberOfSentWakeUpMessages++;
                return true;
            } else {
                return false;
            }
        }
        
    }
    
//------------------------------------------------------------------------------------------------
        
    protected boolean sendMessageToSink(String token) throws NoRoomException, IllegalActionException{
        
        double commCost = this.eventCommCostMap.get(token.split("_")[0]);
//        System.out.println("CommCost: " + commCost);

        if ((!gateway.getExpression().equals("")&&!gateway.getExpression().equals("END")) && (Double.parseDouble(battery.getValueAsString()) >= commCost)){
//            System.out.println("Nodo " + this.getName() + " bateria antes de enviar mensagem sink: " + battery.getValueAsString());
            battery.setExpression(Double.toString( ( Double.parseDouble(battery.getValueAsString()) - commCost )));
            if (this.synchronizedRealTime.getExpression().equals("false"))
                    this.timeOfDeath = (this.getDirector().getModelTime().add(((Double.parseDouble(battery.getValueAsString())/Double.parseDouble(idleEnergyCost.getExpression())))));
//            System.out.println("Nodo " + this.getName() + " bateria depois de enviar mensagem sink: " + battery.getValueAsString());
            
            out.send(0, new StringToken("{event="+token+",gateway="+gateway.getExpression()+"}"));

//            System.out.println(this.getName() +" mandou mensagem de evento para "+ "{event="+token+",gateway="+gateway.getExpression()+"}");
            this.numberOfSentMessages++;
            
            if (gateway.getExpression().equals("NetworkMainGateway")) {
                return true;
            } else {
                this.sleepFlag = true;
            }
            
            return true;
        } else {
                return false;
        }
        
    }
    
//------------------------------------------------------------------------------------------------
    
    protected void buildIcon() throws IllegalActionException, NameDuplicationException {
        super.buildIcon();
        _circle_wurRadius = new EllipseAttribute(this.node_icon, "_circle_wurRadius");
        _circle_wurRadius.centered.setToken("true");
        _circle_wurRadius.width.setToken("50");
        _circle_wurRadius.height.setToken("50");
        _circle_wurRadius.fillColor.setToken("{0.0, 0.0, 0.5, 0.05}");
        _circle_wurRadius.lineColor.setToken("{0.0, 0.0, 0.0, 0.1}");

    }
    
}