package mimodek2.serializer;

import java.util.HashMap;

public interface StatefulObject {
	
	public HashMap<String, Object> getState();
	
	public void setState(HashMap<String, Object> state);
	
}
