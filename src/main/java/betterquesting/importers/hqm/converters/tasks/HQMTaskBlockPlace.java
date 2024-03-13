package betterquesting.importers.hqm.converters.tasks;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import betterquesting.api.questing.tasks.ITask;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api.utils.JsonHelper;
import betterquesting.importers.hqm.HQMUtilities;
import betterquesting.questing.tasks.TaskInteractItem;
import net.minecraft.nbt.NBTTagCompound;

public class HQMTaskBlockPlace {

    public ITask[] convertTask(JsonObject json) {
        List<ITask> tList = new ArrayList<>();

        for (JsonElement je : JsonHelper.GetArray(json, "blocks")) {
            if (!(je instanceof JsonObject))
                continue;
            JsonObject jObj = je.getAsJsonObject();

            TaskInteractItem task = new TaskInteractItem();
            BigItemStack stack = HQMUtilities.HQMStackT1(JsonHelper.GetObject(jObj, "item"));
            task.targetItem = new BigItemStack(stack.writeToNBT(new NBTTagCompound(), false));
            task.required = JsonHelper.GetNumber(jObj, "required", 1).intValue();
            tList.add(task);
        }

        return tList.toArray(new ITask[0]);
    }

}
