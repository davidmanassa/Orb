package com.nuno1212s.tradewindow.trades;

import com.nuno1212s.inventories.InventoryItem;
import com.nuno1212s.main.MainData;
import com.nuno1212s.playermanager.PlayerData;
import com.nuno1212s.tradewindow.TradeMain;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import sun.applet.Main;

import java.util.List;
import java.util.UUID;

public class Trade {

    @Getter
    private final UUID player1, player2;

    @Getter
    @Setter
    private boolean player1Accepted, player2Accepted;

    @Getter
    private long player1Coins, player2Coins;

    @Getter
    private Inventory tradeInventory;

    public Trade(UUID player1, UUID player2) {
        this.player1 = player1;
        this.player2 = player2;
        this.player1Coins = 0;
        this.player2Coins = 0;
        this.tradeInventory = TradeMain.getIns().getTradeInventory().buildInventory(this);
        this.player1Accepted = false;
        this.player2Accepted = false;
    }

    public void playerChangedTrade() {
        this.player2Accepted = false;
        this.player1Accepted = false;
    }

    public boolean player1ToggleAccept(UUID player) {
        if (player.equals(player1)) {
            return false;
        }

        this.player1Accepted = !this.player1Accepted;

        if (this.player1Accepted && this.player2Accepted) {
            doTrade();
        }

        return true;
    }

    public boolean player2ToggleAccept(UUID player) {
        if (player.equals(player2)) {
            return false;
        }

        this.player2Accepted = !this.player2Accepted;

        checkTrade();

        return true;
    }

    /**
     * Checks if the trade can be executed and if it can, executes it
     */
    private void checkTrade() {

        if (this.player1Accepted && this.player2Accepted) {
            doTrade();
        }

    }

    private void doTrade() {

        PlayerData player1 = MainData.getIns().getPlayerManager().getPlayer(this.player1),
                player2 = MainData.getIns().getPlayerManager().getPlayer(this.player2);

        if (player1.isPlayerOnServer() && player2.isPlayerOnServer()) {
            giveItems("PLAYER1", player2.getPlayerReference(Player.class));

            giveItems("PLAYER2", player1.getPlayerReference(Player.class));

            MainData.getIns().getServerCurrencyHandler().addCurrency(player1, this.player2Coins);

            MainData.getIns().getServerCurrencyHandler().addCurrency(player2, this.player1Coins);

        } else {

            //Players participating in trade not in the server, destroy the trade

            destroyTrade();
        }

    }

    /**
     * Is the player provided the player 1
     *
     * @param player The ID of the player to check
     * @return
     */
    public boolean isPlayer1(UUID player) {
        return player1.equals(player);
    }

    /**
     * Is the player provided the player 2
     *
     * @param player The ID of the player to check
     * @return
     */
    public boolean isPlayer2(UUID player) {
        return player2.equals(player);
    }

    /**
     * Set the coins on the trade
     *
     * @param player
     * @param coins
     */
    public void setCoins(UUID player, long coins) {
        if (isPlayer1(player)) {
            this.player1Coins = coins;
        } else {
            this.player2Coins = coins;
        }
    }

    void destroyTrade() {

        Player player1 = Bukkit.getServer().getPlayer(this.getPlayer1()), player2 = Bukkit.getServer().getPlayer(this.getPlayer2());

        try {
            if (player1.getOpenInventory() != null && player1.getOpenInventory().getTitle().equalsIgnoreCase(this.tradeInventory.getName())) {
                player1.closeInventory();
            }

            if (player2.getOpenInventory() != null && player2.getOpenInventory().getTitle().equalsIgnoreCase(this.tradeInventory.getName())) {
                player2.closeInventory();
            }
        } finally {

            //Even if there is an error with closing the inventories, this code will be executed

            /*
            Give items to the original owner
             */
            giveItems("PLAYER1", player1);

            giveItems("PLAYER2", player2);

            PlayerData pD1 = MainData.getIns().getPlayerManager().getPlayer(this.player1),
                    pD2 = MainData.getIns().getPlayerManager().getPlayer(this.player2);

            MainData.getIns().getServerCurrencyHandler().addCurrency(pD1, this.player1Coins);

            MainData.getIns().getServerCurrencyHandler().addCurrency(pD2, this.player2Coins);
        }

    }

    /**
     * Give the items in the slots with the given flag to the given playerInstance
     *
     * @param flag           The flag to look for
     * @param playerInstance The player instance
     */
    private void giveItems(String flag, Player playerInstance) {

        List<? extends InventoryItem> playerSlots = TradeMain.getIns().getTradeInventory().getItemsWithFlag(flag);

        for (InventoryItem playerSlot : playerSlots) {
            ItemStack item = this.tradeInventory.getItem(playerSlot.getSlot());

            if (item != null && item.getType() != Material.AIR) {

                playerInstance.getInventory().addItem(item);

            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Trade) {
            return this.player1.equals(((Trade) obj).getPlayer1()) && this.player2.equals(((Trade) obj).getPlayer2());
        }

        return false;
    }
}
