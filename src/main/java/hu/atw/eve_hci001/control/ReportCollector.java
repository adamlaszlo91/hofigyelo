package hu.atw.eve_hci001.control;

import hu.atw.eve_hci001.model.ConfigManager;
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
 * Id�j�r�s jelent�sek lek�r�s�t v�gz� sz�l.
 * 
 * @author �d�m L�szl�
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
	 * A sz�l ind�t�s�ra szolg�l� met�dus.
	 */
	public void start() {
		this.t = new Thread(this);
		this.t.start();
	}

	/**
	 * A sz�l meg�ll�t�s�ra szolg�l� met�dus.
	 */
	public void stop() {
		this.t = null;
	}

	/**
	 * Id�j�r�s jelent�sek lek�r�se.
	 */
	public void run() {
		Thread thisThread = Thread.currentThread();
		while (this.t == thisThread) {
			try {
				this.gatherData();
				Thread.sleep(this.controller.getRefreshInterval());
			} catch (InterruptedException ie) {
				/* nem �rdekes */
			}
		}
	}

	/**
	 * Begy�jti a szerverr�l az id�j�r�s jelent�seket.Szinkroniz�ls, mert
	 * k�v�lr�l is megh�vhat� azonnali friss�t�sre.
	 */
	public synchronized void gatherData() {
		logger.debug("Adatgy�jt�s");
		this.weatherReports.clear();
		try {
			Document doc = Jsoup.connect(url).get();
			Elements elements = doc.select("area[onmouseover]");
			for (Element w : elements) {
				this.convertandAddWeatherReport(w.attr("onmouseover"));
			}
		} catch (Exception e) {
			/* csak h�l�zathoz/oldalhoz kapcsol�d� probl�ma lehet */
			this.controller.showAlert("Hiba!",
					"Probl�ma az adatok lek�r�s�n�l.",
					TrayIcon.MessageType.ERROR);
			return;
		}
		this.controller.refreshReports(this.weatherReports);
		logger.debug("Adatgy�jt�s v�ge");
	}

	/**
	 * Egy id�j�r�s jelent�s sz�veges alakj�t WeatherReport objektumm�
	 * konvert�lja, �s hozz�adja az eddig �sszegy�jt�ttekhez.
	 * 
	 * @param data
	 *            A jelent�s adatai nyers sz�vegk�nt.
	 */
	private void convertandAddWeatherReport(String data) {
		data = data.substring(5, data.length() - 2);
		String tokens[] = data.split("<br>");
		if (tokens.length != 2) {
			/* nem jelent�s */
			return;
		}
		WeatherReport weatherReport = new WeatherReport();
		/* t�pus es h�m�rs�klet */
		tokens[0] = this.selectAmongLtGt(tokens[0]);
		String typeAndDegreeTokens[] = tokens[0].split(", ");
		weatherReport.setType(typeAndDegreeTokens[0]);
		if (typeAndDegreeTokens.length == 2) {
			/* van Celsius inf� is */
			weatherReport.setDegree(typeAndDegreeTokens[1]);
		}
		/* hely, id�, felt�lt� */
		String locationAndSomeMoreTokens[] = tokens[1].split(", ");
		/* el�fordulhat pl. Budapest, Ferihegy, 08:21 */
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
			/* van felt�lt� is */
			weatherReport.setUser(timeAndUserTokens[1]);
		}
		/* az azonos, de m�s felt�lt�t�l j�v� jelent�sekb�l csak egy kell */
		if (!this.weatherReports.contains(weatherReport))
			this.weatherReports.add(weatherReport);
	}

	/**
	 * Elt�vol�tja a HTML tag-eket a sz�vegb�l.
	 * 
	 * @param s
	 *            A megtiszt�tani k�v�nt sz�veg.
	 * @return A sz�veg HTML tag-ek n�lk�l.
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
