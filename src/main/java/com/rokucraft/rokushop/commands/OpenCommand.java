package com.rokucraft.rokushop.commands;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.Command;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.bukkit.parsers.PlayerArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.paper.PaperCommandManager;
import com.rokucraft.rokushop.entities.Shop;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Optional;

public class OpenCommand {
    private final Command<CommandSender> command;
    private final CommandArgument<CommandSender, Shop> shopArgument;
    private final CommandArgument<CommandSender, Player> playerArgument;

    public OpenCommand(Command.Builder<CommandSender> builder, PaperCommandManager<CommandSender> manager) {
        shopArgument = manager.argumentBuilder(Shop.class, "shop").build();
        playerArgument = PlayerArgument.optional("player");
        command = builder.literal("open", ArgumentDescription.of("Open a shop"))
                .argument(shopArgument, ArgumentDescription.of("The shop to open"))
                .argument(playerArgument, ArgumentDescription.of("The player you want to show the shop to"))
                .handler(this::execute)
                .build();
    }

    private void execute(@NonNull CommandContext<CommandSender> context) {
         Shop shop = context.get(shopArgument);
         Optional<Player> target = context.getOptional(playerArgument);

    }

    public Command<CommandSender> command() {
        return command;
    }
}
