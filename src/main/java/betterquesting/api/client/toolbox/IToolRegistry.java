package betterquesting.api.client.toolbox;

import java.util.Collection;

import net.minecraft.util.ResourceLocation;

import betterquesting.api2.client.toolbox.IToolTab;

public interface IToolRegistry {

    void registerToolTab(ResourceLocation tabID, IToolTab tab);

    IToolTab getTabByID(ResourceLocation tabID);

    Collection<IToolTab> getAllTabs();
}
