package org.mifek.wfcgdmc.commands;

import com.google.common.collect.Lists;
import kotlin.Pair;
import kotlin.Triple;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import org.jetbrains.annotations.NotNull;
import org.mifek.vgl.commands.Generate;
import org.mifek.vgl.implementations.Area;
import org.mifek.vgl.implementations.Block;
import org.mifek.vgl.implementations.Blocks;
import org.mifek.vgl.implementations.PlacementStyle;
import org.mifek.vgl.utils.TemplateHolder;
import org.mifek.vgl.wfc.MinecraftWfcAdapterOptions;
import org.mifek.vgl.wfc.StreamOptions;
import org.mifek.wfc.models.options.Cartesian3DModelOptions;
import org.mifek.wfcgdmc.WfcGdmc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ReplicateCommand extends CommandBase {
    private static final Generate generate = new Generate();
    private final List<String> aliases = Lists.newArrayList("replicate", "r");

    @NotNull
    @Override
    public String getName() {
        return aliases.get(0);
    }

    @NotNull
    @Override
    public String getUsage(@NotNull ICommandSender sender) {
        return aliases.get(0) + " <template> <x1> <y1> <z1> <x2> <y2> <z2>";
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
        if (args.length != 7) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Expected 7 arguments <template> <x1> <y1> <z1> <x2> <y2> <z2>."));
            return;
        }

        int x1, y1, z1, x2, y2, z2, tmp;
        try {
            x1 = (int) CommandBase.parseDouble(args[1]);
            y1 = (int) CommandBase.parseDouble(args[2]);
            z1 = (int) CommandBase.parseDouble(args[3]);
            x2 = (int) CommandBase.parseDouble(args[4]);
            y2 = (int) CommandBase.parseDouble(args[5]);
            z2 = (int) CommandBase.parseDouble(args[6]);
        } catch (NumberInvalidException exception) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Expected 6 NUMBER arguments <x1> <y1> <z1> <x2> <y2> <z2> after <template> argument."));
            return;
        }

        if (x1 > x2) {
            tmp = x1;
            x1 = x2;
            x2 = tmp;
        }
        if (y1 > y2) {
            tmp = y1;
            y1 = y2;
            y2 = tmp;
        }
        if (z1 > z2) {
            tmp = z1;
            z1 = z2;
            z2 = tmp;
        }
        int w = x2 - x1, h = y2 - y1, d = z2 - z1;

        String name = args[0];
        sender.sendMessage(new TextComponentString("Replicating " + name + " at [" + x1 + ", " + y1 + ", " + z1 + "] in area of dimensions " + w + "x" + h + "x" + d));

        Area area = new Area(x1, y1, z1, w, h, d);

        Block[][][] template = TemplateHolder.INSTANCE.getTemplates().get(name);

        Block drs = null;
        int dX = 0, dY = 0, dZ = 0;
        for (int z = 0; z < template[0][0].length && drs == null; z++) {
            for (int y = 0; y < template[0].length && drs == null; y++) {
                for (int x = 0; x < template.length && drs == null; x++) {
                    if (template[x][y][z].getBlock() == Blocks.OAK_DOOR) {
                        drs = template[x][y][z];
                        dX = x;
                        dY = y;
                        dZ = z;
                    }
                }
            }
        }

        if (drs == null) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Selected template " + name + " did not contain any doors."));
            return;
        }
        final int doorsX = dX, doorsY = dY, doorsZ = dZ;
        final Block doors = drs;

        MinecraftWfcAdapterOptions options = new MinecraftWfcAdapterOptions(
                2,
                () -> {
                    ArrayList<Pair<Triple<Integer, Integer, Integer>, ? extends Block>> holder = new ArrayList<>();
                    HashMap<String, Object> emptyParams = new HashMap<>();

                    for (int x = 0; x < w; x++) {
                        for (int y = 1; y < h; y++) {
                            holder.add(new Pair<>(new Triple<>(x, y, 0), new Block(Blocks.AIR, emptyParams)));
                            holder.add(new Pair<>(new Triple<>(x, y, d - 1), new Block(Blocks.AIR, emptyParams)));
                            if (y == h - 1 || x == 0 || x == w - 1) {
                                for (int z = 0; z < d; z++) {
                                    holder.add(new Pair<>(new Triple<>(x, y, z), new Block(Blocks.AIR, emptyParams)));
                                }
                            }
                        }
                    }

                    holder.add(new Pair<>(new Triple<>(doorsX, doorsY, doorsZ), doors));

                    return holder.iterator();
                },
                new Cartesian3DModelOptions(false, true, false, false, true, false, false, false),
                null,
                5,
                new StreamOptions(WfcGdmc.overWorldBlockStream, area, PlacementStyle.ON_COLLAPSE)
        );

        new Thread(() -> generate.execute(name, area, options)).start();
    }
}
