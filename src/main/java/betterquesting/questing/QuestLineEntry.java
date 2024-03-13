package betterquesting.questing;

import betterquesting.api.questing.IQuestLineEntry;
import net.minecraft.nbt.NBTTagCompound;

public class QuestLineEntry implements IQuestLineEntry {

    private int sizeX = 0;
    private int sizeY = 0;
    private int posX = 0;
    private int posY = 0;

    public QuestLineEntry(NBTTagCompound json) {
        this.readFromNBT(json);
    }

    public QuestLineEntry(int x, int y) {
        this(x, y, 24, 24);
    }

    @Deprecated
    public QuestLineEntry(int x, int y, int size) {
        this.sizeX = size;
        this.sizeY = size;
        this.posX = x;
        this.posY = y;
    }

    public QuestLineEntry(int x, int y, int sizeX, int sizeY) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.posX = x;
        this.posY = y;
    }

    @Override @Deprecated
    public int getSize() { return Math.max(getSizeX(), getSizeY()); }

    @Override
    public int getSizeX() { return this.sizeX; }

    @Override
    public int getSizeY() { return this.sizeY; }

    @Override
    public int getPosX() { return posX; }

    @Override
    public int getPosY() { return posY; }

    @Override
    public void setPosition(int posX, int posY) {
        this.posX = posX;
        this.posY = posY;
    }

    @Override @Deprecated
    public void setSize(int size) {
        this.sizeX = size;
        this.sizeY = size;
    }

    @Override
    public void setSize(int sizeX, int sizeY) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
    }

    @Deprecated @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        return writeToNBT(nbt, false);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt, boolean reduce) {
        nbt.setInteger("sizeX", sizeX);
        nbt.setInteger("sizeY", sizeY);
        nbt.setInteger("x", posX);
        nbt.setInteger("y", posY);
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound json) {
        if (json.hasKey("size", 99)) {
            sizeX = json.getInteger("size");
            sizeY = sizeX;
        } else {
            sizeX = json.getInteger("sizeX");
            sizeY = json.getInteger("sizeY");
        }
        posX = json.getInteger("x");
        posY = json.getInteger("y");
    }

    @Override
    public String toString() {
        return "QuestLineEntry{" + "sizeX=" + sizeX + ", sizeY=" + sizeY + ", posX=" + posX + ", posY=" + posY + '}';
    }

}
