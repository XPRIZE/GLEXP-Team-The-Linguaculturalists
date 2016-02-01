package com.linguaculturalists.phoenicia.models;

import android.test.AndroidTestCase;

import com.linguaculturalists.phoenicia.util.PhoeniciaContext;
import com.orm.androrm.DatabaseAdapter;
import com.orm.androrm.Model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mhall on 1/28/16.
 */
public class BankTest extends AndroidTestCase {

    private GameSession session;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        PhoeniciaContext.context = getContext();
        assertNotNull("Null context", PhoeniciaContext.context);

        List<Class<? extends Model>> models = new ArrayList<Class<? extends Model>>();
        models.add(GameSession.class);

        DatabaseAdapter.setDatabaseName("game_db");
        DatabaseAdapter adapter = DatabaseAdapter.getInstance(PhoeniciaContext.context);
        adapter.setModels(models);

        this.session = new GameSession();
        session.session_name.set("test session");
        session.locale_pack.set("test-locale");
        session.current_level.set("test-level");
        session.save(PhoeniciaContext.context);

        assertNotNull(session.getId());
        Bank.init(session);

    }

    @Override
    protected void tearDown() throws Exception {
        Bank.getInstance().clear();
        super.tearDown();
    }

    public void testDebit() {
        assertEquals(0, this.session.account_balance.get().intValue());
        Bank.getInstance().credit(5);
        assertEquals(5, this.session.account_balance.get().intValue());
    }

    public void testCredit() {
        this.session.account_balance.set(5);
        assertEquals(5, this.session.account_balance.get().intValue());
        Bank.getInstance().debit(3);
        assertEquals(2, this.session.account_balance.get().intValue());
    }

    public void testListener() {
        MockBankUpdateListener listener = new MockBankUpdateListener();
        Bank.getInstance().addUpdateListener(listener);

        assertFalse(listener.called);
        Bank.getInstance().credit(1);
        assertTrue(listener.called);
        assertEquals(1, listener.call_count);
        assertEquals(1, listener.new_balance);

        Bank.getInstance().removeUpdateListener(listener);
    }

    public class MockBankUpdateListener implements Bank.BankUpdateListener {
        public boolean called = false;
        public int call_count = 0;
        public int new_balance = 0;
        @Override
        public void onBankAccountUpdated(int new_balance) {
            this.called = true;
            this.call_count++;
            this.new_balance = new_balance;
        }
    }
}
