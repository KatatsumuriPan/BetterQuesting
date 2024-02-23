package betterquesting.api.questing;

import net.minecraft.nbt.NBTTagCompound;

import betterquesting.api2.storage.INBTSaveLoad;

public interface IQuestLineEntry extends INBTSaveLoad<NBTTagCompound> {

    @Deprecated
    int getSize();

    int getSizeX();

    int getSizeY();

    int getPosX();

    int getPosY();

    void setPosition(int posX, int posY);

    @Deprecated
    void setSize(int size);

    void setSize(int sizeX, int sizeY);
}
