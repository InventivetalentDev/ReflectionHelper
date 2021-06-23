package org.inventivetalent.reflection.minecraft;

import org.bukkit.Bukkit;

import java.util.regex.Matcher;

public class MinecraftVersion {

    public static final MinecraftVersion VERSION;

    static {
        System.out.println("[ReflectionHelper/MinecraftVersion] I am loaded from package " + Minecraft.class.getPackage().getName());
        try {
            VERSION = MinecraftVersion.getVersion();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get version", e);
        }
        System.out.println("[ReflectionHelper/MinecraftVersion] Version is " + VERSION);
    }

    private final String packageName;
    private final int version;
    private final String nmsFormat;
    private final String obcFormat;
    private final String nmsPackage;
    private final String obcPackage;
    private final boolean nmsVersionPrefix;

    MinecraftVersion(String packageName, int version, String nmsFormat, String obcFormat, boolean nmsVersionPrefix) {
        this.packageName = packageName;
        this.version = version;
        this.nmsFormat = nmsFormat;
        this.obcFormat = obcFormat;
        this.nmsPackage = String.format(this.nmsFormat, packageName);
        this.obcPackage = String.format(this.obcFormat, packageName);
        this.nmsVersionPrefix = nmsVersionPrefix;
    }

    MinecraftVersion(String packageName, int version) {
        this(packageName, version, "net.minecraft.server.%s", "org.bukkit.craftbukkit.%s", true);
    }

    // Used by SantiyCheck
    MinecraftVersion(Minecraft.Version version) {
        this(version.name(), version.version());
    }

    /**
     * @return the version-number
     */
    public int version() {
        return version;
    }

    /**
     * @deprecated use {@link #getNmsPackage()} / {@link #getObcPackage()} instead
     */
    @Deprecated
    public String packageName() {
        return packageName;
    }

    /**
     * @return the full package name for net.minecraft....
     */
    public String getNmsPackage() {
        return nmsPackage;
    }

    /**
     * @return the full package name for org.bukkit....
     */
    public String getObcPackage() {
        return obcPackage;
    }

    /**
     * @return if the nms package name has version prefix
     */
    public boolean hasNMSVersionPrefix() {
        return nmsVersionPrefix;
    }

    /**
     * @param version the version to check
     * @return <code>true</code> if this version is older than the specified version
     */
    public boolean olderThan(Minecraft.Version version) {
        return version() < version.version();
    }

    /**
     * @param version the version to check
     * @return <code>true</code> if this version is equals than the specified version
     */
    public boolean equal(Minecraft.Version version) {
        return version() < version.version();
    }

    /**
     * @param version the version to check
     * @return <code>true</code> if this version is newer than the specified version
     */
    public boolean newerThan(Minecraft.Version version) {
        return version() >= version.version();
    }

    /**
     * @param oldVersion The older version to check
     * @param newVersion The newer version to check
     * @return <code>true</code> if this version is newer than the oldVersion and older that the newVersion
     */
    public boolean inRange(Minecraft.Version oldVersion, Minecraft.Version newVersion) {
        return newerThan(oldVersion) && olderThan(newVersion);
    }

    public boolean matchesPackageName(String packageName) {
        return this.packageName.toLowerCase().contains(packageName.toLowerCase());
    }

    @Override
    public String toString() {
        return packageName + " (" + version() + ")";
    }

    public static MinecraftVersion getVersion() {
        Class serverClass;
        try {
            serverClass = Bukkit.getServer().getClass();
        } catch (Exception e) {
            System.err.println("[ReflectionHelper/MinecraftVersion] Failed to get bukkit server class: " + e.getMessage());
            System.err.println("[ReflectionHelper/MinecraftVersion] Assuming we're in a test environment!");
            return null;
        }
        String name = serverClass.getPackage().getName();
        String versionPackage = name.substring(name.lastIndexOf('.') + 1);
        for (Minecraft.Version version : Minecraft.Version.values()) {
            MinecraftVersion minecraftVersion = version.minecraft();
            if (minecraftVersion.matchesPackageName(versionPackage)) {
                return minecraftVersion;
            }
        }
        System.err.println("[ReflectionHelper/MinecraftVersion] Failed to find version enum for '" + name + "'/'" + versionPackage + "'");

        System.out.println("[ReflectionHelper/MinecraftVersion] Generating dynamic constant...");
        Matcher matcher = Minecraft.NUMERIC_VERSION_PATTERN.matcher(versionPackage);
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
            String packageName = "v" + versionPackage.substring(1).toUpperCase();

            //dynamic register version
            System.out.println("[ReflectionHelper/MinecraftVersion] Injected dynamic version " + packageName + " (#" + numVersion + ").");
            System.out.println("[ReflectionHelper/MinecraftVersion] Please inform inventivetalent about the outdated version, as this is not guaranteed to work.");
            return new MinecraftVersion(packageName, numVersion);
        }

        System.err.println("[ReflectionHelper/MinecraftVersion] Failed to create dynamic version for " + versionPackage);

        return new MinecraftVersion("UNKNOWN", -1);
    }
}
