package hu.atw.eve_hci001.control;

import hu.atw.eve_hci001.model.ReportCollector;
import hu.atw.eve_hci001.model.WeatherReport;
import hu.atw.eve_hci001.view.SysTray;

import java.awt.TrayIcon;
import java.util.ArrayList;

/**
 * Control objektum a H�figyel�h�z.
 * 
 * @author �d�m L�szl�
 * 
 */
public class Control {
	private ArrayList<WeatherReport> weatherReports;
	private ReportCollector reportCollector;
	private SysTray sysTray;
	private boolean notify;

	/**
	 * Konstruktor a Control oszt�lyhoz.
	 */
	public Control() {
		this.notify = true;
		this.sysTray = new SysTray(this);
		this.weatherReports = new ArrayList<WeatherReport>();
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
		ArrayList<WeatherReport> snowReports = new ArrayList<WeatherReport>();
		String tooltipText = "";
		for (WeatherReport weatherReport : newWeatherReports) {
			if (weatherReport.getType().contains("hav")
					|| weatherReport.getType().contains("h�")) {
				snowReports.add(weatherReport);
				if (!this.weatherReports.contains(weatherReport)) {
					tooltipText += weatherReport.getType() + " - "
							+ weatherReport.getLocation() + ", "
							+ weatherReport.getTime() + "\n";
				}
			}
		}
		this.sysTray.refreshSnowReports(snowReports);
		this.sysTray.weatherUpdated();
		this.weatherReports.clear();
		for (WeatherReport weatherReport : newWeatherReports) {
			this.weatherReports.add(weatherReport);
		}
		if (this.notify && !tooltipText.equals(""))
			this.showAlert("�j h�jelent�s", tooltipText,
					TrayIcon.MessageType.INFO);
	}

	/**
	 * A program le�ll�t�sa.
	 */
	public void exit() {
		this.reportCollector.stop();
		this.sysTray.remove();
		System.exit(0);
	}

	/**
	 * Be�ll�tja, hogy megjelenjen-e �rtes�t�s �j h�jelent�sr�l.
	 * 
	 * @param notify
	 *            Igen/nem.
	 */
	public void setNotify(boolean notify) {
		this.notify = notify;
	}

	/**
	 * Megjelen�t egy felugr� �rtes�t�st.
	 * 
	 * @param text
	 *            Az �rtes�t�s sz�vege.
	 * @param mType
	 *            Az �rtes�t�s t�pusa.
	 */
	public void showAlert(String title, String text, TrayIcon.MessageType mType) {
		this.sysTray.showAlert(title, text, mType);
	}

}
