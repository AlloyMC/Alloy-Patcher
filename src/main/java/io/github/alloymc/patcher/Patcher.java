package io.github.alloymc.patcher;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.swing.SwingWorker;

import org.cadixdev.atlas.Atlas;
import org.cadixdev.bombe.asm.jar.JarEntryRemappingTransformer;
import org.cadixdev.lorenz.asm.LorenzRemapper;

import io.github.alloymc.patcher.transformer.MixinRefmapTransformer;
import io.github.alloymc.patcher.transformer.MixinRemappingTransformer;

public class Patcher extends SwingWorker<Void, Void> {
	private static Boolean running = false;

	@Override
	protected Void doInBackground() throws Exception {
		synchronized (running) {
			if (running) {
				LOGGER.log("Already Runnning!");
				return null;
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

			LOGGER.log("Patching Complete!");
		} else {
			LOGGER.log("Unable to patch, as no mods are in the input directory!");
		}

		synchronized (running) {
			running = false;
		}

		return null;
	}

	private File remapJar(File modJar, String name, File outputDir) throws IOException {
		LOGGER.log("Collecting Mixin Refmaps for Jar: " + name);
		ZipFile jarIn = new ZipFile(modJar);
		Set<String> refmaps = stream(jarIn.entries())
				.map(ze -> ze.getName())
				.filter(n -> n.endsWith("refmap.json"))
				.collect(Collectors.toSet());
		jarIn.close();

		LOGGER.log("Remapping Mod Jar: " + name);

		final Atlas atlas = new Atlas();
		atlas.install(ctx -> new MixinRemappingTransformer(Mappings.mappingsConversion));
		atlas.install(ctx -> new JarEntryRemappingTransformer(
				new LorenzRemapper(Mappings.mappingsConversion, ctx.inheritanceProvider())));
		atlas.install(ctx -> new MixinRefmapTransformer(refmaps, Mappings.mappingsConversion, "searge"));

		File result = new File(outputDir, "remapped_" + modJar.getName());
		long t = System.currentTimeMillis();
		atlas.run(Paths.get(modJar.toURI()), Paths.get(result.toURI()));
		LOGGER.log("Remapping took " + (System.currentTimeMillis() - t) + "ms");
		atlas.close();
		return result;
	}

	// Stolen from stack overflow. I'm lazy and wanted to use a stream
	private static <T> Stream<T> stream(Enumeration<T> e) {
		return StreamSupport.stream(
				Spliterators.spliteratorUnknownSize(
						new Iterator<T>() {
							public T next() {
								return e.nextElement();
							}
							public boolean hasNext() {
								return e.hasMoreElements();
							}
						},
						Spliterator.ORDERED), false);
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
