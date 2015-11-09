package com.linguaculturalists.phoenicia.models;

import android.content.Context;
import android.util.AndroidException;

import com.linguaculturalists.phoenicia.PhoeniciaGame;
import com.orm.androrm.Filter;

import org.andengine.util.debug.Debug;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by mhall on 8/15/15.
 */
public class Inventory {
    private static Inventory instance;
    private PhoeniciaGame game;
    private List<InventoryUpdateListener> listeners;

    private Inventory(PhoeniciaGame game) {
        this.game = game;
        this.listeners = new LinkedList<InventoryUpdateListener>();
    }

    public static void init(PhoeniciaGame game) {
        instance = new Inventory(game);
    }
    
    public static Inventory getInstance() {
        return instance;
    }

    public void clear() {
        List<InventoryItem> itemsList = InventoryItem.objects(this.game.activity).filter(this.game.sessionFilter).toList();
        InventoryItem[] items = new InventoryItem[itemsList.size()];
        items = itemsList.toArray(items);
        for (int i = 0; i < items.length; i++) {
            items[i].quantity.set(0);
        }
        for (int i = 0; i < this.listeners.size(); i++) {
            Debug.d("Calling update listener: "+this.listeners.get(i).getClass());
            this.listeners.get(i).onInventoryUpdated(items);
        }
    }

    public InventoryItem get(String inventory_id) {
        final Filter filter = new Filter();
        filter.is("item_name", inventory_id);
        InventoryItem item;
        try {
            item = InventoryItem.objects(this.game.activity).filter(this.game.sessionFilter).filter(filter).toList().get(0);
            if (item != null) {
                return item;
            }
        } catch (IndexOutOfBoundsException e) {
            Debug.d("No record for "+inventory_id+", creating a new one");
        }
        item = new InventoryItem();
        item.item_name.set(inventory_id);
        item.quantity.set(0);
        item.history.set(0);
        return item;
    }

    public int add(final String inventory_id) {
        Debug.d("Adding item: " + inventory_id);
        final Filter filter = new Filter();
        filter.is("item_name", inventory_id);
        InventoryItem item;
        try {
            item = InventoryItem.objects(this.game.activity).filter(this.game.sessionFilter).filter(filter).toList().get(0);
            if (item != null) {
                Debug.d("Found record for " + inventory_id + ", updating to " + (item.quantity.get() + 1));
                item.quantity.set(item.quantity.get() + 1);
                item.history.set(item.history.get() + 1);
            }
        } catch (IndexOutOfBoundsException e) {
            Debug.d("No record for "+inventory_id+", creating a new one");
            item = new InventoryItem();
            item.game.set(this.game.session);
            item.item_name.set(inventory_id);
            item.quantity.set(1);
            item.history.set(1);
        }
        item.save(this.game.activity);
        this.inventoryUpdated(item);
        return item.quantity.get();
    }

    public int subtract(final String inventory_id) throws Exception {
        Debug.d("Subtracting item: " + inventory_id);
        final Filter filter = new Filter();
        filter.is("item_name", inventory_id);
        try {
            InventoryItem item = InventoryItem.objects(this.game.activity).filter(this.game.sessionFilter).filter(filter).toList().get(0);
            if (item != null) {
                final int count = item.quantity.get() - 1;
                item.quantity.set(count);
                item.save(this.game.activity);
                this.inventoryUpdated(item);
                return count;
            }
        } catch (IndexOutOfBoundsException e) {
            throw new Exception("Can not subtract item "+inventory_id+" from inventory because it has none");
        }
        return 0;
    }

    public int getCount(String inventory_id) {
        final Filter filter = new Filter();
        filter.is("item_name", inventory_id);
        List<InventoryItem> items = InventoryItem.objects(this.game.activity).filter(this.game.sessionFilter).filter(filter).toList();
        if (items.size() > 0) {
            return items.get(0).quantity.get();
        } else {
            Debug.d("No record of inventory item: "+inventory_id);
            return 0;
        }
    }

    public int getHistory(String inventory_id) {
        final Filter filter = new Filter();
        filter.is("item_name", inventory_id);
        List<InventoryItem> items = InventoryItem.objects(this.game.activity).filter(this.game.sessionFilter).filter(filter).toList();
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
        this.listeners.add(listener);
    }
    public void removeUpdateListener(InventoryUpdateListener listener) {
        this.listeners.remove(listener);
    }

    public interface InventoryUpdateListener {
        public void onInventoryUpdated(final InventoryItem[] item);
    }

}
