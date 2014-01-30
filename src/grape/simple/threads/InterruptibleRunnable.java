package grape.simple.threads;

/**
 * Define cancel and pause functionalities to {@link Runnable}. The implementor of the <code>run()</code> method should check regularly 
 * if the runnable has been canceled using <code>isCanceled()</code> method and stop the execution of the process as soon as possible
 * making sure no data corruption will occur from this.
 * 
 * @author Bogdan Udrescu (bogdan.udrescu@gmail.com) 
 */
public interface InterruptibleRunnable extends Runnable {

	/**
	 * Gets whether the process should cancel its execution.
	 * @return	true if the process should stop.
	 */
	boolean isCanceled();

	/**
	 * Gets whether the process should paused its execution.
	 * @return	true if the process should paused.
	 */
	boolean isPaused();

	/**
	 * Gets whether the process is running.
	 * @return	true if the process is running.
	 */
	boolean isRunning();

	/**
	 * Cancel the current execution of this runnable.
	 */
	void cancel();

	/**
	 * Pause the runnable.
	 */
	void pause();

	/**
	 * Restart the runnable.
	 */
	void restart();

	/**
	 * Gets the name of the task.
	 * @return	the name of the task.
	 */
	String getName();

	/**
	 * Gets the description of the task.
	 * @return	the description of the task.
	 */
	String getDescription();

	/**
	 * Destroy the runnable for good.
	 */
	void destroy();

}
