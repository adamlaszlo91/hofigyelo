package hu.atw.eve_hci001;

import hu.atw.eve_hci001.control.HofigyeloController;

import java.awt.SystemTray;

/**
 * Main osztály a Hófigyelõ számára.
 * 
 * @author Ádám László
 * 
 */
public class Main {

	/**
	 * Main metódus.
	 * 
	 * @param args
	 *            Nem szükségel paramétereket.
	 */
	public static void main(String[] args) {
		if (!SystemTray.isSupported()) {
			System.out.println("SystemTray nem tamogatott.");
			System.exit(0);
		}
		new HofigyeloController();
	}

}
