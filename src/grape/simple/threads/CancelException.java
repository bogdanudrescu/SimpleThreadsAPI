package grape.simple.threads;

/**
 * Thrown when a running process was canceled.
 * 
 * @author Bogdan Udrescu (bogdan.udrescu@gmail.com) 
 */
public class CancelException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * Create a simple exception.
	 */
	public CancelException() {
	}

}
