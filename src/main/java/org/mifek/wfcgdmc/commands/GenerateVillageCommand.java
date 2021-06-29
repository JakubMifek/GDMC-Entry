package org.mifek.wfcgdmc.commands;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
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
import org.mifek.vgl.implementations.Area;
import org.mifek.vgl.implementations.PlacementStyle;
import org.mifek.vgl.wfc.MinecraftVillageAdapter;
import org.mifek.vgl.wfc.MinecraftVillageAdapterOptions;
import org.mifek.vgl.wfc.MinecraftWfcAdapterOptions;
import org.mifek.vgl.wfc.StreamOptions;
import org.mifek.wfc.models.options.Cartesian2DModelOptions;
import org.mifek.wfc.models.options.Cartesian3DModelOptions;
import org.mifek.wfcgdmc.WfcGdmc;

import java.util.*;

public class GenerateVillageCommand extends CommandBase implements ICommand {
    private final List<String> aliases = Arrays.asList("generate_village", "gv");
    private final HashMap<String, Object> emptyMap = new HashMap<>();
    private GenerateHouseCommand generateHouseCommand;

    @NotNull
    @Override
    public String getName() {
        return aliases.get(0);
    }

    @NotNull
    @Override
    public String getUsage(@NotNull ICommandSender sender) {
        return aliases.get(0) + "<x> <y> <z> <w> <h> <d>";
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
        if (args.length < 6) {
            Minecraft instance = Minecraft.getMinecraft();
            if (instance.objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK) {
                BlockPos blockVector = instance.objectMouseOver.getBlockPos();
                double bX = blockVector.getX();
                double bY = blockVector.getY();
                double bZ = blockVector.getZ();

                args = new String[]{String.valueOf(bX), String.valueOf(bY), String.valueOf(bZ), args[0], args[1], args[2]};
            }
        }

        if (args.length != 6) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Expected 6 arguments <x> <y> <z> <w> <h> <d>."));
            return;
        }

        int x, y, z, w, h, d;
        try {
            x = (int) CommandBase.parseDouble(args[0]);
            y = (int) CommandBase.parseDouble(args[1]);
            z = (int) CommandBase.parseDouble(args[2]);
            w = (int) CommandBase.parseDouble(args[3]);
            h = (int) CommandBase.parseDouble(args[4]);
            d = (int) CommandBase.parseDouble(args[5]);
        } catch (NumberInvalidException exception) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Expected 6 NUMBER arguments <x> <y> <z> <w> <h> <d>."));
            return;
        }

        WfcGdmc.executors.submit(() -> {
            try {
                sender.sendMessage(new TextComponentString("Generating village at [" + x + ";" + y + ";" + z + "] with dimensions [" + w + "x" + h + "x" + d + "]..."));

                HashMap<String, Pair<Float, Triple<Integer, Integer, Integer>>> templates = new HashMap<>();
                templates.put("test3", new Pair<>(1f, new Triple<>(5, 10, 5)));
                templates.put("silo", new Pair<>(1f, new Triple<>(13, 10, 13)));
                templates.put("fountain", new Pair<>(1f, new Triple<>(5, 10, 5)));
                templates.put("ter2", new Pair<>(1f, new Triple<>(4, 10, 6)));

                MinecraftVillageAdapterOptions options = new MinecraftVillageAdapterOptions(
                        new Cartesian2DModelOptions(
                                true, true, true,
                                true, false, true, false,
                                true, false, true, false,
                                false,
                                false, false, 1.0f
                        ),
                        templates,
                        57f,
                        w * d / (12 * 12),
                        null
                );

                Iterable<Triple<String, Pair<Integer, Integer>, Pair<Integer, Integer>>> layout = null;
                int distance = Integer.MAX_VALUE;

                for (int i = 0; i < 30; i++) {
                    Iterable<Triple<String, Pair<Integer, Integer>, Pair<Integer, Integer>>> result = MinecraftVillageAdapter.Companion.generate(w, d, options);
                    if (result == null) continue;

                    List<?> list = Lists.newArrayList(result);

                    if (Math.abs(list.size() - options.getDesiredNumberOfHouses()) < distance) {
                        distance = list.size();
                        layout = result;
                    }
                }

                if (layout == null) {
                    sender.sendMessage(new TextComponentString("...finished village generation. Wasn't able to generate layout."));
                    return;
                }

                Optional<Integer> minX = templates.keySet().stream().map(key ->
                        templates.getOrDefault(key, new Pair<>(1.0f, new Triple<>(5, 5, 5))).getSecond().getFirst()).reduce(Math::min);
//                Optional<Integer> minY = templates.keySet().stream().map(key ->
//                        templates.getOrDefault(key, new Pair<>(1.0f, new Triple<>(5, 5, 5))).getSecond().getFirst()).reduce(Math::min);
                Optional<Integer> minZ = templates.keySet().stream().map(key ->
                        templates.getOrDefault(key, new Pair<>(1.0f, new Triple<>(5, 5, 5))).getSecond().getFirst()).reduce(Math::min);

                for (Triple<String, Pair<Integer, Integer>, Pair<Integer, Integer>> house : layout) {
                    if (
                            house.getThird().getFirst() < minX.orElse(5) ||
                                    house.getThird().getFirst() > 30 ||
                                    house.getThird().getSecond() < minZ.orElse(5) ||
                                    house.getThird().getSecond() > 30
                    ) {
                        System.out.println("Skipping house with dimensions [" + house.getThird().getFirst().toString() + " x " + house.getThird().getSecond().toString() + "]");
                        continue;
                    }
                    System.out.println("Generating house");

                    WfcGdmc.executors.submit(() -> {
                        this.generateHouseCommand.execute(server, sender, new String[]{
                                house.getFirst(),
                                String.valueOf(x + house.getSecond().getFirst()),
                                String.valueOf(y),
                                String.valueOf(z + house.getSecond().getSecond()),
                                house.getThird().getFirst().toString(),
                                String.valueOf(h),
                                house.getThird().getSecond().toString()
                        });
                    });
                }

                System.out.println("finished");

//                generateHouse.execute(name, area, new MinecraftWfcAdapterOptions(
//                        2, null,
//                        new Cartesian3DModelOptions(
//                                false, true, false,
//                                true, false, true,
//                                Collections.emptySet(),
//                                Collections.emptySet(),
//                                false, false, 1. / 3.
//                        ),
//                        null,
//                        1,
//                        new StreamOptions(
//                                WfcGdmc.overWorldBlockStream, area, PlacementStyle.ON_COLLAPSE
//                        ),
//                        name
//                ));
//                sender.sendMessage(new TextComponentString("...finished " + name));
            } catch (Error error) {
                sender.sendMessage(new TextComponentString("...finished village generation with an unfortunate error " + error.getMessage()));
            }
        });

    }

    @Override
    public void init() {
        for (ICommand command : WfcGdmc.commands) {
            if (command.getName().equals("generate_house")) {
                this.generateHouseCommand = (GenerateHouseCommand) command;
                return;
            }
        }
    }
}
