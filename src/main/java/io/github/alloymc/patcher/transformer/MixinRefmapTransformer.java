package io.github.alloymc.patcher.transformer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Set;

import org.cadixdev.bombe.jar.JarEntryTransformer;
import org.cadixdev.bombe.jar.JarResourceEntry;
import org.cadixdev.bombe.type.signature.MethodSignature;
import org.cadixdev.lorenz.MappingSet;
import org.cadixdev.lorenz.model.ClassMapping;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import io.github.alloymc.patcher.Logger;

// Taken from JamieRocks fabric-forge
// MIT-Licensed
public class MixinRefmapTransformer implements JarEntryTransformer {
	private static final Logger LOGGER = new Logger("MixinRefmapTransformer");

	private static final Gson GSON = new GsonBuilder()
			.setPrettyPrinting()
			.create();

	private final Set<String> refmaps;
	private final MappingSet mappings;
	private final String name;

	public MixinRefmapTransformer(final Set<String> refmaps, final MappingSet mappings, final String name) {
		this.refmaps = refmaps;
		this.mappings = mappings;
		this.name = name;
	}

	@Override
	public JarResourceEntry transform(final JarResourceEntry entry) {
		if (!this.refmaps.contains(entry.getName())) return entry;

		final ByteArrayInputStream bais = new ByteArrayInputStream(entry.getContents());
		try (final InputStreamReader reader = new InputStreamReader(bais)) {
			final JsonElement raw = new JsonParser().parse(reader);
			if (!raw.isJsonObject()) {
				LOGGER.warn("Malformed Mixin refmap" + entry.getName() + ", will likely error");
				return entry;
			}
			final JsonObject refmap = raw.getAsJsonObject();

			final JsonObject mappings = refmap.getAsJsonObject("mappings");
			for (final Map.Entry<String, JsonElement> mapping : mappings.entrySet()) {
				final JsonObject mixinMappings = mapping.getValue().getAsJsonObject();
				for (final Map.Entry<String, JsonElement> mixinMapping : mixinMappings.entrySet()) {
					final String obf = mixinMapping.getValue().getAsString();

					// Class
					if (!obf.startsWith("L") && !obf.contains("(")) {
						final String deobf = this.mappings.computeClassMapping(obf)
								.map(ClassMapping::getFullDeobfuscatedName)
								.orElse(obf);

						mixinMapping.setValue(new JsonPrimitive(deobf));
					}
					// LType;methodNamedescriptor
					else {
						final int classEnd = obf.indexOf(';');
						final String owner = obf.substring(1, classEnd);
						final MethodSignature signature = MethodSignature.of(obf.substring(classEnd + 1));

						this.mappings.computeClassMapping(owner).ifPresent(klass -> {
							klass.getMethodMapping(signature).ifPresent(method -> {
								final String deobf = "L" + klass.getFullDeobfuscatedName() + ";" +
										method.getDeobfuscatedSignature().toJvmsIdentifier();

								mixinMapping.setValue(new JsonPrimitive(deobf));
							});
						});
					}
				}
			}

			final JsonObject data = refmap.getAsJsonObject("data");
			data.remove("named:intermediary"); // todo: test without
			data.add(this.name, mappings);

			return new JarResourceEntry(entry.getName(), entry.getTime(),
					GSON.toJson(refmap).getBytes()
					);
		}
		catch (final IOException ignored) {
		}

		return entry;
	}

}