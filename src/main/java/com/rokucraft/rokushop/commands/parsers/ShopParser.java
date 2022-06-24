package com.rokucraft.rokushop.commands.parsers;

import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import com.rokucraft.rokushop.RokuShop;
import com.rokucraft.rokushop.entities.Shop;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class ShopParser<C> implements ArgumentParser<C, Shop> {
    private final RokuShop plugin;

    public ShopParser(RokuShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NonNull ArgumentParseResult<@NonNull Shop> parse(
            @NonNull CommandContext<@NonNull C> commandContext,
            @NonNull Queue<@NonNull String> inputQueue
    ) {
        final String input = inputQueue.peek();
        if (input == null)
            return ArgumentParseResult.failure(new NoInputProvidedException(ShopParser.class, commandContext));

        Shop result = plugin.shops().get(input);
        if (result == null) {
            return ArgumentParseResult.failure(new IllegalArgumentException("Unknown shop " + input));
        }
        inputQueue.remove();
        return ArgumentParseResult.success(result);
    }

    @Override
    public @NonNull List<@NonNull String> suggestions(
            @NonNull CommandContext<C> commandContext,
            @NonNull String input
    ) {
        return new ArrayList<>(plugin.shops().keySet());
    }

    @Override
    public boolean isContextFree() {
        return true;
    }
}
