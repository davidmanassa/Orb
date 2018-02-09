package com.nuno1212s.rankup.main;

import com.nuno1212s.displays.DisplayMain;
import com.nuno1212s.displays.placeholders.PlaceHolderManager;
import com.nuno1212s.main.BukkitMain;
import com.nuno1212s.main.MainData;
import com.nuno1212s.modulemanager.Module;
import com.nuno1212s.modulemanager.ModuleData;
import com.nuno1212s.playermanager.PlayerData;
import com.nuno1212s.rankup.commands.RGroupCommand;
import com.nuno1212s.rankup.economy.CoinCommand;
import com.nuno1212s.rankup.events.*;
import com.nuno1212s.rankup.mysql.MySql;
import com.nuno1212s.rankup.playermanager.RUPlayerData;
import com.nuno1212s.rankup.rankup.RankUpCommand;
import com.nuno1212s.rankup.rankup.RankUpManager;
import com.nuno1212s.util.ServerCurrencyHandler;
import lombok.Getter;
import net.sacredlabyrinth.phaed.simpleclans.Clan;
import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;
import org.bukkit.entity.Player;

import java.text.NumberFormat;


/**
 * Main Class
 */
@ModuleData(name = "RankUp", version = "1.1-SNAPSHOT", dependencies = {"Crates", "Displays", "Classes", "Boosters", "Minas"})
public class Main extends Module {

    @Getter
    static Main ins;

    @Getter
    MySql mysql;

    @Getter
    RankUpManager rankUpManager;

    @Override
    public void onEnable() {
        ins = this;
        mysql = new MySql();
        mysql.createTables();
        rankUpManager = new RankUpManager(this);

        MainData.getIns().getMessageManager().addMessageFile(getFile("messages.json", true));

        registerServerEconomy();

        PlaceHolderManager placeHolderManager = DisplayMain.getIns().getPlaceHolderManager();

        placeHolderManager.registerPlaceHolder("%coins%", (d) ->
                NumberFormat.getInstance().format(((RUPlayerData) d).getCoins())
        );

        placeHolderManager.registerPlaceHolder("%nextRank%", (d) -> {
            short nextGroup = this.rankUpManager.getNextGroup(d.getServerGroup());

            if (nextGroup == -1) {
                return "N/A";
            }

            return MainData.getIns().getPermissionManager().getGroup(nextGroup).getGroupPrefix();
        });

        placeHolderManager.registerPlaceHolder("%progress%", (d) -> {
            if (d instanceof RUPlayerData) {
                return rankUpManager.getProgression((RUPlayerData) d);
            } else {
                return "N/A";
            }
        });

        placeHolderManager.registerPlaceHolder("%clan%", (d) -> {

            ClanPlayer clanPlayer = SimpleClans.getInstance().getClanManager().getClanPlayer(d.getPlayerID());
            if (clanPlayer == null) {
                return "None";
            } else {
                return clanPlayer.getClan().getName();
            }
        });

        registerCommand(new String[]{"coins", "coin"}, new CoinCommand());
        registerCommand(new String[]{"rankup"}, new RankUpCommand());
        registerCommand(new String[]{"serverrank"}, new RGroupCommand());

        BukkitMain plugin = BukkitMain.getIns();

        plugin.getServer().getPluginManager().registerEvents(new PlayerJoinListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerUpdateListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerDisconnectListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new MCMMOExperienceListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new ForceMCMMOBreakEvent(), plugin);
    }

    @Override
    public void onDisable() {

    }

    /**
     * Set the servers economy handler
     *
     * TODO: check if the playerdata is instance of RUPlayerData safely
     */
    private void registerServerEconomy() {
        MainData.getIns().setServerCurrencyHandler(new ServerCurrencyHandler() {
            @Override
            public long getCurrencyAmount(PlayerData playerData) {
                return ((RUPlayerData) playerData).getCoins();
            }

            @Override
            public boolean removeCurrency(PlayerData playerData, long amount) {
                RUPlayerData playerData1 = (RUPlayerData) playerData;
                if (playerData1.getCoins() > amount) {
                    playerData1.setCoins(playerData1.getCoins() - amount);
                    return true;
                }
                return false;
            }

            @Override
            public void addCurrency(PlayerData playerData, long amount) {
                ((RUPlayerData) playerData).setCoins(((RUPlayerData) playerData).getCoins() + amount);
            }

            @Override
            public boolean hasCurrency(PlayerData playerData, long amount) {
                RUPlayerData playerData1 = (RUPlayerData) playerData;
                return playerData1.getCoins() > amount;
            }
        });
    }


}