package net.jasper.mod;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.jasper.mod.gui.option.PlayerAutomaOptionsScreen;

public class ModMenuApiImpl implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return (ConfigScreenFactory<PlayerAutomaOptionsScreen>) parent -> new PlayerAutomaOptionsScreen("", parent);
    }

}
