package org.mifek.wfcgdmc.commands;

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
import org.mifek.wfc.datatypes.Direction3D;
import org.mifek.wfc.models.options.Cartesian3DModelOptions;
import org.mifek.wfcgdmc.WfcGdmc;

import java.util.*;

public class ReplicateManyCommand extends CommandBase implements ICommand {
    private static final Generate generate = new Generate();
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

        HashSet<Direction3D> setPlanes = new HashSet<>();
        setPlanes.add(Direction3D.UP);
        setPlanes.add(Direction3D.FORWARD);
        setPlanes.add(Direction3D.RIGHT);
        setPlanes.add(Direction3D.DOWN);
        setPlanes.add(Direction3D.BACKWARD);
        setPlanes.add(Direction3D.LEFT);

        for (int a = 0; a < amount; a++)
            for (int b = 0; b < amount; b++) {
                int id = a * amount + b;
                Area area = new Area(x1 + b * w, y1, z1 + a * d, w, h, d);

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
                        new Cartesian3DModelOptions(
                                false, true, false,
                                true, false, true,
                                setPlanes, Collections.emptySet(),
                                false, false, 1.0),
                        null,
                        0,
                        new StreamOptions(WfcGdmc.overWorldBlockStream, area, PlacementStyle.ON_COLLAPSE),
                        name
                );

                WfcGdmc.executors.submit(() -> {
                    System.out.println("Executing order " + id);
                    generate.execute(name, area, options);
                });
            }
    }

    @Override
    public void init() {
//        System.out.println("Replicate Many Command is initializing the templates.");
//        for (String key : TemplateHolder.INSTANCE.getTemplates().keySet()) {
//            Area area = new Area(0, 0, 0, 3, 3, 3);
//            try {
//                generate.execute(key, area, new MinecraftWfcAdapterOptions(
//                        2,
//                        null,
//                        new Cartesian3DModelOptions(false, true, false, true, false, true, false, false, 1.0),
//                        null,
//                        0,
//                        null,
//                        key
//                ));
//            } catch (Error ignored) {
//            }
//        }
    }
}
