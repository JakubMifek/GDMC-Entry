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
        @Config.RangeInt(min = 1, max = 512)
        public int NUMBER_OF_PLACED_BLOCKS = 16;

        @Config.Comment("Sets how many MS to wait between batch placements.")
        @Config.RangeInt(min = 0, max = 60000)
        public int PLACING_DELAY = 32;

        @Config.Comment("Sets how many MS to wait before checking for new batch of blocks to place when queue is empty.")
        @Config.RangeInt(min = 10, max = 60000)
        public int WAIT_DELAY = 100;


        @Config.Comment("Sets max house width for generate house command.")
        @Config.RangeInt(min = 6, max = 256)
        public int HOUSE_WIDTH = 16;


        @Config.Comment("Sets max house height for generate house command.")
        @Config.RangeInt(min = 6, max = 256)
        public int HOUSE_HEIGHT = 12;


        @Config.Comment("Sets max house depth for generate house command.")
        @Config.RangeInt(min = 6, max = 256)
        public int HOUSE_DEPTH = 16;
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