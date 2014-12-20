package hu.atw.eve_hci001.hofigyelo.control;

import hu.atw.eve_hci001.hofigyelo.model.WeatherReport;
import hu.atw.eve_hci001.hofigyelo.model.exception.MalformedConfigFileException;
import hu.atw.eve_hci001.hofigyelo.view.SettingsPanel;
import hu.atw.eve_hci001.hofigyelo.view.SysTray;

import java.awt.TrayIcon;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Controller osztály a Hófigyelőhöz.
 * 
 * @author Ádám László
 * 
 */
public class HofigyeloController {
	private ArrayList<WeatherReport> lastShownWeatherReports;
	private ReportCollector reportCollector;
	private ConfigManager configManager;
	private SysTray sysTray;
	private SettingsPanel settingsPanel;

	/**
	 * Konstruktor.
	 */
	public HofigyeloController() {
		sysTray = new SysTray(this);
		configManager = new ConfigManager("./hofigyelo.ini");
		/* konfigurásiód fájl beolvasása */
		if (!configManager.isConfigFileAvailable()) {
			showAlert(
					"Figyelem!",
					"A konfigurációs fájl nem található. Az alapértelmezett beállítások kerülnek használatra.",
					TrayIcon.MessageType.WARNING);
		} else {
			try {
				configManager.refreshConfig();
			} catch (IOException e) {
				showAlert(
						"Figyelem!",
						"A konfigurációs fájl olvasása sikertelen. Az alapértelmezett beállítások kerülnek használatra.",
						TrayIcon.MessageType.WARNING);
			} catch (MalformedConfigFileException e) {
				showAlert(
						"Figyelem!",
						"Hibás konfigurációs fájl. Az alapértelmezett beállítások kerülnek használatra.",
						TrayIcon.MessageType.WARNING);
			}
		}
		sysTray.refreshShownSettings();
		lastShownWeatherReports = new ArrayList<WeatherReport>();
		reportCollector = new ReportCollector(this);
		reportCollector.start();
	}

	/**
	 * Frissíti a tárolja időjárás jelentéseket.
	 * 
	 * @param newWeatherReports
	 *            A friss időjárás jelentések listája.
	 */
	public void refreshReports(ArrayList<WeatherReport> newWeatherReports) {
		ArrayList<WeatherReport> reportsToBeShown = new ArrayList<WeatherReport>();
		String tooltipText = "";
		for (WeatherReport weatherReport : newWeatherReports) {
			Boolean isWatched = configManager.isWatchedType(weatherReport
					.getType());
			/* ismeretlen/új időjárás állapot */
			if (isWatched == null) {
				showAlert("Figyelem!", "Ismeretlen időjárás állapot: \""
						+ weatherReport.getType()
						+ "\"\nKérem, jelezze a fejlesztőnek!",
						TrayIcon.MessageType.WARNING);
				/* figyelt időjárás állapot */
			} else if (isWatched) {
				reportsToBeShown.add(weatherReport);
				if (!lastShownWeatherReports.contains(weatherReport)) {
					tooltipText += weatherReport.getType() + " - "
							+ weatherReport.getLocation() + ", "
							+ weatherReport.getTime() + "\n";
				}
			}
		}
		sysTray.refreshSnowReports(reportsToBeShown);
		sysTray.weatherUpdated();
		/* jelentések átmásolása a helyi listába */
		lastShownWeatherReports.clear();
		for (WeatherReport weatherReport : reportsToBeShown) {
			lastShownWeatherReports.add(weatherReport);
		}
		if (configManager.isNotifyRequiested() && !tooltipText.equals(""))
			showAlert("Új jelentés(ek)", tooltipText,
					TrayIcon.MessageType.INFO);
	}

	/**
	 * Beállítja, hogy megjelenjen-e értesítés új jelentésről. "Beállítások"
	 * ablkalból való beállítás esetén hívódik meg.
	 * 
	 * @param notifyRequiested
	 *            Megjelenjen-e értesítés.
	 */
	public void setNotifyRequiestedFromPanel(boolean notifyRequiested) {
		configManager.setNotifyRequiested(notifyRequiested);
		sysTray.refreshShownSettings();
	}

	/**
	 * Beállítja, hogy megjelenjen-e értesítés új jelentésről. Menüből való
	 * beállítás esetén hívódik meg.
	 * 
	 * @param notifyRequiested
	 *            Megjelenjen-e értesítés.
	 */
	public void setNotifyRequiestedFromMenu(boolean notifyRequiested) {
		configManager.setNotifyRequiested(notifyRequiested);
		if (settingsPanel != null) {
			settingsPanel.refreshNotifySetting();
		}
	}

	/**
	 * Megjelenít egy felugró értesítést.
	 * 
	 * @param title
	 *            Az értesítés címe.
	 * 
	 * @param text
	 *            Az értesítés szövege.
	 * @param mType
	 *            Az értesítés típusa.
	 */
	public void showAlert(String title, String text, TrayIcon.MessageType mType) {
		sysTray.showAlert(title, text, mType);
	}

	/**
	 * A beállításokat tartalmató ablak megjelenítése.
	 */
	public void showSettingsPanel() {
		settingsPanel = new SettingsPanel(this);
	}

	/**
	 * A beállításokat tartalmató ablakot eltávolítja.
	 */
	public void destroySettingsPanel() {
		settingsPanel = null;
	}

	/**
	 * Leállítja a programot.
	 */
	public void exit() {
		reportCollector.stop();
		sysTray.remove();
		System.exit(0);
	}

	/**
	 * @return A felhasználó kért-e értesítést.
	 */
	public boolean isNotifyRequiested() {
		return configManager.isNotifyRequiested();
	}

	/**
	 * 
	 * @return A frissítés gyakorisága milliszekundumban.
	 */
	public long getRefreshInterval() {
		return configManager.getRefreshInterval();
	}

	/**
	 * @return A figyelt időjárási állapotok és státuszuk.
	 */
	public HashMap<String, Boolean> getWatchedTypes() {
		return configManager.getWatchedTypes();
	}

	/**
	 * Beállítja, hogy egy adott időjárási állapotot figyeljen-e a program.
	 * 
	 * @param weatherType
	 *            Az időjárási állapot typusa.
	 * @param b
	 *            Figyelje-e.
	 */
	public void modifyWatchedType(String weatherType, boolean b) {
		configManager.modifyWatchedType(weatherType, b);
	}

	/**
	 * Az aktuális beállítások konfigurációs fájlba való írása.
	 */
	public void writeSettings() {
		try {
			configManager.writeConfigFile();
		} catch (IOException e) {
			showAlert("Hiba!", "A beállítások mentése sikertelen.",
					TrayIcon.MessageType.ERROR);
		}
	}

	/**
	 * Azonnal frissíti a jelentéseket.
	 */
	public void refreshReportsNow() {
		synchronized (reportCollector.getT()) {
			reportCollector.getT().notify();
		}
	}

	/**
	 * 
	 * @param refreshInterval
	 *            A frissítés gyakorisága milliszekundumban.
	 */
	public void setRefreshInterval(long refreshInterval) {
		configManager.setRefreshInterval(refreshInterval);
	}
}
