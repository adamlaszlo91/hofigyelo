package hu.atw.eve_hci001;

import hu.atw.eve_hci001.control.HofigyeloController;

import java.awt.SystemTray;

/**
 * Main oszt�ly a H�figyel� sz�m�ra.
 * 
 * @author �d�m L�szl�
 * 
 */
public class Main {

	/**
	 * Main met�dus.
	 * 
	 * @param args
	 *            Nem sz�ks�gel param�tereket.
	 */
	public static void main(String[] args) {
		if (!SystemTray.isSupported()) {
			System.out.println("SystemTray nem tamogatott.");
			System.exit(0);
		}
		new HofigyeloController();
	}

}
