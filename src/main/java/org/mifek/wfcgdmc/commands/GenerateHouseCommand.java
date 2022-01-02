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
import org.mifek.vgl.ConstantsKt;
import org.mifek.vgl.commands.GenerateHouse;
import org.mifek.vgl.implementations.Area;
import org.mifek.vgl.implementations.PlacedBlock;
import org.mifek.vgl.implementations.PlacementStyle;
import org.mifek.vgl.utils.TemplateHolder;
import org.mifek.vgl.wfc.MinecraftWfcAdapterOptions;
import org.mifek.vgl.wfc.StreamOptions;
import org.mifek.wfcgdmc.WfcGdmc;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;

/*
class GenerateRunnable implements Callable<PlacedBlock[][][]> {
    private final ICommandSender sender;
    private final String name;
    private final GenerateHouse generate;
    private final IArea area;
    private final int x1;
    private final int y1;
    private final int z1;
    private final int w;
    private final int h;
    private final int d;
    private final boolean ret;
    public PlacedBlock[][][] res;

    GenerateRunnable(ICommandSender sender, String name, GenerateHouse generate, IArea area, int x1, int y1, int z1, int w, int h, int d, boolean ret) {
        this.sender = sender;
        this.name = name;
        this.generate = generate;
        this.area = area;
        this.x1 = x1;
        this.y1 = y1;
        this.z1 = z1;
        this.w = w;
        this.h = h;
        this.d = d;
        this.ret = ret;
    }

    @Override
    public PlacedBlock[][][] call() {
        try {
            sender.sendMessage(new TextComponentString("Generating house based on " + name + " at [" + x1 + ";" + y1 + ";" + z1 + "] with dimensions [" + w + "x" + h + "x" + d + "]"));

            PlacedBlock[][][] res = generate.execute(name, area, new MinecraftWfcAdapterOptions(
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
                    ret ? new StreamOptions(
                            WfcGdmc.overWorldBlockStream, area, PlacementStyle.ON_COLLAPSE
                    ) : null,
                    name
            ));

            sender.sendMessage(new TextComponentString("...finished " + name));

            if (res == null) res = new PlacedBlock[0][0][0];

            this.res = res;

            streamHouse(res);

            return this.res;
        } catch (Error error) {
            sender.sendMessage(new TextComponentString("...finished " + name + " with an unfortunate error " + error.getMessage()));
            return new PlacedBlock[0][0][0];
        }
    }

    private void streamHouse(PlacedBlock[][][] house) {
        if (house.length == 0) return;

        GenerateHouse.Companion.stream(
                WfcGdmc.overWorldBlockStream,
                new Area(house[0][0][0].getX(), house[0][0][0].getY(), house[0][0][0].getZ(), house.length, house[0].length, house[0][0].length),
                house, null, GenerateHouse.Companion.getBREAKABLE());

        GenerateHouse.Companion.stream(
                WfcGdmc.overWorldBlockStream,
                new Area(house[0][0][0].getX(), house[0][0][0].getY(), house[0][0][0].getZ(), house.length, house[0].length, house[0][0].length),
                house, GenerateHouse.Companion.getBREAKABLE(), null);
    }
}
*/

public class GenerateHouseCommand extends CommandBase implements ICommand {
    private final List<String> aliases = Arrays.asList("generate_house", "gh");
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

    public Future<PlacedBlock[][][]> executeFuture(@NotNull MinecraftServer server, @NotNull ICommandSender sender, String[] args, Boolean stream) {
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
            return null;
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
            return null;
        }

        String name = args[0];
        Area area = new Area(x1, y1, z1, w, h, d);
        MinecraftWfcAdapterOptions options = GenerateHouse.Companion.getDefaultOptions();


        return WfcGdmc.executors.submit(() -> {
            PlacedBlock[][][] result = generate.execute(name, area, options.copy(2, options.getSetBlocks(), options.getModelOptions(), options.getDebugOptions(), 2, stream ? new StreamOptions(WfcGdmc.overWorldBlockStream, area, PlacementStyle.ON_FINISH) : null, name));
            if (!stream && result != null) {
                GenerateHouse.Companion.stream(WfcGdmc.overWorldBlockStream, area, result, null, ConstantsKt.getBREAKABLE());
                GenerateHouse.Companion.stream(WfcGdmc.overWorldBlockStream, area, result, ConstantsKt.getBREAKABLE(), null);
            }
            return result;
        });
    }

    @Override
    public void execute(@NotNull MinecraftServer server, @NotNull ICommandSender sender, String[] args) {
        executeFuture(server, sender, args, true);
    }

    @Override
    public void init() {
        MinecraftWfcAdapterOptions options = GenerateHouse.Companion.getDefaultOptions();
        Area area = new Area(0, 0, 0, 3, 3, 3);
        for (String name : TemplateHolder.INSTANCE.getTemplates().keySet()) {
            WfcGdmc.executors.submit(() -> {
                generate.execute(name, area, options.copy(2, options.getSetBlocks(), options.getModelOptions(), options.getDebugOptions(), 0, options.getStreamOptions(), name));
            });
        }
    }
}
