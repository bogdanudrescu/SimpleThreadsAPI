package grape.simple.threads;

import java.beans.PropertyChangeListener;

/**
 * Define cancel and pause functionalities to {@link Runnable}. The implementor
 * of the <code>run()</code> method should check regularly if the runnable has
 * been canceled using <code>isCanceled()</code> method and stop the execution
 * of the process as soon as possible making sure no data corruption will occur
 * from this.
 * 
 * @author Bogdan Udrescu (bogdan.udrescu@gmail.com)
 */
public interface InterruptibleRunnable extends Runnable {

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
    void resume();

    /**
     * Gets the state of the process as imposed by the user, by calling
     * {@link #cancel()}, {@link #pause()} or {@link #resume()}.
     * 
     * @return the state of the process.
     * @see InterruptibleRunnableState
     */
    public InterruptibleRunnableState getState();

    /**
     * Gets the real state of the process. A process won't stop or paused right
     * away when the {@link #pause()}, {@link #cancel()} or {@link #resume()}
     * are called. These methods must anyway be called from another Thread then
     * the one executing the Runnable. So the process will get paused or
     * canceled or resumed a bit later.
     * 
     * @return the real state of the process.
     * @see InterruptibleRunnableState
     */
    public InterruptibleRunnableState getRealState();

    /**
     * Register a listener that will be notified when the state of the process
     * or any other property will change.
     * 
     * @param listener
     *            the listener to be registered.
     */
    public void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * Register a listener that will be notified when the specified property
     * will change.
     * 
     * @param propertyName
     *            the name of the property.
     * @param listener
     *            the listener to be registered.
     */
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener);

    /**
     * Gets the percent of download data so far.
     * 
     * @return the percent of download data so far.
     */
    public float getPercentCompleted();

    /**
     * Gets whether the process should cancel its execution.
     * 
     * @return true if the process should stop.
     * @deprecated use {@link #getState()} or {@link #getRealState()}.
     */
    boolean isCanceled();

    /**
     * Gets whether the process should paused its execution.
     * 
     * @return true if the process should paused.
     * @deprecated use {@link #getState()} or {@link #getRealState()}.
     */
    boolean isPaused();

    /**
     * Gets whether the process is running.
     * 
     * @return true if the process is running.
     * @deprecated use {@link #getState()} or {@link #getRealState()}.
     */
    boolean isRunning();

    /**
     * Gets the name of the task.
     * 
     * @return the name of the task.
     */
    String getName();

    /**
     * Gets the description of the task.
     * 
     * @return the description of the task.
     */
    String getDescription();

    /**
     * Destroy the runnable for good.
     */
    void destroy();

}
