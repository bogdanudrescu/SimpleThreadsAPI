package grape.simple.threads.test;

import grape.simple.threads.AbstractInterruptibleRunnable;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Process downloading a file.
 * 
 * @author Bogdan Udrescu (bogdan.udrescu@gmail.com)
 */
public class DownloadProcess extends AbstractInterruptibleRunnable {

	/*
	 * The url to download.
	 */
	private String urlToDownload;

	/*
	 * The name of the file where to store the downloaded data.
	 */
	private String fileNameToStore;

	/**
	 * Create a process to download a file and store it on the hdd.
	 * @param urlToDownload		the url to download.
	 * @param fileNameToStore	the name of the file where to store the downloaded data.
	 */
	public DownloadProcess(String urlToDownload, String fileNameToStore) {
		this.urlToDownload = urlToDownload;
		this.fileNameToStore = fileNameToStore;
	}

	/*
	 * The input stream to read the data from.
	 */
	private InputStream inputStream;

	/*
	 * The output stream to write the data to.
	 */
	private OutputStream outputStream;

	/*
	 * The URL connection.
	 */
	private URLConnection connection;

	/* (non-Javadoc)
	 * @see grape.simple.threads.AbstractInterruptibleRunnable#execute()
	 */
	@Override
	protected void execute() throws Exception {
		URL url = new URL(urlToDownload);

		connection = url.openConnection();

		System.out.println("Connecting to " + urlToDownload);

		// Read the lenght of the download content.
		String contentLengthString = connection.getHeaderField("Content-Length");
		long contentLength = Long.parseLong(contentLengthString);

		System.out.println("Connection established!");

		checkInterruption();

		// Gets the data from the input stream and puts it into the output stream.
		inputStream = connection.getInputStream();
		outputStream = new FileOutputStream(fileNameToStore);

		byte[] bytes = new byte[4096];
		int length = -1;
		long contentLengthRead = 0;

		while ((length = inputStream.read(bytes)) != -1) {

			outputStream.write(bytes, 0, length);
			outputStream.flush();

			contentLengthRead += length;

			// Set the completed percent.
			setPercentCompleted(100f * contentLengthRead / contentLength);

			checkInterruption();
		}
	}

	/* (non-Javadoc)
	 * @see grape.simple.threads.AbstractInterruptibleRunnable#executed()
	 */
	@Override
	protected void executed() {

		System.out.println("Donwload finished.");

		if (outputStream != null) {
			try {
				outputStream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (inputStream != null) {
			try {
				inputStream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/* (non-Javadoc)
	 * @see grape.simple.threads.AbstractInterruptibleRunnable#cancel()
	 */
	@Override
	public synchronized void cancel() {

		connection.setConnectTimeout(1);
		connection.setReadTimeout(1);

		super.cancel();
	}

	/**
	 * Test url download.
	 */
	public static void main(String[] args) {
		new Thread(new DownloadProcess("http://mirrors.hostingromania.ro/apache.org//httpd/httpd-2.2.26.tar.gz", "test.dat")).start();
	}

}
