package com.linguaculturalists.phoenicia.models;

import android.content.Context;
import android.util.AndroidException;

import com.linguaculturalists.phoenicia.PhoeniciaGame;
import com.linguaculturalists.phoenicia.util.PhoeniciaContext;
import com.orm.androrm.Filter;

import org.andengine.util.debug.Debug;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Managing class for adding and removing items from the player's inventory.
 */
public class Inventory {
    protected static Inventory instance;
    //private PhoeniciaGame game;
    private GameSession session;
    private List<InventoryUpdateListener> listeners;

    protected Inventory(GameSession session) {
        //this.game = game;
        this.session = session;
        this.listeners = new LinkedList<InventoryUpdateListener>();
    }

    /**
     * Initialize the singleton for a given session
     *
     * @param session
     */
    public static void init(GameSession session) {
        instance = new Inventory(session);
    }

    /**
     * Returns an Singleton instance of the Inventory class.
     * You must call init(PhoeniciaGame) before calling this method
     *
     * @return
     */
    public static Inventory getInstance() {
        return instance;
    }

    /**
     * Retrieve a list of all items in the player's inventory.
     *
     * @return a list of \link InventoryItem InventoryItems \endlink with a positive quantity
     */
    public List<InventoryItem> items() {
        Filter positiveQuantity = new Filter();
        positiveQuantity.is("quantity", ">", 0);
        List<InventoryItem> items = InventoryItem.objects(PhoeniciaContext.context).filter(this.session.filter).filter(positiveQuantity).orderBy("-quantity").toList();
        return items;
    }

    /**
     * Set all InventoryItem quantities to 0
     */
    public void clear() {
        List<InventoryItem> itemsList = InventoryItem.objects(PhoeniciaContext.context).filter(this.session.filter).toList();
        InventoryItem[] items = new InventoryItem[itemsList.size()];
        items = itemsList.toArray(items);
        for (int i = 0; i < items.length; i++) {
            items[i].delete(PhoeniciaContext.context);
        }
        for (int i = 0; i < this.listeners.size(); i++) {
            Debug.d("Calling update listener: " + this.listeners.get(i).getClass());
            this.listeners.get(i).onInventoryUpdated(items);
        }
    }

    /**
     * Get an InventoryItem by name
     *
     * @param inventory_id name of the InventoryItem
     * @return an existing InventoryItem from the inventory, or a new one with 0 quantity
     */
    public InventoryItem get(String inventory_id) {
        final Filter filter = new Filter();
        filter.is("item_name", inventory_id);
        InventoryItem item;
        try {
            item = InventoryItem.objects(PhoeniciaContext.context).filter(this.session.filter).filter(filter).toList().get(0);
            if (item != null) {
                return item;
            }
        } catch (IndexOutOfBoundsException e) {
            Debug.d("No record for " + inventory_id + ", creating a new one");
        }
        item = new InventoryItem();
        item.item_name.set(inventory_id);
        item.quantity.set(0);
        item.history.set(0);
        return item;
    }

    public int add(final String inventory_id) {
        return this.add(inventory_id, 1);
    }
    /**
     * Increment the quantity of an InventoryItem (creating a new one of necessary).
     *
     * @param inventory_id name of the InventoryItem
     * @return the new quantity of this item
     */
    public int add(final String inventory_id, final int quantity) {
        Debug.d("Adding item: " + inventory_id);
        final Filter filter = new Filter();
        filter.is("item_name", inventory_id);
        InventoryItem item;
        try {
            item = InventoryItem.objects(PhoeniciaContext.context).filter(this.session.filter).filter(filter).toList().get(0);
            if (item != null) {
                Debug.d("Found record for " + inventory_id + ", updating to " + (item.quantity.get() + 1));
                item.quantity.set(item.quantity.get() + quantity);
                item.history.set(item.history.get() + quantity);
            }
        } catch (IndexOutOfBoundsException e) {
            Debug.d("No record for " + inventory_id + ", creating a new one");
            item = new InventoryItem();
            item.game.set(this.session);
            item.item_name.set(inventory_id);
            item.quantity.set(1);
            item.history.set(1);
        }
        item.save(PhoeniciaContext.context);
        this.inventoryUpdated(item);
        return item.quantity.get();
    }

    public int subtract(final String inventory_id) throws Exception {
        return this.subtract(inventory_id, 1);
    }
    /**
     * Decrement the quantity of an InventoryItem.
     *
     * Throws an exception if there are no items matching this name in the player's inventory
     * /TODO: Properly handle cases when subtracting an item isn't allowed
     * @param inventory_id name of the InventoryItem
     * @return the new quantity of this item
     */
    public int subtract(final String inventory_id, final int quantity) throws Exception {
        Debug.d("Subtracting item: " + inventory_id);
        final Filter filter = new Filter();
        filter.is("item_name", inventory_id);
        try {
            InventoryItem item = InventoryItem.objects(PhoeniciaContext.context).filter(this.session.filter).filter(filter).toList().get(0);
            if (item != null) {
                final int count = item.quantity.get() - quantity;
                item.quantity.set(count);
                item.save(PhoeniciaContext.context);
                this.inventoryUpdated(item);
                return count;
            }
        } catch (IndexOutOfBoundsException e) {
            throw new Exception("Can not subtract item "+inventory_id+" from inventory because it has none");
        }
        return 0;
    }

    /**
     * Get the current quantity of InventoryItems of this name
     * @param inventory_id name of the InventoryItem
     * @return current quantity of this item
     */
    public int getCount(String inventory_id) {
        final Filter filter = new Filter();
        filter.is("item_name", inventory_id);
        List<InventoryItem> items = InventoryItem.objects(PhoeniciaContext.context).filter(this.session.filter).filter(filter).toList();
        if (items.size() > 0) {
            return items.get(0).quantity.get();
        } else {
            Debug.d("No record of inventory item: "+inventory_id);
            return 0;
        }
    }

    /**
     * Get the cumulative number of items of this name that were ever added to the inventory
     * @param inventory_id name of the InventoryItem
     * @return cumulative number of this item added to the inventory
     */
    public int getHistory(String inventory_id) {
        final Filter filter = new Filter();
        filter.is("item_name", inventory_id);
        List<InventoryItem> items = InventoryItem.objects(PhoeniciaContext.context).filter(this.session.filter).filter(filter).toList();
        if (items.size() > 0) {
            return items.get(0).history.get();
        } else {
            Debug.d("No record of inventory item: "+inventory_id);
            return 0;
        }
    }

    private void inventoryUpdated(InventoryItem item) {
        Debug.d("Inventory updated for: " + item.item_name.get());
        InventoryItem[] items = {item};
        for (int i = 0; i < this.listeners.size(); i++) {
            Debug.d("Calling update listener: "+this.listeners.get(i).getClass());
            this.listeners.get(i).onInventoryUpdated(items);
        }
    }
    public void addUpdateListener(InventoryUpdateListener listener) {
        this.listeners.remove(listener);
        this.listeners.add(listener);
    }
    public void removeUpdateListener(InventoryUpdateListener listener) {
        this.listeners.remove(listener);
    }

    /**
     * Callback listener for changes to item quantities in the inventory.
     */
    public interface InventoryUpdateListener {
        void onInventoryUpdated(final InventoryItem[] item);
    }

}
