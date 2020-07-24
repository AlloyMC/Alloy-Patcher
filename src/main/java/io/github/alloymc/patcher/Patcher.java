package io.github.alloymc.patcher;

import java.io.File;

public class Patcher implements Runnable {
	private static Boolean running = false;

	@Override
	public void run() {
		synchronized (running) {
			if (running) {
				LOGGER.log("Already Runnning!");
				return;
			} else {
				running = true;
			}
		}

		File inputDir = new File("input");
		File[] modJars;

		if (!inputDir.mkdir() && (modJars = inputDir.listFiles()).length > 0) {
			File outputDir = new File("output");
			outputDir.mkdir();

			for (File modJar : modJars) {
			}
		} else {
			LOGGER.log("Unable to patch, as no mods are in the input directory!");
		}
		
		synchronized (running) {
			running = false;
		}
	}

	private static final Logger LOGGER = new Logger("Patcher");
}
