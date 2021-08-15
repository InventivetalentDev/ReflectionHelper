package org.inventivetalent.reflection.minecraft;

import org.inventivetalent.reflection.resolver.ConstructorResolver;
import org.inventivetalent.reflection.resolver.FieldResolver;
import org.inventivetalent.reflection.resolver.MethodResolver;
import org.inventivetalent.reflection.resolver.ResolverQuery;
import org.inventivetalent.reflection.resolver.minecraft.NMSClassResolver;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DataWatcher {

    static NMSClassResolver nmsClassResolver = new NMSClassResolver();

    static Class<?> ItemStack = nmsClassResolver.resolveSilent("ItemStack", "world.item.ItemStack");
    static Class<?> ChunkCoordinates = nmsClassResolver.resolveSilent("ChunkCoordinates", "world.level.ChunkCoordinates");
    static Class<?> BlockPosition = nmsClassResolver.resolveSilent("BlockPosition", "core.BlockPosition");
    static Class<?> Vector3f = nmsClassResolver.resolveSilent("Vector3f", "core.Vector3f");
    static Class<?> DataWatcher = nmsClassResolver.resolveSilent("DataWatcher", "network.syncher.DataWatcher");
    static Class<?> Entity = nmsClassResolver.resolveSilent("Entity", "world.entity.Entity");

    static ConstructorResolver DataWacherConstructorResolver = new ConstructorResolver(DataWatcher);

    static FieldResolver DataWatcherFieldResolver = new FieldResolver(DataWatcher);

    static MethodResolver DataWatcherMethodResolver = new MethodResolver(DataWatcher);

    public static Object newDataWatcher(Object entity) throws ReflectiveOperationException {
        return DataWacherConstructorResolver.resolve(new Class[]{Entity}).newInstance(entity);
    }

    public static Object setValue(Object dataWatcher, int index, Object dataWatcherObject/*1.9*/, Object value) throws ReflectiveOperationException {
        if (Minecraft.VERSION.olderThan(Minecraft.Version.v1_9_R1)) {
            return V1_8.setValue(dataWatcher, index, value);
        } else {
            return V1_9.setValue(dataWatcher, dataWatcherObject, value);
        }
    }

    public static Object setValue(Object dataWatcher, int index, V1_9.ValueType type, Object value) throws ReflectiveOperationException {
        return setValue(dataWatcher, index, type.getType(), value);
    }

    public static Object setValue(Object dataWatcher, int index, Object value, FieldResolver dataWatcherObjectFieldResolver/*1.9*/, String... dataWatcherObjectFieldNames/*1.9*/) throws ReflectiveOperationException {
        if (Minecraft.VERSION.olderThan(Minecraft.Version.v1_9_R1)) {
            return V1_8.setValue(dataWatcher, index, value);
        } else {
            Object dataWatcherObject = dataWatcherObjectFieldResolver.resolve(dataWatcherObjectFieldNames).get(null/*Should be a static field*/);
            return V1_9.setValue(dataWatcher, dataWatcherObject, value);
        }
    }

    @Deprecated
    public static Object getValue(DataWatcher dataWatcher, int index) throws ReflectiveOperationException {
        if (Minecraft.VERSION.olderThan(Minecraft.Version.v1_9_R1)) {
            return V1_8.getValue(dataWatcher, index);
        } else {
            return V1_9.getValue(dataWatcher, index);
        }
    }

    public static Object getValue(Object dataWatcher, int index, V1_9.ValueType type) throws ReflectiveOperationException {
        return getValue(dataWatcher, index, type.getType());
    }

    public static Object getValue(Object dataWatcher, int index, Object dataWatcherObject/*1.9*/) throws ReflectiveOperationException {
        if (Minecraft.VERSION.olderThan(Minecraft.Version.v1_9_R1)) {
            return V1_8.getWatchableObjectValue(V1_8.getValue(dataWatcher, index));
        } else {
            return V1_9.getValue(dataWatcher, dataWatcherObject);
        }
    }

    //TODO: update type-ids to 1.9
    public static int getValueType(Object value) {
        int type = 0;
        if (value instanceof Number) {
            if (value instanceof Byte) {
                type = 0;
            } else if (value instanceof Short) {
                type = 1;
            } else if (value instanceof Integer) {
                type = 2;
            } else if (value instanceof Float) {
                type = 3;
            }
        } else if (value instanceof String) {
            type = 4;
        } else if (value != null && value.getClass().equals(ItemStack)) {
            type = 5;
        } else if (value != null && (value.getClass().equals(ChunkCoordinates) || value.getClass().equals(BlockPosition))) {
            type = 6;
        } else if (value != null && value.getClass().equals(Vector3f)) {
            type = 7;
        }

        return type;
    }

    /**
     * Helper class for versions newer than 1.9
     */
    public static class V1_9 {

        static Class<?> DataWatcherItem = nmsClassResolver.resolveSilent("DataWatcher$Item", "network.syncher.DataWatcher$Item");//>= 1.9 only
        static Class<?> DataWatcherObject = nmsClassResolver.resolveSilent("DataWatcherObject", "network.syncher.DataWatcherObject");//>= 1.9 only

        static ConstructorResolver DataWatcherItemConstructorResolver;//>=1.9 only

        static FieldResolver DataWatcherItemFieldResolver;//>=1.9 only
        static FieldResolver DataWatcherObjectFieldResolver;//>=1.9 only

        public static Object newDataWatcherItem(Object dataWatcherObject, Object value) throws ReflectiveOperationException {
            if (DataWatcherItemConstructorResolver == null) {
                DataWatcherItemConstructorResolver = new ConstructorResolver(DataWatcherItem);
            }
            return DataWatcherItemConstructorResolver.resolveFirstConstructor().newInstance(dataWatcherObject, value);
        }

        public static Object setItem(Object dataWatcher, int index, Object dataWatcherObject, Object value) throws ReflectiveOperationException {
            return setItem(dataWatcher, index, newDataWatcherItem(dataWatcherObject, value));
        }

        public static Object setItem(Object dataWatcher, int index, Object dataWatcherItem) throws ReflectiveOperationException {
            Map<Integer, Object> map = (Map<Integer, Object>) DataWatcherFieldResolver.resolveByLastTypeSilent(Map.class).get(dataWatcher);
            map.put(index, dataWatcherItem);
            return dataWatcher;
        }

        public static Object setValue(Object dataWatcher, Object dataWatcherObject, Object value) throws ReflectiveOperationException {
            DataWatcherMethodResolver.resolve("set").invoke(dataWatcher, dataWatcherObject, value);
            return dataWatcher;
        }

        //		public static Object getValue(Object dataWatcher, int index) throws ReflectiveOperationException {
        //			Map<Integer, Object> map = (Map<Integer, Object>) DataWatcherFieldResolver.resolve("c").get(dataWatcher);
        //			return map.get(index);
        //		}

        public static Object getItem(Object dataWatcher, Object dataWatcherObject) throws ReflectiveOperationException {
            return DataWatcherMethodResolver.resolve(new ResolverQuery("c", DataWatcherObject)).invoke(dataWatcher, dataWatcherObject);
        }

        public static Object getValue(Object dataWatcher, Object dataWatcherObject) throws ReflectiveOperationException {
            return DataWatcherMethodResolver.resolve("get").invoke(dataWatcher, dataWatcherObject);
        }

        public static Object getValue(Object dataWatcher, ValueType type) throws ReflectiveOperationException {
            return getValue(dataWatcher, type.getType());
        }

        public static Object getItemObject(Object item) throws ReflectiveOperationException {
            if (DataWatcherItemFieldResolver == null) { DataWatcherItemFieldResolver = new FieldResolver(DataWatcherItem); }
            return DataWatcherItemFieldResolver.resolve("a").get(item);
        }

        public static int getItemIndex(Object dataWatcher, Object item) throws ReflectiveOperationException {
            int index = -1;//Return -1 if the item is not in the DataWatcher
            Map<Integer, Object> map = (Map<Integer, Object>) DataWatcherFieldResolver.resolveByLastTypeSilent(Map.class).get(dataWatcher);
            for (Map.Entry<Integer, Object> entry : map.entrySet()) {
                if (entry.getValue().equals(item)) {
                    index = entry.getKey();
                    break;
                }
            }
            return index;
        }

        public static Type getItemType(Object item) throws ReflectiveOperationException {
            if (DataWatcherObjectFieldResolver == null) { DataWatcherObjectFieldResolver = new FieldResolver(DataWatcherObject); }
            Object object = getItemObject(item);
            Object serializer = DataWatcherObjectFieldResolver.resolve("b").get(object);
            Type[] genericInterfaces = serializer.getClass().getGenericInterfaces();
            if (genericInterfaces.length > 0) {
                Type type = genericInterfaces[0];
                if (type instanceof ParameterizedType) {
                    Type[] actualTypes = ((ParameterizedType) type).getActualTypeArguments();
                    if (actualTypes.length > 0) {
                        return actualTypes[0];
                    }
                }
            }
            return null;
        }

        public static Object getItemValue(Object item) throws ReflectiveOperationException {
            if (DataWatcherItemFieldResolver == null) { DataWatcherItemFieldResolver = new FieldResolver(DataWatcherItem); }
            return DataWatcherItemFieldResolver.resolve("b").get(item);
        }

        public static void setItemValue(Object item, Object value) throws ReflectiveOperationException {
            DataWatcherItemFieldResolver.resolve("b").set(item, value);
        }

        public enum ValueType {

            /**
             * Byte
             */
            @Deprecated
            ENTITY_FLAG("world.entity.Entity", 57, 0 /*"ax", "ay"*/),
            ENTITY_SHARED_FLAGS("world.entity.Entity", 57, 0),
            /**
             * Integer
             */
            ENTITY_AIR_TICKS("world.entity.Entity", 58, 1),
            /**
             * String
             */
            ENTITY_NAME("world.entity.Entity", 59, 2/*"az", "aA"*/),
            /**
             * Byte &lt; 1.9 Boolean &gt; 1.9
             */
            ENTITY_NAME_VISIBLE("world.entity.Entity", 60, 3/*"aA", "aB"*/),
            /**
             * Boolean
             */
            ENTITY_SILENT("world.entity.Entity", 61, 4/*"aB", "aC"*/),

            ENTITY_NO_GRAVITY("world.entity.Entity", 0, 5),

            ENTITY_POSE("world.entity.Entity", 0, 6),

            ENTITY_TICKS_FROZEN("world.entity.Entity", 0, 7),

            //////////

            ENTITY_LIVING_FLAGS("world.entity.EntityLiving", 0, 0),

            /**
             * Float
             */
            ENTITY_LIVING_HEALTH("world.entity.EntityLiving", 0, 1),

            @Deprecated
            ENTITY_LIVING_f("world.entity.EntityLiving", 4, 2/*"f"*/),
            ENTITY_LIVING_COLOR("world.entity.EntityLiving", 4, 2),

            @Deprecated
            ENTITY_LIVING_g("world.entity.EntityLiving", 5, 3/*"g"*/),
            ENTITY_LIVING_AMBIENCE("world.entity.EntityLiving", 5, 3),

            @Deprecated
            ENTITY_LIVING_h("world.entity.EntityLiving", 6, 4/*"h"*/),
            ENTITY_LIVING_ARROW_COUNT("world.entity.EntityLiving", 6, 4),

            ENTITY_LIVING_STINGER_COUNT("world.entity.EntityLiving", 6, 5),

            //////////

            /**
             * Byte
             */
            ENTITY_INSENTIENT_FLAG("world.entity.EntityInsentient", 0, 0/* "a"*/),

            ///////////

            /**
             * Integer
             */
            ENTITY_SLIME_SIZE("world.entity.monster.EntitySlime", 0, 0/* "bt", "bu"*/),

            /////////////

            @Deprecated
            ENTITY_WITHER_a("world.entity.boss.wither.EntityWither", 0, 0/*"a"*/),
            ENTITY_WITHER_TARGET_A("world.entity.boss.wither.EntityWither", 0, 0),

            @Deprecated
            ENTITY_WIHER_b("world.entity.boss.wither.EntityWither", 1, 1/*"b"*/),
            ENTITY_WITHER_TARGET_B("world.entity.boss.wither.EntityWither", 1, 1),

            @Deprecated
            ENTITY_WITHER_c("world.entity.boss.wither.EntityWither", 2, 2/*"c"*/),
            ENTITY_WITHER_TARGET_C("world.entity.boss.wither.EntityWither", 2, 2),

            @Deprecated
            ENTITY_WITHER_bw("world.entity.boss.wither.EntityWither", 3, 3/*"bw", "bx"*/),
            ENTITY_WITHER_ID("world.entity.boss.wither.EntityWither", 3, 3),

            //////////

            ENTITY_AGEABLE_CHILD("world.entity.EntityAgeable", 0, 0),

            //////////

            ENTITY_HORSE_CHESTED_ID("world.entity.animal.horse.EntityHorseChestedAbstract", 0, 0),

            ///////////

            ENTITY_HORSE_ABSTRACT_FLAGS("world.entity.animal.horse.EntityHorseAbstract", 0, 0),
            ENTITY_HORSE_ABSTRACT_OWNER_UUID("world.entity.animal.horse.EntityHorseAbstract", 0, 1),

            ENTITY_HORSE_TYPE_VARIANT("world.entity.animal.horse.EntityHorse", 0, 0),

            /////////


            /**
             * Float
             */
            ENTITY_HUMAN_ABSORPTION_HEARTS("world.entity.player.EntityHuman", 0, 0 /*"a"*/),

            /**
             * Integer
             */
            ENTITY_HUMAN_SCORE("world.entity.player.EntityHuman", 1, 1 /*"b"*/),

            /**
             * Byte
             */
            ENTITY_HUMAN_SKIN_LAYERS("world.entity.player.EntityHuman", 2, 2 /*"bp", "bq"*/),

            /**
             * Byte (0 = left, 1 = right)
             */
            ENTITY_HUMAN_MAIN_HAND("world.entity.player.EntityHuman", 3, 3/*"bq", "br"*/),

            ENTITY_HUMAN_SHOULDER_LEFT("world.entity.player.EntityHuman", 0, 4),
            ENTITY_HUMAN_SHOULDER_RIGHT("world.entity.player.EntityHuman", 0, 5),
            ;

            private Object type;

            ValueType(String className, String... fieldNames) {
                try {
                    this.type = new FieldResolver(nmsClassResolver.resolve(className)).resolve(fieldNames).get(null);
                } catch (Exception e) {
                    System.err.println("[ReflectionHelper] Failed to find DataWatcherObject for " + className + " " + Arrays.toString(fieldNames));
                }
            }

            ValueType(String className, int index) {
                try {
                    this.type = new FieldResolver(nmsClassResolver.resolve(className)).resolveIndex(index).get(null);
                } catch (Exception e) {
                    System.err.println("[ReflectionHelper] Failed to find DataWatcherObject for " + className + " #" + index);
                }
            }

            ValueType(String className, int ignored, int offset) {
                List<String> dataWatcherFields = new ArrayList<>();
                try {
                    Class<?> clazz = nmsClassResolver.resolve(className);
                    for (Field field : clazz.getDeclaredFields()) {
                        if ("DataWatcherObject".equals(field.getType().getSimpleName())) {
                            dataWatcherFields.add(field.getName());
                        }
                        if (dataWatcherFields.size() > offset + 1) break;
                    }
                    this.type = new FieldResolver(clazz).resolveAccessor(dataWatcherFields.get(offset)).get(null);
                } catch (Exception e) {
                    System.err.println("[ReflectionHelper] Failed to find DataWatcherObject for " + className + " #" + ignored + " (offset: " + offset + ", fields: " + dataWatcherFields + ")");
                }
            }

            public boolean hasType() {
                return getType() != null;
            }

            public Object getType() {
                return type;
            }
        }

    }

    /**
     * Helper class for versions older than 1.8
     */
    public static class V1_8 {

        static Class<?> WatchableObject = nmsClassResolver.resolveSilent("WatchableObject", "DataWatcher$WatchableObject");//<=1.8 only

        static ConstructorResolver WatchableObjectConstructorResolver;//<=1.8 only

        static FieldResolver WatchableObjectFieldResolver;//<=1.8 only

        public static Object newWatchableObject(int index, Object value) throws ReflectiveOperationException {
            return newWatchableObject(getValueType(value), index, value);
        }

        public static Object newWatchableObject(int type, int index, Object value) throws ReflectiveOperationException {
            if (WatchableObjectConstructorResolver == null) {
                WatchableObjectConstructorResolver = new ConstructorResolver(WatchableObject);
            }
            return WatchableObjectConstructorResolver.resolve(new Class[]{
                    int.class,
                    int.class,
                    Object.class}).newInstance(type, index, value);
        }

        public static Object setValue(Object dataWatcher, int index, Object value) throws ReflectiveOperationException {
            int type = getValueType(value);

            Map map = (Map) DataWatcherFieldResolver.resolveByLastType(Map.class).get(dataWatcher);
            map.put(index, newWatchableObject(type, index, value));

            return dataWatcher;
        }

        public static Object getValue(Object dataWatcher, int index) throws ReflectiveOperationException {
            Map map = (Map) DataWatcherFieldResolver.resolveByLastType(Map.class).get(dataWatcher);

            return map.get(index);
        }

        public static int getWatchableObjectIndex(Object object) throws ReflectiveOperationException {
            if (WatchableObjectFieldResolver == null) { WatchableObjectFieldResolver = new FieldResolver(WatchableObject); }
            return WatchableObjectFieldResolver.resolve("b").getInt(object);
        }

        public static int getWatchableObjectType(Object object) throws ReflectiveOperationException {
            if (WatchableObjectFieldResolver == null) { WatchableObjectFieldResolver = new FieldResolver(WatchableObject); }
            return WatchableObjectFieldResolver.resolve("a").getInt(object);
        }

        public static Object getWatchableObjectValue(Object object) throws ReflectiveOperationException {
            if (WatchableObjectFieldResolver == null) { WatchableObjectFieldResolver = new FieldResolver(WatchableObject); }
            return WatchableObjectFieldResolver.resolve("c").get(object);
        }

    }

    private DataWatcher() {
    }

}
