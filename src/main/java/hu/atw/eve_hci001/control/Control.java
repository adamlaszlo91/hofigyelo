package hu.atw.eve_hci001.control;

import hu.atw.eve_hci001.model.ReportCollector;
import hu.atw.eve_hci001.model.WeatherReport;
import hu.atw.eve_hci001.view.SysTray;

import java.awt.TrayIcon;
import java.util.ArrayList;

/**
 * Control objektum a Hófigyelõhöz.
 * 
 * @author Ádám László
 * 
 */
public class Control {
	private ArrayList<WeatherReport> weatherReports;
	private ReportCollector reportCollector;
	private SysTray sysTray;
	private boolean notify;

	/**
	 * Konstruktor a Control osztályhoz.
	 */
	public Control() {
		this.notify = true;
		this.sysTray = new SysTray(this);
		this.weatherReports = new ArrayList<WeatherReport>();
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
		ArrayList<WeatherReport> snowReports = new ArrayList<WeatherReport>();
		String tooltipText = "";
		for (WeatherReport weatherReport : newWeatherReports) {
			if (weatherReport.getType().contains("hav")
					|| weatherReport.getType().contains("hó")) {
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
			this.showAlert("Új hójelentés", tooltipText,
					TrayIcon.MessageType.INFO);
	}

	/**
	 * A program leállítása.
	 */
	public void exit() {
		this.reportCollector.stop();
		this.sysTray.remove();
		System.exit(0);
	}

	/**
	 * Beállítja, hogy megjelenjen-e értesítés új hójelentésrõl.
	 * 
	 * @param notify
	 *            Igen/nem.
	 */
	public void setNotify(boolean notify) {
		this.notify = notify;
	}

	/**
	 * Megjelenít egy felugró értesítést.
	 * 
	 * @param text
	 *            Az értesítés szövege.
	 * @param mType
	 *            Az értesítés típusa.
	 */
	public void showAlert(String title, String text, TrayIcon.MessageType mType) {
		this.sysTray.showAlert(title, text, mType);
	}

}
