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

/**
 * Az �rtes�t�si ikont kezel� oszt�ly.
 * 
 * @author �d�m L�szl�
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
		this.trayIcon.setToolTip("H�figyel�\nFriss�tve: soha");
		/* popup men� */
		this.popup = new PopupMenu();
		/* ha t�l hamar jelen�tik meg, fennakadhat a program */
		this.popup.setEnabled(false);

		this.currentReportsMenu = new Menu("Jelenleg havazik (0)");
		this.popup.add(currentReportsMenu);

		this.notificationMenu = new Menu();
		this.notifyYesNoItem = new MenuItem();
		this.notificationMenu.setLabel("Figyelmeztet�s (?)");
		this.notifyYesNoItem.setLabel("?");
		this.notifyYesNoItem.addActionListener(this);
		this.notificationMenu.add(this.notifyYesNoItem);
		this.popup.add(this.notificationMenu);

		this.settingsItem = new MenuItem("Be�ll�t�sok");
		this.settingsItem.addActionListener(this);
		this.popup.add(this.settingsItem);

		this.idokepHuItem = new MenuItem("idokep.hu");
		this.idokepHuItem.addActionListener(this);
		popup.add(idokepHuItem);

		this.exitItem = new MenuItem("Kil�p�s");
		this.exitItem.addActionListener(this);
		this.popup.add(exitItem);

		this.trayIcon.setPopupMenu(popup);

		try {
			this.tray.add(this.trayIcon);
		} catch (AWTException e) {
			/* ha nem jelenik meg, azt �gyis l�tjuk */
		}
		this.popup.setEnabled(true);
	}

	public void actionPerformed(ActionEvent e) {
		/* kil�p�s */
		if (e.getSource() == this.exitItem) {
			this.controller.exit();
			/* idokep.hu link */
		} else if (e.getSource() == this.idokepHuItem) {
			this.openWebpage("http://www.idokep.hu/idokep");
			/* �rtes�t�s tilt�sa/enged�lyez�se */
		} else if (e.getSource() == this.notifyYesNoItem) {
			if (this.controller.isNotifyRequiested()) {
				this.notificationMenu.setLabel("Figyelmeztet�s (Kikapcsolva)");
				this.notifyYesNoItem.setLabel("Bekapcsol");
				this.controller.setNotifyRequiestedFromMenu(false);
			} else {
				this.notificationMenu.setLabel("Figyelmeztet�s (Bekapcsolva)");
				this.notifyYesNoItem.setLabel("Kikapcsol");
				this.controller.setNotifyRequiestedFromMenu(true);
			}
		} else if (e.getSource() == this.settingsItem) {
			this.controller.showSettingsPanel();
		}
	}

	/**
	 * Elt�vol�tja az �rtes�t�si ikont.
	 */
	public void remove() {
		this.tray.remove(this.trayIcon);
	}

	/**
	 * Friss�ti a Figyelmeztet�s men�pontot.
	 */
	public void refreshShownSettings() {
		this.popup.setEnabled(false);
		this.popup.removeAll();
		if (this.controller.isNotifyRequiested()) {
			this.notificationMenu.setLabel("Figyelmeztet�s (Bekapcsolva)");
			this.notifyYesNoItem.setLabel("Kikapcsol");
		} else {
			this.notificationMenu.setLabel("Figyelmeztet�s (Kikapcsolva)");
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
	 * Friss�ti a men�b�l el�rhet� jelent�seket.
	 * 
	 * @param weatherReports
	 *            Az �j jelent�sek.
	 */
	public void refreshSnowReports(ArrayList<WeatherReport> weatherReports) {
		this.reportCounter = 0;
		/* nem engedi bek�vetkezni az update-mik�zben-l�that� probl�mat */
		this.popup.setEnabled(false);
		this.popup.removeAll();
		this.currentReportsMenu.removeAll();
		for (WeatherReport weatherReport : weatherReports) {
			MenuItem snowItem = new MenuItem(weatherReport.getType() + " - "
					+ weatherReport.getLocation() + ", "
					+ weatherReport.getTime());
			this.currentReportsMenu.add(snowItem);
			this.reportCounter++;
		}
		this.currentReportsMenu.setLabel("Jelent�sek (" + this.reportCounter
				+ ")");
		this.popup.add(this.currentReportsMenu);
		this.popup.add(this.notificationMenu);
		this.popup.add(this.settingsItem);
		this.popup.add(this.idokepHuItem);
		this.popup.add(this.exitItem);
		this.popup.setEnabled(true);
	}

	/**
	 * Ha �j ellen�rz�s futott le, ezt a f�ggv�nyt megh�vva be�ll�tja a
	 * legut�bbi friss�t�s id�pontj�t a tooltip-ben.
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
					TrayIcon.MessageType.ERROR);
		}
	}

}
