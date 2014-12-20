package hu.atw.eve_hci001.hofigyelo.view;

import hu.atw.eve_hci001.hofigyelo.control.ConfigManager;
import hu.atw.eve_hci001.hofigyelo.control.HofigyeloController;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 * Beállításokat tartalmazó ablakot megjelenítő osztály.
 * 
 * @author László Ádám
 * 
 */
public class SettingsPanel {
	private HofigyeloController controller;
	private JButton applyButton;
	private JButton closeButton;
	private JButton refreshNowButton;
	private JCheckBox notifyRequiestedBox;
	private JComboBox<String> intervalBox;
	private final long[] intervals = { 30000, 60000, 300000, 600000, 1800000,
			3600000 };
	private JFrame frame;

	/**
	 * Konstruktor. Inicializálja a grafikus elemeket.
	 * 
	 * @param controller
	 *            A controller objektum.
	 */
	public SettingsPanel(HofigyeloController controller) {
		this.controller = controller;
		frame = new JFrame("Beállítások - Hófigyelő");
		Image image = new ImageIcon(ConfigManager.getImage("snowflake.png"))
				.getImage();
		frame.setIconImage(image);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent evt) {
				onExit();
			}
		});

		/* tab és gomb mezők */
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout());
		frame.getContentPane().add(topPanel, BorderLayout.CENTER);

		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		frame.getContentPane().add(buttonsPanel, BorderLayout.PAGE_END);
		applyButton = new JButton("Megjegyzés");
		applyButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				controller.writeSettings();
				applyButton.setEnabled(false);
			}
		});
		applyButton.setEnabled(false);
		buttonsPanel.add(applyButton);
		closeButton = new JButton("Bezár");
		closeButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				onExit();
			}
		});
		buttonsPanel.add(closeButton);

		/* tabok */
		JPanel refreshSettingsTab = createRefreshSettingsTab();
		JPanel watchedSettingsTab = createWatchedSettingsTab();
		JPanel infoTab = createInfoTab();

		JTabbedPane tabs = new JTabbedPane();
		tabs.addTab("Frissítés", refreshSettingsTab);
		tabs.addTab("Figyelés", watchedSettingsTab);
		tabs.addTab("Info", infoTab);
		topPanel.add(tabs);

		frame.pack();
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	/**
	 * Frissíti a figyelmeztetési beállítást
	 */
	public void refreshNotifySetting() {
		this.notifyRequiestedBox.setSelected(controller.isNotifyRequiested());
	}

	/**
	 * Elkészíti a figyelési beállítások fület.
	 * 
	 * @return A figyelési beállítások fül.
	 */
	private JPanel createWatchedSettingsTab() {
		JPanel watchedSettingsTab = new JPanel();
		HashMap<String, Boolean> watchedTypes = controller.getWatchedTypes();
		watchedSettingsTab.setLayout(new GridLayout(
				(watchedTypes.size() / 2) + 1, 2));
		for (String key : watchedTypes.keySet()) {
			JCheckBox checkBox = new JCheckBox(key);
			checkBox.setSelected(watchedTypes.get(key));
			checkBox.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					controller.modifyWatchedType(checkBox.getText(),
							checkBox.isSelected());
					applyButton.setEnabled(true);
				}
			});
			watchedSettingsTab.add(checkBox);
		}
		return watchedSettingsTab;
	}

	/**
	 * Elkészíti a figyelési beállítások fület.
	 * 
	 * @return A figyelési beállítások fül.
	 */
	private JPanel createRefreshSettingsTab() {
		JPanel refreshSettingsTab = new JPanel();
		refreshSettingsTab.setLayout(new GridLayout(6, 1));
		JPanel forComboBox = new JPanel();
		forComboBox.setLayout(new FlowLayout(FlowLayout.LEFT));
		JLabel intervalText = new JLabel("Frissítés gyakorisága");
		forComboBox.add(intervalText);
		intervalBox = new JComboBox<String>();
		intervalBox.addItem("30  másodperc");
		intervalBox.addItem("1 perc");
		intervalBox.addItem("5 perc");
		intervalBox.addItem("10 perc");
		intervalBox.addItem("30 perc");
		intervalBox.addItem("1 óra");
		for (int i = 0; i < this.intervals.length; i++) {
			if (controller.getRefreshInterval() == intervals[i]) {
				intervalBox.setSelectedIndex(i);
				break;
			}
		}
		intervalBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (intervalBox.getSelectedIndex() == -1)
					return;
				controller.setRefreshInterval(intervals[intervalBox
						.getSelectedIndex()]);
				applyButton.setEnabled(true);
			}
		});
		forComboBox.add(this.intervalBox);
		refreshSettingsTab.add(forComboBox);

		notifyRequiestedBox = new JCheckBox("Figyelmeztetés");
		notifyRequiestedBox.setSelected(controller.isNotifyRequiested());
		notifyRequiestedBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				controller.setNotifyRequiestedFromPanel(notifyRequiestedBox
						.isSelected());
				applyButton.setEnabled(true);
			}
		});
		refreshSettingsTab.add(notifyRequiestedBox);

		JPanel forRefreshButton = new JPanel();
		forRefreshButton.setLayout(new FlowLayout(FlowLayout.LEFT));
		refreshNowButton = new JButton("Frissítés most");
		refreshNowButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				controller.refreshReportsNow();
			}
		});
		forRefreshButton.add(refreshNowButton);
		refreshSettingsTab.add(forRefreshButton);
		return refreshSettingsTab;
	}

	/**
	 * Elkészíti az info fület.
	 * 
	 * @return Az info fül.
	 */
	private JPanel createInfoTab() {
		JPanel refreshSettingsTab = new JPanel();
		JLabel infoLabel = new JLabel();
		String title = "Hófigyelő 1.1";
		String mail = "adam.laszlo.91@gmail.com";
		String http = "http://www.http://eve-hci001.atw.hu/";
		String longInfo = "A program a bejelentéseket a<br><a href=\"http://www.idokep.hu\">http://www.idokep.hu</a> -ról szerzi be.<br>Ezen program nem az említett oldal hivatalos alkalmazása,<br> és a készítő nem kívánja sem aként feltüntetni,<br> sem kereskedelmi forgalomba hozni.";
		infoLabel.setText("<html><u>" + title + "</u><br><br><b>e-mail</b>: "
				+ mail + "<br><b>web:</b> <a href=\"" + http + "\">" + http
				+ "</a><br><br>" + longInfo + "</html>");
		refreshSettingsTab.add(infoLabel);
		return refreshSettingsTab;
	}

	/**
	 * Az ablak bezárása.
	 */
	private void onExit() {
		frame.setVisible(false);
		frame.dispose();
		controller.destroySettingsPanel();
	}
}
