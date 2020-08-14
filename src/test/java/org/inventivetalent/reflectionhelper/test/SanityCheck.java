package org.inventivetalent.reflectionhelper.test;

import org.inventivetalent.reflection.minecraft.Minecraft;
import org.inventivetalent.reflection.minecraft.MinecraftVersion;
import org.inventivetalent.reflection.resolver.ConstructorResolver;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class SanityCheck {

    @Test
    public void runSanityCheck(){
        MinecraftVersion v1_14 = createVersion(Minecraft.Version.v1_14_R1);
        MinecraftVersion v1_13 = createVersion(Minecraft.Version.v1_13_R1);

        assertTrue(v1_14.newerThan(Minecraft.Version.v1_13_R2));
        assertTrue(v1_13.olderThan(Minecraft.Version.v1_14_R1));

        assertTrue(v1_13.newerThan(Minecraft.Version.v1_8_R1));

        assertTrue(v1_13.newerThan(Minecraft.Version.v1_8_R1) &&
                v1_13.olderThan(Minecraft.Version.v1_14_R1));
    }


    public MinecraftVersion createVersion(Minecraft.Version version){
        ConstructorResolver constructorResolver = new ConstructorResolver(MinecraftVersion.class);
        try {
            return (MinecraftVersion) constructorResolver.resolveLastConstructor().newInstance(version);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
        return null;
    }
}
