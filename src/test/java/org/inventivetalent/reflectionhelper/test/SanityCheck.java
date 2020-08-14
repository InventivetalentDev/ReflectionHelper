package org.inventivetalent.reflectionhelper.test;

import org.inventivetalent.reflection.minecraft.Minecraft;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class SanityCheck {

    @Test
    public void runSanityCheck(){
        assertTrue(Minecraft.VersionConstant.v1_14_R1.minecraft().newerThan(Minecraft.VersionConstant.v1_13_R2));
        assertTrue(Minecraft.VersionConstant.v1_13_R2.minecraft().olderThan(Minecraft.VersionConstant.v1_14_R1));

        assertTrue(Minecraft.VersionConstant.v1_13_R2.minecraft().newerThan(Minecraft.VersionConstant.v1_8_R1));

        assertTrue(Minecraft.VersionConstant.v1_13_R2.minecraft().newerThan(Minecraft.VersionConstant.v1_8_R1) &&
                Minecraft.VersionConstant.v1_13_R2.minecraft().olderThan(Minecraft.VersionConstant.v1_14_R1));
    }
}
