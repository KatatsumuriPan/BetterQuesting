package betterquesting.questing.tasks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.logging.log4j.Level;

import betterquesting.NBTUtil;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api.utils.ItemComparison;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.utils.ParticipantInfo;
import betterquesting.client.gui2.editors.tasks.GuiEditTaskTame;
import betterquesting.client.gui2.tasks.PanelTaskTame;
import betterquesting.core.BetterQuesting;
import betterquesting.questing.tasks.factory.FactoryTaskTame;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TaskTame implements ITask {

    private static final int DEFAULT_REQUIRED = 1;
    private static final boolean DEFAULT_IGNORE_NBT = true;
    private static final boolean DEFAULT_SUBTYPES = true;
    private final Set<UUID> completeUsers = new TreeSet<>();
    public final HashMap<UUID, Integer> userProgress = new HashMap<>();
    public String idName = "minecraft:wolf";
    public int required = DEFAULT_REQUIRED;
    public boolean ignoreNBT = DEFAULT_IGNORE_NBT;
    public boolean subtypes = DEFAULT_SUBTYPES;

    /**
     * NBT representation of the intended target. Used only for NBT comparison checks
     */
    public NBTTagCompound targetTags = new NBTTagCompound();

    @Override
    public String getUnlocalisedName() { return "bq_standard.task.tame"; }

    @Override
    public ResourceLocation getFactoryID() { return FactoryTaskTame.INSTANCE.getRegistryName(); }

    @Override
    public void detect(ParticipantInfo pInfo, DBEntry<IQuest> quest) {
        final List<Tuple<UUID, Integer>> progress = getBulkProgress(pInfo.ALL_UUIDS);
        int prev = completeUsers.size();

        progress.forEach((value) -> {
            if (value.getSecond() >= required)
                setComplete(value.getFirst());
        });

        if (prev != completeUsers.size())
            pInfo.markDirtyParty(Collections.singletonList(quest.getID()));
    }

    public void onAnimalTamed(ParticipantInfo pInfo, DBEntry<IQuest> quest, @Nonnull EntityLivingBase entity) {
        Class<? extends Entity> subject = entity.getClass();
        ResourceLocation targetID = new ResourceLocation(idName);
        Class<? extends Entity> target = EntityList.getClass(targetID);
        ResourceLocation subjectID = EntityList.getKey(subject);

        if (subjectID == null || target == null) {
            return; // Missing necessary data
        } else if (subtypes && !target.isAssignableFrom(subject)) {
            return; // This is not the intended target or sub-type
        } else if (!subtypes && !subjectID.equals(targetID)) {
            return; // This isn't the exact target required
        }

        NBTTagCompound subjectTags = new NBTTagCompound();
        entity.writeToNBTOptional(subjectTags);
        if (!ignoreNBT && !ItemComparison.CompareNBTTag(targetTags, subjectTags, true))
            return;

        final List<Tuple<UUID, Integer>> progress = getBulkProgress(pInfo.ALL_UUIDS);

        progress.forEach((value) -> {
            if (isComplete(value.getFirst()))
                return;
            int np = Math.min(required, value.getSecond() + 1);
            setUserProgress(value.getFirst(), np);
            if (np >= required)
                setComplete(value.getFirst());
        });

        pInfo.markDirtyParty(Collections.singletonList(quest.getID()));
    }

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
            userProgress.clear();
        } else {
            completeUsers.remove(uuid);
            userProgress.remove(uuid);
        }
    }

    @Nullable @Override @SideOnly(Side.CLIENT)
    public IGuiPanel getTaskGui(IGuiRect rect, DBEntry<IQuest> quest) {
        return new PanelTaskTame(rect, this);
    }

    @Nullable @Override @SideOnly(Side.CLIENT)
    public GuiScreen getTaskEditor(GuiScreen parent, DBEntry<IQuest> quest) {
        return new GuiEditTaskTame(parent, quest, this);
    }

    @Deprecated @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        return writeToNBT(nbt, false);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt, boolean reduce) {
        nbt.setString("target", idName);
        if (!reduce || required != DEFAULT_REQUIRED)
            nbt.setInteger("required", required);
        if (!reduce || subtypes != DEFAULT_SUBTYPES)
            nbt.setBoolean("subtypes", subtypes);
        if (!reduce || ignoreNBT != DEFAULT_IGNORE_NBT)
            nbt.setBoolean("ignoreNBT", ignoreNBT);
        if (!reduce || !targetTags.isEmpty())
            nbt.setTag("targetNBT", targetTags);

        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        idName = nbt.getString("target");
        required = NBTUtil.getInteger(nbt, "required", DEFAULT_REQUIRED);
        subtypes = NBTUtil.getBoolean(nbt, "subtypes", DEFAULT_SUBTYPES);
        ignoreNBT = NBTUtil.getBoolean(nbt, "ignoreNBT", DEFAULT_IGNORE_NBT);
        targetTags = nbt.getCompoundTag("targetNBT");
    }

    @Override
    public void readProgressFromNBT(NBTTagCompound nbt, boolean merge) {
        if (!merge) {
            completeUsers.clear();
            userProgress.clear();
        }

        NBTTagList cList = nbt.getTagList("completeUsers", 8);
        for (int i = 0; i < cList.tagCount(); i++) {
            try {
                completeUsers.add(UUID.fromString(cList.getStringTagAt(i)));
            } catch (Exception e) {
                BetterQuesting.logger.log(Level.ERROR, "Unable to load UUID for task", e);
            }
        }

        NBTTagList pList = nbt.getTagList("userProgress", 10);
        for (int n = 0; n < pList.tagCount(); n++) {
            try {
                NBTTagCompound pTag = pList.getCompoundTagAt(n);
                UUID uuid = UUID.fromString(pTag.getString("uuid"));
                userProgress.put(uuid, pTag.getInteger("value"));
            } catch (Exception e) {
                BetterQuesting.logger.log(Level.ERROR, "Unable to load user progress for task", e);
            }
        }
    }

    @Override
    public NBTTagCompound writeProgressToNBT(NBTTagCompound nbt, @Nullable List<UUID> users) {
        NBTTagList jArray = new NBTTagList();
        NBTTagList progArray = new NBTTagList();

        if (users != null) {
            users.forEach((uuid) -> {
                if (completeUsers.contains(uuid))
                    jArray.appendTag(new NBTTagString(uuid.toString()));

                Integer data = userProgress.get(uuid);
                if (data != null) {
                    NBTTagCompound pJson = new NBTTagCompound();
                    pJson.setString("uuid", uuid.toString());
                    pJson.setInteger("value", data);
                    progArray.appendTag(pJson);
                }
            });
        } else {
            completeUsers.forEach((uuid) -> jArray.appendTag(new NBTTagString(uuid.toString())));

            userProgress.forEach((uuid, data) -> {
                NBTTagCompound pJson = new NBTTagCompound();
                pJson.setString("uuid", uuid.toString());
                pJson.setInteger("value", data);
                progArray.appendTag(pJson);
            });
        }

        nbt.setTag("completeUsers", jArray);
        nbt.setTag("userProgress", progArray);

        return nbt;
    }

    private void setUserProgress(UUID uuid, int progress) {
        userProgress.put(uuid, progress);
    }

    public int getUsersProgress(UUID uuid) {
        Integer n = userProgress.get(uuid);
        return n == null ? 0 : n;
    }

    private List<Tuple<UUID, Integer>> getBulkProgress(@Nonnull List<UUID> uuids) {
        if (uuids.size() <= 0)
            return Collections.emptyList();
        List<Tuple<UUID, Integer>> list = new ArrayList<>();
        uuids.forEach((key) -> list.add(new Tuple<>(key, getUsersProgress(key))));
        return list;
    }

    @Override
    public List<String> getTextForSearch() { return Collections.singletonList(idName); }

}
