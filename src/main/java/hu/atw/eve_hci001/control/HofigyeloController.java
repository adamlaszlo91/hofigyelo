package hu.atw.eve_hci001.control;

import hu.atw.eve_hci001.model.ConfigManager;
import hu.atw.eve_hci001.model.WeatherReport;
import hu.atw.eve_hci001.model.exception.MalformedConfigFileException;
import hu.atw.eve_hci001.view.SettingsPanel;
import hu.atw.eve_hci001.view.SysTray;

import java.awt.TrayIcon;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Controller osztály a Hófigyelõhöz.
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
		this.sysTray = new SysTray(this);
		this.configManager = new ConfigManager("./hofigyelo.ini");
		/* konfigurásiód fájl beolvasása */
		if (!this.configManager.isConfigFileAvailable()) {
			this.showAlert(
					"Figyelem!",
					"A konfigurációs fájl nem található. Az alapértelmezett beállítások kerülnek használatra.",
					TrayIcon.MessageType.WARNING);
		} else {
			try {
				this.configManager.refreshConfig();
			} catch (IOException e) {
				this.showAlert(
						"Figyelem!",
						"A konfigurációs fájl olvasása sikertelen. Az alapértelmezett beállítások kerülnek használatra.",
						TrayIcon.MessageType.WARNING);
			} catch (MalformedConfigFileException e) {
				this.showAlert(
						"Figyelem!",
						"Hibás konfigurációs fájl. Az alapértelmezett beállítások kerülnek használatra.",
						TrayIcon.MessageType.WARNING);
			}
		}
		this.sysTray.refreshShownSettings();
		this.lastShownWeatherReports = new ArrayList<WeatherReport>();
		this.reportCollector = new ReportCollector(this);
		this.reportCollector.start();
	}

	/**
	 * Frissíti a tárolja idõjárás jelentéseket.
	 * 
	 * @param newWeatherReports
	 *            A friss idõjárás jelentések listája.
	 */
	public void refreshReports(ArrayList<WeatherReport> newWeatherReports) {
		ArrayList<WeatherReport> reportsToBeShown = new ArrayList<WeatherReport>();
		String tooltipText = "";
		for (WeatherReport weatherReport : newWeatherReports) {
			Boolean isWatched = this.configManager.isWatchedType(weatherReport
					.getType());
			/* ismeretlen/új idõjárás állapot */
			if (isWatched == null) {
				this.showAlert("Figyelem!", "Ismeretlen idõjárás állapot: \""
						+ weatherReport.getType()
						+ "\"\nKérem, jelezze a fejlesztõnek!",
						TrayIcon.MessageType.WARNING);
				/* figyelt idõjárás állapot */
			} else if (isWatched) {
				reportsToBeShown.add(weatherReport);
				if (!this.lastShownWeatherReports.contains(weatherReport)) {
					tooltipText += weatherReport.getType() + " - "
							+ weatherReport.getLocation() + ", "
							+ weatherReport.getTime() + "\n";
				}
			}
		}
		this.sysTray.refreshSnowReports(reportsToBeShown);
		this.sysTray.weatherUpdated();
		/* jelentések átmásolása a helyi listába */
		this.lastShownWeatherReports.clear();
		for (WeatherReport weatherReport : reportsToBeShown) {
			this.lastShownWeatherReports.add(weatherReport);
		}
		if (this.configManager.isNotifyRequiested() && !tooltipText.equals(""))
			this.showAlert("Új jelentés(ek)", tooltipText,
					TrayIcon.MessageType.INFO);
	}

	/**
	 * Beállítja, hogy megjelenjen-e értesítés új jelentésrõl. "Beállítások"
	 * ablkalból való beállítás esetén hívódik meg.
	 * 
	 * @param notifyRequiested
	 *            Megjelenjen-e értesítés.
	 */
	public void setNotifyRequiestedFromPanel(boolean notifyRequiested) {
		this.configManager.setNotifyRequiested(notifyRequiested);
		this.sysTray.refreshShownSettings();
	}

	/**
	 * Beállítja, hogy megjelenjen-e értesítés új jelentésrõl. Menübõl való
	 * beállítás esetén hívódik meg.
	 * 
	 * @param notifyRequiested
	 *            Megjelenjen-e értesítés.
	 */
	public void setNotifyRequiestedFromMenu(boolean notifyRequiested) {
		this.configManager.setNotifyRequiested(notifyRequiested);
		if (this.settingsPanel != null) {
			this.settingsPanel.refreshNotifySetting();
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
		this.sysTray.showAlert(title, text, mType);
	}

	/**
	 * A beállításokat tartalmató ablak megjelenítése.
	 */
	public void showSettingsPanel() {
		this.settingsPanel = new SettingsPanel(this);
	}

	/**
	 * A beállításokat tartalmató ablakot eltávolítja.
	 */
	public void destroySettingsPanel() {
		this.settingsPanel = null;
	}

	/**
	 * Leállítja a programot.
	 */
	public void exit() {
		this.reportCollector.stop();
		this.sysTray.remove();
		System.exit(0);
	}

	/**
	 * @return A felhasználó kért-e értesítést.
	 */
	public boolean isNotifyRequiested() {
		return this.configManager.isNotifyRequiested();
	}

	/**
	 * 
	 * @return A frissítés gyakorisága milliszekundumban.
	 */
	public long getRefreshInterval() {
		return this.configManager.getRefreshInterval();
	}

	/**
	 * @return A figyelt idõjárási állapotok és státuszuk.
	 */
	public HashMap<String, Boolean> getWatchedTypes() {
		return this.configManager.getWatchedTypes();
	}

	/**
	 * Beállítja, hogy egy adott idõjárási állapotot figyeljen-e a program.
	 * 
	 * @param weatherType
	 *            Az idõjárási állapot typusa.
	 * @param b
	 *            Figyelje-e.
	 */
	public void modifyWatchedType(String weatherType, boolean b) {
		this.configManager.modifyWatchedType(weatherType, b);
	}

	/**
	 * Az aktuális beállítások konfigurációs fájlba való írása.
	 */
	public void writeSettings() {
		try {
			this.configManager.writeConfigFile();
		} catch (IOException e) {
			this.showAlert("Hiba!", "A beállítások mentése sikertelen.",
					TrayIcon.MessageType.ERROR);
		}
	}

	/**
	 * Azonnal frissíti a jelentéseket.
	 */
	public void refreshReportsNow() {
		this.reportCollector.gatherData();
	}

	/**
	 * 
	 * @param refreshInterval
	 *            A frissítés gyakorisága milliszekundumban.
	 */
	public void setRefreshInterval(long refreshInterval) {
		this.configManager.setRefreshInterval(refreshInterval);
	}
}
