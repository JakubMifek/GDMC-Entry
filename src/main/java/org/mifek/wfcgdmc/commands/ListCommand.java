package org.mifek.wfcgdmc.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import org.jetbrains.annotations.NotNull;
import org.mifek.vgl.utils.TemplateHolder;

import java.util.Arrays;
import java.util.List;

public class ListCommand extends CommandBase implements ICommand {
    private final List<String> aliases = Arrays.asList("list", "ls");

    @NotNull
    @Override
    public String getName() {
        return aliases.get(0);
    }

    @NotNull
    @Override
    public String getUsage(@NotNull ICommandSender sender) {
        return aliases.get(0);
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
        sender.sendMessage(new TextComponentString("Available templates:"));
        for (String key : TemplateHolder.INSTANCE.getTemplates().keySet()) {
            sender.sendMessage(new TextComponentString("  - " + key));
        }
    }

    @Override
    public void init() {
    }
}
