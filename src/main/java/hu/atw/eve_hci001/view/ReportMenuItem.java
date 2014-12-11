package hu.atw.eve_hci001.view;

import hu.atw.eve_hci001.model.WeatherReport;

import java.awt.MenuItem;

/**
 * Egy olyan MenuItem, ami egy hozzá társított időjárás jelentése tartalmaz.
 * 
 * @author László Ádám
 *
 */
public class ReportMenuItem extends MenuItem {

	private static final long serialVersionUID = 1L;
	private WeatherReport weatherReport;

	/**
	 * Konstruktor.
	 * 
	 * @param weatherReport A társított jelentés.
	 */
	public ReportMenuItem(WeatherReport weatherReport) {
		super(weatherReport.shortDescription());
		this.weatherReport = weatherReport;
	}

	/**
	 * Visszaadja a társított jelentés szöveges reprezentációját.
	 * 
	 * @return A társított jelentés szövegesen.
	 */
	public String getReportText() {
		return this.weatherReport.toString();
	}

}
