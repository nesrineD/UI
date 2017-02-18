package flink.gelly;

import java.awt.EventQueue;
import java.io.IOException;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;


public class App {

	/**
	 * @param args
	 * @throws IOException 
	 */

	public static void main(String[] args) throws IOException {
	
		 EventQueue.invokeLater(new Runnable() {
				public void run() {
					try {
						TextProcessor frame = new TextProcessor();
						frame.setVisible(true);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
	
	}

}
