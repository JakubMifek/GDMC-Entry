package org.mifek.wfcgdmc.commands;

import kotlin.random.Random;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import org.jetbrains.annotations.NotNull;
import org.mifek.vgl.implementations.Blocks;
import org.mifek.vgl.implementations.PlacedBlock;
import org.mifek.vgl.palettes.PaletteKeys;
import org.mifek.vgl.village.FloorPlan;
import org.mifek.wfcgdmc.WfcGdmc;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class GenerateFloorPlanCommand extends CommandBase implements ICommand {
    private final List<String> aliases = Arrays.asList("generate_floor_plan", "gfp");

    @NotNull
    @Override
    public String getName() {
        return aliases.get(0);
    }

    @NotNull
    @Override
    public String getUsage(@NotNull ICommandSender sender) {
        return aliases.get(0) + " <x1> <y1> <z1> <w> <d>";
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
        if (args.length != 5) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Expected 5 arguments <x1> <y1> <z1> <w> <d>."));
            return;
        }

        int x1, y1, z1, w, d;
        try {
            x1 = (int) CommandBase.parseDouble(args[0]);
            y1 = (int) CommandBase.parseDouble(args[1]);
            z1 = (int) CommandBase.parseDouble(args[2]);
            w = (int) CommandBase.parseDouble(args[3]);
            d = (int) CommandBase.parseDouble(args[4]);
        } catch (NumberInvalidException exception) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Expected 5 NUMBER arguments <x1> <y1> <z1> <w> <d>."));
            return;
        }

        sender.sendMessage(new TextComponentString("Generating floor plan at [" + x1 + ", " + y1 + ", " + z1 + "] in area of dimensions " + w + "x" + 2 + "x" + d));

        WfcGdmc.executors.submit(() -> {
            PaletteKeys[][] result = FloorPlan.Companion.generate(w, d, Random.Default.nextInt());

            for (int x = 0; x < result.length; x++) {
                for (int z = 0; z < result[x].length; z++) {
                    int direction = 0;

                    if (x > 0 && x < result.length - 1) {
                        if ((result[x - 1][z] == PaletteKeys.GROUND || result[x - 1][z] == PaletteKeys.FLOOR) && result[x + 1][z] == PaletteKeys.FLOOR) {
                            direction = 1;
                        } else if (result[x - 1][z] == PaletteKeys.FLOOR && (result[x + 1][z] == PaletteKeys.GROUND || result[x + 1][z] == PaletteKeys.FLOOR)) {
                            direction = 3;
                        }
                    }

                    if (z > 0 && z < result.length - 1) {
                        if ((result[x][z - 1] == PaletteKeys.GROUND || result[x][z - 1] == PaletteKeys.FLOOR) && result[x][z + 1] == PaletteKeys.FLOOR) {
                            direction = 2;
                        } else if (result[x][z - 1] == PaletteKeys.FLOOR && (result[x][z + 1] == PaletteKeys.GROUND || result[x][z + 1] == PaletteKeys.FLOOR)) {
                            direction = 0;
                        }
                    }

                    Iterable<PlacedBlock> placedBlocks = mapToPlacedBlocks(result[x][z], x1 + x, y1, z1 + z, direction);
                    for (PlacedBlock block : placedBlocks) {
                        WfcGdmc.overWorldBlockStream.add(block);
                    }
                }
            }
            System.out.println("Passed for processing");
        });
    }

    private final HashMap<String, Object> floorMap = new HashMap<>();
    private final HashMap<String, Object> emptyMap = new HashMap<>();
    private final HashMap<String, Object> wallMap = new HashMap<>();
    private final HashMap<String, Object> bottomDoorMap = new HashMap<>();
    private final HashMap<String, Object> topDoorMap = new HashMap<>();

    private Iterable<PlacedBlock> mapToPlacedBlocks(PaletteKeys paletteKey, int x, int y1, int z, int direction) {
        switch (paletteKey) {
            case FLOOR:
                return Arrays.asList(
                        new PlacedBlock(x, y1, z, Blocks.OAK_WOOD_PLANK, floorMap),
                        new PlacedBlock(x, y1 + 1, z, Blocks.AIR, emptyMap),
                        new PlacedBlock(x, y1 + 2, z, Blocks.AIR, emptyMap)
                );
            case GROUND:
                return Arrays.asList(
                        new PlacedBlock(x, y1, z, Blocks.GRASS, emptyMap),
                        new PlacedBlock(x, y1 + 1, z, Blocks.AIR, emptyMap),
                        new PlacedBlock(x, y1 + 2, z, Blocks.AIR, emptyMap)
                );
            case WALL:
                return Arrays.asList(
                        new PlacedBlock(x, y1, z, Blocks.STONE, wallMap),
                        new PlacedBlock(x, y1 + 1, z, Blocks.STONE, wallMap),
                        new PlacedBlock(x, y1 + 2, z, Blocks.AIR, emptyMap)
                );
            case DOORS:
                HashMap<String, Object> bottomCopy = new HashMap<>(bottomDoorMap);
                HashMap<String, Object> topCopy = new HashMap<>(topDoorMap);

                String facing = direction == 0 ? "NORTH" : direction == 1 ? "EAST" : direction == 2 ? "SOUTH" : "WEST";

                bottomCopy.put("facing", facing);
                topCopy.put("facing", facing);

                return Arrays.asList(
                        new PlacedBlock(x, y1, z, Blocks.OAK_WOOD_PLANK, floorMap),
                        new PlacedBlock(x, y1 + 1, z, Blocks.OAK_DOOR, bottomCopy),
                        new PlacedBlock(x, y1 + 2, z, Blocks.OAK_DOOR, topCopy)
                );
        }

        return Collections.emptyList();
    }

    @Override
    public void init() {
        floorMap.put("variant", "DARK_OAK");
        wallMap.put("variant", "STONE");
        bottomDoorMap.put("hinge", "LEFT");
        bottomDoorMap.put("half", "LOWER");
        bottomDoorMap.put("facing", "EAST");
        topDoorMap.put("hinge", "LEFT");
        topDoorMap.put("half", "UPPER");
        topDoorMap.put("facing", "EAST");
    }
}
