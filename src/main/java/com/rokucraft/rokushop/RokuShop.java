package com.rokucraft.rokushop;

import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.paper.PaperCommandManager;
import com.rokucraft.rokushop.commands.OpenCommand;
import com.rokucraft.rokushop.commands.parsers.ShopParser;
import com.rokucraft.rokushop.entities.Shop;
import com.rokucraft.rokushop.serializers.ItemStackSerializer;
import io.leangen.geantyref.TypeToken;
import net.kyori.adventure.serializer.configurate4.ConfigurateComponentSerializer;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.util.Map;
import java.util.logging.Level;

public final class RokuShop extends JavaPlugin {
    private PaperCommandManager<CommandSender> manager;
    private Map<String, Shop> shops;
    @Override
    public void onEnable() {
        try {
            manager = PaperCommandManager.createNative(this, CommandExecutionCoordinator.simpleCoordinator());
            manager.registerBrigadier();
        } catch (Exception e) {
            this.getLogger().severe("Failed to initialize the command manager");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        manager.parserRegistry().registerParserSupplier(TypeToken.get(Shop.class), options -> new ShopParser<>(this));
        var builder = manager.commandBuilder("rokushop");
        manager.command(new OpenCommand(builder, manager).command());
        loadShops();
    }

    public void loadShops() {
        File[] shopFiles = new File(getDataFolder(), "shops").listFiles();
        if (shopFiles == null) {
            this.getLogger().log(Level.SEVERE, "Unable to load configuration");
            this.getPluginLoader().disablePlugin(this);
            return;
        }
        for (File shopFile : shopFiles) {
            loadShop(shopFile);
        }
    }

    private void loadShop(File file) {
        var loader = YamlConfigurationLoader.builder()
                .file(file)
                .defaultOptions(opts -> opts.serializers(builder ->
                                builder.register(ItemStack.class, ItemStackSerializer.INSTANCE)
                                        .registerAll(
                                        ConfigurateComponentSerializer.builder()
                                                .scalarSerializer(MiniMessage.miniMessage())
                                                .build()
                                                .serializers()
                                        )
                ))
                .build();
        try {
            shops = loader.load().get(new TypeToken<>() {});
        } catch (ConfigurateException e) {
            this.getLogger().log(Level.SEVERE, "Unable to load configuration " + file.getName(), e);
        }
    }

    public Map<String, Shop> shops() {
        return shops;
    }
}
