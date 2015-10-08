package mimodek2.tracking.osc;

import netP5.NetAddress;
import oscP5.OscEventListener;
import oscP5.OscMessage;
import oscP5.OscP5;
import oscP5.OscStatus;

import java.util.ArrayList;
import java.util.HashMap;

public class OSCom implements OscEventListener{
	
	OscP5 oscP5;
	NetAddress remoteLocation;
	HashMap<String, ArrayList<OscMessageListener>> messageListeners;
	
	public OSCom(int inPort, String remoteHost, int outPort){
		messageListeners = new HashMap<String, ArrayList<OscMessageListener>>(); 
		oscP5 = new OscP5(this, inPort);
		remoteLocation = new NetAddress(remoteHost, outPort);
		oscP5.addListener(this);
		
	}
	
	public String getLocalIP(){
		return oscP5.ip();
	}
	
	public void sendAddTriggerMessage(String triggerId, float posX, float posY, int radius){
		OscMessage myMessage = new OscMessage("/mimodek/trigger/add");
		  // poiID : string, pos.x : float (0..1), pos.y : float(0..1), radius: int
		  myMessage.add( triggerId );
		  myMessage.add( posX );
		  myMessage.add( posY );
		  myMessage.add( radius );

		  /* send the message */
		  oscP5.send(myMessage, remoteLocation); 
	}
	
	public void addListener(String address, OscMessageListener listener) {
		if( !messageListeners.containsKey(address) )
			messageListeners.put(address, new ArrayList<OscMessageListener>());
		
		messageListeners.get(address).add(listener);
	}

	public void oscEvent(OscMessage message) {

		String address = message.addrPattern();
		if( ! messageListeners.containsKey( address ) )
			return;
		
		for( OscMessageListener listener: messageListeners.get(address))
			listener.handleMessage( message );
	}

	public void oscStatus(OscStatus arg0) {
	
	}

}
