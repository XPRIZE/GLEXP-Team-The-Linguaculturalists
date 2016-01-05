package com.linguaculturalists.phoenicia.models;

import android.content.Context;

import com.linguaculturalists.phoenicia.PhoeniciaGame;
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
 * Created by mhall on 1/3/16.
 */
public class Bank extends Model {
    private static Bank instance;
    private PhoeniciaGame game;
    private List<BankUpdateListener> listeners;

    private Bank(PhoeniciaGame game){
        this.game = game;

        this.listeners = new ArrayList<BankUpdateListener>();
    }

    public static void init(PhoeniciaGame game) {
        instance = new Bank(game);
    }

    public static Bank getInstance() {
        return instance;
    }

    public void clear() {
        this.game.session.account_balance.set(0);
        this.game.session.gross_income.set(0);
        this.accountUpdated(0);
    }

    public int debit(int amount) {
        int new_balance = this.game.session.account_balance.get() - amount;
        if (new_balance > 0) {
            this.game.session.account_balance.set(new_balance);
        }
        this.game.session.save(this.game.activity.getApplicationContext());
        this.accountUpdated(new_balance);
        return new_balance;
    }

    public int credit(int amount) {
        int new_balance = this.game.session.account_balance.get() + amount;
        this.game.session.account_balance.set(new_balance);

        int new_income= this.game.session.gross_income.get() + amount;
        this.game.session.gross_income.set(new_income);

        this.game.session.save(this.game.activity.getApplicationContext());
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

    public interface BankUpdateListener {
        public void onBankAccountUpdated(int new_balance);
    }
}
