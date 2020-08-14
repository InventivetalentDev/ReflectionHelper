package org.inventivetalent.reflection.minecraft;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.inventivetalent.reflection.resolver.minecraft.NMSClassResolver;
import org.inventivetalent.reflection.resolver.minecraft.OBCClassResolver;
import org.inventivetalent.reflection.util.AccessUtil;

import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper class to access minecraft/bukkit specific objects
 */
public class Minecraft {
    static final Pattern NUMERIC_VERSION_PATTERN = Pattern.compile("v([0-9])_([0-9]*)_R([0-9])");

    public static final MinecraftVersion VERSION;

    private static NMSClassResolver nmsClassResolver = new NMSClassResolver();
    private static OBCClassResolver obcClassResolver = new OBCClassResolver();
    private static Class<?> NmsEntity;
    private static Class<?> CraftEntity;

    static {
        System.out.println("[ReflectionHelper] I am loaded from package " + Minecraft.class.getPackage().getName());
        VERSION = VersionConstant.getVersion();
        System.out.println("[ReflectionHelper] Version is " + VERSION);

        try {
            NmsEntity = nmsClassResolver.resolve("Entity");
            CraftEntity = obcClassResolver.resolve("entity.CraftEntity");
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return the current NMS/OBC version (format <code>&lt;version&gt;.</code>
     */
    public static String getVersion() {
        return VERSION.packageName() + ".";
    }

    public static Object getHandle(Object object) throws ReflectiveOperationException {
        Method method;
        try {
            method = AccessUtil.setAccessible(object.getClass().getDeclaredMethod("getHandle"));
        } catch (ReflectiveOperationException e) {
            method = AccessUtil.setAccessible(CraftEntity.getDeclaredMethod("getHandle"));
        }
        return method.invoke(object);
    }

    public static Entity getBukkitEntity(Object object) throws ReflectiveOperationException {
        Method method;
        try {
            method = AccessUtil.setAccessible(NmsEntity.getDeclaredMethod("getBukkitEntity"));
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

    public enum VersionConstant {

        UNKNOWN(-1),

        v1_7_R1(10701),
        v1_7_R2(10702),
        v1_7_R3(10703),
        v1_7_R4(10704),

        v1_8_R1(10801),
        v1_8_R2(10802),
        v1_8_R3(10803),
        //Does this even exists?
        v1_8_R4(10804),

        v1_9_R1(10901),
        v1_9_R2(10902),

        v1_10_R1(11001),

        v1_11_R1(11101),

        v1_12_R1(11201),

        v1_13_R1(11301),
        v1_13_R2(11302),

        v1_14_R1(11401),

        v1_15_R1(11501),

        v1_16_R1(11601),
        v1_16_R2(11602);

        private MinecraftVersion version;

        VersionConstant(int version) {
            this.version = new MinecraftVersion(name(), version);
        }

        /**
         * @return the minecraft version
         */
        public MinecraftVersion minecraft() {
            return version;
        }

        public static MinecraftVersion getVersion() {
            String name = Bukkit.getServer().getClass().getPackage().getName();
            String versionPackage = name.substring(name.lastIndexOf('.') + 1) + ".";
            for (VersionConstant version : values()) {
                MinecraftVersion minecraftVersion = version.minecraft();
                if (minecraftVersion.matchesPackageName(versionPackage)) {
                    return minecraftVersion;
                }
            }
            System.err.println("[ReflectionHelper] Failed to find version enum for '" + name + "'/'" + versionPackage + "'");

            System.out.println("[ReflectionHelper] Generating dynamic constant...");
            Matcher matcher = NUMERIC_VERSION_PATTERN.matcher(versionPackage);
            while (matcher.find()) {
                if (matcher.groupCount() < 3) {
                    continue;
                }

                String majorString = matcher.group(1);
                String minorString = matcher.group(2);
                if (minorString.length() == 1) {
                    minorString = "0" + minorString;
                }
                String patchString = matcher.group(3);
                if (patchString.length() == 1) {
                    patchString = "0" + patchString;
                }

                String numVersionString = majorString + minorString + patchString;
                int numVersion = Integer.parseInt(numVersionString);
                String packageName = versionPackage.substring(0, versionPackage.length() - 1);

                //dynamic register version
                return new MinecraftVersion(packageName, numVersion);
            }

            return UNKNOWN.minecraft();
        }
    }

    public static class MinecraftVersion {

        private String packageName;
        private int version;

        MinecraftVersion(String packageName, int version) {
            this.packageName = packageName;
            this.version = version;
        }

        /**
         * @return the version-number
         */
        public int version() {
            return version;
        }

        /**
         * @return the package name
         */
        public String packageName() {
            return packageName;
        }

        /**
         * @param version the version to check
         * @return <code>true</code> if this version is older than the specified version
         */
        public boolean olderThan(MinecraftVersion version) {
            return version() < version.version();
        }

        /**
         * @param version the version to check
         * @return <code>true</code> if this version is older than the specified version
         */
        public boolean olderThan(VersionConstant version) {
            return olderThan(version.minecraft());
        }

        /**
         * @param version the version to check
         * @return <code>true</code> if this version is newer than the specified version
         */
        public boolean newerThan(MinecraftVersion version) {
            return version() >= version.version();
        }

        /**
         * @param version the version to check
         * @return <code>true</code> if this version is newer than the specified version
         */
        public boolean newerThan(VersionConstant version) {
            return newerThan(version.minecraft());
        }

        /**
         * @param oldVersion The older version to check
         * @param newVersion The newer version to check
         * @return <code>true</code> if this version is newer than the oldVersion and older that the newVersion
         */
        public boolean inRange(MinecraftVersion oldVersion, MinecraftVersion newVersion) {
            return newerThan(oldVersion) && olderThan(newVersion);
        }

        public boolean matchesPackageName(String packageName) {
            return packageName.toLowerCase().contains(packageName.toLowerCase());
        }

        @Override
        public String toString() {
            return packageName.toLowerCase() + " (" + version() + ")";
        }
    }
}
