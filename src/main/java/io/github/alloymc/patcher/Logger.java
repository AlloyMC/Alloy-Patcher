package io.github.alloymc.patcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.zip.GZIPOutputStream;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

public final class Logger extends SwingWorker<T, V> {
	public Logger(String name) {
		this.name = name;
	}

	private final String name;

	public void log(String message) {
		String formattedMessage = "[" + this.name + "/INFO] @" + FORMAT.format(LocalDateTime.now()) + " " + message;
		WRITER.ifPresent(pw -> pw.println(formattedMessage));
		System.out.println(formattedMessage);
		SwingUtilities.invokeLater(Main::refresh);
	}

	public void warn(String message) {
		String formattedMessage = "[" + this.name + " /WARN] @" + FORMAT.format(LocalDateTime.now()) + " " + message;
		WRITER.ifPresent(pw -> pw.println(formattedMessage));
		System.out.println(formattedMessage);
		SwingUtilities.invokeLater(Main::refresh);
	}

	private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
	private static final DateTimeFormatter FORMAT_FILENAME = DateTimeFormatter.ofPattern("dd.MM.yyyy HH.mm.ss");
	private static final File LOG_CACHE_DIR = new File("logs");
	private static final File LOG_FILE = new File("./log_latest.txt");
	private static Optional<PrintWriter> WRITER = Optional.empty();

	public static void openFile() throws IOException {
		LOG_FILE.createNewFile();
		WRITER = Optional.of(new PrintWriter(LOG_FILE));
	}

	public static void closeFile() {
		WRITER.get().close();

		try (FileInputStream fis = new FileInputStream(LOG_FILE)) {
			LOG_CACHE_DIR.mkdir();
			File file = new File("./logs/log_" + FORMAT_FILENAME.format(LocalDateTime.now()) + ".txt.gz");
			file.createNewFile();

			try (GZIPOutputStream fos = new GZIPOutputStream(new FileOutputStream(file))) {
				byte[] buf = new byte[1024];
				int lengthCopied;

				while ((lengthCopied = fis.read(buf)) != -1) {
					fos.write(buf, 0, lengthCopied);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		WRITER = Optional.empty();
	}
}

