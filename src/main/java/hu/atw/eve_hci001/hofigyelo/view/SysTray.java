package hu.atw.eve_hci001.hofigyelo.view;

import hu.atw.eve_hci001.hofigyelo.control.ConfigManager;
import hu.atw.eve_hci001.hofigyelo.control.HofigyeloController;
import hu.atw.eve_hci001.hofigyelo.model.WeatherReport;

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
public class SysTray {
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
		tray = SystemTray.getSystemTray();
		Image image = new ImageIcon(ConfigManager.getImage("snowflake.png"))
				.getImage();
		trayIcon = new TrayIcon(image);
		trayIcon.setImageAutoSize(true);
		trayIcon.setToolTip("Hófigyelő\nFrissítve: soha");
		/* popup menü */
		popup = new PopupMenu();
		/* ha túl hamar jelenítik meg, fennakadhat a program */
		popup.setEnabled(false);

		currentReportsMenu = new Menu("Jelenleg havazik (0)");
		popup.add(currentReportsMenu);

		notificationMenu = new Menu();
		notifyYesNoItem = new MenuItem();
		notificationMenu.setLabel("Figyelmeztetés (?)");
		notifyYesNoItem.setLabel("?");
		notificationMenu.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				controller.setNotifyRequiestedFromMenu(!controller
						.isNotifyRequiested());
				refreshShownSettings();
			}
		});
		notificationMenu.add(notifyYesNoItem);
		popup.add(notificationMenu);

		settingsItem = new MenuItem("Beállítások");
		settingsItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				controller.showSettingsPanel();
			}
		});
		popup.add(settingsItem);

		idokepHuItem = new MenuItem("idokep.hu");
		idokepHuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				openWebpage("http://www.idokep.hu/idokep");
			}
		});
		popup.add(idokepHuItem);

		exitItem = new MenuItem("Kilépés");
		exitItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				controller.exit();
			}
		});
		popup.add(exitItem);

		trayIcon.setPopupMenu(popup);

		try {
			tray.add(trayIcon);
		} catch (AWTException e) {
			/* ha nem jelenik meg, azt úgyis látjuk */
		}
		popup.setEnabled(true);
	}

	/**
	 * Eltávolítja az értesítési ikont.
	 */
	public void remove() {
		tray.remove(trayIcon);
	}

	/**
	 * Frissíti a Figyelmeztetés menüpontot.
	 */
	public void refreshShownSettings() {
		popup.setEnabled(false);
		popup.removeAll();
		if (controller.isNotifyRequiested()) {
			notificationMenu.setLabel("Figyelmeztetés (Bekapcsolva)");
			notifyYesNoItem.setLabel("Kikapcsol");
		} else {
			notificationMenu.setLabel("Figyelmeztetés (Kikapcsolva)");
			notifyYesNoItem.setLabel("Bekapcsol");
		}
		popup.add(currentReportsMenu);
		popup.add(notificationMenu);
		popup.add(settingsItem);
		popup.add(idokepHuItem);
		popup.add(exitItem);
		popup.setEnabled(true);
	}

	/**
	 * Frissíti a menüből elérhetó jelentéseket.
	 * 
	 * @param weatherReports
	 *            Az új jelentések.
	 */
	public void refreshSnowReports(ArrayList<WeatherReport> weatherReports) {
		reportCounter = 0;
		/* nem engedi bekövetkezni az update-miközben-látható problémat */
		popup.setEnabled(false);
		popup.removeAll();
		currentReportsMenu.removeAll();
		for (WeatherReport weatherReport : weatherReports) {
			MenuItem reportItem = new MenuItem(weatherReport.shortDescription());
			reportItem.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					JOptionPane.showMessageDialog(null,
							weatherReport.toString(), "Információ",
							JOptionPane.INFORMATION_MESSAGE);
				}
			});
			currentReportsMenu.add(reportItem);
			reportCounter++;
		}
		currentReportsMenu.setLabel("Jelentések (" + reportCounter + ")");
		popup.add(currentReportsMenu);
		popup.add(notificationMenu);
		popup.add(settingsItem);
		popup.add(idokepHuItem);
		popup.add(exitItem);
		popup.setEnabled(true);
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
		trayIcon.setToolTip("Hófigyelő\nFrissítve: " + time);
	}

	/**
	 * Megjelenít egy értesítést felugró buborékban.
	 * 
	 * @param text
	 *            Az értesítés szövege.
	 */
	public void showAlert(String title, String text, TrayIcon.MessageType mType) {
		trayIcon.displayMessage(title, text, mType);
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
			showAlert("Hiba!", "Nem sikerült megnyitni a böngészőt.",
					TrayIcon.MessageType.ERROR);
		}
	}

}
