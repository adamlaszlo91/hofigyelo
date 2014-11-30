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
 * Az értesítési ikont kezelõ osztály.
 * 
 * @author Ádám László
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
	 * Konstruktor a SysTray osztályhoz.
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
		this.trayIcon.setToolTip("Hófigyelõ\nFrissítve: soha");
		/* menu */
		this.popup = new PopupMenu();

		this.currentlySnowing = new Menu("Jelenleg havazik (0)");
		this.popup.add(currentlySnowing);

		this.notification = new Menu("Figyelmeztetés (Bekapcsolva)");
		this.notifyYesNo = new MenuItem("Kikapcsol");
		this.notifyYesNo.addActionListener(this);
		this.notification.add(this.notifyYesNo);
		this.popup.add(this.notification);

		this.idokepHu = new MenuItem("idokep.hu");
		this.idokepHu.addActionListener(this);
		popup.add(idokepHu);

		this.exit = new MenuItem("Kilépés");
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
	 * Eltávolítja az értesítési ikont.
	 */
	public void remove() {
		this.tray.remove(this.trayIcon);
	}

	public void actionPerformed(ActionEvent e) {
		/* kilépés */
		if (e.getSource() == this.exit) {
			this.control.exit();
			/* idokep.hu link */
		} else if (e.getSource() == this.idokepHu) {
			this.openWebpage("http://www.idokep.hu/idokep");
			/* értesítés tiltása/engedélyezése */
		} else if (e.getSource() == this.notifyYesNo) {
			if (this.notify) {
				this.notification.setLabel("Figyelmeztetés (Kikapcsolva)");
				this.notifyYesNo.setLabel("Bekapcsol");
				this.notify = false;
			} else {
				this.notification.setLabel("Figyelmeztetés (Bekapcsolva)");
				this.notifyYesNo.setLabel("Kikapcsol");
				this.notify = true;
			}
			this.control.setNotify(this.notify);
		}
	}

	/**
	 * Frissíti a menübõl elérhetó havazással kapcsolatos jelentéseket.
	 * 
	 * @param snowReports
	 *            A havazásról szóló jelentések.
	 */
	public void refreshSnowReports(ArrayList<WeatherReport> snowReports) {
		this.reportCounter = 0;
		/* nem engedi bekövetkezni az update-miközben-látható problémat */
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
	 * Ha új ellenõrzés futott le, ez a függvény beállítja a legutóbbi frissítés
	 * idõpontját a tooltip-ben.
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
		this.trayIcon.setToolTip("Hófigyelõ\nFrissítve: " + time);
	}

	/**
	 * Megjelenít egy értesítést felugró buborékban.
	 * 
	 * @param text
	 *            Az értesítés szövege.
	 */
	public void showAlert(String title, String text, TrayIcon.MessageType mType) {
		this.trayIcon.displayMessage(title, text, mType);
	}

	/**
	 * Utasítja a rendszert egy weboldal megnyitására az alapértelmezett
	 * bögéyszõvel.
	 * 
	 * @param urlString
	 *            A webolda URL-eje.
	 */
	private void openWebpage(String urlString) {
		try {
			Desktop.getDesktop().browse(new URL(urlString).toURI());
		} catch (Exception e) {
			this.showAlert("Hiba!", "Nem sikerült megnyitni a böngészõt.",
					TrayIcon.MessageType.WARNING);
		}
	}

}
