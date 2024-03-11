package betterquesting.questing.tasks;

import betterquesting.NBTUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.logging.log4j.Level;

import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api.utils.ItemComparison;
import betterquesting.api.utils.JsonHelper;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.utils.ParticipantInfo;
import betterquesting.client.gui2.tasks.PanelTaskCrafting;
import betterquesting.core.BetterQuesting;
import betterquesting.questing.tasks.factory.FactoryTaskCrafting;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TaskCrafting implements ITask {

    private static final boolean DEFAULT_PARTIAL_MATCH = true;
    private static final boolean DEFAULT_IGNORE_NBT = false;
    private static final boolean DEFAULT_ALLOW_ANVIL = false;
    private static final boolean DEFAULT_ALLOW_SMELT = true;
    private static final boolean DEFAULT_ALLOW_CRAFT = true;
    private final Set<UUID> completeUsers = new TreeSet<>();
    public final NonNullList<BigItemStack> requiredItems = NonNullList.create();
    public final TreeMap<UUID, int[]> userProgress = new TreeMap<>();
    public boolean partialMatch = DEFAULT_PARTIAL_MATCH;
    public boolean ignoreNBT = DEFAULT_IGNORE_NBT;
    public boolean allowAnvil = DEFAULT_ALLOW_ANVIL;
    public boolean allowSmelt = DEFAULT_ALLOW_SMELT;
    public boolean allowCraft = DEFAULT_ALLOW_CRAFT;

    @Override
    public ResourceLocation getFactoryID() { return FactoryTaskCrafting.INSTANCE.getRegistryName(); }

    @Override
    public boolean isComplete(UUID uuid) {
        return completeUsers.contains(uuid);
    }

    @Override
    public void setComplete(UUID uuid) {
        completeUsers.add(uuid);
    }

    @Override
    public String getUnlocalisedName() { return "bq_standard.task.crafting"; }

    @Override
    public void detect(ParticipantInfo pInfo, DBEntry<IQuest> quest) {
        pInfo.ALL_UUIDS.forEach((uuid) -> {
            if (isComplete(uuid))
                return;

            int[] tmp = getUsersProgress(uuid);
            for (int i = 0; i < requiredItems.size(); i++) {
                BigItemStack rStack = requiredItems.get(i);
                if (tmp[i] < rStack.stackSize)
                    return;
            }
            setComplete(uuid);
        });

        pInfo.markDirtyParty(Collections.singletonList(quest.getID()));
    }

    public void onItemCraft(ParticipantInfo pInfo, DBEntry<IQuest> quest, ItemStack stack) {
        if (!allowCraft)
            return;
        onItemInternal(pInfo, quest, stack);
    }

    public void onItemSmelt(ParticipantInfo pInfo, DBEntry<IQuest> quest, ItemStack stack) {
        if (!allowSmelt)
            return;
        onItemInternal(pInfo, quest, stack);
    }

    public void onItemAnvil(ParticipantInfo pInfo, DBEntry<IQuest> quest, ItemStack stack) {
        if (!allowAnvil)
            return;
        onItemInternal(pInfo, quest, stack);
    }

    private void onItemInternal(ParticipantInfo pInfo, DBEntry<IQuest> quest, ItemStack stack) {
        if (stack.isEmpty())
            return;

        final List<Tuple<UUID, int[]>> progress = getBulkProgress(pInfo.ALL_UUIDS);
        boolean changed = false;

        for (int i = 0; i < requiredItems.size(); i++) {
            final BigItemStack rStack = requiredItems.get(i);
            final int index = i;

            if (ItemComparison.StackMatch(rStack.getBaseStack(), stack, !ignoreNBT, partialMatch) || ItemComparison.OreDictionaryMatch(rStack
                    .getOreIngredient(), rStack.GetTagCompound(), stack, !ignoreNBT, partialMatch)) {
                progress.forEach((entry) -> {
                    if (entry.getSecond()[index] >= rStack.stackSize)
                        return;
                    entry.getSecond()[index] = Math.min(entry.getSecond()[index] + stack.getCount(), rStack.stackSize);
                });
                changed = true;
            }
        }

        if (changed) {
            setBulkProgress(progress);
            detect(pInfo, quest);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt, boolean reduce) {
        if (!reduce || partialMatch != DEFAULT_PARTIAL_MATCH)
            nbt.setBoolean("partialMatch", partialMatch);
        if (!reduce || ignoreNBT != DEFAULT_IGNORE_NBT)
            nbt.setBoolean("ignoreNBT", ignoreNBT);
        if (!reduce || allowCraft != DEFAULT_ALLOW_CRAFT)
            nbt.setBoolean("allowCraft", allowCraft);
        if (!reduce || allowSmelt != DEFAULT_ALLOW_SMELT)
            nbt.setBoolean("allowSmelt", allowSmelt);
        if (!reduce || allowAnvil != DEFAULT_ALLOW_ANVIL)
            nbt.setBoolean("allowAnvil", allowAnvil);

        NBTTagList itemArray = new NBTTagList();
        for (BigItemStack stack : this.requiredItems) {
            itemArray.appendTag(JsonHelper.ItemStackToJson(stack, new NBTTagCompound(), reduce));
        }
        nbt.setTag("requiredItems", itemArray);

        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        partialMatch = NBTUtil.getBoolean(nbt, "partialMatch", DEFAULT_PARTIAL_MATCH);
        ignoreNBT = NBTUtil.getBoolean(nbt, "ignoreNBT", DEFAULT_IGNORE_NBT);
        allowCraft = NBTUtil.getBoolean(nbt, "allowCraft", DEFAULT_ALLOW_CRAFT);
        allowSmelt = NBTUtil.getBoolean(nbt, "allowSmelt", DEFAULT_ALLOW_SMELT);
        allowAnvil = NBTUtil.getBoolean(nbt, "allowAnvil", DEFAULT_ALLOW_ANVIL);

        requiredItems.clear();
        NBTTagList iList = nbt.getTagList("requiredItems", 10);
        for (int i = 0; i < iList.tagCount(); i++) {
            requiredItems.add(JsonHelper.JsonToItemStack(iList.getCompoundTagAt(i)));
        }
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

                int[] data = new int[requiredItems.size()];
                NBTTagList dNbt = pTag.getTagList("data", 3);
                for (int i = 0; i < data.length && i < dNbt.tagCount(); i++) // TODO: Change this to an int array. This is dumb...
                {
                    data[i] = dNbt.getIntAt(i);
                }

                userProgress.put(uuid, data);
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

                int[] data = userProgress.get(uuid);
                if (data != null) {
                    NBTTagCompound pJson = new NBTTagCompound();
                    pJson.setString("uuid", uuid.toString());
                    NBTTagList pArray = new NBTTagList(); // TODO: Why the heck isn't this just an int array?!
                    for (int i : data)
                        pArray.appendTag(new NBTTagInt(i));
                    pJson.setTag("data", pArray);
                    progArray.appendTag(pJson);
                }
            });
        } else {
            completeUsers.forEach((uuid) -> jArray.appendTag(new NBTTagString(uuid.toString())));

            userProgress.forEach((uuid, data) -> {
                NBTTagCompound pJson = new NBTTagCompound();
                pJson.setString("uuid", uuid.toString());
                NBTTagList pArray = new NBTTagList(); // TODO: Why the heck isn't this just an int array?!
                for (int i : data)
                    pArray.appendTag(new NBTTagInt(i));
                pJson.setTag("data", pArray);
                progArray.appendTag(pJson);
            });
        }

        nbt.setTag("completeUsers", jArray);
        nbt.setTag("userProgress", progArray);

        return nbt;
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

    @Override
    public IGuiPanel getTaskGui(IGuiRect rect, DBEntry<IQuest> context) {
        return new PanelTaskCrafting(rect, this);
    }

    @Override @SideOnly(Side.CLIENT)
    public GuiScreen getTaskEditor(GuiScreen parent, DBEntry<IQuest> quest) {
        return null;
    }

    private void setUserProgress(UUID uuid, int[] progress) {
        userProgress.put(uuid, progress);
    }

    public int[] getUsersProgress(UUID uuid) {
        int[] progress = userProgress.get(uuid);
        return progress == null || progress.length != requiredItems.size() ? new int[requiredItems.size()] : progress;
    }

    private List<Tuple<UUID, int[]>> getBulkProgress(@Nonnull List<UUID> uuids) {
        if (uuids.size() <= 0)
            return Collections.emptyList();
        List<Tuple<UUID, int[]>> list = new ArrayList<>();
        uuids.forEach((key) -> list.add(new Tuple<>(key, getUsersProgress(key))));
        return list;
    }

    private void setBulkProgress(@Nonnull List<Tuple<UUID, int[]>> list) {
        list.forEach((entry) -> setUserProgress(entry.getFirst(), entry.getSecond()));
    }

    @Override
    public List<String> getTextForSearch() {
        List<String> texts = new ArrayList<>();
        for (BigItemStack bigStack : requiredItems) {
            ItemStack stack = bigStack.getBaseStack();
            texts.add(stack.getDisplayName());
            if (bigStack.hasOreDict()) {
                texts.add(bigStack.getOreDict());
            }
        }
        return texts;
    }

}
