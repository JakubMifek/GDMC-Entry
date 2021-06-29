package org.mifek.wfcgdmc.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import org.mifek.vgl.commands.Test;

import java.util.Arrays;
import java.util.List;

public class    HelloWorldCommand extends CommandBase implements ICommand {
    private final List<String> aliases = Arrays.asList("hello_world", "hw");

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
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        if (args.length > 0) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "No arguments expected."));
            return;
        }

        sender.sendMessage(new TextComponentString(new Test().test()));
    }

    @Override
    public void init() {

    }
}
