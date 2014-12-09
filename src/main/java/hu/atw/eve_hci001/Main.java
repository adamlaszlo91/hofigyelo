package hu.atw.eve_hci001;

import hu.atw.eve_hci001.control.ConfigManager;
import hu.atw.eve_hci001.control.HofigyeloController;

import java.awt.SystemTray;

import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
		BasicConfigurator.configure();
		Logger logger = LoggerFactory.getLogger(ConfigManager.class);
		if (!SystemTray.isSupported()) {
			logger.error("SystemTray nem tamogatott.");
			System.exit(0);
		}
		new HofigyeloController();
	}

}
