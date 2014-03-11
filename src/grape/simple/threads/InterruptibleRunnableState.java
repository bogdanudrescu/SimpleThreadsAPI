package grape.simple.threads;

/**
 * The list of states an interruptible runnable can take.
 * 
 * @author Bogdan Udrescu (bogdan.udrescu@gmail.com)
 */
public enum InterruptibleRunnableState {

	/**
	 * The initial state of the process in its holding phase.
	 */
	NOT_RUNNING,

	/**
	 * The running state of the process.
	 */
	RUNNING,

	/**
	 * The paused state of the process.
	 */
	PAUSED,

	/**
	 * The canceled state of the process. A process which terminates because of a cancel call will remain 
	 * with status CANCELED until started again. A process which finish normally should move to NOT_RUNNING state.
	 */
	CANCELED

}
