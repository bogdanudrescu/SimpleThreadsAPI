package grape.simple.threads;

/**
 * Thrown when a running process was canceled.
 * 
 * @author Bogdan Udrescu (bogdan.udrescu@gmail.com) 
 */
@SuppressWarnings("serial")
public class CancelException extends RuntimeException {

	/**
	 * Create a simple exception.
	 */
	public CancelException() {
	}

}
