package io.github.alloymc.patcher.transformer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.cadixdev.bombe.jar.JarClassEntry;
import org.cadixdev.bombe.jar.JarEntryTransformer;
import org.cadixdev.lorenz.MappingSet;
import org.cadixdev.lorenz.model.ClassMapping;
import org.cadixdev.lorenz.model.FieldMapping;
import org.cadixdev.lorenz.model.MethodMapping;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

// Taken from JamieRocks fabric-forge
// MIT-Licensed
public class MixinRemappingTransformer implements JarEntryTransformer {

	private final MappingSet mappings;

	public MixinRemappingTransformer(final MappingSet mappings) {
		this.mappings = mappings;
	}

	@Override
	public JarClassEntry transform(final JarClassEntry entry) {
		final ClassNode node = new ClassNode();
		new ClassReader(entry.getContents()).accept(node, 0);

		if (node.invisibleAnnotations != null) {
			final List<ClassMapping<?, ?>> targetClasses = new ArrayList<>();

			for (final AnnotationNode annotation : node.invisibleAnnotations) {
				if (!"Lorg/spongepowered/asm/mixin/Mixin;".equals(annotation.desc)) continue;

				for (int i = 0; i < annotation.values.size(); i++) {
					final String key = (String) annotation.values.get(i);
					final Object value = annotation.values.get(++i);

					if ("value".equals(key)) {
						for (final Type targetType : (List<Type>) value) {
							this.mappings.computeClassMapping(targetType.getClassName()).ifPresent(targetClasses::add);
						}
					}
					else if ("targets".equals(key)) {
						for (final String targetType : (List<String>) value) {
							this.mappings.computeClassMapping(targetType).ifPresent(targetClasses::add);
						}
					}
				}
			}

			if (node.methods != null) {
				for (final MethodNode method : node.methods) {
					if (method.visibleAnnotations == null) continue;

					for (final AnnotationNode annotation : method.visibleAnnotations) {
						if (!"Lorg/spongepowered/asm/mixin/Shadow;".equals(annotation.desc)) continue;

						getMethodMapping(targetClasses, method).ifPresent(methodMapping -> {
							this.mappings.getOrCreateClassMapping(node.name)
							.getOrCreateMethodMapping(method.name, method.desc)
							.setDeobfuscatedName(methodMapping.getDeobfuscatedName());
						});
					}
				}
			}

			if (node.fields != null) {
				for (final FieldNode field : node.fields) {
					if (field.visibleAnnotations == null) continue;

					for (final AnnotationNode annotation : field.visibleAnnotations) {
						if (!"Lorg/spongepowered/asm/mixin/Shadow;".equals(annotation.desc)) continue;

						getFieldMapping(targetClasses, field).ifPresent(methodMapping -> {
							this.mappings.getOrCreateClassMapping(node.name)
							.getOrCreateFieldMapping(field.name)
							.setDeobfuscatedName(methodMapping.getDeobfuscatedName());
						});
					}
				}
			}
		}

		// We just create mappings, we don't actually change anything
		return entry;
	}

	private static Optional<MethodMapping> getMethodMapping(final List<ClassMapping<?, ?>> targetClasses, final MethodNode method) {
		for (final ClassMapping<?, ?> targetClass : targetClasses) {
			final Optional<MethodMapping> mapping = targetClass.getMethodMapping(method.name, method.desc);
			if (mapping.isPresent()) return mapping;
		}
		return Optional.empty();
	}

	private static Optional<FieldMapping> getFieldMapping(final List<ClassMapping<?, ?>> targetClasses, final FieldNode field) {
		for (final ClassMapping<?, ?> targetClass : targetClasses) {
			final Optional<FieldMapping> mapping = targetClass.getFieldMapping(field.name);
			if (mapping.isPresent()) return mapping;
		}
		return Optional.empty();
	}

}