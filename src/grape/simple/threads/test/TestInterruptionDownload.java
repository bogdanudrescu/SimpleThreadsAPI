package grape.simple.threads.test;

import grape.simple.threads.AbstractInterruptibleRunnable;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * A bit more complex test involving the download of a file and save it on the disk.
 * 
 * @author Bogdan Udrescu (bogdan.udrescu@gmail.com)
 */
@SuppressWarnings("serial")
public class TestInterruptionDownload extends TestInterruption {

	/*
	 * Text field holding the url to download.
	 */
	private JTextField urlTextField;

	/*
	 * Text field holding the file name where to save the download data.
	 */
	private JTextField fileTextField;

	/**
	 * Create a simple test to download a file.
	 */
	public TestInterruptionDownload() {
		super();

		//		urlTextField = new JTextField("http://mirrors.hostingromania.ro/apache.org//httpd/httpd-2.2.26.tar.gz");
		urlTextField = new JTextField("http://weather.aeroplus.nl/");
		fileTextField = new JTextField("foo.dat");

		JPanel panel = new JPanel(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(5, 5, 5, 5);
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.BOTH;

		panel.add(new JLabel("Download from "), c);

		c.gridx = 1;
		panel.add(urlTextField, c);

		c.gridx = 0;
		c.gridy = 1;
		panel.add(new JLabel("Save as ..."), c);

		c.gridx = 1;
		panel.add(fileTextField, c);

		getContentPane().add(panel, BorderLayout.CENTER);
	}

	/* (non-Javadoc)
	 * @see grape.simple.threads.test.TestInterruption#createProcess()
	 */
	@Override
	protected AbstractInterruptibleRunnable createProcess() {
		AbstractInterruptibleRunnable process = new DownloadProcess(urlTextField.getText(), fileTextField.getText());
		process.addPropertyChangeListener("percentCompleted", new PercentCompletedListener());
		return process;
	}

	/*
	 * Listen to complete download percent and notify the progress bar.
	 */
	class PercentCompletedListener implements PropertyChangeListener {

		/* (non-Javadoc)
		 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
		 */
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getPropertyName().equals("percentCompleted")) {

				Number percent = (Number) evt.getNewValue();
				getProgressBar().setValue(percent.intValue());
			}
		}

	}

	/**
	 * Start the test app.
	 */
	public static void main(String[] args) {
		TestInterruptionDownload app = new TestInterruptionDownload();

		app.setBounds(100, 100, 600, 400);
		app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		app.setVisible(true);
	}

}
