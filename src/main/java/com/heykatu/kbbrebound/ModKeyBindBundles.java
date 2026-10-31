package com.matyrobbrt.keybindbundles;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(ModKeyBindBundles.MOD_ID)
public class ModKeyBindBundles {
    public static final String MOD_ID = "keybindbundles";
    public ModKeyBindBundles() {
        if (FMLEnvironment.dist.isClient()) {
            initClient(FMLJavaModLoadingContext.get().getModEventBus());
        }
    }

    private void initClient(IEventBus bus) {
        ModKeyBindBundlesClient.init(bus);
    }
}
