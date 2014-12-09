package hu.atw.eve_hci001.view;

import hu.atw.eve_hci001.control.ConfigManager;
import hu.atw.eve_hci001.control.HofigyeloController;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
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
public class SettingsPanel implements ActionListener {
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
		this.frame = new JFrame("Beállítások - Hófigyelő");
		Image image = new ImageIcon(ConfigManager.getImage("snowflake.png"))
				.getImage();
		frame.setIconImage(image);
		this.frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent evt) {
				onExit();
			}
		});

		/* tab és gomb mezők */
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout());
		this.frame.getContentPane().add(topPanel, BorderLayout.CENTER);

		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		this.frame.getContentPane().add(buttonsPanel, BorderLayout.PAGE_END);
		this.applyButton = new JButton("Megjegyzés");
		this.applyButton.addActionListener(this);
		this.applyButton.setEnabled(false);
		buttonsPanel.add(applyButton);
		this.closeButton = new JButton("Bezár");
		this.closeButton.addActionListener(this);
		buttonsPanel.add(this.closeButton);

		/* tabok */
		JPanel refreshSettingsTab = this.createRefreshSettingsTab();
		JPanel watchedSettingsTab = this.createWatchedSettingsTab();
		JPanel infoTab = this.createInfoTab();

		JTabbedPane tabs = new JTabbedPane();
		tabs.addTab("Frissítés", refreshSettingsTab);
		tabs.addTab("Figyelés", watchedSettingsTab);
		tabs.addTab("Info", infoTab);
		topPanel.add(tabs);

		this.frame.pack();
		this.frame.setResizable(false);
		this.frame.setLocationRelativeTo(null);
		this.frame.setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JCheckBox) {
			JCheckBox checkBox = (JCheckBox) e.getSource();
			if (checkBox.getMnemonic() == KeyEvent.VK_SEMICOLON) {
				// egy időjárás típus figyelése változott
				this.controller.modifyWatchedType(checkBox.getText(),
						checkBox.isSelected());
				this.applyButton.setEnabled(true);
			} else if (e.getSource() == this.notifyRequiestedBox) {
				this.controller.setNotifyRequiestedFromPanel(((JCheckBox) e
						.getSource()).isSelected());
				this.applyButton.setEnabled(true);
			}
			/* ablak bezására */
		} else if (e.getSource() == this.closeButton) {
			this.onExit();
			/* beállítások alkalmazása */
		} else if (e.getSource() == this.applyButton) {
			this.controller.writeSettings();
			this.applyButton.setEnabled(false);
			/* frissítés most */
		} else if (e.getSource() == this.refreshNowButton) {
			this.controller.refreshReportsNow();
			/* intervallum beállítása */
		} else if (e.getSource() == this.intervalBox) {
			if (this.intervalBox.getSelectedIndex() == -1)
				return;
			this.controller.setRefreshInterval(this.intervals[this.intervalBox
					.getSelectedIndex()]);
			this.applyButton.setEnabled(true);
			/* figyelmeztetés */
		}
	}

	/**
	 * Frissíti a figyelmeztetési beállítást
	 */
	public void refreshNotifySetting() {
		this.notifyRequiestedBox.setSelected(this.controller
				.isNotifyRequiested());
	}

	/**
	 * Elkészíti a figyelési beállítások fület.
	 * 
	 * @return A figyelési beállítások fül.
	 */
	private JPanel createWatchedSettingsTab() {
		JPanel watchedSettingsTab = new JPanel();
		HashMap<String, Boolean> watchedTypes = this.controller
				.getWatchedTypes();
		watchedSettingsTab.setLayout(new GridLayout(
				(watchedTypes.size() / 2) + 1, 2));
		for (String key : watchedTypes.keySet()) {
			JCheckBox checkBox = new JCheckBox(key);
			checkBox.setSelected(watchedTypes.get(key));
			/*
			 * mnemonic alapján azonosítható, hogy a cehckbox egy időjárás
			 * típushoz van rendelve
			 */
			checkBox.setMnemonic(KeyEvent.VK_SEMICOLON);
			checkBox.addActionListener(this);
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
		this.intervalBox = new JComboBox<String>();
		this.intervalBox.addItem("30  másodperc");
		this.intervalBox.addItem("1 perc");
		this.intervalBox.addItem("5 perc");
		this.intervalBox.addItem("10 perc");
		this.intervalBox.addItem("30 perc");
		this.intervalBox.addItem("1 óra");
		for (int i = 0; i < this.intervals.length; i++){
			if (this.controller.getRefreshInterval() == this.intervals[i]){
				this.intervalBox.setSelectedIndex(i);
				break;
			}
		}
		this.intervalBox.addActionListener(this);
		forComboBox.add(this.intervalBox);
		refreshSettingsTab.add(forComboBox);

		this.notifyRequiestedBox = new JCheckBox("Figyelmeztetés");
		this.notifyRequiestedBox.setSelected(this.controller
				.isNotifyRequiested());
		this.notifyRequiestedBox.addActionListener(this);
		refreshSettingsTab.add(this.notifyRequiestedBox);

		JPanel forRefreshButton = new JPanel();
		forRefreshButton.setLayout(new FlowLayout(FlowLayout.LEFT));
		this.refreshNowButton = new JButton("Frissítés most");
		this.refreshNowButton.addActionListener(this);
		forRefreshButton.add(this.refreshNowButton);
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
		String title = "Hófigyelő 1.0";
		String mail = "adam.laszlo.91@gmail.com";
		String http = "http://www.http://eve-hci001.atw.hu/";
		String longInfo = "A program a bejelentéseket a<br><a href=\"http://www.idokep.hu\">http://www.idokep.hu</a> -ról szerzi be.<br>Ezen program nem az említett oldal hivatalos alkalmazása,<br> és a készítő nem kívánja sem aként feltüntetni,<br> sem kereskedelmi forgalomba hozni.";
		infoLabel.setText("<html><u>" + title + "</u><br><br><b>e-mail</b>: " + mail
				+ "<br><b>web:</b> <a href=\"" + http + "\">" + http + "</a><br><br>" + longInfo + "</html>");
		refreshSettingsTab.add(infoLabel);
		return refreshSettingsTab;
	}

	/**
	 * Az ablak bezárása.
	 */
	private void onExit() {
		this.frame.setVisible(false);
		this.frame.dispose();
		this.controller.destroySettingsPanel();
	}
}
