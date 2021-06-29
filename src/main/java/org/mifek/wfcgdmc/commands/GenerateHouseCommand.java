package org.mifek.wfcgdmc.commands;

import kotlin.Pair;
import kotlin.Triple;
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
import org.mifek.vgl.commands.GenerateHouse;
import org.mifek.vgl.implementations.*;
import org.mifek.vgl.utils.TemplateHolder;
import org.mifek.vgl.wfc.*;
import org.mifek.wfc.datatypes.Direction3D;
import org.mifek.wfc.models.options.Cartesian2DModelOptions;
import org.mifek.wfc.models.options.Cartesian3DModelOptions;
import org.mifek.wfcgdmc.WfcGdmc;

import java.util.*;

public class GenerateHouseCommand extends CommandBase implements ICommand {
    private final List<String> aliases = Arrays.asList("generate_house", "gh");
    private final HashMap<String, Object> emptyMap = new HashMap<>();
    private final GenerateHouse generate = new GenerateHouse();

    @NotNull
    @Override
    public String getName() {
        return aliases.get(0);
    }

    @NotNull
    @Override
    public String getUsage(@NotNull ICommandSender sender) {
        return aliases.get(0) + "<template> <x> <y> <z> <w> <h> <d>";
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
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Expected 7 arguments <template> <x> <y> <z> <w> <h> <d>."));
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
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Expected 6 NUMBER arguments <x> <y> <z> <w> <h> <d>."));
            return;
        }

        String name = args[0];
        Area area = new Area(x1, y1, z1, w, h, d);

        WfcGdmc.executors.submit(() -> {
            try {
                sender.sendMessage(new TextComponentString("Generating house based on " + name + " at [" + x1 + ";" + y1 + ";" + z1 + "] with dimensions [" + w + "x" + h + "x" + d + "]"));

                generate.execute(name, area, new MinecraftWfcAdapterOptions(
                        2, null,
                        new Cartesian3DModelOptions(
                                false, true, false,
                                true, false, true,
                                Collections.emptySet(),
                                Collections.emptySet(),
                                false, false, 1. / 3.
                        ),
                        null,
                        1,
                        new StreamOptions(
                                WfcGdmc.overWorldBlockStream, area, PlacementStyle.ON_COLLAPSE
                        ),
                        name
                ));
                sender.sendMessage(new TextComponentString("...finished " + name));
            } catch (Error error) {
                sender.sendMessage(new TextComponentString("...finished " + name + " with an unfortunate error " + error.getMessage()));
            }
        });

    }

    @Override
    public void init() {
        Area area = new Area(0, 0, 0, 3, 3, 3);
        for (String name : TemplateHolder.INSTANCE.getTemplates().keySet()) {
            generate.execute(name, area, new MinecraftWfcAdapterOptions(
                    2, null,
                    new Cartesian3DModelOptions(
                            false, true, false,
                            true, false, true,
                            Collections.emptySet(),
                            Collections.emptySet(),
                            false, false, 1. / 3.
                    ),
                    null,
                    0,
                    null,
                    name
            ));
        }
    }
}
