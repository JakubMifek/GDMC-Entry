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
import org.mifek.vgl.commands.Generate;
import org.mifek.vgl.implementations.Area;
import org.mifek.vgl.implementations.Block;
import org.mifek.vgl.implementations.PlacementStyle;
import org.mifek.vgl.utils.TemplateHolder;
import org.mifek.vgl.wfc.MinecraftWfcAdapterOptions;
import org.mifek.vgl.wfc.StreamOptions;
import org.mifek.wfc.datatypes.Direction3D;
import org.mifek.wfc.models.options.Cartesian3DModelOptions;
import org.mifek.wfcgdmc.WfcGdmc;

import java.util.*;

public class ReplicateCommand extends CommandBase implements ICommand {
    private static final Generate generate = new Generate();
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

        Block[][][] template = TemplateHolder.INSTANCE.getTemplates().get(name);

/*
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

        final Block drsF = drs;
        final int dXF = dX, dYF = dY, dZF = dZ;
*/

        Iterable<Pair<Triple<Integer, Integer, Integer>, ? extends Block>> setBlocks = () -> {
            ArrayList<Pair<Triple<Integer, Integer, Integer>, ? extends Block>> holder = new ArrayList<>();
            HashMap<String, Object> emptyParams = new HashMap<>();
            /*
            if (drsF != null) {
                final int doorsX = dXF, doorsY = dYF, doorsZ = dZF;
                final Block doors = drsF;
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
            }
            */

            holder.add(new Pair<>(new Triple<>(w / 2, 0, d / 2), template[template.length / 2][0][template[0][0].length / 2]));

            System.out.println(holder.get(0));

            return holder.iterator();
        };

        HashSet<Direction3D> setPlanes = new HashSet<>();
        setPlanes.add(Direction3D.UP);
        setPlanes.add(Direction3D.FORWARD);
        setPlanes.add(Direction3D.RIGHT);
        setPlanes.add(Direction3D.DOWN);
        setPlanes.add(Direction3D.BACKWARD);
        setPlanes.add(Direction3D.LEFT);

        MinecraftWfcAdapterOptions options = new MinecraftWfcAdapterOptions(
                2,
                setBlocks,
                new Cartesian3DModelOptions(false, true, false, true, false, true, setPlanes, Collections.emptySet(), false, false, 0.333333),
                null,
                1,
                new StreamOptions(WfcGdmc.overWorldBlockStream, area, PlacementStyle.ON_COLLAPSE),
                name
        );

        WfcGdmc.executors.submit(() -> {
            try {
                generate.execute(name, area, options);
            } catch (Error error) {
                System.out.println("Failed with unfortunate error: " + error.getMessage());
            }
        });
    }

    @Override
    public void init() {

    }
}
