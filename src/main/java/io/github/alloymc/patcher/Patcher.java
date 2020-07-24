package io.github.alloymc.patcher;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.cadixdev.atlas.Atlas;
import org.cadixdev.bombe.asm.jar.JarEntryRemappingTransformer;
import org.cadixdev.lorenz.asm.LorenzRemapper;

public class Patcher implements Runnable {
	private static Boolean running = false;

	@Override
	public void run() {
		synchronized (running) {
			if (running) {
				LOGGER.log("Already Runnning!");
				return;
			} else {
				LOGGER.log("Remapping Jars!");
				running = true;
			}
		}

		File inputDir = new File("input");
		File[] modJars;

		if (!inputDir.mkdir() && (modJars = inputDir.listFiles()).length > 0) {
			File outputDir = new File("output");
			File tempOutputDir = new File("output/temp");
			tempOutputDir.mkdirs();

			LOGGER.log("Searching for mod jars...");

			for (File modJar : modJars) {
				String name = modJar.getName();

				if (name.endsWith(".jar")) {
					LOGGER.log("Discovered mod jar: " + name);

					try {
						this.patchJar(this.remapJar(modJar, name, tempOutputDir), name, outputDir);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		} else {
			LOGGER.log("Unable to patch, as no mods are in the input directory!");
		}

		synchronized (running) {
			running = false;
		}
	}

	private File remapJar(File modJar, String name, File outputDir) throws IOException {
		LOGGER.log("Remapping Mod Jar: " + name);

		final Atlas atlas = new Atlas();
		atlas.install(ctx -> new JarEntryRemappingTransformer(
				new LorenzRemapper(Mappings.mappingsConversion, ctx.inheritanceProvider())));

		File result = new File(outputDir, "remapped_" + modJar.getName());
		long t = System.currentTimeMillis();
		atlas.run(Paths.get(modJar.toURI()), Paths.get(result.toURI()));
		LOGGER.log("Remapping took " + (System.currentTimeMillis() - t) + "ms");
		atlas.close();
		return result;
	}

	private void patchJar(File modJar, String name, File outputDir) throws ZipException, IOException {
		LOGGER.log("Patching Mod Jar: " + name);

		ZipFile jarIn = new ZipFile(modJar);
		Enumeration<? extends ZipEntry> lexManos = jarIn.entries();

		while (lexManos.hasMoreElements()) {
			ZipEntry cpw = lexManos.nextElement(); // I got bored naming vars ok

			if (!cpw.isDirectory()) { // names in the form e.g. "tk/valoeghese/irishwolves/mixin/MixinSpud.class"
				String fileName = cpw.getName();

				if (fileName.endsWith(".class")) {
					// Patch Time!
				}
			}
		}

		jarIn.close();
	}

	private static final Logger LOGGER = new Logger("Patcher");
}
