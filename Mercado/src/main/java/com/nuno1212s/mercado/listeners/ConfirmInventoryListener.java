package com.nuno1212s.mercado.listeners;

import com.nuno1212s.main.MainData;
import com.nuno1212s.mercado.main.Main;
import com.nuno1212s.mercado.marketmanager.Item;
import com.nuno1212s.mercado.marketmanager.MarketManager;
import com.nuno1212s.mercado.util.InventoryListener;
import com.nuno1212s.playermanager.PlayerData;
import com.nuno1212s.util.NBTDataStorage.NBTCompound;
import com.nuno1212s.inventories.InventoryItem;
import com.nuno1212s.util.Pair;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Handles confirm inventorylisteners clicks
 */
public class ConfirmInventoryListener extends InventoryListener {

    public ConfirmInventoryListener() {
        super(Main.getIns().getMarketManager().getConfirmInventoryData());
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        MarketManager marketManager = Main.getIns().getMarketManager();
        if (marketManager.getConfirmInventoryData().equals(e.getInventory())) {
            if (e.getClick().isShiftClick()) {
                e.setResult(Event.Result.DENY);
            }
        } else {
            return;
        }

        if (e.getClickedInventory() == null) {
            return;
        }
        if (e.getInventory().getName().equals(e.getClickedInventory().getName())) {
            if (e.getCurrentItem() == null) {
                return;
            }

            e.setResult(Event.Result.DENY);

            InventoryItem item = marketManager.getConfirmInventoryData().getItem(e.getSlot());

            if (item == null) {
                return;
            }

            if (item.hasItemFlag("CONFIRM")) {
                InventoryItem show_item = marketManager.getConfirmInventoryData().getItemWithFlag("SHOW_ITEM");
                ItemStack item1 = e.getInventory().getItem(show_item.getSlot());
                NBTCompound itemData = new NBTCompound(item1);
                String itemID = (String) itemData.getValues().get("ItemID");
                Item buyItem = marketManager.getItem(itemID);

                if (buyItem == null) {
                    addCloseException(e.getWhoClicked().getUniqueId());
                    MainData.getIns().getMessageManager().getMessage("ERROR_NO_ITEM").sendTo(e.getWhoClicked());
                    e.getWhoClicked().closeInventory();
                    marketManager.openInventory((Player) e.getWhoClicked(), marketManager.getPage(e.getWhoClicked().getUniqueId()));
                    return;
                }

                if (buyItem.isSold()) {
                    MainData.getIns().getMessageManager().getMessage("ITEM_SOLD_ALREADY").sendTo(e.getWhoClicked());
                    return;
                }

                if (buyItem.isServerCurrency()) {
                    if (!MainData.getIns().hasServerCurrency()) {
                        MainData.getIns().getMessageManager().getMessage("NO_SERVER_CURRENCY").sendTo(e.getWhoClicked());
                        return;
                    }

                    PlayerData playerData = MainData.getIns().getPlayerManager().getPlayer(e.getWhoClicked().getUniqueId());

                    MainData.getIns().getServerCurrencyHandler().removeCurrency(playerData, buyItem.getCost())
                            .thenAccept((completed) -> {

                                if (completed) {
                                    MainData.getIns().getEventCaller().callUpdateInformationEvent(playerData);
                                    buyItem.deliverItem((Player) e.getWhoClicked());
                                    addCloseException(e.getWhoClicked().getUniqueId());
                                    e.getWhoClicked().closeInventory();
                                    Main.getIns().getMarketManager().openInventory((Player) e.getWhoClicked(), getPageForPlayer(e.getWhoClicked().getUniqueId()));
                                } else {
                                    MainData.getIns().getMessageManager().getMessage("NO_SERVER_CURRENCY").sendTo(e.getWhoClicked());
                                }

                            });

                } else {
                    PlayerData playerData = MainData.getIns().getPlayerManager().getPlayer(e.getWhoClicked().getUniqueId());

                    if (playerData.removeCash(buyItem.getCost())) {
                        buyItem.deliverItem((Player) e.getWhoClicked());

                        addCloseException(e.getWhoClicked().getUniqueId());

                        e.getWhoClicked().closeInventory();
                        Main.getIns().getMarketManager().openInventory((Player) e.getWhoClicked(), getPageForPlayer(e.getWhoClicked().getUniqueId()));

                    } else {
                        MainData.getIns().getMessageManager().getMessage("NO_CASH").sendTo(e.getWhoClicked());
                    }
                }

            } else if (item.hasItemFlag("CANCEL")) {
                int page = marketManager.getPage(e.getWhoClicked().getUniqueId());

                if (page == -1) {
                    e.getWhoClicked().closeInventory();
                    return;
                }

                addCloseException(e.getWhoClicked().getUniqueId());
                e.getWhoClicked().closeInventory();
                marketManager.openInventory((Player) e.getWhoClicked(), page);
            }
        }
    }

}
