package grape.simple.threads.test;

import grape.simple.threads.AbstractInterruptibleRunnable;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
public class TestInterruption extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;

	/*
	 * The play/pause/stop buttons.
	 */
	private JButton play;
	private JButton pause;
	private JButton stop;

	/*
	 * A process doing something.
	 */
	private AnimateProcess animateProcess;

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

		getContentPane().setLayout(new BorderLayout());

		getContentPane().add(controlPanel, BorderLayout.NORTH);

		refreshButtonsStatus();
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {

		// Play.
		if (e.getSource() == play) {
			if (animateProcess == null) {

				progressView = new ProgressView();
				getContentPane().add(progressView, BorderLayout.CENTER);

				getContentPane().doLayout();
				progressView.doLayout();

				animateProcess = new AnimateProcess(progressView);

				new Thread(animateProcess).start();

			} else {
				animateProcess.restart();
			}

			// Pause.
		} else if (e.getSource() == pause) {
			animateProcess.pause();

			// Stop.
		} else if (e.getSource() == stop) {
			animateProcess.cancel();
			animateProcess = null;

			getContentPane().remove(progressView);
			progressView = null;

			getContentPane().repaint();
		}

		refreshButtonsStatus();
	}

	/*
	 * Refresh the status of the buttons.
	 */
	private void refreshButtonsStatus() {
		if (animateProcess == null) {
			refreshButtonsStatus(true, false, false);

		} else {

			if (animateProcess.isPaused()) {
				refreshButtonsStatus(true, false, true);

			} else if (animateProcess.isCanceled()) { // Here will never reach for our sample.
				refreshButtonsStatus(true, false, false);

			} else {
				refreshButtonsStatus(false, true, true);
			}
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
	 * Start the test app.
	 */
	public static void main(String[] args) {
		TestInterruption app = new TestInterruption();

		app.setBounds(100, 100, 500, 200);
		app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		app.setVisible(true);
	}

}

/**
 * Simple animation process.
 * 
 * @author Bogdan Udrescu (bogdan.udrescu@gmail.com)
 */
class AnimateProcess extends AbstractInterruptibleRunnable {

	/*
	 * The animation.
	 */
	private Animation animation;

	/**
	 * Create the animation process using the animation.
	 * @param animation	the animation object.
	 */
	public AnimateProcess(Animation animation) {
		this.animation = animation;
	}

	/* (non-Javadoc)
	 * @see grape.simple.threads.AbstractInterruptibleRunnable#execute()
	 */
	@Override
	protected void execute() {

		// We're only doing a repetition here, which can be handled also simpler, only with a running state,
		// but for complex processes this interruption mechanism using checkInterruption() will be quite handy.
		while (true) {

			animation.animate();

			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
			}

			// Check if the process should be interrupted.
			checkInterruption();
		}

	}
}

/**
 * Simple animation interface to animate one frame/call.
 * 
 * @author Bogdan Udrescu (bogdan.udrescu@gmail.com)
 */
interface Animation {

	/**
	 * Animate one frame.
	 */
	void animate();
}

/**
 * A progress view to see how the operations work.
 * 
 * @author Bogdan Udrescu (bogdan.udrescu@gmail.com)
 */
class ProgressView extends JPanel implements Animation {

	private static final long serialVersionUID = 1L;

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

	/*
	 * The progress direction (1 for increase, -1 for decrease).
	 */
	private int direction = 1;

	/* (non-Javadoc)
	 * @see grape.simple.threads.test.Animation#animate()
	 */
	public void animate() {
		int value = progressBar.getValue();

		value += direction;

		progressBar.setValue(value);

		if (value == progressBar.getMaximum()) {
			direction = -1;

		} else if (value == progressBar.getMinimum()) {
			direction = 1;
		}
	}

}