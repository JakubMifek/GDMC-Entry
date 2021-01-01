package org.mifek.wfcgdmc;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.mifek.vgl.implementations.PlacedBlock;
import org.mifek.vgl.interfaces.IBlockStream;
import org.mifek.wfcgdmc.config.WfcGdmcConfig;

import java.util.concurrent.ConcurrentLinkedDeque;

public class BlockStream extends Thread implements IBlockStream, Runnable {
    private final ConcurrentLinkedDeque<PlacedBlock> linkedList = new ConcurrentLinkedDeque<>();
    private final World world;
    private final WfcGdmcConfig.Client config;

    BlockStream(World world) {
        this.world = world;
        config = WfcGdmcConfig.client;
    }

    @Override
    public void add(@NotNull PlacedBlock placedBlock) {
        linkedList.add(placedBlock);
    }

    private boolean isEmpty() {
        return linkedList.isEmpty();
    }

    private PlacedBlock removeFirst() {
        return linkedList.removeFirst();
    }

    private void setState(Block b, BlockPos bp, PlacedBlock block) {
        IBlockState state = b.getDefaultState();

        for (IProperty<?> prop : state.getPropertyKeys()) {
            if (!block.getProps().containsKey(prop.getName())) continue;

            if (prop instanceof PropertyEnum) state = BlockStream.setEnumProperty(state, (PropertyEnum) prop, block);
        }

        world.setBlockState(bp, state);
    }

    @Override
    public void run() {
        long sleepTime;
        while (true) try {
            if (!isEmpty()) {
                for (int i = 0; i < Math.min(linkedList.size(), config.NUMBER_OF_PLACED_BLOCKS); i++) {
                    PlacedBlock block = removeFirst();
                    if (block == null || block.getBlock().getId().length() == 0) continue;

                    System.out.println("Placing block " + block.getBlock().getId());

                    BlockPos bp = new BlockPos(block.getX(), block.getY(), block.getZ());
                    Block b = Block.getBlockFromName(block.getBlock().getId());

                    setState(b, bp, block);
                }
                sleepTime = config.PLACING_DELAY;
            } else sleepTime = config.WAIT_DELAY;

            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @NotNull
    private static <T extends Enum<T> & IStringSerializable> IBlockState setEnumProperty(IBlockState state, IProperty<T> prop, PlacedBlock block) throws Error {
        PropertyEnum<T> property = (PropertyEnum<T>) prop;
        for (T value : property.getAllowedValues())
            if (value.getName().toLowerCase().equals(block.getProps().get(property.getName())))
                return state.withProperty(property, value);

        throw new Error("Given property was not an EnumProperty. " + prop.getName() + " " + prop.getValueClass().getName() + " " + prop.getClass().getName());
    }
}

