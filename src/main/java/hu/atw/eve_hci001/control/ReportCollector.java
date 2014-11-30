package hu.atw.eve_hci001.control;

import hu.atw.eve_hci001.model.WeatherReport;

import java.awt.TrayIcon;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Idõjárás jelentések lekérését végzõ szál.
 * 
 * @author Ádám László
 * 
 */
public class ReportCollector implements Runnable {
	private Thread t;
	private HofigyeloController hofigyeloController;
	private final String url = "http://www.idokep.hu/idokep";
	private ArrayList<WeatherReport> weatherReports;
	/* a frissítés gyakorisága */
	private long timeOut;

	/**
	 * Konstruktor a ReportCollector osztályhoz.
	 * 
	 * @param hofigyeloController
	 *            A hofigyeloControllerl objektum.
	 */
	public ReportCollector(HofigyeloController hofigyeloController) {
		this.weatherReports = new ArrayList<WeatherReport>();
		this.hofigyeloController = hofigyeloController;
		this.timeOut = 60000;
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
	 * Idõjárás jelentések lekérése.
	 */
	public void run() {
		Thread thisThread = Thread.currentThread();
		while (this.t == thisThread) {
			try {
				this.weatherReports.clear();
				Document doc = Jsoup.connect(url).get();
				Elements elements = doc.select("area[onmouseover]");
				for (Element w : elements) {
					this.convertandAddWeatherReport(w.attr("onmouseover"));
				}
				this.hofigyeloController.refreshReports(this.weatherReports);
				Thread.sleep(this.timeOut);
			} catch (InterruptedException ie) {
				/* nem érdekes */
			} catch (Exception e) {
				/* hálózathoz/oldalhoz kapcsolódó probléma */
				this.hofigyeloController.showAlert("Hiba!",
						"Probléma az adatok lekérésénél.",
						TrayIcon.MessageType.ERROR);
			}
		}
	}

	/**
	 * Egy idõjárás jelentés szöveges alakját WeatherReport objektummá
	 * konvertálja, és hozzáadja az eddig összegyûjtöttekhez.
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
		/* típus es hõmérséklet */
		tokens[0] = this.selectAmongLtGt(tokens[0]);
		String typeAndDegreeTokens[] = tokens[0].split(", ");
		weatherReport.setType(typeAndDegreeTokens[0]);
		if (typeAndDegreeTokens.length == 2) {
			/* van Celsius infó is */
			weatherReport.setDegree(typeAndDegreeTokens[1]);
		}
		/* hely, idõ, feltöltõ */
		String locationAndSomeMoreTokens[] = tokens[1].split(", ");
		/* elõfordulhat pl. Budapest, Ferihegy, 08:21 */
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
		String timeAndUserTokens[] = timeAndUser.split(" ");
		weatherReport.setTime(timeAndUserTokens[0]);
		if (timeAndUserTokens.length == 2) {
			/* van feltöltõ is */
			weatherReport.setUser(timeAndUserTokens[1]);
		}
		/* az azonos, de más feltöltõtõl jövõ jelentésekbõl csak egy kell */
		if (!this.weatherReports.contains(weatherReport))
			this.weatherReports.add(weatherReport);
	}

	/**
	 * Eltávolítja a HTML tag-eket a szövegbõl.
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
