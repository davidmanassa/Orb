package com.nuno1212s.boosters.listeners;

import com.nuno1212s.boosters.boosters.Booster;
import com.nuno1212s.boosters.main.Main;
import com.nuno1212s.main.MainData;
import com.nuno1212s.inventories.InventoryData;
import com.nuno1212s.inventories.InventoryItem;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Inventory listener
 */
public class OwnBoostersInventoryListener implements Listener {

    private List<UUID> onClose = new ArrayList<>();

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (Main.getIns().getInventoryManager().getMyBoostersInventory().equals(e.getInventory())) {
            e.setResult(Event.Result.DENY);
        }
    }

    @EventHandler
    public void onInventory(InventoryClickEvent e) {
        if (Main.getIns().getInventoryManager().getMyBoostersInventory().equals(e.getInventory())) {

            if (e.getClick().isShiftClick()) {
                e.setResult(Event.Result.DENY);
            }

        } else {
            return;
        }

        if (e.getClickedInventory() == null) {
            return;
        }

        if (Main.getIns().getInventoryManager().getMyBoostersInventory().equals(e.getClickedInventory())) {
            if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) {
                return;
            }

            e.setResult(Event.Result.DENY);

            InventoryData mainInventory = Main.getIns().getInventoryManager().getMyBoostersInventory();

            InventoryItem item = mainInventory.getItem(e.getSlot());

            if (item == null) {
                Booster b = Main.getIns().getInventoryManager().getBoosterConnectedToItem(e.getCurrentItem());
                if (b == null) {
                    return;
                }

                if (b.isActivated()) {
                    return;
                }

                if (!Main.getIns().getBoosterManager().canActivateBooster(e.getWhoClicked().getUniqueId())) {
                    MainData.getIns().getMessageManager().getMessage("BOOSTER_ACTIVE").sendTo(e.getWhoClicked());
                    return;
                }

                onClose.add(e.getWhoClicked().getUniqueId());
                e.getWhoClicked().closeInventory();
                e.getWhoClicked().openInventory(Main.getIns().getInventoryManager().buildConfirmInventory(b));

            } else {
                if (item.hasItemFlag("NEXT_PAGE")) {
                    UUID playerID = e.getWhoClicked().getUniqueId();
                    int page = Main.getIns().getInventoryManager().getPage(playerID);

                    Inventory inv = Main.getIns().getInventoryManager().buildInventoryForPlayer(playerID, page + 1);

                    e.getClickedInventory().setContents(inv.getContents());

                    Main.getIns().getInventoryManager().setPage(playerID, page + 1);
                } else if (item.hasItemFlag("PREVIOUS_PAGE")) {
                    UUID playerID = e.getWhoClicked().getUniqueId();
                    int page = Main.getIns().getInventoryManager().getPage(playerID);

                    //Check if the current page is > 1 (The first page is 1)
                    if (page > 1) {
                        Inventory inv = Main.getIns().getInventoryManager().buildInventoryForPlayer(playerID, page - 1);
                        e.getClickedInventory().setContents(inv.getContents());
                        Main.getIns().getInventoryManager().setPage(playerID, page - 1);
                    } else {
                        e.getWhoClicked().closeInventory();
                        e.getWhoClicked().openInventory(Main.getIns().getInventoryManager().buildLandingInventory());
                    }
                }
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (Main.getIns().getInventoryManager().getMyBoostersInventory().equals(e.getInventory()) && !onClose.contains(e.getPlayer().getUniqueId())) {
            Main.getIns().getInventoryManager().removePage(e.getPlayer().getUniqueId());
        } else {
            onClose.remove(e.getPlayer().getUniqueId());
        }
    }

}
