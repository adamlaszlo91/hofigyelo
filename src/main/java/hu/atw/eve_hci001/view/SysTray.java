package hu.atw.eve_hci001.view;

import hu.atw.eve_hci001.control.ConfigManager;
import hu.atw.eve_hci001.control.HofigyeloController;
import hu.atw.eve_hci001.model.WeatherReport;

import java.awt.AWTException;
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
import javax.swing.JOptionPane;

/**
 * Az értesítési ikont kezelő osztály.
 * 
 * @author Ádám László
 * 
 */
public class SysTray implements ActionListener {
	private SystemTray tray;
	private TrayIcon trayIcon;
	private HofigyeloController controller;
	private PopupMenu popup;
	private Menu currentReportsMenu;
	private Menu notificationMenu;
	private MenuItem notifyYesNoItem;
	private MenuItem idokepHuItem;
	private MenuItem exitItem;
	private MenuItem settingsItem;
	private int reportCounter;

	/**
	 * Konstruktor.
	 * 
	 * @param controller
	 *            A controller objektum.
	 */
	public SysTray(HofigyeloController controller) {
		this.controller = controller;
		this.tray = SystemTray.getSystemTray();
		Image image = new ImageIcon(ConfigManager.getImage("snowflake.png"))
				.getImage();
		this.trayIcon = new TrayIcon(image);
		this.trayIcon.setImageAutoSize(true);
		this.trayIcon.setToolTip("Hófigyelő\nFrissítve: soha");
		/* popup menü */
		this.popup = new PopupMenu();
		/* ha túl hamar jelenítik meg, fennakadhat a program */
		this.popup.setEnabled(false);

		this.currentReportsMenu = new Menu("Jelenleg havazik (0)");
		this.popup.add(currentReportsMenu);

		this.notificationMenu = new Menu();
		this.notifyYesNoItem = new MenuItem();
		this.notificationMenu.setLabel("Figyelmeztetés (?)");
		this.notifyYesNoItem.setLabel("?");
		this.notifyYesNoItem.addActionListener(this);
		this.notificationMenu.add(this.notifyYesNoItem);
		this.popup.add(this.notificationMenu);

		this.settingsItem = new MenuItem("Beállítások");
		this.settingsItem.addActionListener(this);
		this.popup.add(this.settingsItem);

		this.idokepHuItem = new MenuItem("idokep.hu");
		this.idokepHuItem.addActionListener(this);
		popup.add(idokepHuItem);

		this.exitItem = new MenuItem("Kilépés");
		this.exitItem.addActionListener(this);
		this.popup.add(exitItem);

		this.trayIcon.setPopupMenu(popup);

		try {
			this.tray.add(this.trayIcon);
		} catch (AWTException e) {
			/* ha nem jelenik meg, azt úgyis látjuk */
		}
		this.popup.setEnabled(true);
	}

	public void actionPerformed(ActionEvent e) {
		/* kilépés */
		if (e.getSource() == this.exitItem) {
			this.controller.exit();
			/* idokep.hu link */
		} else if (e.getSource() == this.idokepHuItem) {
			this.openWebpage("http://www.idokep.hu/idokep");
			/* értesítés tiltása/engedélyezése */
		} else if (e.getSource() == this.notifyYesNoItem) {
			if (this.controller.isNotifyRequiested()) {
				this.notificationMenu.setLabel("Figyelmeztetés (Kikapcsolva)");
				this.notifyYesNoItem.setLabel("Bekapcsol");
				this.controller.setNotifyRequiestedFromMenu(false);
			} else {
				this.notificationMenu.setLabel("Figyelmeztetés (Bekapcsolva)");
				this.notifyYesNoItem.setLabel("Kikapcsol");
				this.controller.setNotifyRequiestedFromMenu(true);
			}
			/* beállítások megnyitása */
		} else if (e.getSource() == this.settingsItem) {
			this.controller.showSettingsPanel();
			/* bővebb info egy jelentésről */
		} else if (e.getSource() instanceof ReportMenuItem) {
			JOptionPane.showMessageDialog(null,
					((ReportMenuItem) e.getSource()).getReportText(),
					"Információ", JOptionPane.INFORMATION_MESSAGE);
		}
	}

	/**
	 * Eltávolítja az értesítési ikont.
	 */
	public void remove() {
		this.tray.remove(this.trayIcon);
	}

	/**
	 * Frissíti a Figyelmeztetés menüpontot.
	 */
	public void refreshShownSettings() {
		this.popup.setEnabled(false);
		this.popup.removeAll();
		if (this.controller.isNotifyRequiested()) {
			this.notificationMenu.setLabel("Figyelmeztetés (Bekapcsolva)");
			this.notifyYesNoItem.setLabel("Kikapcsol");
		} else {
			this.notificationMenu.setLabel("Figyelmeztetés (Kikapcsolva)");
			this.notifyYesNoItem.setLabel("Bekapcsol");
		}
		this.popup.add(this.currentReportsMenu);
		this.popup.add(this.notificationMenu);
		this.popup.add(this.settingsItem);
		this.popup.add(this.idokepHuItem);
		this.popup.add(this.exitItem);
		this.popup.setEnabled(true);
	}

	/**
	 * Frissíti a menüből elérhetó jelentéseket.
	 * 
	 * @param weatherReports
	 *            Az új jelentések.
	 */
	public void refreshSnowReports(ArrayList<WeatherReport> weatherReports) {
		this.reportCounter = 0;
		/* nem engedi bekövetkezni az update-miközben-látható problémat */
		this.popup.setEnabled(false);
		this.popup.removeAll();
		this.currentReportsMenu.removeAll();
		for (WeatherReport weatherReport : weatherReports) {
			ReportMenuItem reportItem = new ReportMenuItem(weatherReport);
			reportItem.addActionListener(this);
			this.currentReportsMenu.add(reportItem);
			this.reportCounter++;
		}
		this.currentReportsMenu.setLabel("Jelentések (" + this.reportCounter
				+ ")");
		this.popup.add(this.currentReportsMenu);
		this.popup.add(this.notificationMenu);
		this.popup.add(this.settingsItem);
		this.popup.add(this.idokepHuItem);
		this.popup.add(this.exitItem);
		this.popup.setEnabled(true);
	}

	/**
	 * Ha új ellenőrzés futott le, ezt a függvényt meghívva beállítja a
	 * legutóbbi frissítés időpontját a tooltip-ben.
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
		this.trayIcon.setToolTip("Hófigyelő\nFrissítve: " + time);
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
	 * bögéyszővel.
	 * 
	 * @param urlString
	 *            A webolda URL-eje.
	 */
	private void openWebpage(String urlString) {
		try {
			Desktop.getDesktop().browse(new URL(urlString).toURI());
		} catch (Exception e) {
			this.showAlert("Hiba!", "Nem sikerült megnyitni a böngészőt.",
					TrayIcon.MessageType.ERROR);
		}
	}

}
