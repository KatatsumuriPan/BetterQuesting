package betterquesting.questing.tasks;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.logging.log4j.Level;

import betterquesting.NBTUtil;
import betterquesting.api.questing.IQuest;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.utils.ParticipantInfo;
import betterquesting.client.gui2.tasks.PanelTaskLocation;
import betterquesting.core.BetterQuesting;
import betterquesting.questing.tasks.factory.FactoryTaskLocation;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

public class TaskLocation implements ITaskTickable {

    private static final String DEFAULT_STRUCTURE = "";
    private static final String DEFAULT_BIOME = "";
    private static final int DEFAULT_X = 0;
    private static final int DEFAULT_Y = 0;
    private static final int DEFAULT_Z = 0;
    private static final int DEFAULT_DIM = 0;
    private static final int DEFAULT_RANGE = -1;
    private static final boolean DEFAULT_VISIBLE = false;
    private static final boolean DEFAULT_HIDE_INFO = false;
    private static final boolean DEFAULT_INVERT = false;
    private static final boolean DEFAULT_TAXI_CAB = false;
    private final Set<UUID> completeUsers = new TreeSet<>();
    public String name = "New Location";
    public String structure = DEFAULT_STRUCTURE;
    public String biome = DEFAULT_BIOME;
    public int x = DEFAULT_X;
    public int y = DEFAULT_Y;
    public int z = DEFAULT_Z;
    public int dim = DEFAULT_DIM;
    public int range = DEFAULT_RANGE;
    public boolean visible = DEFAULT_VISIBLE;
    public boolean hideInfo = DEFAULT_HIDE_INFO;
    public boolean invert = DEFAULT_INVERT;
    public boolean taxiCab = DEFAULT_TAXI_CAB;

    @Override
    public ResourceLocation getFactoryID() { return FactoryTaskLocation.INSTANCE.getRegistryName(); }

    @Override
    public String getUnlocalisedName() { return "bq_standard.task.location"; }

    @Override
    public boolean isComplete(UUID uuid) {
        return completeUsers.contains(uuid);
    }

    @Override
    public void setComplete(UUID uuid) {
        completeUsers.add(uuid);
    }

    @Override
    public void resetUser(@Nullable UUID uuid) {
        if (uuid == null) {
            completeUsers.clear();
        } else {
            completeUsers.remove(uuid);
        }
    }

    @Override
    public void tickTask(@Nonnull ParticipantInfo pInfo, DBEntry<IQuest> quest) {
        if (pInfo.PLAYER.ticksExisted % 100 == 0)
            internalDetect(pInfo, quest);
    }

    @Override
    public void detect(@Nonnull ParticipantInfo pInfo, DBEntry<IQuest> quest) {
        internalDetect(pInfo, quest);
    }

    private void internalDetect(@Nonnull ParticipantInfo pInfo, DBEntry<IQuest> quest) {
        if (!pInfo.PLAYER.isEntityAlive() || !(pInfo.PLAYER instanceof EntityPlayerMP))
            return;

        EntityPlayerMP playerMP = (EntityPlayerMP) pInfo.PLAYER;

        boolean flag = false;

        if (playerMP.dimension == dim && (range <= 0 || getDistance(playerMP) <= range)) {
            if (!StringUtils.isNullOrEmpty(biome) && !new ResourceLocation(biome).equals(playerMP.getServerWorld()
                    .getBiome(playerMP.getPosition())
                    .getRegistryName())) {
                if (!invert)
                    return;
            } else if (!StringUtils.isNullOrEmpty(structure) && !playerMP.getServerWorld()
                    .getChunkProvider()
                    .isInsideStructure(playerMP.world, structure, playerMP.getPosition())) {
                if (!invert)
                    return;
            } else if (visible && range > 0) // Do not do ray casting with infinite range!
            {
                Vec3d pPos = new Vec3d(playerMP.posX, playerMP.posY + playerMP.getEyeHeight(), playerMP.posZ);
                Vec3d tPos = new Vec3d(x, y, z);
                RayTraceResult mop = playerMP.world.rayTraceBlocks(pPos, tPos, false, true, false);

                if (mop == null || mop.typeOfHit != RayTraceResult.Type.BLOCK) {
                    flag = true;
                }
            } else {
                flag = true;
            }
        }

        if (flag != invert) {
            pInfo.ALL_UUIDS.forEach((uuid) -> {
                if (!isComplete(uuid))
                    setComplete(uuid);
            });
            pInfo.markDirtyParty(Collections.singletonList(quest.getID()));
        }
    }

    private double getDistance(EntityPlayer player) {
        if (!taxiCab) {
            return player.getDistance(x, y, z);
        } else {
            BlockPos pPos = player.getPosition();
            return Math.abs(pPos.getX() - x) + Math.abs(pPos.getY() - y) + Math.abs(pPos.getZ() - z);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt, boolean reduce) {
        nbt.setString("name", name);
        if (!reduce || x != DEFAULT_X)
            nbt.setInteger("posX", x);
        if (!reduce || y != DEFAULT_Y)
            nbt.setInteger("posY", y);
        if (!reduce || z != DEFAULT_Z)
            nbt.setInteger("posZ", z);
        if (!reduce || dim != DEFAULT_DIM)
            nbt.setInteger("dimension", dim);
        if (!reduce || !biome.equals(DEFAULT_BIOME))
            nbt.setString("biome", biome);
        if (!reduce || !structure.equals(DEFAULT_STRUCTURE))
            nbt.setString("structure", structure);
        if (!reduce || range != DEFAULT_RANGE)
            nbt.setInteger("range", range);
        if (!reduce || visible != DEFAULT_VISIBLE)
            nbt.setBoolean("visible", visible);
        if (!reduce || hideInfo != DEFAULT_HIDE_INFO)
            nbt.setBoolean("hideInfo", hideInfo);
        if (!reduce || invert != DEFAULT_INVERT)
            nbt.setBoolean("invert", invert);
        if (!reduce || taxiCab != DEFAULT_TAXI_CAB)
            nbt.setBoolean("taxiCabDist", taxiCab);

        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        name = nbt.getString("name");
        x = NBTUtil.getInteger(nbt, "posX", DEFAULT_X);
        y = NBTUtil.getInteger(nbt, "posY", DEFAULT_Y);
        z = NBTUtil.getInteger(nbt, "posZ", DEFAULT_Z);
        dim = NBTUtil.getInteger(nbt, "dimension", DEFAULT_DIM);
        biome = NBTUtil.getString(nbt, "biome", DEFAULT_BIOME);
        structure = NBTUtil.getString(nbt, "structure", DEFAULT_STRUCTURE);
        range = NBTUtil.getInteger(nbt, "range", DEFAULT_RANGE);
        visible = NBTUtil.getBoolean(nbt, "visible", DEFAULT_VISIBLE);
        hideInfo = NBTUtil.getBoolean(nbt, "hideInfo", DEFAULT_HIDE_INFO);
        invert = NBTUtil.getBoolean(nbt, "invert", DEFAULT_INVERT) || nbt.getBoolean("invertDistance");
        taxiCab = NBTUtil.getBoolean(nbt, "taxiCabDist", DEFAULT_TAXI_CAB);
    }

    @Override
    public NBTTagCompound writeProgressToNBT(NBTTagCompound nbt, @Nullable List<UUID> users) {
        NBTTagList jArray = new NBTTagList();

        completeUsers.forEach((uuid) -> {
            if (users == null || users.contains(uuid))
                jArray.appendTag(new NBTTagString(uuid.toString()));
        });

        nbt.setTag("completeUsers", jArray);

        return nbt;
    }

    @Override
    public void readProgressFromNBT(NBTTagCompound nbt, boolean merge) {
        if (!merge)
            completeUsers.clear();
        NBTTagList cList = nbt.getTagList("completeUsers", 8);
        for (int i = 0; i < cList.tagCount(); i++) {
            try {
                completeUsers.add(UUID.fromString(cList.getStringTagAt(i)));
            } catch (Exception e) {
                BetterQuesting.logger.log(Level.ERROR, "Unable to load UUID for task", e);
            }
        }
    }

    @Override
    public IGuiPanel getTaskGui(IGuiRect rect, DBEntry<IQuest> quest) {
        return new PanelTaskLocation(rect, this);
    }

    @Override
    public GuiScreen getTaskEditor(GuiScreen parent, DBEntry<IQuest> quest) {
        return null;
    }

    @Override
    public List<String> getTextForSearch() { return Collections.singletonList(name); }

}
