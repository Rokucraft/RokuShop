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

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public final class RokuShop extends JavaPlugin {
    private PaperCommandManager<CommandSender> manager;
    private static Economy econ;
    private Map<String, Shop> shops;
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
        loadShopsInFile(new File(getDataFolder(), "shops"));
    }

    private void loadShopsInFile(File file) {
        if (file.isDirectory()) {
            File[] subFiles = file.listFiles();
            if (subFiles == null) {
                this.getLogger().warning("Cannot read files in directory " + file.getName());
                return;
            }
            for (File subFile : subFiles) {
                loadShopsInFile(subFile);
            }
            return;
        }
        if (file.isFile()) {
            loadShop(file);
            return;
        }
        this.getLogger().warning("Cannot load shops in " + file.getName());
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
            Map<String, Shop> shopsInFile = loader.load().get(new TypeToken<>() {});
            if (shopsInFile != null) {
                shops.putAll(shopsInFile);
            }
        } catch (ConfigurateException e) {
            this.getLogger().log(Level.SEVERE, "Unable to load configuration " + file.getName(), e);
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
