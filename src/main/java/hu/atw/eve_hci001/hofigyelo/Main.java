package hu.atw.eve_hci001.hofigyelo;

import hu.atw.eve_hci001.hofigyelo.control.ConfigManager;
import hu.atw.eve_hci001.hofigyelo.control.HofigyeloController;

import java.awt.SystemTray;

import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main osztály a Hófigyelő számára.
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
		BasicConfigurator.configure();
		Logger logger = LoggerFactory.getLogger(ConfigManager.class);
		if (!SystemTray.isSupported()) {
			logger.error("SystemTray nem tamogatott.");
			System.exit(0);
		}
		new HofigyeloController();
	}

}
