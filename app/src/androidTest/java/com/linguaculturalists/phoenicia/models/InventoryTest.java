package com.linguaculturalists.phoenicia.models;

import android.test.AndroidTestCase;
import android.test.mock.MockContext;

import com.linguaculturalists.phoenicia.util.PhoeniciaContext;
import com.orm.androrm.DatabaseAdapter;
import com.orm.androrm.Model;

import java.util.ArrayList;
import java.util.List;

public class InventoryTest extends AndroidTestCase {

    public void setUp() throws Exception {
        super.setUp();
        PhoeniciaContext.context = getContext();
        assertNotNull("Null context", PhoeniciaContext.context);

        List<Class<? extends Model>> models = new ArrayList<Class<? extends Model>>();
        models.add(GameSession.class);
        models.add(InventoryItem.class);

        DatabaseAdapter.setDatabaseName("game_db");
        DatabaseAdapter adapter = DatabaseAdapter.getInstance(PhoeniciaContext.context);
        adapter.setModels(models);


        GameSession session = new GameSession();
        session.session_name.set("test session");
        session.locale_pack.set("test-locale");
        session.current_level.set("test-level");
        session.save(PhoeniciaContext.context);

        assertNotNull(session.getId());
        Inventory.init(session);
    }

    public void tearDown() throws Exception {
        Inventory.getInstance().clear();
        PhoeniciaContext.context = null;
        super.tearDown();
    }

    public void testClear() throws Exception {
        Inventory.getInstance().add("foo");
        assertEquals(1, Inventory.getInstance().items().size());
        Inventory.getInstance().clear();
        assertEquals(0, Inventory.getInstance().items().size());
        assertEquals(0, Inventory.getInstance().getCount("foo"));
    }

    public void testGet() throws Exception {
        assertEquals(0, Inventory.getInstance().getCount("foo"));
        Inventory.getInstance().add("foo");
        InventoryItem item = Inventory.getInstance().get("foo");
        assertNotNull(item);
        assertEquals(1, item.quantity.get().intValue());
    }

    public void testAdd() throws Exception {
        assertEquals(0, Inventory.getInstance().getCount("foo"));
        int newCount = Inventory.getInstance().add("foo");
        assertEquals(1, newCount);
        assertEquals(1, Inventory.getInstance().getCount("foo"));
    }

    public void testSubtract() throws Exception {
        assertEquals(0, Inventory.getInstance().getCount("foo"));
        int newCount = Inventory.getInstance().add("foo");
        assertEquals(1, newCount);
        assertEquals(1, Inventory.getInstance().getCount("foo"));
        newCount = Inventory.getInstance().subtract("foo");
        assertEquals(0, newCount);
        assertEquals(0, Inventory.getInstance().getCount("foo"));
    }

    public void testGetCount() throws Exception {
        assertEquals(0, Inventory.getInstance().getCount("foo"));
        int newCount = Inventory.getInstance().add("foo");
        assertEquals(1, newCount);
        assertEquals(1, Inventory.getInstance().getCount("foo"));
    }

    public void testGetHistory() throws Exception {
        assertEquals(0, Inventory.getInstance().getHistory("foo"));
        Inventory.getInstance().add("foo");
        Inventory.getInstance().add("foo");
        Inventory.getInstance().subtract("foo");
        assertEquals(1, Inventory.getInstance().getCount("foo"));
        assertEquals(2, Inventory.getInstance().getHistory("foo"));
    }

}