package org.mifek.wfcgdmc.commands;

import com.google.common.collect.Lists;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import org.mifek.vgl.Test;

import java.util.List;

public class HelloWorldCommand extends CommandBase {
    private final List<String> aliases = Lists.newArrayList("hello_world", "hw");

    @Override
    public String getName() {
        return aliases.get(0);
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return aliases.get(0);
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
        if (args.length > 0) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "No arguments expected."));
            return;
        }

        sender.sendMessage(new TextComponentString(new Test().test()));
    }
}
