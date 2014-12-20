package hu.atw.eve_hci001.hofigyelo.control;

import hu.atw.eve_hci001.hofigyelo.model.exception.MalformedConfigFileException;

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
		notifyRequiested = true;
		refreshInterval = 60000;
		watchedTypes = new HashMap<String, Boolean>();
		/*
		 * alapértelmezett inicializálás: hóval kapcsolatos időjárás események
		 * figyelésére
		 */
		watchedTypes.put("derült", false);
		watchedTypes.put("gyengén felhős", false);
		watchedTypes.put("közepesen felhős", false);
		watchedTypes.put("erősen felhős", false);
		watchedTypes.put("borult", false);
		watchedTypes.put("szitálás", false);
		watchedTypes.put("gyenge eső", false);
		watchedTypes.put("eső", false);
		watchedTypes.put("ónoseső", false);
		watchedTypes.put("záporeső", false);
		watchedTypes.put("havaseső", true);
		watchedTypes.put("hószállingózás", true);
		watchedTypes.put("havazás", true);
		watchedTypes.put("intenzív havazás", true);
		watchedTypes.put("hófúvás", true);
		watchedTypes.put("hózápor", true);
		watchedTypes.put("párásság", false);
		watchedTypes.put("köd", false);
		watchedTypes.put("száraz zivatar", false);
		watchedTypes.put("zivatar", false);
		watchedTypes.put("hózivatar", true);
		watchedTypes.put("jégeső", false);
		watchedTypes.put("pára", false);
		watchedTypes.put("zápor", false);
		/* konfigurációs fájl elérhetőségének vizsgálata */
		try {
			FileReader fr = new FileReader(configFilePath);
			fr.close();
			configFileAvailable = true;
		} catch (FileNotFoundException fnfe) {
			configFileAvailable = false;
		} catch (IOException ioe) {
			/* valami probléma lehet a fájlkezeléssel */
			configFileAvailable = false;
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
		logger.debug("Jelzés beállítás: " + notifyRequiested);
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
		logger.debug("Időköz beállítás: " + refreshInterval + " ms");
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
		if (!watchedTypes.containsKey(type))
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
		logger.debug("Figyelés beállítása: " + weatherType + " = " + b);
		watchedTypes.put(weatherType, b);
	}

	/**
	 * Frissíti a tárolt beállításokat a konfigurációs fájl alapján. Ha az nem
	 * elérhetőként lett megállapítva az osztály létrehozásakor, nem csinál
	 * semmit.
	 */
	public synchronized void refreshConfig() throws IOException,
			MalformedConfigFileException {
		if (!configFileAvailable)
			return;
		String configText = readConfigFile();
		buildConfig(configText);
	}

	/**
	 * A jelenlegi beállításokat elmenti a konfigurációs fájlba, és
	 * elérhetőségét igazra állítja. Ha előzetesen nem elérhetőként lett
	 * megállapítva, a fájl létrehozásra kerül.
	 * 
	 * @throws IOException
	 */
	public synchronized void writeConfigFile() throws IOException {
		logger.debug("Fájlba írás");
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(configFilePath), "UTF-8"));
			bw.write("# értesítés\n");
			bw.write("CONFIG_notifyRequiested = "
					+ (notifyRequiested ? "1" : "0") + "\n");
			bw.write("# értesítés időzítése\n");
			bw.write("CONFIG_refreshInterval = " + refreshInterval + "\n");
			bw.write("# figyelt jelentések\n");
			for (String key : watchedTypes.keySet()) {
				bw.write(key.replace(" ", "_") + " = "
						+ (watchedTypes.get(key) ? "1" : "0") + "\n");
			}
			configFileAvailable = true;
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
					configFilePath), "UTF-8"));
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
						notifyRequiested = true;
					} else {
						notifyRequiested = false;
					}
				} else if (pairs[0].equals("CONFIG_refreshInterval")) {
					refreshInterval = Integer.parseInt(pairs[1]);
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
