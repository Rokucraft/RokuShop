package com.rokucraft.rokushop.commands.parsers;

import com.rokucraft.rokushop.RokuShop;
import com.rokucraft.rokushop.entities.Shop;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;

public class ShopParser<C> implements ArgumentParser<C, Shop>, BlockingSuggestionProvider.Strings<C> {
    private final RokuShop plugin;

    public ShopParser(RokuShop plugin) {
        this.plugin = plugin;
    }

    public static <C> ParserDescriptor<C, Shop> shopParser(RokuShop plugin) {
        return ParserDescriptor.of(new ShopParser<>(plugin), Shop.class);
    }

    @Override
    public @NonNull ArgumentParseResult<@NonNull Shop> parse(
            @NonNull CommandContext<@NonNull C> commandContext,
            @NonNull CommandInput commandInput
    ) {
        final String input = commandInput.peekString();
        Shop result = plugin.shops().get(input);
        if (result == null) {
            return ArgumentParseResult.failure(new IllegalArgumentException("Unknown shop " + input));
        }
        commandInput.readString();
        return ArgumentParseResult.success(result);
    }

    @Override
    public @NonNull Iterable<@NonNull String> stringSuggestions(
            @NonNull CommandContext<C> commandContext,
            @NonNull CommandInput input
    ) {
        return plugin.shops().keySet();
    }
}
