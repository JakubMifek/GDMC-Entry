package org.mifek.wfcgdmc.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.mifek.vgl.commands.SaveTemplate;
import org.mifek.vgl.implementations.Block;
import org.mifek.wfcgdmc.utils.BlockState;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class SaveTemplateCommand extends CommandBase implements ICommand {
    private static final SaveTemplate command = new SaveTemplate();

    private final List<String> aliases = Arrays.asList("save_template", "st");

    @Override
    public String getName() {
        return aliases.get(0);
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return aliases.get(0) + " <x1> <y1> <z1> <x2> <y2> <z2> <name?>";
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
        if (args.length != 6 && args.length != 7) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Expected 6 number arguments <x1> <y1> <z1> <x2> <y2> <z2> and one optional text argument <name>."));
            return;
        }

        int x1, y1, z1, x2, y2, z2, tmp;
        try {
            x1 = (int) CommandBase.parseDouble(args[0]);
            y1 = (int) CommandBase.parseDouble(args[1]);
            z1 = (int) CommandBase.parseDouble(args[2]);
            x2 = (int) CommandBase.parseDouble(args[3]);
            y2 = (int) CommandBase.parseDouble(args[4]);
            z2 = (int) CommandBase.parseDouble(args[5]);
        } catch (NumberInvalidException exception) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Expected 6 NUMBER arguments <x1> <y1> <z1> <x2> <y2> <z2>."));
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

        String name = (args.length == 7 ? args[6] : "template_" + new Date().getTime()) + ".tmpl";

        sender.sendMessage(new TextComponentString(
                "Saving template " + name + " between [" + x1 + ", " + y1 + ", " + z1 + "] and [" + x2 + ", " + y2 + ", " + z2 + "]")
        );

        World world = server.getEntityWorld();

        Block[][][] blocks = new Block[x2 - x1 + 1][y2 - y1 + 1][z2 - z1 + 1];
        for (int z = 0; z < blocks[0][0].length; z++)
            for (int y = 0; y < blocks[0].length; y++)
                for (int x = 0; x < blocks.length; x++)
                    blocks[x][y][z] = BlockState.serialize(world.getBlockState(new BlockPos(x1 + x, y1 + y, z1 + z)));

        command.execute(blocks, name);
    }

    @Override
    public void init() {

    }
}
