package org.mifek.wfcgdmc;

import net.minecraft.command.ICommand;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.mifek.wfcgdmc.commands.*;


@Mod(modid = WfcGdmc.MODID, name = WfcGdmc.NAME, version = WfcGdmc.VERSION)
public class WfcGdmc {
    public static final String MODID = "wfcgdmc";
    static final String NAME = "WFC GDMC Entry";
    static final String VERSION = "1.0.0";
    public static BlockStream overWorldBlockStream;

    static final ICommand[] commands = new ICommand[]{
            new HelloWorldCommand(),
            new SpawnHouseCommand(),
            new GenerateHouseCommand(),
            new SpawnBlockCommand(),
            new SaveTemplateCommand(),
            new ReplicateCommand(),
            new ReplicateManyCommand()
    };

    @EventHandler
    public static void preInit(FMLPreInitializationEvent event) {
    }

    @EventHandler
    public static void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new WfcGdmc());
    }

    @EventHandler
    public static void serverStarting(FMLServerStartingEvent event) {
        for (ICommand command : commands) {
            event.registerServerCommand(command);
        }
        WfcGdmc.overWorldBlockStream = new BlockStream(event.getServer().worlds[0]);
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        WfcGdmc.overWorldBlockStream.tick();
    }


}
