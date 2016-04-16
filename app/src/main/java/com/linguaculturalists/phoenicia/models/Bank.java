package com.linguaculturalists.phoenicia.models;

import android.content.Context;

import com.linguaculturalists.phoenicia.PhoeniciaGame;
import com.linguaculturalists.phoenicia.util.PhoeniciaContext;
import com.orm.androrm.Model;
import com.orm.androrm.field.CharField;
import com.orm.androrm.field.DoubleField;
import com.orm.androrm.field.ForeignKeyField;
import com.orm.androrm.field.IntegerField;
import com.orm.androrm.migration.Migrator;

import org.andengine.util.debug.Debug;

import java.util.ArrayList;
import java.util.List;

/**
 * Management class for accessing and updating the in-game currency of a game session.
 */
public class Bank extends Model {
    private static Bank instance;
    //private PhoeniciaGame game;
    private GameSession session;
    private List<BankUpdateListener> listeners;

    private Bank(GameSession session){
       this.session = session;

        this.listeners = new ArrayList<BankUpdateListener>();
    }

    /**
     * Initialize the singleton for the given session
     * @param session
     */
    public static void init(GameSession session) {
        instance = new Bank(session);
    }

    /**
     * Returns an Singleton instance of the Bank class.
     * You must call init(PhoeniciaGame) before calling this method
     * @return
     */
    public static Bank getInstance() {
        return instance;
    }

    /**
     * Clears the current state of the Bank, including GameSession
     */
    public void clear() {
        this.session.account_balance.set(0);
        this.session.gross_income.set(0);
        this.accountUpdated(0);
    }

    /**
     * Subtract from the player's in-game currency account
     * @param amount to subtract from the player's balance
     * @return the new balance if the previous balance is more than \a amount, otherwise the previous balance
     */
    public int debit(int amount) {
        int new_balance = this.session.account_balance.get() - amount;
        if (new_balance >= 0) {
            this.session.account_balance.set(new_balance);
            this.session.save(PhoeniciaContext.context);
            this.accountUpdated(new_balance);
            return new_balance;
        } else {
            return this.session.account_balance.get();
        }
    }

    /**
     * Add to the player's in-game currency account
     * @param amount to add to the player's balance
     * @return the new balance of the player's account
     */
    public int credit(int amount) {
        int new_balance = this.session.account_balance.get() + amount;
        this.session.account_balance.set(new_balance);

        int new_income= this.session.gross_income.get() + amount;
        this.session.gross_income.set(new_income);

        this.session.save(PhoeniciaContext.context);
        this.accountUpdated(new_balance);
        return new_balance;
    }

    private void accountUpdated(int new_balance) {
        Debug.d("Bank account updated");
        for (int i = 0; i < this.listeners.size(); i++) {
            Debug.d("Calling update listener: "+this.listeners.get(i).getClass());
            this.listeners.get(i).onBankAccountUpdated(new_balance);
        }
    }
    public void addUpdateListener(BankUpdateListener listener) {
        this.listeners.add(listener);
    }
    public void removeUpdateListener(BankUpdateListener listener) {
        this.listeners.remove(listener);
    }

    /**
     * Called by the Bank class whenever the player's in-game current balance changes
     */
    public interface BankUpdateListener {
        public void onBankAccountUpdated(int new_balance);
    }
}
