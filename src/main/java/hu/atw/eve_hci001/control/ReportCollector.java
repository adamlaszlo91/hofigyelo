package hu.atw.eve_hci001.control;

import hu.atw.eve_hci001.model.WeatherReport;

import java.awt.TrayIcon;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Id�j�r�s jelent�sek lek�r�s�t v�gz� sz�l.
 * 
 * @author �d�m L�szl�
 * 
 */
public class ReportCollector implements Runnable {
	private Thread t;
	private HofigyeloController hofigyeloController;
	private final String url = "http://www.idokep.hu/idokep";
	private ArrayList<WeatherReport> weatherReports;
	/* a friss�t�s gyakoris�ga */
	private long timeOut;

	/**
	 * Konstruktor a ReportCollector oszt�lyhoz.
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
				this.weatherReports.clear();
				Document doc = Jsoup.connect(url).get();
				Elements elements = doc.select("area[onmouseover]");
				for (Element w : elements) {
					this.convertandAddWeatherReport(w.attr("onmouseover"));
				}
				this.hofigyeloController.refreshReports(this.weatherReports);
				Thread.sleep(this.timeOut);
			} catch (InterruptedException ie) {
				/* nem �rdekes */
			} catch (Exception e) {
				/* h�l�zathoz/oldalhoz kapcsol�d� probl�ma */
				this.hofigyeloController.showAlert("Hiba!",
						"Probl�ma az adatok lek�r�s�n�l.",
						TrayIcon.MessageType.ERROR);
			}
		}
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
