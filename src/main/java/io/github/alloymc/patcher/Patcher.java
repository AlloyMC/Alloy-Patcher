package io.github.alloymc.patcher;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

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
			File tempOutputDir = new File("output/temp");
			tempOutputDir.mkdirs();

			for (File modJar : modJars) {
				try {
					this.patchJar(this.remapJar(modJar, tempOutputDir), outputDir);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else {
			LOGGER.log("Unable to patch, as no mods are in the input directory!");
		}

		synchronized (running) {
			running = false;
		}
	}

	private File remapJar(File modJar, File outputDir) {
		final Atlas atlas = new Atlas();
		
	}

	private void patchJar(File modJar, File outputDir) throws ZipException, IOException {
		ZipFile zipFile = new ZipFile(modJar);
		Enumeration<? extends ZipEntry> lexManos = zipFile.entries();

		while (lexManos.hasMoreElements()) {
			ZipEntry cpw = lexManos.nextElement(); // I got bored naming vars ok

			if (!cpw.isDirectory()) { // names in the form e.g. "tk/valoeghese/irishwolves/mixin/MixinSpud.class"
				String fileName = cpw.getName();

				if (fileName.endsWith(".class")) {
					// Patch Time!
				}
			}
		}
	}

	private static final Logger LOGGER = new Logger("Patcher");
}
