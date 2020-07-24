package io.github.alloymc.patcher;

import java.nio.file.Paths;

import org.cadixdev.lorenz.MappingSet;
import org.cadixdev.lorenz.io.MappingFormats;

import net.fabricmc.lorenztiny.TinyMappingFormat;

public class Mappings {
	public static final MappingSet mappingsConversion;

	static {
		new Logger("Mappings").log("Preparing \"Intermediary -> Srg\" Mappings");
		MappingSet set = null;

		try {
			MappingSet tsrg = MappingFormats.TSRG.createReader(
					Patcher.class.getClassLoader().getResourceAsStream("joined.tsrg"))
					.read(); // official -> srg

			MappingSet tiny = TinyMappingFormat.LEGACY.createReader(
					Paths.get(Patcher.class.getClassLoader().getResource("mappings.tiny").toURI())
					, "intermediary", "official").read(); // intermediary -> official

			set = tiny.merge(tsrg);
		} catch (Exception e) {
			e.printStackTrace(); // yes this is cursed
			System.exit(-1);
		}

		mappingsConversion = set;
	}
}
