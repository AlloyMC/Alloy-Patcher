package io.github.alloymc.patcher;

import java.io.File;

public class Patcher implements Runnable {
	@Override
	public void run() {
		File inputDir = new File("input");
		File[] modJars;

		if (!inputDir.mkdir() && (modJars = inputDir.listFiles()).length > 0) {
			File outputDir = new File("output");
			outputDir.mkdir();
		} else {
			LOGGER.log("Unable to patch, as no mods are in the input directory!");
		}
	}

	private static final Logger LOGGER = new Logger("Patcher");
}
