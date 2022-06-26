package com.rokucraft.rokushop.commands;

import cloud.commandframework.Command;
import com.rokucraft.rokushop.RokuShop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

public class ReloadCommand {
    private final Command<CommandSender> command;

    public ReloadCommand(Command.Builder<CommandSender> builder, RokuShop plugin) {
        command = builder.literal("reload")
                .permission("rokushop.reload")
                .handler(context -> {
                    plugin.loadShops();
                    context.getSender().sendMessage(Component.text("Shops reloaded!", NamedTextColor.GREEN));
                })
                .build();
    }

    public Command<CommandSender> command() {
        return command;
    }
}
