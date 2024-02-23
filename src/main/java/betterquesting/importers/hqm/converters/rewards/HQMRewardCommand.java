package betterquesting.importers.hqm.converters.rewards;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import betterquesting.api.questing.rewards.IReward;
import betterquesting.questing.rewards.RewardCommand;

public class HQMRewardCommand {

    public IReward[] convertReward(JsonElement json) {
        if (!(json instanceof JsonArray))
            return null;

        List<IReward> rList = new ArrayList<>();

        for (JsonElement je : json.getAsJsonArray()) {
            if (!(je instanceof JsonPrimitive))
                continue;
            RewardCommand reward = new RewardCommand();
            reward.command = je.getAsString();
            reward.viaPlayer = true;
            rList.add(reward);
        }

        return rList.toArray(new IReward[0]);
    }

}
