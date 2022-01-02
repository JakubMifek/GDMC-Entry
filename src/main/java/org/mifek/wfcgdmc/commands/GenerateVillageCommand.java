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
import org.mifek.vgl.ConstantsKt;
import org.mifek.vgl.implementations.Area;
import org.mifek.vgl.implementations.Blocks;
import org.mifek.vgl.implementations.PlacedBlock;
import org.mifek.vgl.wfc.MinecraftVillageAdapter;
import org.mifek.vgl.wfc.MinecraftVillageAdapterOptions;
import org.mifek.wfc.datastructures.IntArray3D;
import org.mifek.wfc.models.options.Cartesian2DModelOptions;
import org.mifek.wfcgdmc.WfcGdmc;
import org.mifek.wfcgdmc.config.WfcGdmcConfig;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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

        double pathChance = WfcGdmcConfig.client.PATH_CHANCE / 100.0;

        WfcGdmc.executors.submit(() -> {
            try {
                sender.sendMessage(new TextComponentString("Generating village at [" + x + ";" + y + ";" + z + "] with dimensions [" + w + "x" + h + "x" + d + "]..."));

                HashMap<String, Triple<Float, Pair<Integer, Integer>, Pair<Integer, Integer>>> templates = new HashMap<>();
                templates.put("test4", new Triple<>(2f, new Pair<>(8, 8), new Pair<>(28, 28)));
                templates.put("silo", new Triple<>(0.5f, new Pair<>(13, 13), new Pair<>(17, 17)));
                templates.put("fountain", new Triple<>(0.5f, new Pair<>(7, 7), new Pair<>(7, 7)));
                templates.put("ter2", new Triple<>(2f, new Pair<>(10, 10), new Pair<>(24, 24)));

                MinecraftVillageAdapterOptions options = new MinecraftVillageAdapterOptions(
                        new Cartesian2DModelOptions(
                                true, true, true,
                                true, false, true, false,
                                true, false, true, false,
                                false,
                                false, false, 1.0f
                        ),
                        templates,
                        1.5f,
                        w * d / (18 * 18),
                        null
                );

                Iterable<Triple<String, Pair<Integer, Integer>, Pair<Integer, Integer>>> layout = null;
                int distance = Integer.MAX_VALUE;

                for (int i = 0; i < 30; i++) {
                    Iterable<Triple<String, Pair<Integer, Integer>, Pair<Integer, Integer>>> result = MinecraftVillageAdapter.Companion.generate(w, d, options);
                    if (result == null) continue;

                    List<?> list = StreamSupport.stream(result.spliterator(), false).collect(Collectors.toList());

                    if (Math.abs(list.size() - options.getDesiredNumberOfHouses()) < distance) {
                        distance = Math.abs(list.size() - options.getDesiredNumberOfHouses());
                        layout = result;
                    }
                }

                if (layout == null) {
                    sender.sendMessage(new TextComponentString("...finished village generation. Wasn't able to generate layout."));
                    return;
                }

                List<?> list = StreamSupport.stream(layout.spliterator(), false).collect(Collectors.toList());

                sender.sendMessage(new TextComponentString("... layout generation finished, it contains " + list.size() + " houses..."));

                ConcurrentLinkedQueue<Future<PlacedBlock[][][]>> futures = new ConcurrentLinkedQueue<>();

                for (Triple<String, Pair<Integer, Integer>, Pair<Integer, Integer>> house : layout) {
                    System.out.println("Generating house");

                    Future<?> f = WfcGdmc.executors.submit(() -> {
                        futures.add(this.generateHouseCommand.executeFuture(server, sender, new String[]{
                                house.getFirst(),
                                String.valueOf(x + house.getSecond().getFirst()),
                                String.valueOf(y),
                                String.valueOf(z + house.getSecond().getSecond()),
                                house.getThird().getFirst().toString(),
                                String.valueOf(h),
                                house.getThird().getSecond().toString()
                        }, false));
                    });

                    while (!f.isDone()) {
                        Thread.sleep(50);
                    }
                }

                Future<?> generation = WfcGdmc.executors.submit(() -> {
                    try {
                        while (!futures.stream().allMatch(it -> it.isDone() || it.isCancelled())) {
                            try {
                                System.out.println("Waiting for tasks to finish. " + futures.stream().filter(Future::isDone).count() + " done and " + futures.stream().filter(Future::isCancelled).count() + " cancelled from " + futures.size() + " tasks.");
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        List<PlacedBlock[][][]> houses = futures.stream().filter(Future::isDone).map(it -> {
                            try {
                                return it.get();
                            } catch (InterruptedException | ExecutionException e) {
                                e.printStackTrace();
                            }
                            return new PlacedBlock[0][0][0];
                        }).filter(Objects::nonNull).collect(Collectors.toList());

                        List<PlacedBlock> blocks = addPaths(houses, new Area(x, y, z, w, h, d), pathChance);
                        for (PlacedBlock block : blocks) {
                            streamBlock(block);
                        }

                        sender.sendMessage(new TextComponentString("...finished village generation."));
                    } catch (Exception | Error error) {
                        sender.sendMessage(new TextComponentString("...finished village generation with an unfortunate error " + error.getMessage()));
                        error.printStackTrace();
                    }
                });

                WfcGdmc.executors.submit(() -> {
                    while (!generation.isCancelled() && !generation.isDone()) {
                        try {
                            System.out.println("Waiting for generation to finish.");
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    sender.sendMessage(new TextComponentString("Generation done."));
                });
            } catch (Exception | Error error) {
                sender.sendMessage(new TextComponentString("...finished village generation with an unfortunate error " + error.getMessage()));
            }
        });
    }

    private List<PlacedBlock> addPaths(List<PlacedBlock[][][]> houses, Area area, double pathChance) {
        System.out.println("Layout");
        IntArray3D layout = new IntArray3D(area.getWidth(), 3, area.getDepth(), it -> 0); // 0 = dirt/air, 1 = house, 2 = doors, 3 = path

        for (PlacedBlock[][][] house : houses) {
            for (PlacedBlock[][] row : house) {
                for (int z = 0; z < row[0].length; z++) {
                    if (row[0][z].getBlock() != Blocks.DIRT && row[0][z].getBlock() != Blocks.GRASS) {
                        layout.set(row[0][z].getX() - area.getX(), 0, row[0][z].getZ() - area.getZ(), 1);

                        if (ConstantsKt.getDOORS().contains(row[1][z].getBlock())) {
                            layout.set(row[0][z].getX() - area.getX(), 1, row[0][z].getZ() - area.getZ(), 2);
                        } else if (row[1][z].getBlock() != Blocks.AIR) {
                            layout.set(row[0][z].getX() - area.getX(), 1, row[0][z].getZ() - area.getZ(), 1);
                        }

                        if (ConstantsKt.getDOORS().contains(row[2][z].getBlock())) {
                            layout.set(row[0][z].getX() - area.getX(), 2, row[0][z].getZ() - area.getZ(), 2);
                        } else if (row[2][z].getBlock() != Blocks.AIR) {
                            layout.set(row[0][z].getX() - area.getX(), 2, row[0][z].getZ() - area.getZ(), 1);
                        }
                    }
                }
            }
        }

        System.out.println("...filled.");

        List<Pair<Integer, Integer>> doors = new ArrayList<>();
        for (int x = 0; x < layout.getWidth(); x++) {
            for (int z = 0; z < layout.getDepth(); z++) {
                if (layout.get(x, 1, z) != 2) continue;

                doors.add(new Pair<>(x, z));
            }
        }

        System.out.println("Doors located. (" + doors.size() + ")");
        Random rand = new Random();

        for (int i = 0; i < doors.size() - 1; i++) {
            for (int j = i + 1; j < doors.size(); j++) {
                if (Math.pow(doors.get(i).getFirst() - doors.get(j).getFirst(), 2) + Math.pow(doors.get(i).getSecond() - doors.get(j).getSecond(), 2) > 900 || rand.nextDouble() > 0.85)
                    continue; // Skip 30% of roads

                System.out.println("Connecting doors " + i + " with " + j);
                connectDoors(doors.get(i), doors.get(j), layout, pathChance);
            }
        }

        System.out.println("Doors connected.");

        HashMap<String, Object> grass = new HashMap<>();
        grass.put("type", "tall_grass");
        HashMap<String, Object> lowerGrass = new HashMap<>();
        lowerGrass.put("variant", "double_grass");
        lowerGrass.put("half", "lower");
        HashMap<String, Object> upperGrass = new HashMap<>();
        upperGrass.put("variant", "double_grass");
        upperGrass.put("half", "upper");
        ArrayList<PlacedBlock> blocks = new ArrayList<>();
        for (int x = 0; x < layout.getWidth(); x++) {
            for (int z = 0; z < layout.getDepth(); z++) {
                if (layout.get(x, 0, z) == 3) {
                    blocks.add(new PlacedBlock(area.getX() + x, area.getY(), area.getZ() + z, Blocks.GRASS_PATH, emptyMap));
                } else if (layout.get(x, 0, z) == 0) {
                    double r = rand.nextDouble();

                    if (r <= 0.01 && layout.get(x, 0, z) == 0 && layout.get(x, 1, z) == 0 && layout.get(x, 2, z) == 0) { // Tall grass
                        blocks.add(new PlacedBlock(area.getX() + x, area.getY() + 1, area.getZ() + z, Blocks.SUNFLOWER, lowerGrass));
                        blocks.add(new PlacedBlock(area.getX() + x, area.getY() + 2, area.getZ() + z, Blocks.SUNFLOWER, upperGrass));
                    } else if (r <= 0.11 && layout.get(x, 0, z) == 0 && layout.get(x, 1, z) == 0) { // Grass
                        blocks.add(new PlacedBlock(area.getX() + x, area.getY() + 1, area.getZ() + z, Blocks.TALL_GRASS, grass));
                    }
                }
            }
        }

        System.out.println("Grass added.");

        return blocks;
    }

    private void connectDoors(Pair<Integer, Integer> door1, Pair<Integer, Integer> door2, IntArray3D layout, double pathChance) {
        int x1 = door1.getFirst();
        int z1 = door1.getSecond();

        int x2 = door2.getFirst();
        int z2 = door2.getSecond();

        if (x1 > 0 && (layout.get(x1 - 1, 0, z1) == 0 || layout.get(x1 - 1, 0, z1) == 3)) x1--;
        else if (x1 < layout.getWidth() - 1 && (layout.get(x1 + 1, 0, z1) == 0 || layout.get(x1 + 1, 0, z1) == 3)) x1++;
        else if (z1 > 0 && (layout.get(x1, 0, z1 - 1) == 0 || layout.get(x1, 0, z1 - 1) == 3)) z1--;
        else if (z1 < layout.getDepth() - 1 && (layout.get(x1, 0, z1 + 1) == 0 || layout.get(x1, 0, z1 + 1) == 3)) z1++;
        else {
            System.out.println("Skipping path placement for " + door1 + " and " + door2);
            return;
        }

        if (x2 > 0 && (layout.get(x2 - 1, 0, z2) == 0 || layout.get(x2 - 1, 0, z2) == 3)) x2--;
        else if (x2 < layout.getWidth() - 1 && (layout.get(x2 + 1, 0, z2) == 0 || layout.get(x2 + 1, 0, z2) == 3)) x2++;
        else if (z2 > 0 && (layout.get(x2, 0, z2 - 1) == 0 || layout.get(x2, 0, z2 - 1) == 3)) z2--;
        else if (z2 < layout.getDepth() - 1 && (layout.get(x2, 0, z2 + 1) == 0 || layout.get(x2, 0, z2 + 1) == 3)) z2++;
        else {
            System.out.println("Skipping path placement for " + door1 + " and " + door2);
            return;
        }

        System.out.println("Running A*");

        try {
            aStar(x1, z1, x2, z2, layout, new Random(), 1, 30, pathChance);

            for (int i : layout.getIndices()) {
                if (layout.get(i) == 4) layout.set(i, 0);
                else if (layout.get(i) == 5) layout.set(i, 3);
            }
        } catch (Throwable error) {
            error.printStackTrace();
        }
        System.out.println("A* finished");
    }

    private boolean aStar(int x, int z, int tX, int tZ, IntArray3D layout, Random r, int depth, int maxDepth, double pathChance) {
        System.out.println("Testing " + x + " " + z + " against " + tX + " " + tZ);
        int prev = layout.get(x, 0, z);
        layout.set(x, 0, z, prev == 0 ? 4 : 5);

        if (x == tX && z == tZ) {
            System.out.println("Target found");
            layout.set(x, 0, z, prev);
            if (r.nextDouble() <= pathChance) {
                layout.set(x, 0, z, 3);
            }
            if (x > 0 && (layout.get(x - 1, 0, z) == 0 || layout.get(x - 1, 0, z) == 3) && r.nextDouble() <= pathChance) {
                layout.set(x - 1, 0, z, 3);
            }
            if (x < layout.getWidth() - 1 && (layout.get(x + 1, 0, z) == 0 || layout.get(x + 1, 0, z) == 3) && r.nextDouble() <= pathChance) {
                layout.set(x + 1, 0, z, 3);
            }
            if (z > 0 && (layout.get(x, 0, z - 1) == 0 || layout.get(x, 0, z - 1) == 3) && r.nextDouble() <= pathChance) {
                layout.set(x, 0, z - 1, 3);
            }
            if (z < layout.getDepth() - 1 && (layout.get(x, 0, z + 1) == 0 || layout.get(x, 0, z + 1) == 3) && r.nextDouble() <= pathChance) {
                layout.set(x, 0, z + 1, 3);
            }

            return true;
        }

        if (depth == maxDepth) {
//            layout.set(x, 0, z, prev);
            return false;
        }

        List<Pair<Integer, Integer>> positions = new ArrayList<>(8);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (x - i + 1 >= 0 &&
                        x - i + 1 < layout.getWidth() &&
                        z - j + 1 >= 0 &&
                        z - j + 1 < layout.getDepth() &&
                        (i != 1 || j != 1) &&
                        (layout.get(x - i + 1, 0, z - j + 1) == 0 || layout.get(x - i + 1, 0, z - j + 1) == 3)
                ) {
                    positions.add(new Pair<>(x - i + 1, z - j + 1));
                }
            }
        }

        if (positions.isEmpty()) {
//            layout.set(x, 0, z, prev);
            return false;
        }

        positions.sort((a, b) -> (int) Math.round(
                (Math.pow(tX - a.getFirst(), 2) + Math.pow(tZ - a.getSecond(), 2)) -
                        (Math.pow(tX - b.getFirst(), 2) + Math.pow(tZ - b.getSecond(), 2))
        ));

//        int amnt = Math.max(1 - r.nextInt(3), 0);
//        for (int i = 0; i < amnt; i++) {
//        if (positions.size() > 1 && r.nextInt(10) == 0) {
//            int a = 0;
//            int b = 1;
//            Pair<Integer, Integer> tmp = positions.get(a);
//            positions.set(a, positions.get(b));
//            positions.set(b, tmp);


//        }


        for (Pair<Integer, Integer> position : positions) {
            if (aStar(position.getFirst(), position.getSecond(), tX, tZ, layout, r, depth + 1, maxDepth, pathChance)) {
                layout.set(x, 0, z, prev);
                if (r.nextDouble() <= pathChance) {
                    layout.set(x, 0, z, 3);
                }

                if (x > 0 && (layout.get(x - 1, 0, z) == 0 || layout.get(x - 1, 0, z) == 3) && r.nextDouble() <= pathChance) {
                    layout.set(x - 1, 0, z, 3);
                }
                if (x < layout.getWidth() - 1 && (layout.get(x + 1, 0, z) == 0 || layout.get(x + 1, 0, z) == 3) && r.nextDouble() <= pathChance) {
                    layout.set(x + 1, 0, z, 3);
                }
                if (z > 0 && (layout.get(x, 0, z - 1) == 0 || layout.get(x, 0, z - 1) == 3) && r.nextDouble() <= pathChance) {
                    layout.set(x, 0, z - 1, 3);
                }
                if (z < layout.getDepth() - 1 && (layout.get(x, 0, z + 1) == 0 || layout.get(x, 0, z + 1) == 3) && r.nextDouble() <= pathChance) {
                    layout.set(x, 0, z + 1, 3);
                }

                return true;
            }
        }

//        layout.set(x, 0, z, prev);
        return false;
    }

    private void streamBlock(PlacedBlock block) {
        WfcGdmc.overWorldBlockStream.add(block);
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
