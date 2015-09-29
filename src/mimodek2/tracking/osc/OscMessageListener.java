package mimodek2.tracking.osc;

import oscP5.OscMessage;

public interface OscMessageListener {

	public void handleMessage(OscMessage message);

}
