package org.mifek.wfcgdmc.config;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.mifek.wfcgdmc.WfcGdmc;

@Config(modid = WfcGdmc.MODID)
public class WfcGdmcConfig {
    public static final Client client = new Client();

    public static class Client {

        @Config.Comment("Sets how many blocks are to be placed in single batch.")
        @Config.RangeInt(min = 1, max = 4096)
        public int NUMBER_OF_PLACED_BLOCKS = 512;

        @Config.Comment("Sets the percentage chance of a path spawning.")
        @Config.RangeInt(min = 0, max = 100)
        public int PATH_CHANCE = 65;
        /*
        @Config.Comment("Sets how many MS to wait between batch placements.")
        @Config.RangeInt(min = 0, max = 60000)
        public int PLACING_DELAY = 32;

        @Config.Comment("Sets how many MS to wait before checking for new batch of blocks to place when queue is empty.")
        @Config.RangeInt(min = 10, max = 60000)
        public int WAIT_DELAY = 100;
        */
    }

    @Mod.EventBusSubscriber(modid = WfcGdmc.MODID)
    private static class EventHandler {

        /**
         * Inject the new values and save to the config file when the config has been changed from the GUI.
         *
         * @param event The event
         */
        @SubscribeEvent
        public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
            if (event.getModID().equals(WfcGdmc.MODID)) ConfigManager.sync(WfcGdmc.MODID, Config.Type.INSTANCE);
        }
    }
}