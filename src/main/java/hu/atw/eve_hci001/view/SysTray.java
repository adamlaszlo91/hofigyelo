package hu.atw.eve_hci001.view;

import hu.atw.eve_hci001.control.Control;
import hu.atw.eve_hci001.model.WeatherReport;

import java.awt.Desktop;
import java.awt.Image;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;

import javax.swing.ImageIcon;

/**
 * Az �rtes�t�si ikont kezel� oszt�ly.
 * 
 * @author �d�m L�szl�
 * 
 */
public class SysTray implements ActionListener {
	private SystemTray tray;
	private TrayIcon trayIcon;
	private Control control;
	private PopupMenu popup;
	private Menu currentlySnowing;
	private Menu notification;
	private MenuItem notifyYesNo;
	private MenuItem idokepHu;
	private MenuItem exit;
	private boolean notify;
	private int reportCounter;

	/**
	 * Konstruktor a SysTray oszt�lyhoz.
	 * 
	 * @param control
	 *            A Control objektum.
	 */
	public SysTray(Control control) {
		this.control = control;
		this.notify = true;
		this.tray = SystemTray.getSystemTray();
		Image image = new ImageIcon("src/main/resources/snowflake.png")
				.getImage();
		this.trayIcon = new TrayIcon(image);
		this.trayIcon.setImageAutoSize(true);
		this.trayIcon.setToolTip("H�figyel�\nFriss�tve: soha");
		/* menu */
		this.popup = new PopupMenu();

		this.currentlySnowing = new Menu("Jelenleg havazik (0)");
		this.popup.add(currentlySnowing);

		this.notification = new Menu("Figyelmeztet�s (Bekapcsolva)");
		this.notifyYesNo = new MenuItem("Kikapcsol");
		this.notifyYesNo.addActionListener(this);
		this.notification.add(this.notifyYesNo);
		this.popup.add(this.notification);

		this.idokepHu = new MenuItem("idokep.hu");
		this.idokepHu.addActionListener(this);
		popup.add(idokepHu);

		this.exit = new MenuItem("Kil�p�s");
		this.exit.addActionListener(this);
		this.popup.add(exit);

		this.trayIcon.setPopupMenu(popup);
		try {
			this.tray.add(this.trayIcon);
		} catch (Exception e) {
			System.out.println("TrayIcon could not be added.");
		}
	}

	/**
	 * Elt�vol�tja az �rtes�t�si ikont.
	 */
	public void remove() {
		this.tray.remove(this.trayIcon);
	}

	public void actionPerformed(ActionEvent e) {
		/* kil�p�s */
		if (e.getSource() == this.exit) {
			this.control.exit();
			/* idokep.hu link */
		} else if (e.getSource() == this.idokepHu) {
			this.openWebpage("http://www.idokep.hu/idokep");
			/* �rtes�t�s tilt�sa/enged�lyez�se */
		} else if (e.getSource() == this.notifyYesNo) {
			if (this.notify) {
				this.notification.setLabel("Figyelmeztet�s (Kikapcsolva)");
				this.notifyYesNo.setLabel("Bekapcsol");
				this.notify = false;
			} else {
				this.notification.setLabel("Figyelmeztet�s (Bekapcsolva)");
				this.notifyYesNo.setLabel("Kikapcsol");
				this.notify = true;
			}
			this.control.setNotify(this.notify);
		}
	}

	/**
	 * Friss�ti a men�b�l el�rhet� havaz�ssal kapcsolatos jelent�seket.
	 * 
	 * @param snowReports
	 *            A havaz�sr�l sz�l� jelent�sek.
	 */
	public void refreshSnowReports(ArrayList<WeatherReport> snowReports) {
		this.reportCounter = 0;
		/* nem engedi bek�vetkezni az update-mik�zben-l�that� probl�mat */
		this.popup.removeAll();
		this.currentlySnowing.removeAll();
		for (WeatherReport weatherReport : snowReports) {
			MenuItem snowItem = new MenuItem(weatherReport.getType() + " - "
					+ weatherReport.getLocation() + ", "
					+ weatherReport.getTime());
			this.currentlySnowing.add(snowItem);
			this.reportCounter++;
		}
		this.currentlySnowing.setLabel("Jelenleg havazik ("
				+ this.reportCounter + ")");
		this.popup.add(this.currentlySnowing);
		this.popup.add(this.notification);
		this.popup.add(this.idokepHu);
		this.popup.add(this.exit);
	}

	/**
	 * Ha �j ellen�rz�s futott le, ez a f�ggv�ny be�ll�tja a legut�bbi friss�t�s
	 * id�pontj�t a tooltip-ben.
	 */
	public void weatherUpdated() {
		Calendar now = Calendar.getInstance();
		int hour = now.get(Calendar.HOUR_OF_DAY);
		int min = now.get(Calendar.MINUTE);
		String minString;
		if (min < 10) {
			minString = "0" + min;
		} else {
			minString = "" + min;
		}
		String time = ("" + hour + ":" + minString);
		this.trayIcon.setToolTip("H�figyel�\nFriss�tve: " + time);
	}

	/**
	 * Megjelen�t egy �rtes�t�st felugr� bubor�kban.
	 * 
	 * @param text
	 *            Az �rtes�t�s sz�vege.
	 */
	public void showAlert(String title, String text, TrayIcon.MessageType mType) {
		this.trayIcon.displayMessage(title, text, mType);
	}

	/**
	 * Utas�tja a rendszert egy weboldal megnyit�s�ra az alap�rtelmezett
	 * b�g�ysz�vel.
	 * 
	 * @param urlString
	 *            A webolda URL-eje.
	 */
	private void openWebpage(String urlString) {
		try {
			Desktop.getDesktop().browse(new URL(urlString).toURI());
		} catch (Exception e) {
			this.showAlert("Hiba!", "Nem siker�lt megnyitni a b�ng�sz�t.",
					TrayIcon.MessageType.WARNING);
		}
	}

}
