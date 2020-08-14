package org.inventivetalent.reflection.minecraft;

import org.bukkit.Bukkit;

import java.util.regex.Matcher;

public class MinecraftVersion {

    public static final MinecraftVersion VERSION;

    static {
        System.out.println("[ReflectionHelper/MinecraftVersion] I am loaded from package " + Minecraft.class.getPackage().getName());
        VERSION = MinecraftVersion.getVersion();
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
        return packageName.toLowerCase() + " (" + version() + ")";
    }

    public static MinecraftVersion getVersion() {
        String name = Bukkit.getServer().getClass().getPackage().getName();
        String versionPackage = name.substring(name.lastIndexOf('.') + 1) + ".";
        for (Minecraft.Version version : Minecraft.Version.values()) {
            MinecraftVersion minecraftVersion = version.minecraft();
            if (minecraftVersion.matchesPackageName(versionPackage)) {
                return minecraftVersion;
            }
        }
        System.err.println("[ReflectionHelper] Failed to find version enum for '" + name + "'/'" + versionPackage + "'");

        System.out.println("[ReflectionHelper] Generating dynamic constant...");
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
            String packageName = versionPackage.substring(0, versionPackage.length() - 1);

            //dynamic register version
            System.out.println("[ReflectionHelper] Injected dynamic version " + packageName + " (#" + numVersion + ").");
            System.out.println("[ReflectionHelper] Please inform inventivetalent about the outdated version, as this is not guaranteed to work.");
            return new MinecraftVersion(packageName, numVersion);
        }

        return new MinecraftVersion("UNKNOWN", -1);
    }
}
