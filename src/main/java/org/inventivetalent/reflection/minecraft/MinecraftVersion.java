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

    MinecraftVersion(String packageName, int version) {
        this.packageName = packageName;
        this.version = version;
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
     * @return the package name
     */
    public String packageName() {
        return packageName;
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
        String name = Bukkit.getServer().getClass().getPackage().getName();
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
