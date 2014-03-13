package grape.simple.threads;

import static grape.simple.threads.InterruptibleRunnableState.CANCELED;
import static grape.simple.threads.InterruptibleRunnableState.NOT_RUNNING;
import static grape.simple.threads.InterruptibleRunnableState.PAUSED;
import static grape.simple.threads.InterruptibleRunnableState.RUNNING;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Abstract implementation of the InterruptibleRunnable including the cancel/pause functionality. What's left in the subclasses is to
 * implement the {@link #execute()} method with your functionality and to check the interruption from time to time by simply calling 
 * {@link #checkInterruption()}.
 * <br/>
 * Please make sure not to surround the code calling {@link #checkInterruption()} with a try/catch on {@link Exception}, as this 
 * will block the interruption in case of a CANCEL call. The {@link #checkInterruption()} is actually throwing a {@link CancelException} 
 * that should be caught in {@link #run()}, which is the trick used to cancel the execution.
 * <br/>
 * In case of pause, simply calling {@link #pause()} will block the execution of the thread at the next {@link #checkInterruption()} call. 
 * A pause can be call off with a {@link #resume()} call.
 * 
 * @author Bogdan Udrescu (bogdan.udrescu@gmail.com)
 * @thanks Stefan Voinea (stefanvlad.voinea@gmail.com)
 */
public abstract class AbstractInterruptibleRunnable implements InterruptibleRunnable {

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public final void run() {

		// Set the state to RUNNING, because the process just started.
		//
		// We keep the NOT_RUNNING state just to make the difference between the CANCEL and the 
		// initial state of the process. It's a bit weird to say that the initial state before running,
		// is CANCELED, while the process never run.
		synchronized (this) {

			// FIXME Make it more clear that when a process gets CANCELED, in order to start it again, 
			// the user must call resume, to set the state back to NOT_RUNNING.
			if (realState == NOT_RUNNING) {
				setState(RUNNING);
				setRealState(RUNNING);
			}
		}

		try {
			// Just to make sure if the process gets canceled immediately after it's started.
			checkInterruption();

			// Execute the process.
			execute();

		} catch (CancelException e) {
			// Do nothing, the process was canceled with a reason.

		} catch (Exception e) {
			exception = e;

		} finally {

			// Finalize the process.
			executed();

			// Finally set the state back to NOT_RUNNING, only and only if the current state is RUNNING.
			// In case of a CANCEL state we want to keep the CANCEL state as current, to let the user know that the 
			// process was terminated by a user call.
			synchronized (this) {
				if (realState == RUNNING) {
					setState(NOT_RUNNING);
					setRealState(NOT_RUNNING);
				}
			}

		}

	}

	/*
	 * Exception caught in run.
	 */
	private Exception exception;

	/**
	 * Gets exception caught in the run implementation.
	 * @return	an exception rose while performing, if any.
	 */
	public Exception getException() {
		return exception;
	}

	/**
	 * Write here any piece of code your process must execute, that normally you'd write in the {@link #run()} method.
	 * <br/>
	 * Also make sure to call constantly {@link #checkInterruption()} to stop or pause your process when this is required.
	 * And make sure that at any level, these calls are not surrounded with a try/catch on {@link Exception},
	 * otherwise the cancel operation won't work.
	 * @throws Exception	any exception that might occur.
	 */
	protected abstract void execute() throws Exception;

	/**
	 * This method gets called on the finally block, at the end of run method.
	 * <br/>
	 * We don't need this method abstract because some times you don't need to do any destruction at the end of the process.
	 */
	protected void executed() {
		// Override this and perform any destruction. But do it fast, you don't need to keep this call too long.
		// 
		// Any exception handling should be done by the implementor of this method.
	}

	/**
	 * If runnable should cancel this will throw an exception.<br/>
	 * If it should pause it will block the execution until the next restart.<br/>
	 * Call this method only from the {@link Thread} executing this runnable.
	 */
	protected void checkInterruption() {

		if (state == PAUSED) {
			synchronized (this) {

				// Double check after lock to make sure we're not messing notify() with wait().
				if (state == PAUSED) {
					try {
						setRealState(PAUSED);

						wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					setRealState(RUNNING);
				}
			}
		}

		// Don't do it with an else because if cancel after pause, this should throw the exception here and cancel the process.
		if (state == CANCELED) {
			synchronized (this) {

				if (state == CANCELED) {
					setRealState(CANCELED);

					throw new CancelException();
				}
			}
		}

	}

	/*
	 * The state.
	 */
	private volatile InterruptibleRunnableState state = NOT_RUNNING;

	/*
	 * The real state, according with the exact time the process actually gets paused or canceled.
	 */
	private volatile InterruptibleRunnableState realState = NOT_RUNNING;

	/* (non-Javadoc)
	 * @see grape.simple.threads.InterruptibleRunnable#getState()
	 */
	public InterruptibleRunnableState getState() {
		return state;
	}

	/* (non-Javadoc)
	 * @see grape.simple.threads.InterruptibleRunnable#getRealState()
	 */
	public InterruptibleRunnableState getRealState() {
		return realState;
	}

	/*
	 * Final call to set the state of the process.
	 */
	private void setState(InterruptibleRunnableState state) {
		InterruptibleRunnableState oldState = this.state;

		this.state = state;

		firePropertyChange("state", oldState, state);
	}

	/*
	 * Final call to set the real state of the process.
	 */
	private void setRealState(InterruptibleRunnableState realState) {
		InterruptibleRunnableState oldRealState = this.realState;

		this.realState = realState;

		firePropertyChange("realState", oldRealState, realState);
	}

	/* (non-Javadoc)
	 * @see grape.simple.threads.InterruptibleRunnable#isCanceled()
	 */
	@Override
	public boolean isCanceled() {
		return state == CANCELED;
	}

	/* (non-Javadoc)
	 * @see grape.simple.threads.InterruptibleRunnable#isPaused()
	 */
	@Override
	public boolean isPaused() {
		return state == PAUSED;
	}

	/* (non-Javadoc)
	 * @see grape.simple.threads.InterruptibleRunnable#isRunning()
	 */
	@Override
	public boolean isRunning() {
		return state == RUNNING;
	}

	/* (non-Javadoc)
	 * @see grape.simple.threads.InterruptibleRunnable#cancel()
	 */
	@Override
	public synchronized void cancel() {

		boolean paused = state == PAUSED;
		setState(CANCELED);

		if (paused) {
			notify();
		}
	}

	/* (non-Javadoc)
	 * @see grape.simple.threads.InterruptibleRunnable#pause()
	 */
	@Override
	public synchronized void pause() {
		setState(PAUSED);
	}

	/* (non-Javadoc)
	 * @see grape.simple.threads.InterruptibleRunnable#resume()
	 */
	@Override
	public synchronized void resume() {

		switch (state) {
			case PAUSED:
				notify();
				setState(RUNNING);
				break;

			case CANCELED:
				setState(NOT_RUNNING);
				break;
		}
	}

	/* (non-Javadoc)
	 * @see grape.simple.threads.InterruptibleRunnable#destroy()
	 */
	@Override
	public void destroy() {
	}

	/* (non-Javadoc)
	 * @see grape.simple.threads.InterruptibleRunnable#getName()
	 */
	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

	/* (non-Javadoc)
	 * @see grape.simple.threads.InterruptibleRunnable#getDescription()
	 */
	@Override
	public String getDescription() {
		return "";
	}

	/*
	 * Used to register property listeners and fire propery change events.
	 */
	private PropertyChangeSupport changeSupport;

	/*
	 * Ensure the property change support is valid.
	 */
	private PropertyChangeSupport ensureChangeSupport() {
		PropertyChangeSupport changeSupport;

		synchronized (this) {
			if (this.changeSupport == null) {
				this.changeSupport = new PropertyChangeSupport(this);
			}
			changeSupport = this.changeSupport;
		}

		return changeSupport;
	}

	/**
	 * Notify the listeners that the specified property has been changed. 
	 * @param propertyName	the property name.
	 * @param oldValue		the old value of the property.
	 * @param newValue		the new value of the property.
	 */
	protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
		PropertyChangeSupport changeSupport = null;

		synchronized (this) {
			changeSupport = this.changeSupport;
		}

		if (changeSupport != null) {
			changeSupport.firePropertyChange(propertyName, oldValue, newValue);
		}
	}

	/**
	 * Register a listener that will be notified when the state of the process or any other property will change.
	 * @param listener	the listener to be registered.
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		PropertyChangeSupport changeSupport = ensureChangeSupport();
		changeSupport.addPropertyChangeListener(listener);
	}

	/**
	 * Register a listener that will be notified when the specified property will change.
	 * @param propertyName	the name of the property.
	 * @param listener		the listener to be registered.
	 */
	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		PropertyChangeSupport changeSupport = ensureChangeSupport();
		changeSupport.addPropertyChangeListener(propertyName, listener);
	}

	/*
	 * The percent of downloaded data.
	 */
	private float percentCompleted;

	/* (non-Javadoc)
	 * @see grape.simple.threads.InterruptibleRunnable#getPercentCompleted()
	 */
	public float getPercentCompleted() {
		return percentCompleted;
	}

	/**
	 * Sets the percent of process completion.
	 * @param percentCompleted	the percent of completion.
	 */
	protected void setPercentCompleted(float percentCompleted) {
		float oldPercentCompleted = this.percentCompleted;

		this.percentCompleted = percentCompleted;

		firePropertyChange("percentCompleted", oldPercentCompleted, this.percentCompleted);
	}

}
