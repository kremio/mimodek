package mimodek2;

import mimodek2.bio.QTree;
import mimodek2.facade.FacadeFactory;

public class UpdateRunnable implements Runnable{
	
	public void run() {
		Mimodek.scentMap = new QTree(0, 0, FacadeFactory.getFacade().width, FacadeFactory.getFacade().height, 1);
		while( Mimodek.keepUpdateThreadRunning ){
			Mimodek.scentMap.update();
			
			try {
				Thread.sleep(100);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			
		}
	}
	
}
