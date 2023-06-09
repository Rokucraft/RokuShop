package com.rokucraft.rokushop;

import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.paper.PaperCommandManager;
import com.rokucraft.rokushop.commands.OpenCommand;
import com.rokucraft.rokushop.commands.ReloadCommand;
import com.rokucraft.rokushop.commands.parsers.ShopParser;
import com.rokucraft.rokushop.entities.Shop;
import com.rokucraft.rokushop.serializers.ItemStackSerializer;
import io.leangen.geantyref.TypeToken;
import net.kyori.adventure.serializer.configurate4.ConfigurateComponentSerializer;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.incendo.interfaces.paper.PaperInterfaceListeners;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public final class RokuShop extends JavaPlugin {
    private PaperCommandManager<CommandSender> manager;
    private static Economy econ;
    private Map<String, Shop> shops;
    private static final PathMatcher YAML_MATCHER = FileSystems.getDefault().getPathMatcher("glob:*.{yml,yaml}");

    @Override
    public void onEnable() {
        setupEconomy();
        if (econ == null) {
            this.getLogger().severe("Failed to get Vault economy! Disabling...");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        try {
            manager = PaperCommandManager.createNative(this, CommandExecutionCoordinator.simpleCoordinator());
            manager.registerBrigadier();
        } catch (Exception e) {
            this.getLogger().severe("Failed to initialize the command manager! Disabling...");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        manager.parserRegistry().registerParserSupplier(TypeToken.get(Shop.class), options -> new ShopParser<>(this));
        var builder = manager.commandBuilder("rokushop");
        manager.command(new OpenCommand(builder, manager, this).command());
        manager.command(new ReloadCommand(builder, this).command());
        loadShops();
        PaperInterfaceListeners.install(this);
    }

    public void loadShops() {
        shops = new HashMap<>();
        loadShopsInFile(getDataFolder().toPath().resolve("shops"));
    }

    private void loadShopsInFile(Path path) {
        if (Files.isDirectory(path)) {
            try (var stream = Files.newDirectoryStream(
                    path,
                    p -> YAML_MATCHER.matches(p.getFileName()) || Files.isDirectory(p)
            )) {
                stream.forEach(this::loadShopsInFile);
            } catch (IOException e) {
                this.getLogger().warning("Cannot read files in directory " + path.getFileName());
            }
            return;
        }
        if (Files.isRegularFile(path)) {
            loadShop(path);
            return;
        }
        this.getLogger().warning("Cannot read files in file " + path.getFileName());
    }

    private void loadShop(Path path) {
        var loader = YamlConfigurationLoader.builder()
                .path(path)
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
            Map<String, Shop> shopsInFile = loader.load().get(new TypeToken<>() {});
            if (shopsInFile != null) {
                shops.putAll(shopsInFile);
            }
        } catch (ConfigurateException e) {
            this.getLogger().log(Level.SEVERE, "Unable to load configuration " + path.getFileName(), e);
        }
    }

    private void setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return;
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return;
        econ = rsp.getProvider();
    }

    public Map<String, Shop> shops() {
        return shops;
    }

    public Economy economy() {
        return econ;
    }
}
