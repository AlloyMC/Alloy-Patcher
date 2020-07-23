package io.github.alloymc.patcher;

import java.awt.BorderLayout;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.BevelBorder;

public class Main {
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setTitle("Alloy Patcher");
		frame.setSize(400, 550);
		frame.setResizable(false);

		JPanel panel = new JPanel(new BorderLayout(5, 10));

		JTextArea log = new JTextArea();
		log.setEditable(false);
		log.setBorder(new BevelBorder(BevelBorder.LOWERED));

		System.setOut(new OutRedirect(log));

		JScrollPane scroll = new JScrollPane (log, 
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		JButton patch = new JButton();
		patch.addActionListener(event -> {
			System.out.println("Hello, World!");
		});
		patch.setText("Patch");

		panel.add(scroll, BorderLayout.CENTER);
		panel.add(patch, BorderLayout.SOUTH);
		frame.add(panel);
		frame.setVisible(true);
	}
	
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
