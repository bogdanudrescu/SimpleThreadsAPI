package grape.simple.threads.test;

import grape.simple.threads.AbstractInterruptibleRunnable;
import grape.simple.threads.InterruptibleRunnable;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

/**
 * Very simple test to show the interruption on threads.
 * 
 * @author Bogdan Udrescu (bogdan.udrescu@gmail.com) 
 */
@SuppressWarnings("serial")
public class TestInterruption extends JFrame implements ActionListener {

	/*
	 * The play/pause/stop buttons.
	 */
	private JButton play;
	private JButton pause;
	private JButton stop;

	/*
	 * A process doing something.
	 */
	private InterruptibleRunnable process;

	/*
	 * The progress view reference.
	 */
	private ProgressView progressView;

	/**
	 * Create the view.
	 */
	public TestInterruption() {

		setTitle("Simple Interruption Test");

		play = new JButton(new ImageIcon(getClass().getResource("Play1Hot.png")));
		play.addActionListener(this);

		pause = new JButton(new ImageIcon(getClass().getResource("PauseHot.png")));
		pause.addActionListener(this);

		stop = new JButton(new ImageIcon(getClass().getResource("Stop1Hot.png")));
		stop.addActionListener(this);

		JPanel controlPanel = new JPanel();
		controlPanel.add(play);
		controlPanel.add(pause);
		controlPanel.add(stop);

		progressView = new ProgressView();

		getContentPane().setLayout(new BorderLayout());

		getContentPane().add(controlPanel, BorderLayout.NORTH);
		getContentPane().add(progressView, BorderLayout.SOUTH);

		progressView.setVisible(false);

		refreshButtonsStatus();
	}

	/**
	 * Gets the progress bar.
	 * @return	the progress bar.
	 */
	protected JProgressBar getProgressBar() {
		return progressView.getProgressBar();
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {

		// Play.
		if (e.getSource() == play) {
			if (process == null) {

				progressView.setVisible(true);

				process = createProcess();
				process.addPropertyChangeListener("realState", new RealStatePropertyListener());

				new Thread(process).start();

			} else {
				process.resume();
			}

			// Pause.
		} else if (e.getSource() == pause) {
			process.pause();

			// Stop.
		} else if (e.getSource() == stop) {
			process.cancel();
		}
	}

	/*
	 * Linsten to the state of the process.
	 */
	class RealStatePropertyListener implements PropertyChangeListener {

		/* (non-Javadoc)
		 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
		 */
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			refreshButtonsStatus();
		}

	}

	/*
	 * Refresh the status of the buttons.
	 */
	private void refreshButtonsStatus() {
		if (process == null) {

			progressView.setVisible(false);
			refreshButtonsStatus(true, false, false);

		} else {

			switch (process.getRealState()) {
				case NOT_RUNNING:
				case CANCELED:

					process.destroy();
					process = null;

					progressView.setVisible(false);
					refreshButtonsStatus(true, false, false);
					break;

				case RUNNING:
					refreshButtonsStatus(false, true, true);
					break;

				case PAUSED:
					refreshButtonsStatus(true, false, true);
					break;
			}

			/*
			if (process.isPaused()) {
				refreshButtonsStatus(true, false, true);

			} else if (process.isCanceled()) {
				refreshButtonsStatus(true, false, false);

			} else {
				refreshButtonsStatus(false, true, true);
			}
			//*/
		}
	}

	/*
	 * Refresh the status of the buttons.
	 */
	private void refreshButtonsStatus(boolean enablePlay, boolean enablePause, boolean enableStop) {
		play.setEnabled(enablePlay);
		pause.setEnabled(enablePause);
		stop.setEnabled(enableStop);
	}

	/**
	 * A progress view to see how the operations work.
	 * 
	 * @author Bogdan Udrescu (bogdan.udrescu@gmail.com)
	 */
	@SuppressWarnings("serial")
	class ProgressView extends JPanel {

		/*
		 * The progress bar.
		 */
		private JProgressBar progressBar;

		/**
		 * Create the progress view.
		 */
		public ProgressView() {
			super(new BorderLayout());

			progressBar = new JProgressBar();

			add(progressBar, BorderLayout.CENTER);

			setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		}

		/**
		 * Gets the progress bar.
		 * @return	the progress bar.
		 */
		public JProgressBar getProgressBar() {
			return progressBar;
		}

		/* (non-Javadoc)
		 * @see javax.swing.JComponent#setVisible(boolean)
		 */
		@Override
		public void setVisible(boolean aFlag) {
			super.setVisible(aFlag);

			if (!aFlag) {
				progressBar.setValue(progressBar.getMinimum());
			}
		}

	}

	/**
	 * Create the process to run.
	 * @return	the process to run.
	 */
	protected InterruptibleRunnable createProcess() {

		/*
		 * Inner class just to show how simple it is to create a process.
		 */
		return new AbstractInterruptibleRunnable() {

			/*
			 * The progress direction (1 for increase, -1 for decrease).
			 */
			private int direction = 1;

			/* (non-Javadoc)
			 * @see grape.simple.threads.AbstractInterruptibleRunnable#execute()
			 */
			@Override
			protected void execute() {

				// We're only doing a repetition here, which can be handled also simpler, only with a running state,
				// but for complex processes this interruption mechanism using checkInterruption() will be quite handy.
				while (true) {

					int value = getProgressBar().getValue();

					value += direction;

					getProgressBar().setValue(value);

					if (value == getProgressBar().getMaximum()) {
						direction = -1;

					} else if (value == getProgressBar().getMinimum()) {
						direction = 1;
					}

					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
					}

					// Check if the process should be interrupted.
					checkInterruption();
				}

			}
		};
	}

	/**
	 * Start the test app.
	 */
	public static void main(String[] args) {
		TestInterruption app = new TestInterruption();

		app.setBounds(100, 100, 500, 200);
		app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		app.setVisible(true);
	}

}
