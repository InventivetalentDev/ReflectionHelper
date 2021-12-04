package org.inventivetalent.reflection.minecraft;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.inventivetalent.reflection.resolver.ConstructorResolver;
import org.inventivetalent.reflection.resolver.FieldResolver;
import org.inventivetalent.reflection.resolver.MethodResolver;
import org.inventivetalent.reflection.resolver.minecraft.NMSClassResolver;
import org.inventivetalent.reflection.resolver.minecraft.OBCClassResolver;
import org.inventivetalent.reflection.util.AccessUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper class to access minecraft/bukkit specific objects
 */
public class Minecraft {

    public static final Pattern NUMERIC_VERSION_PATTERN = Pattern.compile("v([0-9])_([0-9]*)_R([0-9])");

    /**
     * @deprecated use {@link MinecraftVersion#VERSION} instead
     */
    @Deprecated
    public static final Version VERSION;
    public static final MinecraftVersion MINECRAFT_VERSION = MinecraftVersion.VERSION;

    private static NMSClassResolver nmsClassResolver = new NMSClassResolver();
    private static OBCClassResolver obcClassResolver = new OBCClassResolver();
    private static Class<?> NmsEntity;
    private static Class<?> CraftEntity;

    static {
        Version tempVersion = Version.UNKNOWN;
        try {
            tempVersion = Version.getVersion();
        } catch (Exception e) {
            System.out.println("[ReflectionHelper] Failed to get legacy version");
        }
        VERSION = tempVersion;

        try {
            Version.runSanityCheck();
        } catch (Exception e) {
            throw new RuntimeException("Sanity check which should always succeed just failed! Am I crazy?!", e);
        }

        try {
            NmsEntity = nmsClassResolver.resolve("Entity", "world.entity.Entity");
            CraftEntity = obcClassResolver.resolve("entity.CraftEntity");
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return the current NMS/OBC version (format <code>&lt;version&gt;.</code>
     */
    @Deprecated
    public static String getVersion() {
        return MINECRAFT_VERSION.packageName() + ".";
    }

    /**
     * @return the current NMS version package
     */
    public static String getNMSPackage() {
        return MINECRAFT_VERSION.getNmsPackage();
    }

    /**
     * @return the current OBC package
     */
    public static String getOBCPackage() {
        return MINECRAFT_VERSION.getObcPackage();
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
        v1_16_R2(11602),
        v1_16_R3(11603),

        v1_17_R1(11701),

        v1_18_R1(11801),

        /// (Potentially) Upcoming versions
        v1_19_R1(11901),

        v1_20_R1(12001);

        private final MinecraftVersion version;

        Version(int version, String nmsFormat, String obcFormat, boolean nmsVersionPrefix) {
            this.version = new MinecraftVersion(name(), version, nmsFormat, obcFormat, nmsVersionPrefix);
        }

        Version(int version) {
            if (version >= 11701) { // 1.17+ new class package name format
                this.version = new MinecraftVersion(name(), version, "net.minecraft", "org.bukkit.craftbukkit.%s", false);
            } else {
                this.version = new MinecraftVersion(name(), version);
            }
        }

        /**
         * @return the version-number
         */
        public int version() {
            return version.version();
        }

        /**
         * @param version the version to check
         * @return <code>true</code> if this version is older than the specified version
         */
        @Deprecated
        public boolean olderThan(Version version) {
            return version() < version.version();
        }

        /**
         * @param version the version to check
         * @return <code>true</code> if this version is newer than the specified version
         */
        @Deprecated
        public boolean newerThan(Version version) {
            return version() >= version.version();
        }

        /**
         * @param oldVersion The older version to check
         * @param newVersion The newer version to check
         * @return <code>true</code> if this version is newer than the oldVersion and older that the newVersion
         */
        @Deprecated
        public boolean inRange(Version oldVersion, Version newVersion) {
            return newerThan(oldVersion) && olderThan(newVersion);
        }

        public boolean matchesPackageName(String packageName) {
            return packageName.toLowerCase().contains(name().toLowerCase());
        }

        /**
         * @return the minecraft version
         */
        public MinecraftVersion minecraft() {
            return version;
        }

        static void runSanityCheck() {
            assert v1_14_R1.newerThan(v1_13_R2);
            assert v1_13_R2.olderThan(v1_14_R1);

            assert v1_13_R2.newerThan(v1_8_R1);

            assert v1_13_R2.newerThan(v1_8_R1) && v1_13_R2.olderThan(v1_14_R1);
        }

        @Deprecated
        public static Version getVersion() {
            String name = Bukkit.getServer().getClass().getPackage().getName();
            String versionPackage = name.substring(name.lastIndexOf('.') + 1);
            for (Version version : values()) {
                if (version.matchesPackageName(versionPackage)) {return version;}
            }
            System.err.println("[ReflectionHelper] Failed to find version enum for '" + name + "'/'" + versionPackage + "'");

            System.out.println("[ReflectionHelper] Generating dynamic constant...");
            Matcher matcher = NUMERIC_VERSION_PATTERN.matcher(versionPackage);
            while (matcher.find()) {
                if (matcher.groupCount() < 3) {continue;}

                String majorString = matcher.group(1);
                String minorString = matcher.group(2);
                if (minorString.length() == 1) {minorString = "0" + minorString;}
                String patchString = matcher.group(3);
                if (patchString.length() == 1) {patchString = "0" + patchString;}

                String numVersionString = majorString + minorString + patchString;
                int numVersion = Integer.parseInt(numVersionString);
                String packge = versionPackage;

                try {
                    // Add enum value
                    Field valuesField = new FieldResolver(Version.class).resolve("$VALUES");
                    Version[] oldValues = (Version[]) valuesField.get(null);
                    Version[] newValues = new Version[oldValues.length + 1];
                    System.arraycopy(oldValues, 0, newValues, 0, oldValues.length);
                    Version dynamicVersion = (Version) newEnumInstance(Version.class, new Class[]{
                            String.class,
                            int.class,
                            int.class
                    }, new Object[]{
                            packge,
                            newValues.length - 1,
                            numVersion
                    });
                    newValues[newValues.length - 1] = dynamicVersion;
                    valuesField.set(null, newValues);

                    System.out.println("[ReflectionHelper] Injected dynamic version " + packge + " (#" + numVersion + ").");
                    System.out.println("[ReflectionHelper] Please inform inventivetalent about the outdated version, as this is not guaranteed to work.");
                    return dynamicVersion;
                } catch (ReflectiveOperationException e) {
                    e.printStackTrace();
                }
            }

            return UNKNOWN;
        }

        @Override
        public String toString() {
            return name() + " (" + version() + ")";
        }
    }

    @Deprecated
    public static Object newEnumInstance(Class clazz, Class[] types, Object[] values) throws ReflectiveOperationException {
        Constructor constructor = new ConstructorResolver(clazz).resolve(types);
        Field accessorField = new FieldResolver(Constructor.class).resolve("constructorAccessor");
        Object constructorAccessor = accessorField.get(constructor);
        if (constructorAccessor == null) {
            new MethodResolver(Constructor.class).resolve("acquireConstructorAccessor").invoke(constructor);
            constructorAccessor = accessorField.get(constructor);
        }
        return new MethodResolver(constructorAccessor.getClass()).resolve("newInstance").invoke(constructorAccessor, (Object) values);
    }

}
