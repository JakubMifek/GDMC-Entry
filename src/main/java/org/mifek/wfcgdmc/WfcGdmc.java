package org.mifek.wfcgdmc;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.mifek.wfcgdmc.commands.HelloWorldCommand;
import org.mifek.wfcgdmc.commands.SpawnBlockCommand;
import org.mifek.wfcgdmc.commands.SpawnHouseCommand;


@Mod(modid = WfcGdmc.MODID, name = WfcGdmc.NAME, version = WfcGdmc.VERSION)
public class WfcGdmc {
    public static final String MODID = "wfcgdmc";
    static final String NAME = "WFC GDMC Entry";
    static final String VERSION = "1.0.0";
    public static BlockStream overWorldBlockStream;


    @EventHandler
    public static void preInit(FMLPreInitializationEvent event) {
    }

    @EventHandler
    public static void init(FMLInitializationEvent event) {
    }

    @EventHandler
    public static void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new HelloWorldCommand());
        event.registerServerCommand(new SpawnHouseCommand());
        event.registerServerCommand(new SpawnBlockCommand());
        WfcGdmc.overWorldBlockStream = new BlockStream(event.getServer().worlds[0]);
        WfcGdmc.overWorldBlockStream.start();
    }

}
