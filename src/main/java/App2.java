

import java.awt.EventQueue;
import java.io.IOException;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.log4j.BasicConfigurator;


public class App2 {

	/**
	 * @param args
	 * @throws Exception 
	 */

	public static void main(String[] args) throws Exception {
		//BasicConfigurator.configure();
		 EventQueue.invokeLater(new Runnable() {
				public void run() {
					try {
						TextProcessor frame = new TextProcessor();
						frame.setVisible(true);
						//frame.initWindow();
					} catch (Exception e) {
						e.printStackTrace();
					}
			}
			});
	
	}

}
