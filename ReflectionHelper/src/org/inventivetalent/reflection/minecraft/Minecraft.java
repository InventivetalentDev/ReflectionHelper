/*
 * Copyright 2016 inventivetalent. All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without modification, are
 *  permitted provided that the following conditions are met:
 *
 *     1. Redistributions of source code must retain the above copyright notice, this list of
 *        conditions and the following disclaimer.
 *
 *     2. Redistributions in binary form must reproduce the above copyright notice, this list
 *        of conditions and the following disclaimer in the documentation and/or other materials
 *        provided with the distribution.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 *  FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *  ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *  The views and conclusions contained in the software and documentation are those of the
 *  authors and contributors and should not be interpreted as representing official policies,
 *  either expressed or implied, of anybody else.
 */

package org.inventivetalent.reflection.minecraft;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.inventivetalent.reflection.resolver.minecraft.OBCClassResolver;
import org.inventivetalent.reflection.util.AccessUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Helper class to access minecraft/bukkit specific objects
 */
public class Minecraft {

	public static final Version VERSION;

	private static OBCClassResolver obcClassResolver = new OBCClassResolver();
	private static Class<?> CraftEntity;

	static {
		VERSION = Version.getVersion();
		System.out.println("[ReflectionHelper] Version is " + VERSION);

		try {
			CraftEntity = obcClassResolver.resolve("entity.CraftEntity");
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @return the current NMS/OBC version (format <code>&lt;version&gt;.</code>
	 */
	public static String getVersion() {
		return VERSION.name() + ".";
	}

	public static Object getHandle(Object object) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		Method method;
		try {
			method = AccessUtil.setAccessible(object.getClass().getDeclaredMethod("getHandle"));
		} catch (ReflectiveOperationException e) {
			method = AccessUtil.setAccessible(CraftEntity.getDeclaredMethod("getHandle"));
		}
		return method.invoke(object);
	}

	public static Entity getBukkitEntity(Object object) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		Method method;
		try {
			method = AccessUtil.setAccessible(object.getClass().getDeclaredMethod("getBukkitEntity"));
		} catch (ReflectiveOperationException e) {
			method = AccessUtil.setAccessible(CraftEntity.getDeclaredMethod("getHandle"));
		}
		return (Entity) method.invoke(object);
	}

	public static Object getHandleSilent(Object object) {
		try {
			return getHandle(object);
		} catch (Exception e) {
		}
		return null;
	}

	public enum Version {
		UNKNOWN(-1) {
			@Override
			public boolean matchesPackageName(String packageName) {
				return false;
			}
		},

		v1_7_R1(10701),
		v1_7_R2(10702),
		v1_7_R3(10703),
		v1_7_R4(10704),

		v1_8_R1(10801),
		v1_8_R2(10802),
		v1_8_R3(10803),
		//Does this even exists?
		v1_8_R4(10804),

		v1_9_R1(109001);

		private int version;

		Version(int version) {
			this.version = version;
		}

		/**
		 * @return the version-number
		 */
		public int version() {
			return version;
		}

		/**
		 * @param version the version to check
		 * @return <code>true</code> if this version is older than the specified version
		 */
		public boolean olderThan(Version version) {
			return version() < version.version();
		}

		/**
		 * @param version the version to check
		 * @return <code>true</code> if this version is newer than the specified version
		 */
		public boolean newerThan(Version version) {
			return version() >= version.version();
		}

		/**
		 * @param oldVersion The older version to check
		 * @param newVersion The newer version to check
		 * @return <code>true</code> if this version is newer than the oldVersion and older that the newVersion
		 */
		public boolean inRange(Version oldVersion, Version newVersion) {
			return newerThan(oldVersion) && olderThan(newVersion);
		}

		public boolean matchesPackageName(String packageName) {
			return packageName.toLowerCase().contains(name().toLowerCase());
		}

		public static Version getVersion() {
			String name = Bukkit.getServer().getClass().getPackage().getName();
			String versionPackage = name.substring(name.lastIndexOf('.') + 1) + ".";
			for (Version version : values()) {
				if (version.matchesPackageName(versionPackage)) { return version; }
			}
			System.err.println("[ReflectionHelper] Failed to find version enum for '" + name + "'/'" + versionPackage + "'");
			return UNKNOWN;
		}

		@Override
		public String toString() {
			return name() + " (" + version() + ")";
		}
	}

}
