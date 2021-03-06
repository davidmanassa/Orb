package com.nuno1212s.crates;

import com.nuno1212s.crates.commands.CrateCommandManager;
import com.nuno1212s.crates.commands.KeysCommand;
import com.nuno1212s.crates.crates.CrateManager;
import com.nuno1212s.crates.events.*;
import com.nuno1212s.main.BukkitMain;
import com.nuno1212s.main.MainData;
import com.nuno1212s.modulemanager.Module;
import com.nuno1212s.modulemanager.ModuleData;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/**
 * Main Module
 */
@ModuleData(name = "Crates", version = "1.0-BETA", dependencies = {})
@Getter
public class Main extends Module {

    @Getter
    static Main ins;

    CrateManager crateManager;

    @Override
    public void onEnable() {
        ins = this;
        crateManager = new CrateManager(this);


        registerCommand(new String[]{"crate", "crates"}, new CrateCommandManager());
        registerCommand(new String[]{"addcratekeys"}, new KeysCommand());

        MainData.getIns().getMessageManager().addMessageFile(getFile("messages.json", true));

        Plugin plugin = BukkitMain.getIns();
        Bukkit.getServer().getPluginManager().registerEvents(new PlayerCloseInventoryListener(), plugin);
        Bukkit.getServer().getPluginManager().registerEvents(new InventoryClickEventListener(), plugin);
        Bukkit.getServer().getPluginManager().registerEvents(new PlayerInteractListener(), plugin);
        Bukkit.getServer().getPluginManager().registerEvents(new PlayerBreakBlockListener(), plugin);
        Bukkit.getServer().getPluginManager().registerEvents(new PlayerChangeItemNameListener(), plugin);
        Bukkit.getServer().getPluginManager().registerEvents(new CrateDisplayClickListener(), plugin);
    }

    @Override
    public void onDisable() {
        crateManager.save();
    }
}
