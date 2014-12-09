package hu.atw.eve_hci001.model.exception;

public class MalformedConfigFileException extends Exception {

	/**
	 * Exception osztály a konfigurációs fájl hibás felépítésének jelzésére.
	 */
	private static final long serialVersionUID = 1L;

	public MalformedConfigFileException() {
	}

	public MalformedConfigFileException(String message) {
		super(message);
	}

	public MalformedConfigFileException(Throwable cause) {
		super(cause);
	}

	public MalformedConfigFileException(String message, Throwable cause) {
		super(message, cause);
	}

	public MalformedConfigFileException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
