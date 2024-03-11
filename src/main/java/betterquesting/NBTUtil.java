package betterquesting;

import javax.annotation.Nullable;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

public class NBTUtil {
    @SuppressWarnings("unchecked")
    public static <T extends NBTBase> T replaceStrings(T baseTag, String key, String replace) {
        if (baseTag == null) {
            return null;
        }

        if (baseTag instanceof NBTTagCompound) {
            NBTTagCompound compound = (NBTTagCompound) baseTag;

            for (String k : compound.getKeySet()) {
                compound.setTag(k, replaceStrings(compound.getTag(k), key, replace));
            }
        } else if (baseTag instanceof NBTTagList) {
            NBTTagList list = (NBTTagList) baseTag;

            for (int i = 0; i < list.tagCount(); i++) {
                list.set(i, replaceStrings(list.get(i), key, replace));
            }
        } else if (baseTag instanceof NBTTagString) {
            NBTTagString tString = (NBTTagString) baseTag;
            return (T) new NBTTagString(tString.getString().replaceAll(key, replace));
        }

        return baseTag; // Either isn't a string or doesn't contain one
    }

    public static boolean getBoolean(NBTTagCompound tag, String key, boolean defaultValue) {
        return tag.hasKey(key, 99) ? tag.getBoolean(key) : defaultValue;
    }

    public static int getInteger(NBTTagCompound tag, String key, int defaultValue) {
        return tag.hasKey(key, 99) ? tag.getInteger(key) : defaultValue;
    }

    public static float getFloat(NBTTagCompound tag, String key, float defaultValue) {
        return tag.hasKey(key, 99) ? tag.getFloat(key) : defaultValue;
    }

    public static String getString(NBTTagCompound tag, String key, String defaultValue) {
        return tag.hasKey(key, 8) ? tag.getString(key) : defaultValue;
    }

    public static <E extends Enum<E>> E getEnum(NBTTagCompound tag, String key, Class<E> enumClass, boolean ignoreCases, @Nullable E defaultValue) {
        if (tag.hasKey(key, 8)) {
            String valueStr = tag.getString(key);
            for (E value : enumClass.getEnumConstants()) {
                boolean equals = ignoreCases ? value.name().equalsIgnoreCase(valueStr) : value.name().equals(valueStr);
                if (equals) {
                    return value;
                }
            }
        }
        return defaultValue;
    }

}
