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
import org.mifek.vgl.commands.Replicate;
import org.mifek.vgl.implementations.Area;
import org.mifek.vgl.implementations.PlacementStyle;
import org.mifek.vgl.wfc.StreamOptions;
import org.mifek.wfcgdmc.WfcGdmc;

import java.util.*;

public class ReplicateCommand extends CommandBase implements ICommand {
    private static final Replicate command = new Replicate();
    private final List<String> aliases = Arrays.asList("replicate", "r");

    @NotNull
    @Override
    public String getName() {
        return aliases.get(0);
    }

    @NotNull
    @Override
    public String getUsage(@NotNull ICommandSender sender) {
        return aliases.get(0) + " <template> <x1> <y1> <z1> <w> <h> <d>";
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
        if (args.length == 4) {
            Minecraft instance = Minecraft.getMinecraft();
            if (instance.objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK) {
                BlockPos blockVector = instance.objectMouseOver.getBlockPos();
                double bX = blockVector.getX();
                double bY = blockVector.getY();
                double bZ = blockVector.getZ();

                args = new String[]{args[0], String.valueOf(bX), String.valueOf(bY), String.valueOf(bZ), args[1], args[2], args[3]};
            }
        }

        if (args.length != 7) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Expected 7 arguments <template> <x1> <y1> <z1> <w> <h> <d>."));
            return;
        }

        int x1, y1, z1, w, h, d;
        try {
            x1 = (int) CommandBase.parseDouble(args[1]);
            y1 = (int) CommandBase.parseDouble(args[2]);
            z1 = (int) CommandBase.parseDouble(args[3]);
            w = (int) CommandBase.parseDouble(args[4]);
            h = (int) CommandBase.parseDouble(args[5]);
            d = (int) CommandBase.parseDouble(args[6]);
        } catch (NumberInvalidException exception) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Expected 6 NUMBER arguments <x1> <y1> <z1> <w> <h> <d> after <template> argument."));
            return;
        }

        String name = args[0];
        sender.sendMessage(new TextComponentString("Replicating " + name + " at [" + x1 + ", " + y1 + ", " + z1 + "] in area of dimensions " + w + "x" + h + "x" + d));

        Area area = new Area(x1, y1, z1, w, h, d);
        StreamOptions so = new StreamOptions(WfcGdmc.overWorldBlockStream, area, PlacementStyle.ON_COLLAPSE);

        WfcGdmc.executors.submit(() -> {
            try {
                command.execute(name, area, so);
            } catch (Error error) {
                System.out.println("Failed with unfortunate error: " + error.getMessage());
            }
        });
    }

    @Override
    public void init() { }
}
