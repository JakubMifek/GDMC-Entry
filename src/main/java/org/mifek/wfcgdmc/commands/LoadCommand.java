package org.mifek.wfcgdmc.commands;

import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import org.jetbrains.annotations.NotNull;
import org.mifek.vgl.commands.LoadTemplate;
import org.mifek.vgl.implementations.Area;
import org.mifek.vgl.implementations.PlacementStyle;
import org.mifek.vgl.wfc.StreamOptions;
import org.mifek.wfcgdmc.WfcGdmc;

import java.util.*;

public class LoadCommand extends CommandBase implements ICommand {
    private static final LoadTemplate command = new LoadTemplate();
    private final List<String> aliases = Arrays.asList("load", "l");

    @NotNull
    @Override
    public String getName() {
        return aliases.get(0);
    }

    @NotNull
    @Override
    public String getUsage(@NotNull ICommandSender sender) {
        return aliases.get(0) + " <template> <x1> <y1> <z1>";
    }

    @NotNull
    @Override
    public List<String> getAliases() {
        return aliases;
    }

    @Override
    public boolean checkPermission(@NotNull MinecraftServer server, @NotNull ICommandSender sender) {
        return true;
    }

    @Override
    public void execute(@NotNull MinecraftServer server, @NotNull ICommandSender sender, String[] args) {
        if (args.length == 1) {
            Minecraft instance = Minecraft.getMinecraft();
            if (instance.objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK) {
                BlockPos blockVector = instance.objectMouseOver.getBlockPos();
                double bX = blockVector.getX();
                double bY = blockVector.getY();
                double bZ = blockVector.getZ();

                args = new String[]{args[0], String.valueOf(bX), String.valueOf(bY), String.valueOf(bZ)};
            }
        }

        if (args.length != 4) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Expected 4 arguments <template> <x1> <y1> <z1>."));
            return;
        }

        int x1, y1, z1;
        try {
            x1 = (int) CommandBase.parseDouble(args[1]);
            y1 = (int) CommandBase.parseDouble(args[2]);
            z1 = (int) CommandBase.parseDouble(args[3]);
        } catch (NumberInvalidException exception) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Expected 3 NUMBER arguments <x1> <y1> <z1> after <template> argument."));
            return;
        }

        String name = args[0];
        sender.sendMessage(new TextComponentString("Loading " + name + " at [" + x1 + ", " + y1 + ", " + z1 + "]"));
        Area area = new Area(x1, y1, z1, 0, 0, 0);

        WfcGdmc.executors.submit(() -> {
            try {
                command.execute(name, new StreamOptions(WfcGdmc.overWorldBlockStream, area, PlacementStyle.ON_COLLAPSE));
            } catch (Error error) {
                System.out.println("Failed with unfortunate error: " + error.getMessage());
            }
        });
    }

    @Override
    public void init() {
    }
}
