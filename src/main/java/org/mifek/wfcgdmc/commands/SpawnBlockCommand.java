package org.mifek.wfcgdmc.commands;

import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.mifek.vgl.SpawnDirtBlock;
import org.mifek.vgl.implementations.Area;
import org.mifek.vgl.implementations.Blocks;

import java.util.List;

public class SpawnBlockCommand extends CommandBase {
    private final List<String> aliases = Lists.newArrayList("spawn_block", "sb");

    @Override
    public String getName() {
        return aliases.get(0);
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return aliases.get(0) + " <x> <y> <z>";
    }

    @Override
    public List<String> getAliases() {
        return aliases;
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) {
            Minecraft instance = Minecraft.getMinecraft();
            if (instance.objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK) {
                BlockPos blockVector = instance.objectMouseOver.getBlockPos();
                double bX = blockVector.getX();
                double bY = blockVector.getY();
                double bZ = blockVector.getZ();

                args = new String[]{String.valueOf(bX), String.valueOf(bY), String.valueOf(bZ)};
            }
        }

        if (args.length != 3) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Expected 3 number arguments <x> <y> <z>."));
            return;
        }

        int x;
        int y;
        int z;
        try {
            x = (int) CommandBase.parseDouble(args[0]);
            y = (int) CommandBase.parseDouble(args[1]);
            z = (int) CommandBase.parseDouble(args[2]);
        } catch (NumberInvalidException exception) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Expected 3 NUMBER arguments <x> <y> <z>."));
            return;
        }

        sender.sendMessage(new TextComponentString("Spawning block at " + x + " " + y + " " + z));

        Area area = new Area((int) x, (int) y, (int) z, 100, 100, 100);
        World world = sender.getEntityWorld();
        SpawnDirtBlock SB = new SpawnDirtBlock();
        String[][][] result = SB.execute(area);
        for (int X = 0; X < area.getWidth(); X++)
            for (int Y = 0; Y < area.getHeight(); Y++)
                for (int Z = 0; Z < area.getDepth(); Z++) {
                    if (result[X][Y][Z].equals(Blocks.NONE.getId())) continue;

                    world.setBlockState(new BlockPos(x + X, y + Y, z + Z), Block.getBlockFromName(result[X][Y][Z]).getDefaultState());
                }
    }

}
