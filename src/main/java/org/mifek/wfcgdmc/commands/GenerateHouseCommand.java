/*
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
import org.mifek.vgl.commands.GenerateHouse;
import org.mifek.vgl.implementations.Area;
import org.mifek.vgl.implementations.Blocks;
import org.mifek.vgl.wfc.DebugOptions;
import org.mifek.wfcgdmc.WfcGdmc;

import java.util.Arrays;
import java.util.List;

public class GenerateHouseCommand extends CommandBase implements ICommand {
    private static final GenerateHouse GH = new GenerateHouse();
    private final List<String> aliases = Arrays.asList("generate_house", "gh");

    @NotNull
    @Override
    public String getName() {
        return aliases.get(0);
    }

    @NotNull
    @Override
    public String getUsage(@NotNull ICommandSender sender) {
        return aliases.get(0) + " <x> <y> <z>";
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

        int x, y, z;
        try {
            x = (int) CommandBase.parseDouble(args[0]);
            y = (int) CommandBase.parseDouble(args[1]);
            z = (int) CommandBase.parseDouble(args[2]);
        } catch (NumberInvalidException exception) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Expected 3 NUMBER arguments <x> <y> <z>."));
            return;
        }

        sender.sendMessage(new TextComponentString("Generating house at " + x + " " + y + " " + z));

        Area area = new Area(x, y, z, 16, 12, 16);

        new Thread(() -> GH.execute(area, WfcGdmc.overWorldBlockStream, new DebugOptions(Blocks.BEACON, false, null))).start();
    }

    @Override
    public void init() {

    }
}
*/
