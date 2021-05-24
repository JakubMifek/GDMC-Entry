package org.mifek.wfcgdmc.commands;

import com.google.common.collect.Lists;
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
import org.mifek.vgl.commands.SpawnHouse;
import org.mifek.vgl.implementations.Area;
import org.mifek.wfcgdmc.WfcGdmc;

import java.util.List;

public class SpawnHouseCommand extends CommandBase {
    private final List<String> aliases = Lists.newArrayList("spawn_house", "sh");

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

        sender.sendMessage(new TextComponentString("Spawning house at " + x + " " + y + " " + z));

        Area area = new Area(x, y, z, 100, 100, 100);
        SpawnHouse SH = new SpawnHouse();
        SH.execute(area, WfcGdmc.overWorldBlockStream);
    }
}
