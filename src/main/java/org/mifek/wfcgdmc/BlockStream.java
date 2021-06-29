package org.mifek.wfcgdmc;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.mifek.vgl.implementations.Blocks;
import org.mifek.vgl.implementations.PlacedBlock;
import org.mifek.vgl.interfaces.IBlockStream;
import org.mifek.wfcgdmc.config.WfcGdmcConfig;
import org.mifek.wfcgdmc.utils.BlockState;

import java.util.concurrent.ConcurrentLinkedDeque;

public class BlockStream implements IBlockStream {
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

    public void tick() {
        int M = Math.min(linkedList.size(), config.NUMBER_OF_PLACED_BLOCKS);
        for (int i = 0; i < M; i++) {
            PlacedBlock block = removeFirst();

            System.out.println("Placing");

            if (block == null || block.getBlock() == Blocks.NONE) return;

//                System.out.println("Placing block " + block.getBlock().getId());

            BlockPos bp = new BlockPos(block.getX(), block.getY(), block.getZ());

            world.setBlockState(bp, BlockState.deserialize(block));
        }
    }
}

