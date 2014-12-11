package hu.atw.eve_hci001.model;

/**
 * Egy időjárás bejelentést reprezentálo osztály.
 * 
 * @author Ádám László
 * 
 */
public class WeatherReport {
	private String type;
	private String location;
	private String degree;
	private String time;
	private String user;

	/**
	 * Konstruktor.
	 */
	public WeatherReport() {
		this.type = "-";
		this.location = "-";
		this.time = "-";
		this.user = "-";
		this.degree = "-";
	}

	/**
	 * 
	 * @return Az időjárás jelentés típusa.
	 */
	public String getType() {
		return type;
	}

	/**
	 * 
	 * @param type
	 *            At időjárás jelentés típusa.
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * 
	 * @return A jelentés helye.
	 */
	public String getLocation() {
		return location;
	}

	/**
	 * 
	 * @param location
	 *            A jelentés helye.
	 */
	public void setLocation(String location) {
		this.location = location;
	}

	/**
	 * 
	 * @return A bejelentés ideje.
	 */
	public String getTime() {
		return time;
	}

	/**
	 * 
	 * @param time
	 *            A bejelentés ideje.
	 */
	public void setTime(String time) {
		this.time = time;
	}

	/**
	 * 
	 * @return A bejelentő neve.
	 */
	public String getUser() {
		return user;
	}

	/**
	 * 
	 * @param user
	 *            A bejelentő neve.
	 */
	public void setUser(String user) {
		this.user = user;
	}

	/**
	 * 
	 * @return A hőmérséklet Celsius fokban.
	 */
	public String getDegree() {
		return degree;
	}

	/**
	 * 
	 * @param degree
	 *            A hőmérséklet Celsius fokban.
	 */
	public void setDegree(String degree) {
		this.degree = degree;
	}

	/**
	 * Visszaadja a jelentés rövid leírását (typus, hely, időpont)
	 * 
	 * @return A jelentés rövid leírása.
	 */
	public String shortDescription() {
		String desc = this.type + " - " + this.location + ", " + this.time;
		return desc;
	}

	@Override
	public String toString() {
		return "Jelentés:     " + type + "\nHely:     " + location
				+ "\nHőmérséklet:     " + degree + "\nIdőpont:     " + time
				+ "\nBeküldő:     " + user;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((degree == null) ? 0 : degree.hashCode());
		result = prime * result
				+ ((location == null) ? 0 : location.hashCode());
		result = prime * result + ((time == null) ? 0 : time.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((user == null) ? 0 : user.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WeatherReport other = (WeatherReport) obj;
		if (degree == null) {
			if (other.degree != null)
				return false;
		} else if (!degree.equals(other.degree))
			return false;
		if (location == null) {
			if (other.location != null)
				return false;
		} else if (!location.equals(other.location))
			return false;
		if (time == null) {
			if (other.time != null)
				return false;
		} else if (!time.equals(other.time))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		if (user == null) {
			if (other.user != null)
				return false;
		} else if (!user.equals(other.user))
			return false;
		return true;
	}

}
