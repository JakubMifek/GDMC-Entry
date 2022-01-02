package org.mifek.wfcgdmc.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import org.jetbrains.annotations.NotNull;
import org.mifek.vgl.commands.Replicate;
import org.mifek.vgl.implementations.Area;
import org.mifek.vgl.implementations.PlacementStyle;
import org.mifek.vgl.wfc.StreamOptions;
import org.mifek.wfcgdmc.WfcGdmc;

import java.util.*;

public class ReplicateManyCommand extends CommandBase implements ICommand {
    private static final Replicate command = new Replicate();
    private final List<String> aliases = Arrays.asList("replicate_many", "rm");

    @NotNull
    @Override
    public String getName() {
        return aliases.get(0);
    }

    @NotNull
    @Override
    public String getUsage(@NotNull ICommandSender sender) {
        return aliases.get(0) + " <template> <x1> <y1> <z1> <w> <h> <d> <amount>";
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
        if (args.length != 8) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Expected 8 arguments <template> <x1> <y1> <z1> <w> <h> <d> <amount>."));
            return;
        }

        int x1, y1, z1, w, h, d, amount;
        try {
            x1 = (int) CommandBase.parseDouble(args[1]);
            y1 = (int) CommandBase.parseDouble(args[2]);
            z1 = (int) CommandBase.parseDouble(args[3]);
            w = (int) CommandBase.parseDouble(args[4]);
            h = (int) CommandBase.parseDouble(args[5]);
            d = (int) CommandBase.parseDouble(args[6]);
            amount = (int) CommandBase.parseDouble(args[7]);
        } catch (NumberInvalidException exception) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Expected 6 NUMBER arguments <x1> <y1> <z1> <x2> <y2> <z2> after <template> argument."));
            return;
        }

        String name = args[0];
        sender.sendMessage(new TextComponentString("Replicating " + name + " at [" + x1 + ", " + y1 + ", " + z1 + "] in area of dimensions " + w * amount + "x" + h + "x" + d * amount));

        for (int a = 0; a < amount; a++)
            for (int b = 0; b < amount; b++) {
                int id = a * amount + b;
                Area area = new Area(x1 + b * w, y1, z1 + a * d, w, h, d);

                WfcGdmc.executors.submit(() -> {
                    System.out.println("Executing order " + id);
                    command.execute(name, area, new StreamOptions(WfcGdmc.overWorldBlockStream, area, PlacementStyle.ON_COLLAPSE));
                });
            }
    }

    @Override
    public void init() {
    }
}
