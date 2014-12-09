package hu.atw.eve_hci001.control;

import hu.atw.eve_hci001.model.exception.MalformedConfigFileException;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Beállításokat tároló és konfigurációs fájlt kezelő osztály.
 * 
 * @author Ádám László
 * 
 */

public class ConfigManager {
	final Logger logger = LoggerFactory.getLogger(ConfigManager.class);
	private boolean notifyRequiested;
	private long refreshInterval;
	private HashMap<String, Boolean> watchedTypes;
	private boolean configFileAvailable;
	private String configFilePath;

	/**
	 * Konstruktor.
	 * 
	 * @param configFilePath
	 *            A beállításokat tartalmazó fájl elérési útvonala a resources
	 *            könyvtárhoz képes.
	 */
	public ConfigManager(String configFilePath) {
		this.configFilePath = configFilePath;
		this.notifyRequiested = true;
		this.refreshInterval = 60000;
		this.watchedTypes = new HashMap<String, Boolean>();
		/*
		 * alapértelmezett inicializálás, hóval kapcsolatos időjárás események
		 * figyelésére
		 */
		this.watchedTypes.put("derült", false);
		this.watchedTypes.put("gyengén felhős", false);
		this.watchedTypes.put("közepesen felhős", false);
		this.watchedTypes.put("erősen felhős", false);
		this.watchedTypes.put("borult", false);
		this.watchedTypes.put("szitálás", false);
		this.watchedTypes.put("gyenge eső", false);
		this.watchedTypes.put("eső", false);
		this.watchedTypes.put("ónos eső", false);
		this.watchedTypes.put("záporeső", false);
		this.watchedTypes.put("havaseső", true);
		this.watchedTypes.put("hószállingózás", true);
		this.watchedTypes.put("havazás", true);
		this.watchedTypes.put("intenzív havazás", true);
		this.watchedTypes.put("hófúvás", true);
		this.watchedTypes.put("hózápor", true);
		this.watchedTypes.put("párásság", false);
		this.watchedTypes.put("köd", false);
		this.watchedTypes.put("száraz zivatar", false);
		this.watchedTypes.put("zivatar", false);
		this.watchedTypes.put("hózivatar", true);
		this.watchedTypes.put("jégeső", false);
		this.watchedTypes.put("pára", false);
		this.watchedTypes.put("zápor", false);
		/* konfigurációs fájl elérhetőségének vizsgálata */
		try {
			FileReader fr = new FileReader(this.configFilePath);
			fr.close();
			this.configFileAvailable = true;
		} catch (FileNotFoundException fnfe) {
			this.configFileAvailable = false;
		} catch (IOException ioe) {
			/* valami probléma lehet a fájlkezeléssel */
			this.configFileAvailable = false;
		}
	}

	/**
	 * @return A felhasználó kért-e értesítést.
	 */
	public boolean isNotifyRequiested() {
		return notifyRequiested;
	}

	/**
	 * 
	 * @param notifyRequiested
	 *            A felhasználó kér-e értesítést.
	 */
	public void setNotifyRequiested(boolean notifyRequiested) {
		logger.debug("Beállítás: " + notifyRequiested);
		this.notifyRequiested = notifyRequiested;
	}

	/**
	 * 
	 * @return A frissítés gyakorisága milliszekundumban.
	 */
	public long getRefreshInterval() {
		return refreshInterval;
	}

	/**
	 * 
	 * @param refreshInterval
	 *            A frissítés gyakorisága milliszekundumban.
	 */
	public void setRefreshInterval(long refreshInterval) {
		logger.debug("Beállítás: " + refreshInterval + " ms");
		this.refreshInterval = refreshInterval;
	}

	/**
	 * 
	 * @return Elérhető-e a konfigurációs fájl.
	 */
	public boolean isConfigFileAvailable() {
		return configFileAvailable;
	}

	/**
	 * @return A figyelt időjárási állapotok és státuszuk.
	 */
	public HashMap<String, Boolean> getWatchedTypes() {
		return watchedTypes;
	}

	/**
	 * Ellenőrzi, hogy egy adott időjárási állaptot figyel-e a felhasználó.
	 * 
	 * @param type
	 *            Az időjárási állapot.
	 * @return True, ha figyelt, False, ha nem és null, ha ismeretlen időjárási
	 *         viszony.
	 */
	public Boolean isWatchedType(String type) {
		if (!this.watchedTypes.containsKey(type))
			return null;
		return watchedTypes.get(type);
	}

	/**
	 * Beállítja, hogy egy adott időjárási állapotot figyeljen-e a program.
	 * 
	 * @param weatherType
	 *            Az időjárási állapot typusa.
	 * @param b
	 *            Figyelje-e.
	 */
	public void modifyWatchedType(String weatherType, boolean b) {
		logger.debug("Beállítás: " + weatherType + " = " + b);
		this.watchedTypes.put(weatherType, b);
	}

	/**
	 * Frissíti a tárolt beállításokat a konfigurációs fájl alapján. Ha az nem
	 * elérhetőként lett megállapítva az osztály létrehozásakor, nem csinál
	 * semmit.
	 */
	public synchronized void refreshConfig() throws IOException,
			MalformedConfigFileException {
		if (!this.configFileAvailable)
			return;
		String configText = this.readConfigFile();
		this.buildConfig(configText);
	}

	/**
	 * A jelenlegi beállításokat elmenti a konfigurációs fájlba, és
	 * elérhetőségét igazra állítja. Ha előzetesen nem elérhetőként lett
	 * megállapítva, a fájl létrehozásra kerül.
	 * 
	 * @throws IOException
	 */
	public synchronized void writeConfigFile() throws IOException {
		System.out.println("Fájlba írás");
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(this.configFilePath), "UTF-8"));
			bw.write("# értesítés\n");
			bw.write("CONFIG_notifyRequiested = "
					+ (this.notifyRequiested ? "1" : "0") + "\n");
			bw.write("# értesítés időzítése\n");
			bw.write("CONFIG_refreshInterval = " + this.refreshInterval + "\n");
			bw.write("# figyelt jelentések\n");
			for (String key : this.watchedTypes.keySet()) {
				bw.write(key.replace(" ", "_") + " = "
						+ (watchedTypes.get(key) ? "1" : "0") + "\n");
			}
			this.configFileAvailable = true;
		} catch (IOException ioe) {
			throw ioe;
		} finally {
			if (bw != null) {
				bw.close();
			}
		}

	}

	/**
	 * Visszaad egy kép erőforrás fájlt Image objektumként.
	 * 
	 * @param pathAndFileName
	 *            At kép elérési útja.
	 * @return A kép Image objektumként.
	 */
	public static Image getImage(String pathAndFileName) {
		URL url = Thread.currentThread().getContextClassLoader()
				.getResource(pathAndFileName);
		return Toolkit.getDefaultToolkit().getImage(url);
	}

	/**
	 * Beolvassa a konfigurációs fájl tartalmát.
	 * 
	 * @return A konfigurációs fájl szöveges tartalma.
	 * @throws IOException
	 */
	private synchronized String readConfigFile() throws IOException {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(
					this.configFilePath), "UTF-8"));
			StringBuilder builder = new StringBuilder();
			String file = "";
			while ((file = br.readLine()) != null) {
				builder.append(file);
				builder.append("\n");
			}
			return builder.toString();
		} catch (IOException ioe) {
			throw ioe;
		} finally {
			if (br != null) {
				br.close();
			}
		}

	}

	/**
	 * A konfigurációs fájl szöveges tartama alapján frissíti a beállításokat.
	 * 
	 * @param configText
	 *            A konfigurációs fájl szöveges tartama.
	 * @throws MalformedConfigFileException
	 */
	private void buildConfig(String configText)
			throws MalformedConfigFileException {
		if (configText.equals(""))
			return;
		try {
			configText = configText.replace(" ", "");
			configText = configText.replace("\t", "");
			String lines[] = configText.split("\n");
			for (int i = 0; i < lines.length; i++) {
				if (lines[i].startsWith("#"))
					continue;
				String pairs[] = lines[i].split("=");
				if (pairs[0].equals("CONFIG_notifyRequiested")) {
					if (pairs[1].equals("1")) {
						this.notifyRequiested = true;
					} else {
						this.notifyRequiested = false;
					}
				} else if (pairs[0].equals("CONFIG_refreshInterval")) {
					this.refreshInterval = Integer.parseInt(pairs[1]);
				} else {
					if (pairs[1].equals("1")) {
						watchedTypes.put(pairs[0].replace("_", " "), true);
					} else if (pairs[1].equals("0")) {
						watchedTypes.put(pairs[0].replace("_", " "), false);
					}
				}
			}
		} catch (Exception e) {
			throw new MalformedConfigFileException();
		}
	}

}
