package io.github.alloymc.patcher;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Calendar;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.BevelBorder;

public class Main {
	public static void main(String[] args) {
		IOException possibleError = null;

		try {
			Logger.openFile();
		} catch (IOException e) {
			possibleError = e;
		}

		JFrame frame = new JFrame();
		frame.setTitle("Alloy Patcher");
		frame.setSize(600, 400);
		frame.setResizable(false);

		JPanel panel = new JPanel(new BorderLayout(5, 10));

		log = new JTextArea();
		log.setEditable(false);
		log.setBorder(new BevelBorder(BevelBorder.LOWERED));

		OutRedirect redirect = new OutRedirect(log);
		System.setOut(redirect);
		System.setErr(redirect);

		if (possibleError != null) {
			possibleError.printStackTrace();
		}

		LOGGER.log("Current Timezone: " + Calendar.getInstance().getTimeZone().getDisplayName());

		JScrollPane scroll = new JScrollPane (
				log, 
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		JButton patch = new JButton();
		patch.addActionListener(event -> PATCHER.run());
		patch.setText("Patch");

		panel.add(scroll, BorderLayout.CENTER);
		panel.add(patch, BorderLayout.SOUTH);
		frame.add(panel);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				Logger.closeFile();
			};
		});

		frame.setVisible(true);
	}

	public static void refresh() {
		log.updateUI();
	}

	private static final Patcher PATCHER = new Patcher();
	private static final Logger LOGGER = new Logger("Main");
	private static JTextArea log;

	static class OutRedirect extends PrintStream {
		OutRedirect(JTextArea log) {
			super(new OutRedirectStream(log));
		}
	}

	static class OutRedirectStream extends OutputStream {
		OutRedirectStream(JTextArea log) {
			this.log = log;
		}

		private final JTextArea log;
		private StringBuilder buffer = new StringBuilder();

		@Override
		public void write(int b) throws IOException {
			char c = (char) b;
			this.buffer.append(c);

			if (c == '\n') {
				this.log.append(this.buffer.toString());
				this.buffer = new StringBuilder();
			}
		}
	}
}
