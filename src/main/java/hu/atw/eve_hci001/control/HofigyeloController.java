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
 * Controller oszt�ly a H�figyel�h�z.
 * 
 * @author �d�m L�szl�
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
		/* konfigur�si�d f�jl beolvas�sa */
		if (!this.configManager.isConfigFileAvailable()) {
			this.showAlert(
					"Figyelem!",
					"A konfigur�ci�s f�jl nem tal�lhat�. Az alap�rtelmezett be�ll�t�sok ker�lnek haszn�latra.",
					TrayIcon.MessageType.WARNING);
		} else {
			try {
				this.configManager.refreshConfig();
			} catch (IOException e) {
				this.showAlert(
						"Figyelem!",
						"A konfigur�ci�s f�jl olvas�sa sikertelen. Az alap�rtelmezett be�ll�t�sok ker�lnek haszn�latra.",
						TrayIcon.MessageType.WARNING);
			} catch (MalformedConfigFileException e) {
				this.showAlert(
						"Figyelem!",
						"Hib�s konfigur�ci�s f�jl. Az alap�rtelmezett be�ll�t�sok ker�lnek haszn�latra.",
						TrayIcon.MessageType.WARNING);
			}
		}
		this.sysTray.refreshShownSettings();
		this.lastShownWeatherReports = new ArrayList<WeatherReport>();
		this.reportCollector = new ReportCollector(this);
		this.reportCollector.start();
	}

	/**
	 * Friss�ti a t�rolja id�j�r�s jelent�seket.
	 * 
	 * @param newWeatherReports
	 *            A friss id�j�r�s jelent�sek list�ja.
	 */
	public void refreshReports(ArrayList<WeatherReport> newWeatherReports) {
		ArrayList<WeatherReport> reportsToBeShown = new ArrayList<WeatherReport>();
		String tooltipText = "";
		for (WeatherReport weatherReport : newWeatherReports) {
			Boolean isWatched = this.configManager.isWatchedType(weatherReport
					.getType());
			/* ismeretlen/�j id�j�r�s �llapot */
			if (isWatched == null) {
				this.showAlert("Figyelem!", "Ismeretlen id�j�r�s �llapot: \""
						+ weatherReport.getType()
						+ "\"\nK�rem, jelezze a fejleszt�nek!",
						TrayIcon.MessageType.WARNING);
				/* figyelt id�j�r�s �llapot */
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
		/* jelent�sek �tm�sol�sa a helyi list�ba */
		this.lastShownWeatherReports.clear();
		for (WeatherReport weatherReport : reportsToBeShown) {
			this.lastShownWeatherReports.add(weatherReport);
		}
		if (this.configManager.isNotifyRequiested() && !tooltipText.equals(""))
			this.showAlert("�j jelent�s(ek)", tooltipText,
					TrayIcon.MessageType.INFO);
	}

	/**
	 * Be�ll�tja, hogy megjelenjen-e �rtes�t�s �j jelent�sr�l. "Be�ll�t�sok"
	 * ablkalb�l val� be�ll�t�s eset�n h�v�dik meg.
	 * 
	 * @param notifyRequiested
	 *            Megjelenjen-e �rtes�t�s.
	 */
	public void setNotifyRequiestedFromPanel(boolean notifyRequiested) {
		this.configManager.setNotifyRequiested(notifyRequiested);
		this.sysTray.refreshShownSettings();
	}

	/**
	 * Be�ll�tja, hogy megjelenjen-e �rtes�t�s �j jelent�sr�l. Men�b�l val�
	 * be�ll�t�s eset�n h�v�dik meg.
	 * 
	 * @param notifyRequiested
	 *            Megjelenjen-e �rtes�t�s.
	 */
	public void setNotifyRequiestedFromMenu(boolean notifyRequiested) {
		this.configManager.setNotifyRequiested(notifyRequiested);
		if (this.settingsPanel != null) {
			this.settingsPanel.refreshNotifySetting();
		}
	}

	/**
	 * Megjelen�t egy felugr� �rtes�t�st.
	 * 
	 * @param title
	 *            Az �rtes�t�s c�me.
	 * 
	 * @param text
	 *            Az �rtes�t�s sz�vege.
	 * @param mType
	 *            Az �rtes�t�s t�pusa.
	 */
	public void showAlert(String title, String text, TrayIcon.MessageType mType) {
		this.sysTray.showAlert(title, text, mType);
	}

	/**
	 * A be�ll�t�sokat tartalmat� ablak megjelen�t�se.
	 */
	public void showSettingsPanel() {
		this.settingsPanel = new SettingsPanel(this);
	}

	/**
	 * A be�ll�t�sokat tartalmat� ablakot elt�vol�tja.
	 */
	public void destroySettingsPanel() {
		this.settingsPanel = null;
	}

	/**
	 * Le�ll�tja a programot.
	 */
	public void exit() {
		this.reportCollector.stop();
		this.sysTray.remove();
		System.exit(0);
	}

	/**
	 * @return A felhaszn�l� k�rt-e �rtes�t�st.
	 */
	public boolean isNotifyRequiested() {
		return this.configManager.isNotifyRequiested();
	}

	/**
	 * 
	 * @return A friss�t�s gyakoris�ga milliszekundumban.
	 */
	public long getRefreshInterval() {
		return this.configManager.getRefreshInterval();
	}

	/**
	 * @return A figyelt id�j�r�si �llapotok �s st�tuszuk.
	 */
	public HashMap<String, Boolean> getWatchedTypes() {
		return this.configManager.getWatchedTypes();
	}

	/**
	 * Be�ll�tja, hogy egy adott id�j�r�si �llapotot figyeljen-e a program.
	 * 
	 * @param weatherType
	 *            Az id�j�r�si �llapot typusa.
	 * @param b
	 *            Figyelje-e.
	 */
	public void modifyWatchedType(String weatherType, boolean b) {
		this.configManager.modifyWatchedType(weatherType, b);
	}

	/**
	 * Az aktu�lis be�ll�t�sok konfigur�ci�s f�jlba val� �r�sa.
	 */
	public void writeSettings() {
		try {
			this.configManager.writeConfigFile();
		} catch (IOException e) {
			this.showAlert("Hiba!", "A be�ll�t�sok ment�se sikertelen.",
					TrayIcon.MessageType.ERROR);
		}
	}

	/**
	 * Azonnal friss�ti a jelent�seket.
	 */
	public void refreshReportsNow() {
		this.reportCollector.gatherData();
	}

	/**
	 * 
	 * @param refreshInterval
	 *            A friss�t�s gyakoris�ga milliszekundumban.
	 */
	public void setRefreshInterval(long refreshInterval) {
		this.configManager.setRefreshInterval(refreshInterval);
	}
}
