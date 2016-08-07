package com.linguaculturalists.phoenicia.models;

import com.linguaculturalists.phoenicia.locale.Decoration;
import com.linguaculturalists.phoenicia.locale.Game;
import com.linguaculturalists.phoenicia.locale.Letter;
import com.linguaculturalists.phoenicia.locale.Word;
import com.linguaculturalists.phoenicia.util.PhoeniciaContext;
import com.orm.androrm.Filter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mhall on 8/6/16.
 */
public class Assets {
    private static Assets instance;
    private GameSession session;
    private List<AssetsUpdateListener> listeners;

    private Assets(GameSession session) {
        this.session = session;
        this.listeners = new ArrayList<AssetsUpdateListener>();
    }

    public static void init(GameSession session) { instance = new Assets(session); }

    public static Assets getInsance() {
        return instance;
    }
    public void addLetterTile(LetterTile tile) { this.assetsUpdated(); }
    public int getLetterTileCount(Letter letter) {
        Filter byLetter = new Filter();
        byLetter.is("item_name", letter.name);
        return LetterTile.objects(PhoeniciaContext.context).filter(this.session.filter).filter(byLetter).count();
    }

    public void addWordTile(WordTile tile) { this.assetsUpdated(); }
    public int getWordTileCount(Word word) {
        Filter byWord = new Filter();
        byWord.is("item_name", word.name);
        return WordTile.objects(PhoeniciaContext.context).filter(this.session.filter).filter(byWord).count();
    }

    public void addGameTile(GameTile tile) { this.assetsUpdated(); }
    public int getGameTileCount(Game game) {
        Filter byGame = new Filter();
        byGame.is("item_name", game.name);
        return GameTile.objects(PhoeniciaContext.context).filter(this.session.session_filter).filter(byGame).count();
    }

    public void addDecorationTile(DecorationTile tile) { this.assetsUpdated(); }
    public int getDecorationTileCount(Decoration decoration) {
        Filter byDecoration = new Filter();
        byDecoration.is("item_name", decoration.name);
        return DecorationTile.objects(PhoeniciaContext.context).filter(this.session.session_filter).filter(byDecoration).count();
    }

    private void assetsUpdated() {
        for (AssetsUpdateListener listener : listeners) {
            listener.onAssetsUpdated();
        }
    }

    public void addUpdateListener(AssetsUpdateListener listener) {
        this.listeners.add(listener);
    }
    public void removeUpdateListener(AssetsUpdateListener listener) {
        this.listeners.remove(listener);
    }
    public interface AssetsUpdateListener {
        void onAssetsUpdated();
    }
}
