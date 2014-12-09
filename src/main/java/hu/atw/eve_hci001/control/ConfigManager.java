package hu.atw.eve_hci001.control;

import hu.atw.eve_hci001.model.exception.MalformedConfigFileException;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Be�ll�t�sokat t�rol� �s konfigur�ci�s f�jlt kezel� oszt�ly.
 * 
 * @author �d�m L�szl�
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
	 *            A be�ll�t�sokat tartalmaz� f�jl el�r�si �tvonala a resources
	 *            k�nyvt�rhoz k�pes.
	 */
	public ConfigManager(String configFilePath) {
		this.configFilePath = configFilePath;
		this.notifyRequiested = true;
		this.refreshInterval = 60000;
		this.watchedTypes = new HashMap<String, Boolean>();
		/*
		 * alap�rtelmezett inicializ�l�s, h�val kapcsolatos id�j�r�s esem�nyek
		 * figyel�s�re
		 */
		this.watchedTypes.put("der�lt", false);
		this.watchedTypes.put("gyeng�n felh�s", false);
		this.watchedTypes.put("k�zepesen felh�s", false);
		this.watchedTypes.put("er�sen felh�s", false);
		this.watchedTypes.put("borult", false);
		this.watchedTypes.put("szit�l�s", false);
		this.watchedTypes.put("gyenge es�", false);
		this.watchedTypes.put("es�", false);
		this.watchedTypes.put("�nos es�", false);
		this.watchedTypes.put("z�pores�", false);
		this.watchedTypes.put("havases�", true);
		this.watchedTypes.put("h�sz�lling�z�s", true);
		this.watchedTypes.put("havaz�s", true);
		this.watchedTypes.put("intenz�v havaz�s", true);
		this.watchedTypes.put("h�f�v�s", true);
		this.watchedTypes.put("h�z�por", true);
		this.watchedTypes.put("p�r�ss�g", false);
		this.watchedTypes.put("k�d", false);
		this.watchedTypes.put("sz�raz zivatar", false);
		this.watchedTypes.put("zivatar", false);
		this.watchedTypes.put("h�zivatar", true);
		this.watchedTypes.put("j�ges�", false);
		this.watchedTypes.put("p�ra", false);
		this.watchedTypes.put("z�por", false);
		/* konfigur�ci�s f�jl el�rhet�s�g�nek vizsg�lata */
		try {
			FileReader fr = new FileReader(this.configFilePath);
			fr.close();
			this.configFileAvailable = true;
		} catch (FileNotFoundException fnfe) {
			this.configFileAvailable = false;
		} catch (IOException ioe) {
			/* valami probl�ma lehet a f�jlkezel�ssel */
			this.configFileAvailable = false;
		}
	}

	/**
	 * @return A felhaszn�l� k�rt-e �rtes�t�st.
	 */
	public boolean isNotifyRequiested() {
		return notifyRequiested;
	}

	/**
	 * 
	 * @param notifyRequiested
	 *            A felhaszn�l� k�r-e �rtes�t�st.
	 */
	public void setNotifyRequiested(boolean notifyRequiested) {
		logger.debug("Be�ll�t�s: " + notifyRequiested);
		this.notifyRequiested = notifyRequiested;
	}

	/**
	 * 
	 * @return A friss�t�s gyakoris�ga milliszekundumban.
	 */
	public long getRefreshInterval() {
		return refreshInterval;
	}

	/**
	 * 
	 * @param refreshInterval
	 *            A friss�t�s gyakoris�ga milliszekundumban.
	 */
	public void setRefreshInterval(long refreshInterval) {
		logger.debug("Be�ll�t�s: " + refreshInterval + " ms");
		this.refreshInterval = refreshInterval;
	}

	/**
	 * 
	 * @return El�rhet�-e a konfigur�ci�s f�jl.
	 */
	public boolean isConfigFileAvailable() {
		return configFileAvailable;
	}

	/**
	 * @return A figyelt id�j�r�si �llapotok �s st�tuszuk.
	 */
	public HashMap<String, Boolean> getWatchedTypes() {
		return watchedTypes;
	}

	/**
	 * Ellen�rzi, hogy egy adott id�j�r�si �llaptot figyel-e a felhaszn�l�.
	 * 
	 * @param type
	 *            Az id�j�r�si �llapot.
	 * @return True, ha figyelt, False, ha nem �s null, ha ismeretlen id�j�r�si
	 *         viszony.
	 */
	public Boolean isWatchedType(String type) {
		if (!this.watchedTypes.containsKey(type))
			return null;
		return watchedTypes.get(type);
	}

	/**
	 * Be�ll�tja, hogy egy adott id�j�r�si �llapotot figyeljen-e a program.
	 * 
	 * @param weatherType
	 *            Az id�j�r�si �llapot typusa.
	 * @param b
	 *            Figyelje-e.
	 */
	public void modifyWatchedType(String weatherType, boolean b) {
		logger.debug("Be�ll�t�s: " + weatherType + " = " + b);
		this.watchedTypes.put(weatherType, b);
	}

	/**
	 * Friss�ti a t�rolt be�ll�t�sokat a konfigur�ci�s f�jl alapj�n. Ha az nem
	 * el�rhet�k�nt lett meg�llap�tva az oszt�ly l�trehoz�sakor, nem csin�l
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
	 * A jelenlegi be�ll�t�sokat elmenti a konfigur�ci�s f�jlba, �s
	 * el�rhet�s�g�t igazra �ll�tja. Ha el�zetesen nem el�rhet�k�nt lett
	 * meg�llap�tva, a f�jl l�trehoz�sra ker�l.
	 * 
	 * @throws IOException
	 */
	public synchronized void writeConfigFile() throws IOException {
		System.out.println("F�jlba �r�s");
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(this.configFilePath));
			bw.write("# �rtes�t�s\n");
			bw.write("CONFIG_notifyRequiested = "
					+ (this.notifyRequiested ? "1" : "0") + "\n");
			bw.write("# �rtes�t�s id�z�t�se\n");
			bw.write("CONFIG_refreshInterval = " + this.refreshInterval + "\n");
			bw.write("# figyelt jelent�sek\n");
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
	 * Visszaad egy k�p er�soffr�s f�jlt Image objektumk�nt.
	 * 
	 * @param pathAndFileName
	 *            At k�p el�r�si �tja.
	 * @return A k�p Image objektumk�nt.
	 */
	public static Image getImage(String pathAndFileName) {
		URL url = Thread.currentThread().getContextClassLoader()
				.getResource(pathAndFileName);
		return Toolkit.getDefaultToolkit().getImage(url);
	}

	/**
	 * Beolvassa a konfigur�ci�s f�jl tartalm�t.
	 * 
	 * @return A konfigur�ci�s f�jl sz�veges tartalma.
	 * @throws IOException
	 */
	private synchronized String readConfigFile() throws IOException {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(this.configFilePath));
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
	 * A konfigur�ci�s f�jl sz�veges tartama alapj�n friss�ti a be�ll�t�sokat.
	 * 
	 * @param configText
	 *            A konfigur�ci�s f�jl sz�veges tartama.
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
