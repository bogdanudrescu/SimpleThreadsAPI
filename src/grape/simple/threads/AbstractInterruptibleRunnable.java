package grape.simple.threads;

/**
 * Abstract implementation of the InterruptibleRunnable including the cancel/pause functionality. What's left in the subclasses is to
 * implement the {@link #execute()} method with your functionality and to check the interruption from time to time by simply calling 
 * {@link #checkInterruption()}.
 * <br/>
 * Please make the utmost not to surround the code calling {@link #checkInterruption()} with a try/catch on {@link Exception}, as this 
 * will block the interruption in case of a cancel call. The {@link #checkInterruption()} is actually throwing a {@link CancelException} 
 * that should be caught in {@link #run()}, which is the trick used to cancel the execution.
 * <br/>
 * In case of pause, simply calling {@link #pause()} will block the execution of the thread at the next {@link #checkInterruption()} call. 
 * A pause can be call off with a {@link #restart()} call.
 * 
 * @author Bogdan Udrescu (bogdan.udrescu@gmail.com)
 * @author Stefan Voinea (stefanvlad.voinea@gmail.com) 
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
			if (state == NOT_RUNNING) {
				state = RUNNING;
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
			e.printStackTrace();

		} finally {
			// Do nothing for now.
		}
	}

	/**
	 * Write here any piece of code your process must execute, that normally you'd write in the {@link #run()} method.
	 * <br/>
	 * Also make sure to call constantly {@link #checkInterruption()} to stop or pause your process when this is required.
	 * And make sure that at any level, these calls are not surrounded with a try/catch on {@link Exception},
	 * otherwise the cancel operation won't work.
	 */
	protected abstract void execute();

	/**
	 * The initial state of the process.
	 */
	public final static int NOT_RUNNING = -1;

	/**
	 * The running state of the process.
	 */
	public final static int RUNNING = 0;

	/**
	 * The paused state of the process.
	 */
	public final static int PAUSED = 1;

	/**
	 * The canceled state of the process.
	 */
	public final static int CANCELED = 2;

	/*
	 * The state.
	 */
	private volatile int state = NOT_RUNNING;

	/**
	 * Gets the state of the process.
	 * @return	the state of the process.
	 * @see #NOT_RUNNING
	 * @see #RUNNING
	 * @see #PAUSED
	 * @see #CANCELED
	 */
	public int getState() {
		return state;
	}

	/* (non-Javadoc)
	 * @see com.bright55.thread.CancelableRunnable#isCanceled()
	 */
	@Override
	public boolean isCanceled() {
		return state == CANCELED;
	}

	/* (non-Javadoc)
	 * @see com.bright55.thread.CancelableRunnable#isPaused()
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
	 * @see com.bright55.thread.CancelableRunnable#cancel()
	 */
	@Override
	public synchronized void cancel() {
		boolean paused = isPaused();
		state = CANCELED;

		if (paused) {
			notify();
		}
	}

	/* (non-Javadoc)
	 * @see com.bright55.thread.CancelableRunnable#pause()
	 */
	@Override
	public synchronized void pause() {
		state = PAUSED;
	}

	/* (non-Javadoc)
	 * @see com.bright55.thread.CancelableRunnable#restart()
	 */
	@Override
	public synchronized void restart() {
		if (state == PAUSED) {
			notify();
		}

		state = RUNNING;
	}

	/**
	 * If runnable should cancel this will throw an exception.<br/>
	 * If it should pause it will block the execution until the next restart.<br/>
	 * Call this method only from the {@link Thread} executing this runnable.
	 */
	protected void checkInterruption() {

		if (isPaused()) {
			synchronized (this) {

				// Double check lock to make sure we're not messing to notify() with the wait().
				if (isPaused()) {
					try {
						wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}

		// Don't do it with an else because if cancel after pause, this should throw the exception here and cancel the process.
		if (isCanceled()) {
			synchronized (this) {

				if (isCanceled()) {
					throw new CancelException();
				}
			}
		}

	}

	/* (non-Javadoc)
	 * @see com.bright55.thread.CancelableRunnable#destroy()
	 */
	@Override
	public void destroy() {
	}

	/* (non-Javadoc)
	 * @see com.bright55.thread.CancelableRunnable#getName()
	 */
	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

	/* (non-Javadoc)
	 * @see com.bright55.thread.CancelableRunnable#getDescription()
	 */
	@Override
	public String getDescription() {
		return "";
	}

}
