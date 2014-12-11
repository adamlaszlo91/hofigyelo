package hu.atw.eve_hci001.control;

import hu.atw.eve_hci001.model.WeatherReport;

import java.awt.TrayIcon;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Időjárás jelentések lekérését végző szál.
 * 
 * @author Ádám László
 * 
 */

public class ReportCollector implements Runnable {
	final Logger logger = LoggerFactory.getLogger(ConfigManager.class);
	private Thread t;
	private HofigyeloController controller;
	private final String url = "http://www.idokep.hu/idokep";
	private ArrayList<WeatherReport> weatherReports;

	/**
	 * Konstruktor.
	 * 
	 * @param controller
	 *            A controllerl objektum.
	 */
	public ReportCollector(HofigyeloController controller) {
		this.weatherReports = new ArrayList<WeatherReport>();
		this.controller = controller;
	}

	/**
	 * A szál indítására szolgáló metódus.
	 */
	public void start() {
		this.t = new Thread(this);
		this.t.start();
	}

	/**
	 * A szál megállítására szolgáló metódus.
	 */
	public void stop() {
		this.t = null;
	}

	/**
	 * 
	 * @return A futtató szál.
	 */
	public Thread getT() {
		return this.t;
	}

	/**
	 * Időjárás jelentések lekérése.
	 */
	public void run() {
		Thread thisThread = Thread.currentThread();
		while (this.t == thisThread) {
			this.gatherData();
			synchronized (this.t) {
				try {
					this.t.wait(this.controller.getRefreshInterval());
				} catch (InterruptedException e) {
					logger.debug(e.toString());
				}
			}
		}
	}

	/**
	 * Begyűjti a szerverről az időjárás jelentéseket.
	 */
	private void gatherData() {
		logger.debug("Adatgyűjtés");
		this.weatherReports.clear();
		try {
			Document doc = Jsoup.connect(url).get();
			Elements elements = doc.select("area[onmouseover]");
			for (Element w : elements) {
				this.convertandAddWeatherReport(w.attr("onmouseover"));
			}
		} catch (Exception e) {
			/* csak hálózathoz/oldalhoz kapcsolódó probléma lehet */
			this.controller.showAlert("Hiba!",
					"Probléma az adatok lekérésénél.",
					TrayIcon.MessageType.ERROR);
			return;
		}
		this.controller.refreshReports(this.weatherReports);
		logger.debug("Adatgyűjtés vége");
	}

	/**
	 * Egy időjárás jelentés szöveges alakját WeatherReport objektummá
	 * konvertálja, és hozzáadja az eddig összegyűjtöttekhez.
	 * 
	 * @param data
	 *            A jelentés adatai nyers szövegként.
	 */
	private void convertandAddWeatherReport(String data) {
		data = data.substring(5, data.length() - 2);
		String tokens[] = data.split("<br>");
		if (tokens.length != 2) {
			/* nem jelentés */
			return;
		}
		WeatherReport weatherReport = new WeatherReport();
		/* típus es hőmérséklet */
		tokens[0] = this.selectAmongLtGt(tokens[0]);
		String typeAndDegreeTokens[] = tokens[0].split(", ");
		weatherReport.setType(typeAndDegreeTokens[0]);
		if (typeAndDegreeTokens.length == 2) {
			/* van Celsius infó is */
			weatherReport.setDegree(typeAndDegreeTokens[1]);
		}
		/* hely, idő, feltöltő */
		String locationAndSomeMoreTokens[] = tokens[1].split(", ");
		/* előfordulhat pl. Budapest, Ferihegy, 08:21 */
		String location = "";
		String timeAndUser = "";
		for (int i = 0; i < locationAndSomeMoreTokens.length; i++) {
			if (i == locationAndSomeMoreTokens.length - 1) {
				timeAndUser = locationAndSomeMoreTokens[i];
			} else {
				location = location + locationAndSomeMoreTokens[i] + " ";
			}
		}
		weatherReport.setLocation(location);
		System.out.println(timeAndUser);
		String timeAndUserTokens[] = timeAndUser.split(" ");
		weatherReport.setTime(timeAndUserTokens[0]);
		if (timeAndUserTokens.length == 3) {
			/* van feltöltő is */
			weatherReport.setUser(timeAndUserTokens[2]);
		}

		if (!this.weatherReports.contains(weatherReport))
			this.weatherReports.add(weatherReport);
	}

	/**
	 * Eltávolítja a HTML tag-eket a szövegből.
	 * 
	 * @param s
	 *            A megtisztítani kívánt szöveg.
	 * @return A szöveg HTML tag-ek nélkül.
	 */
	private String selectAmongLtGt(String s) {
		String temp = "";
		int c = 0;
		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) == '<') {
				c++;
			} else if (s.charAt(i) == '>') {
				c--;
			} else if (c == 0) {
				temp = temp + s.charAt(i);
			}
		}
		return temp;
	}

}
