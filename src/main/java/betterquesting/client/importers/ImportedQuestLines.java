package betterquesting.client.importers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import betterquesting.api.questing.IQuestLine;
import betterquesting.api.questing.IQuestLineDatabase;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.storage.SimpleDatabase;
import betterquesting.api2.utils.QuestLineSorter;
import betterquesting.questing.QuestLine;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class ImportedQuestLines extends SimpleDatabase<IQuestLine> implements IQuestLineDatabase {

    private final List<Integer> lineOrder = new ArrayList<>();
    private final QuestLineSorter SORTER = new QuestLineSorter(this);

    @Override
    public int getOrderIndex(int lineID) {
        synchronized (lineOrder) {
            if (getValue(lineID) == null) {
                return -1;
            } else if (!lineOrder.contains(lineID)) {
                lineOrder.add(lineID);
            }

            return lineOrder.indexOf(lineID);
        }
    }

    @Override
    public void setOrderIndex(int lineID, int index) {
        synchronized (lineOrder) {
            lineOrder.remove((Integer) lineID);
            lineOrder.add(index, lineID);
        }
    }

    @Override
    public List<DBEntry<IQuestLine>> getSortedEntries() {
        List<DBEntry<IQuestLine>> ary = new ArrayList<>(getEntries());
        ary.sort(SORTER);
        return ary;
    }

    @Deprecated @Override
    public NBTTagList writeToNBT(NBTTagList nbt, List<Integer> subset) {
        return writeToNBT(nbt, subset, false);
    }

    @Override
    public NBTTagList writeToNBT(NBTTagList nbt, List<Integer> subset, boolean reduce) {
        for (DBEntry<IQuestLine> entry : getEntries()) {
            if (subset != null && !subset.contains(entry.getID()))
                continue;
            NBTTagCompound jObj = entry.getValue().writeToNBT(new NBTTagCompound(), null, reduce);
            jObj.setInteger("lineID", entry.getID());
            jObj.setInteger("order", getOrderIndex(entry.getID()));
            nbt.appendTag(jObj);
        }

        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagList json, boolean merge) {
        if (!merge)
            reset();

        List<IQuestLine> unassigned = new ArrayList<>();
        HashMap<Integer, Integer> orderMap = new HashMap<>();

        for (int i = 0; i < json.tagCount(); i++) {
            NBTTagCompound jql = json.getCompoundTagAt(i);

            int id = jql.hasKey("lineID", 99) ? jql.getInteger("lineID") : -1;
            int order = jql.hasKey("order", 99) ? jql.getInteger("order") : -1;
            QuestLine line = new QuestLine();
            line.readFromNBT(jql, merge);

            if (id >= 0) {
                add(id, line);
            } else {
                unassigned.add(line);
            }

            if (order >= 0) {
                orderMap.put(order, id);
            }
        }

        // Legacy support ONLY
        for (IQuestLine q : unassigned)
            add(nextID(), q);

        List<Integer> orderKeys = new ArrayList<>(orderMap.keySet());
        Collections.sort(orderKeys);

        synchronized (lineOrder) {
            lineOrder.clear();
            for (int o : orderKeys)
                lineOrder.add(orderMap.get(o));
        }
    }

    @Override
    public void removeQuest(int questID) {
        for (DBEntry<IQuestLine> ql : getEntries()) {
            ql.getValue().removeID(questID);
        }
    }

    @Override
    public IQuestLine createNew(int id) {
        QuestLine ql = new QuestLine();
        add(id, ql);
        return ql;
    }

}
