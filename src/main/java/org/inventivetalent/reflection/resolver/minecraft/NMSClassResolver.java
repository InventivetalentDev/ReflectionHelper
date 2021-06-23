package org.inventivetalent.reflection.resolver.minecraft;

import org.inventivetalent.reflection.minecraft.Minecraft;
import org.inventivetalent.reflection.minecraft.MinecraftVersion;
import org.inventivetalent.reflection.resolver.ClassResolver;

/**
 * {@link ClassResolver} for <code>net.minecraft.server.*</code> classes
 */
public class NMSClassResolver extends ClassResolver {

	@Override
	public Class resolve(String... names) throws ClassNotFoundException {
		for (int i = 0; i < names.length; i++) {
			if (!names[i].startsWith("net.minecraft")) {
				names[i] = Minecraft.getNMSPackage() + "." + names[i];
			} else if (names[i].contains(".")) {
				/* name contains dot but don't start with NMS (ex: world.entity.Entity) */
				if (MinecraftVersion.VERSION.hasNMSVersionPrefix()) {
					/* use class name only */
					String[] path = names[i].split("\\.");
					names[i] = Minecraft.getNMSPackage() + "." + path[path.length - 1];
				} else {
					/* use the whole name */
					names[i] = Minecraft.getNMSPackage() + "." + names[i];
				}
			}
		}
		return super.resolve(names);
	}
}
