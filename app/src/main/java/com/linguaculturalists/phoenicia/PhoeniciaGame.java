package com.linguaculturalists.phoenicia;

import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.view.MotionEvent;

import org.andengine.audio.music.Music;
import org.andengine.audio.music.MusicFactory;
import org.andengine.audio.sound.Sound;
import org.andengine.audio.sound.SoundFactory;
import org.andengine.engine.camera.ZoomCamera;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.Sprite;
import org.andengine.extension.tmx.TMXLayer;
import org.andengine.extension.tmx.TMXProperties;
import org.andengine.extension.tmx.TMXTileProperty;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.detector.PinchZoomDetector;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.source.AssetBitmapTextureAtlasSource;
import org.andengine.opengl.texture.bitmap.AssetBitmapTexture;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.texture.region.TextureRegionFactory;
import org.andengine.util.adt.color.Color;
import org.andengine.extension.tmx.TMXLoader;
import org.andengine.extension.tmx.TMXTile;
import org.andengine.extension.tmx.TMXTiledMap;
import org.andengine.extension.tmx.util.exception.TMXLoadException;
import org.andengine.util.debug.Debug;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.linguaculturalists.phoenicia.components.MapBlockSprite;
import com.linguaculturalists.phoenicia.components.PlacedBlockSprite;
import com.linguaculturalists.phoenicia.components.ProgressDisplay;
import com.linguaculturalists.phoenicia.locale.Decoration;
import com.linguaculturalists.phoenicia.locale.Game;
import com.linguaculturalists.phoenicia.locale.IntroPage;
import com.linguaculturalists.phoenicia.locale.Letter;
import com.linguaculturalists.phoenicia.locale.Level;
import com.linguaculturalists.phoenicia.locale.Locale;
import com.linguaculturalists.phoenicia.locale.Person;
import com.linguaculturalists.phoenicia.locale.Word;
import com.linguaculturalists.phoenicia.models.Assets;
import com.linguaculturalists.phoenicia.models.Bank;
import com.linguaculturalists.phoenicia.models.Builder;
import com.linguaculturalists.phoenicia.models.DecorationTile;
import com.linguaculturalists.phoenicia.models.DefaultTile;
import com.linguaculturalists.phoenicia.models.GameTile;
import com.linguaculturalists.phoenicia.models.GameTileBuilder;
import com.linguaculturalists.phoenicia.models.GameTileTimer;
import com.linguaculturalists.phoenicia.models.LetterBuilder;
import com.linguaculturalists.phoenicia.models.GameSession;
import com.linguaculturalists.phoenicia.models.Inventory;
import com.linguaculturalists.phoenicia.models.InventoryItem;
import com.linguaculturalists.phoenicia.models.LetterTile;
import com.linguaculturalists.phoenicia.models.Market;
import com.linguaculturalists.phoenicia.models.WordBuilder;
import com.linguaculturalists.phoenicia.models.WordTileBuilder;
import com.linguaculturalists.phoenicia.models.WordTile;
import com.linguaculturalists.phoenicia.models.WorkshopBuilder;
import com.linguaculturalists.phoenicia.ui.HUDManager;
import com.linguaculturalists.phoenicia.ui.SpriteMoveHUD;
import com.linguaculturalists.phoenicia.locale.LocaleLoader;
import com.linguaculturalists.phoenicia.util.GameSounds;
import com.linguaculturalists.phoenicia.util.GameTextures;
import com.linguaculturalists.phoenicia.util.PhoeniciaContext;
import com.linguaculturalists.phoenicia.util.SystemUiHider;
import com.orm.androrm.Filter;

/**
 * The main class for managing a game.
 */
public class PhoeniciaGame implements IUpdateHandler, Inventory.InventoryUpdateListener, Bank.BankUpdateListener, GameSession.ExperienceChangeListener, Assets.AssetsUpdateListener {
    public static final int PERSON_TILE_WIDTH = 256;
    public static final int PERSON_TILE_HEIGHT = 256;
    public static final int LETTER_SPRITE_WIDTH = 64;
    public static final int LETTER_SPRITE_HEIGHT = 64;
    public static final int LETTER_SPRITE_COLS = 3;
    public static final int LETTER_SPRITE_ROWS = 1;
    public static final int LETTER_TEXTURE_COLS = 4;
    public static final int LETTER_TEXTURE_ROWS = 6;
    public static final int WORD_SPRITE_WIDTH = 64;
    public static final int WORD_SPRITE_HEIGHT = 64;
    public static final int WORD_SPRITE_COLS = 3;
    public static final int WORD_SPRITE_ROWS = 1;
    public static final int WORD_TEXTURE_COLS = 4;
    public static final int WORD_TEXTURE_ROWS = 6;
    public static final int GAME_SPRITE_WIDTH = 64;
    public static final int GAME_SPRITE_HEIGHT = 64;
    public static final int GAME_SPRITE_COLS = 3;
    public static final int GAME_SPRITE_ROWS = 1;
    public static final int GAME_TEXTURE_COLS = 4;
    public static final int GAME_TEXTURE_ROWS = 6;
    public static final int DECORATION_SPRITE_WIDTH = 64;
    public static final int DECORATION_SPRITE_HEIGHT = 64;
    public static final int DECORATION_SPRITE_COLS = 3;
    public static final int DECORATION_SPRITE_ROWS = 1;
    public static final int DECORATION_TEXTURE_COLS = 4;
    public static final int DECORATION_TEXTURE_ROWS = 2;
    public Locale locale; /**< the locale used by the game session */
    public Scene scene;
    public ZoomCamera camera;
    private float startCenterX;
    private float startCenterY;
    public GameActivity activity;
    private boolean isStarted;
    private boolean isRunning;

    private float mPinchZoomStartedCameraZoomFactor;
    private PinchZoomDetector mPinchZoomDetector;

    public ITexture fontTexture;
    public Font defaultFont;

    private Music music;

    public AssetBitmapTexture shellTexture;
    public ITiledTextureRegion shellTiles; /**< Tile regions for building the game shell */

    public Map<Person, AssetBitmapTexture> personTextures;
    public Map<Person, TextureRegion> personTiles; /**< Tile regions depicting people */

    public AssetBitmapTexture inventoryTexture;
    public ITiledTextureRegion inventoryTiles; /**< Tile regions for the inventory block */

    public AssetBitmapTexture marketTexture;
    public ITiledTextureRegion marketTiles; /**< Tile regions for the market block */

    public AssetBitmapTexture workshopTexture;
    public ITiledTextureRegion workshopTiles; /**< Tile regions for the workshop block */

    public Map<Letter, AssetBitmapTexture> letterTextures;
    public Map<Letter, ITiledTextureRegion> letterSprites; /**< Tile regions depicting letter sprites */
    public Map<Letter, ITiledTextureRegion> letterBlocks; /**< Tile regions depicting letter blocks */

    public Map<Word, AssetBitmapTexture> wordTextures;
    public Map<Word, ITiledTextureRegion> wordSprites; /**< Tile regions depicting word sprites */
    public Map<Word, ITiledTextureRegion> wordBlocks; /**< Tile regions depicting word blocks */

    public Map<Game, AssetBitmapTexture> gameTextures;
    public Map<Game, ITiledTextureRegion> gameSprites; /**< Tile regions depicting session sprites */
    public Map<Game, ITiledTextureRegion> gameBlocks; /**< Tile regions depicting session blocks */

    public Map<Decoration, AssetBitmapTexture> decorationTextures;
    public Map<Decoration, ITiledTextureRegion> decorationSprites; /**< Tile regions depicting session sprites */
    public Map<Decoration, ITiledTextureRegion> decorationBlocks; /**< Tile regions depicting session blocks */

    public Map<ITiledTextureRegion, Bitmap> spriteMasks; /**< Click mask bitmap for block sprites */

    public Sprite[][] placedSprites; /**< active map tiles arranged according to the ISO map grid */
    public String[][] mapRestrictions; /**< map tile class types arranged according to the ISO map grid */
    public Map<Integer, String> mapTileClass;
    private Letter placeLetter;
    private Word placeWord;

    private TMXTiledMap mTMXTiledMap;

    private Map<String, Music> levelSounds;
    private Map<String, Sound> blockSounds;

    public HUDManager hudManager; /**< The HUD stack manager for this game */
    public Inventory inventory; /**< The Inventory manager for this game */
    public Bank bank; /**< The Bank account manager for this game */

    public GameSession session; /**< The saved GameSession being run */
    //public Filter sessionFilter; /**< AndrOrm query filter to limit results to this GameSession */
    private Set<Builder> builders;
    private float updateTime;
    private long currentTime;

    public String current_level = ""; /**< The current level the player has reached */
    private List<LevelChangeListener> levelListeners;

    public PhoeniciaGame(GameActivity activity, final ZoomCamera camera) {
        FontFactory.setAssetBasePath("fonts/");

        this.activity = activity;
        this.camera = camera;
        this.isStarted = false;
        this.isRunning = false;

        this.levelListeners = new ArrayList<LevelChangeListener>();

        scene = new Scene();
        scene.setBackground(new Background(new Color(0, 0, 0)));

        this.levelSounds = new HashMap<String, Music>();
        this.blockSounds = new HashMap<String, Sound>();
        this.builders = new HashSet<Builder>();
        this.updateTime = 0;

        this.personTextures = new HashMap<Person, AssetBitmapTexture>();
        this.personTiles = new HashMap<Person, TextureRegion>();

        this.letterTextures = new HashMap<Letter, AssetBitmapTexture>();
        this.letterSprites = new HashMap<Letter, ITiledTextureRegion>();
        this.letterBlocks = new HashMap<Letter, ITiledTextureRegion>();

        this.wordTextures = new HashMap<Word, AssetBitmapTexture>();
        this.wordSprites = new HashMap<Word, ITiledTextureRegion>();
        this.wordBlocks = new HashMap<Word, ITiledTextureRegion>();
        this.spriteMasks = new HashMap<ITiledTextureRegion, Bitmap>();

        this.gameTextures = new HashMap<Game, AssetBitmapTexture>();
        this.gameSprites = new HashMap<Game, ITiledTextureRegion>();
        this.gameBlocks = new HashMap<Game, ITiledTextureRegion>();

        this.decorationTextures = new HashMap<Decoration, AssetBitmapTexture>();
        this.decorationSprites = new HashMap<Decoration, ITiledTextureRegion>();
        this.decorationBlocks = new HashMap<Decoration, ITiledTextureRegion>();

        final float minZoomFactor = 1.0f;
        final float maxZoomFactor = 3.0f;
        mPinchZoomDetector = new PinchZoomDetector(new PinchZoomDetector.IPinchZoomDetectorListener() {
            @Override
            public void onPinchZoomStarted(final PinchZoomDetector pPinchZoomDetector, final TouchEvent pTouchEvent) {
                mPinchZoomStartedCameraZoomFactor = camera.getZoomFactor();
            }

            @Override
            public void onPinchZoom(final PinchZoomDetector pPinchZoomDetector, final TouchEvent pTouchEvent, final float pZoomFactor) {
                final float newZoomFactor = mPinchZoomStartedCameraZoomFactor * pZoomFactor;
                if (newZoomFactor >= minZoomFactor && newZoomFactor <= maxZoomFactor) {
                    camera.setZoomFactor(newZoomFactor);
                }
            }

            @Override
            public void onPinchZoomFinished(final PinchZoomDetector pPinchZoomDetector, final TouchEvent pTouchEvent, final float pZoomFactor) {
                //camera.setZoomFactor(mPinchZoomStartedCameraZoomFactor * pZoomFactor);
            }
        });
        scene.setOnSceneTouchListener(new IOnSceneTouchListener() {
            private boolean pressed = false;

            @Override
            public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent) {
                mPinchZoomDetector.onTouchEvent(pSceneTouchEvent);

                switch (pSceneTouchEvent.getAction()) {
                    case TouchEvent.ACTION_DOWN:
                        this.pressed = true;
                        break;
                    case TouchEvent.ACTION_UP:
                        if (this.pressed) {
                            //addBlock((int) pSceneTouchEvent.getX(), (int) pSceneTouchEvent.getY());
                            this.pressed = false;
                            return true;
                        }
                        break;
                    case TouchEvent.ACTION_MOVE:
                        this.pressed = false;
                        MotionEvent motion = pSceneTouchEvent.getMotionEvent();
                        if (motion.getHistorySize() > 0) {
                            for (int i = 1, n = motion.getHistorySize(); i < n; i++) {
                                float calcX = motion.getHistoricalX(i) - motion.getHistoricalX(i - 1);
                                float calcY = motion.getHistoricalY(i) - motion.getHistoricalY(i - 1);
                                final float zoom = camera.getZoomFactor() + 1.0f;

                                camera.setCenter(camera.getCenterX() - (calcX / zoom), camera.getCenterY() + (calcY / zoom));
                            }
                        }
                        return true;
                }
                return false;
            }
        });

        this.hudManager = new HUDManager(this);

    }

    /**
     * Load the game data from both the locale and the saved session.
     * @throws IOException
     */
    public void load(final GameSession session, ProgressDisplay progress) throws IOException {
        this.session = session;
        this.current_level = this.session.current_level.get();
        if (this.current_level == null) this.current_level = "";

        progress.setProgress(0f);
        // Load locale pack
        LocaleLoader localeLoader = new LocaleLoader();
        try {
            this.locale = localeLoader.load(PhoeniciaContext.assetManager.open(this.session.locale_pack.get()));
            Debug.d("Locale map: "+locale.map_src);
        } catch (final IOException e) {
            Debug.e("Error loading Locale from "+this.session.locale_pack.get(), e);
        }

        progress.setProgress(0.1f);

        // Start the Inventory for this session
        Inventory.init(this.session);
        this.inventory = Inventory.getInstance();
        this.inventory.addUpdateListener(this);

        // Start the Assets (tile) manager for this session
        Assets.init(this.session);
        Assets.getInsance().addUpdateListener(this);

        // Start the Market for this session
        Market.init(this);

        // Start the Bank for this session
        Bank.init(this.session);
        this.bank = Bank.getInstance();
        this.bank.addUpdateListener(this);

        // Load font assets
        this.defaultFont = FontFactory.create(PhoeniciaContext.fontManager, PhoeniciaContext.textureManager, 256, 256, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 32, Color.RED_ARGB_PACKED_INT);
        this.defaultFont.load();

        GameSounds.init();

        progress.setProgress(0.2f);
        this.loadLocale(progress);
        this.loadSession(progress);
    }

    private void loadLocale(ProgressDisplay progress) throws IOException {
        // For storing sound data
        blockSounds = new HashMap<String, Sound>();

        // Load background music
        try {
            this.music = MusicFactory.createMusicFromAsset(PhoeniciaContext.musicManager, PhoeniciaContext.context, locale.music_src);
            this.music.setLooping(true);
            this.music.setVolume(0.5f);
        } catch (Exception e) {
            Debug.e("Failed to load background music asset: "+locale.music_src);
        }

        progress.setProgress(0.3f);
        this.loadLocaleMap();
        progress.setProgress(0.4f);
        this.loadLocaleDefaults();
        this.loadLocalePeople();
        progress.setProgress(0.5f);
        this.loadLocaleLetters();
        progress.setProgress(0.6f);
        this.loadLocaleWords();
        progress.setProgress(0.7f);
        this.loadLocaleGames();
        progress.setProgress(0.8f);
        this.loadLocaleDecorations();
        this.loadLocaleLevels();
        progress.setProgress(0.9f);
    }

    private void loadLocaleMap() {
        // Load map assets
        try {
            final TMXLoader tmxLoader = new TMXLoader(PhoeniciaContext.assetManager, PhoeniciaContext.textureManager, TextureOptions.BILINEAR_PREMULTIPLYALPHA, PhoeniciaContext.vboManager);
            this.mTMXTiledMap = tmxLoader.loadFromAsset(this.locale.map_src);

            // Initiate array for holding tile type restrictions
            mapTileClass = new HashMap<Integer, String>();
            mapRestrictions = new String[this.mTMXTiledMap.getTileColumns()][this.mTMXTiledMap.getTileRows()];

            // Add each layer to the scene
            for (TMXLayer tmxLayer : this.mTMXTiledMap.getTMXLayers()){
                scene.attachChild(tmxLayer);

                // Check tiles in this layer for map restrictions
                TMXTile[][] tiles = tmxLayer.getTMXTiles();
                for (int r = 0; r < this.mTMXTiledMap.getTileRows(); r++) {
                    for (int c = 0; c < this.mTMXTiledMap.getTileColumns(); c++) {
                        TMXTile tile = tiles[r][c];
                        if (tile == null) continue;
                        if (!mapTileClass.containsKey(tile.getGlobalTileID())) {
                            try {
                                TMXProperties<TMXTileProperty> props = this.mTMXTiledMap.getTMXTileProperties(tile.getGlobalTileID());
                                if (props == null) continue;
                                for (TMXTileProperty prop : props) {
                                    if (prop.getName().equals("class")) {
                                        //Debug.d("Found map restriction '"+prop.getValue()+"' at "+r+"x"+c);
                                        mapTileClass.put(tile.getGlobalTileID(), prop.getValue());
                                    }
                                }
                            } catch (IllegalArgumentException e) {
                                // Do nothing, it just means there are no properties for this tile
                            }
                        }
                        if (mapTileClass.get(tile.getGlobalTileID()) != null) {
                            //Debug.d("Tile at "+r+"x"+c+" restriction: "+mapTileClass.get(tile.getGlobalTileID()));
                            mapRestrictions[r][c] = mapTileClass.get(tile.getGlobalTileID());
                        }
                    }
                }
            }

            // Initiate array for holding references to block sprites on the map
            placedSprites = new Sprite[this.mTMXTiledMap.getTileColumns()][this.mTMXTiledMap.getTileRows()];

            // Lock the camera to the map's boundaries
            TMXLayer baseLayer = this.mTMXTiledMap.getTMXLayers().get(0);
            this.camera.setBoundsEnabled(true);
            this.camera.setBounds((-baseLayer.getWidth() / 2) + 32, -baseLayer.getHeight(), (baseLayer.getWidth() / 2) + 32, 32);
        } catch (final TMXLoadException e) {
            Debug.e("Error loading map at " + this.locale.map_src, e);
        }

    }

    private void loadLocaleDefaults() throws IOException {
        try {
            shellTexture = new AssetBitmapTexture(PhoeniciaContext.textureManager, PhoeniciaContext.assetManager, this.locale.shell_src);
            shellTexture.load();
            shellTiles = TextureRegionFactory.extractTiledFromTexture(shellTexture, 0, 0, 64 * 8, 64 * 8, 8, 8);

            inventoryTexture = new AssetBitmapTexture(PhoeniciaContext.textureManager, PhoeniciaContext.assetManager, this.locale.inventoryBlock.block_texture);
            inventoryTexture.load();
            int[] inventoryTileSize = GameTextures.calculateTileSize(locale.inventoryBlock.columns, locale.inventoryBlock.rows, locale.inventoryBlock.height);
            inventoryTiles = TextureRegionFactory.extractTiledFromTexture(inventoryTexture, 0, 0, inventoryTileSize[0] * 4, inventoryTileSize[1] * 5, 4, 5);
            this.spriteMasks.put(inventoryTiles, AssetBitmapTextureAtlasSource.create(PhoeniciaContext.assetManager, this.locale.inventoryBlock.block_texture).onLoadBitmap(Bitmap.Config.ALPHA_8));

            marketTexture = new AssetBitmapTexture(PhoeniciaContext.textureManager, PhoeniciaContext.assetManager, this.locale.marketBlock.block_texture);
            marketTexture.load();
            int[] marketTileSize = GameTextures.calculateTileSize(locale.marketBlock.columns, locale.marketBlock.rows, locale.marketBlock.height);
            marketTiles = TextureRegionFactory.extractTiledFromTexture(marketTexture, 0, 0, marketTileSize[0] * 4, marketTileSize[1] * 5, 4, 5);
            this.spriteMasks.put(marketTiles, AssetBitmapTextureAtlasSource.create(PhoeniciaContext.assetManager, this.locale.marketBlock.block_texture).onLoadBitmap(Bitmap.Config.ALPHA_8));

            workshopTexture = new AssetBitmapTexture(PhoeniciaContext.textureManager, PhoeniciaContext.assetManager, this.locale.workshopBlock.block_texture);
            workshopTexture.load();
            int[] workshopTileSize = GameTextures.calculateTileSize(locale.workshopBlock.columns, locale.workshopBlock.rows, locale.workshopBlock.height);
            workshopTiles = TextureRegionFactory.extractTiledFromTexture(workshopTexture, 0, 0, workshopTileSize[0] * 4, workshopTileSize[1] * 5, 4, 5);
            this.spriteMasks.put(workshopTiles, AssetBitmapTextureAtlasSource.create(PhoeniciaContext.assetManager, this.locale.workshopBlock.block_texture).onLoadBitmap(Bitmap.Config.ALPHA_8));

        } catch (final IOException e) {
            e.printStackTrace();
            throw e;
        }

    }

    private void loadLocalePeople() throws IOException {
        for (Person person : this.locale.people) {
            try {
                AssetBitmapTexture texture = new AssetBitmapTexture(PhoeniciaContext.textureManager, PhoeniciaContext.assetManager, person.texture_src);
                texture.load();
                this.personTextures.put(person, texture);
                this.personTiles.put(person, TextureRegionFactory.extractFromTexture(texture, 0, 0, PERSON_TILE_WIDTH, PERSON_TILE_HEIGHT));
            } catch (final IOException e) {
                e.printStackTrace();
                throw e;
            }
        }
    }

    private void loadLocaleLetters() throws IOException {
        List<Letter> blockLetters = locale.letters;
        try {
            // Load letter assets
            for (int i = 0; i < blockLetters.size(); i++) {
                Letter letter = blockLetters.get(i);
                Debug.d("Loading letter sprite texture from " + letter.sprite_texture);
                final AssetBitmapTexture letterSpriteTexture = new AssetBitmapTexture(PhoeniciaContext.textureManager, PhoeniciaContext.assetManager, letter.sprite_texture);
                letterSpriteTexture.load();
                this.letterTextures.put(letter, letterSpriteTexture);
                this.letterSprites.put(letter, TextureRegionFactory.extractTiledFromTexture(letterSpriteTexture, 0, 0, LETTER_SPRITE_WIDTH * LETTER_SPRITE_COLS, LETTER_SPRITE_HEIGHT * LETTER_SPRITE_ROWS, LETTER_SPRITE_COLS, LETTER_SPRITE_ROWS));

                Debug.d("Loading letter block texture from " + letter.block_texture);
                final AssetBitmapTexture letterBlockTexture = new AssetBitmapTexture(PhoeniciaContext.textureManager, PhoeniciaContext.assetManager, letter.block_texture);
                letterBlockTexture.load();
                int[] letterTileSize = GameTextures.calculateTileSize(letter.columns, letter.rows, letter.height);
                ITiledTextureRegion letterTiles = TextureRegionFactory.extractTiledFromTexture(letterBlockTexture, 0, 0, letterTileSize[0] * LETTER_TEXTURE_COLS, letterTileSize[1] * LETTER_TEXTURE_ROWS, LETTER_TEXTURE_COLS, LETTER_TEXTURE_ROWS);
                this.letterBlocks.put(letter, letterTiles);
                this.spriteMasks.put(letterTiles, AssetBitmapTextureAtlasSource.create(PhoeniciaContext.assetManager, letter.block_texture).onLoadBitmap(Bitmap.Config.ALPHA_8));
            }

        } catch (final IOException e)
        {
            e.printStackTrace();
            throw e;
        }

        Debug.d("Loading letter sounds");
        for (int i = 0; i < blockLetters.size(); i++) {
            Letter letter = blockLetters.get(i);
            Debug.d("Loading sound file "+i+": "+letter.sound);
            try {
                blockSounds.put(letter.sound, SoundFactory.createSoundFromAsset(PhoeniciaContext.soundManager, PhoeniciaContext.context, letter.sound));
                blockSounds.put(letter.phoneme, SoundFactory.createSoundFromAsset(PhoeniciaContext.soundManager, PhoeniciaContext.context, letter.phoneme));
            } catch (IOException e) {
                Debug.e("Failed to load letter sound file: "+letter.sound);
                e.printStackTrace();
            }
        }
    }

    private void loadLocaleWords() throws IOException {
        List<Word> blockWords = locale.words;
        try {
            // Load word assets
            for (int i = 0; i < blockWords.size(); i++) {
                Word word = blockWords.get(i);
                Debug.d("Loading word sprite texture from " + word.sprite_texture);
                final AssetBitmapTexture wordSpriteTexture = new AssetBitmapTexture(PhoeniciaContext.textureManager, PhoeniciaContext.assetManager, word.sprite_texture);
                wordSpriteTexture.load();
                this.wordTextures.put(word, wordSpriteTexture);
                this.wordSprites.put(word, TextureRegionFactory.extractTiledFromTexture(wordSpriteTexture, 0, 0, WORD_SPRITE_WIDTH * WORD_SPRITE_COLS, WORD_SPRITE_HEIGHT * WORD_SPRITE_ROWS, WORD_SPRITE_COLS, WORD_SPRITE_ROWS));

                Debug.d("Loading word block texture from " + word.block_texture);
                final AssetBitmapTexture wordTexture = new AssetBitmapTexture(PhoeniciaContext.textureManager, PhoeniciaContext.assetManager, word.block_texture);
                wordTexture.load();
                int[] wordTileSize = GameTextures.calculateTileSize(word.columns, word.rows, word.height);
                Debug.d("Word " + word.name + " has size " + word.columns + "x" + word.rows + " and sprite size " + wordTileSize[0] + "x" + wordTileSize[1]);
                ITiledTextureRegion blockTextureRegion = TextureRegionFactory.extractTiledFromTexture(wordTexture, 0, 0, wordTileSize[0] * WORD_TEXTURE_COLS, wordTileSize[1] * WORD_TEXTURE_ROWS, WORD_TEXTURE_COLS, WORD_TEXTURE_ROWS);
                this.wordBlocks.put(word, blockTextureRegion);

                // Get click mask
                final AssetBitmapTextureAtlasSource wordSpriteTextureSource = AssetBitmapTextureAtlasSource.create(PhoeniciaContext.assetManager, word.block_texture);
                this.spriteMasks.put(blockTextureRegion, wordSpriteTextureSource.onLoadBitmap(Bitmap.Config.ALPHA_8));
            }

        } catch (final IOException e)
        {
            e.printStackTrace();
            throw e;
        }

        Debug.d("Loading word sounds");
        for (int i = 0; i < blockWords.size(); i++) {
            Word word = blockWords.get(i);
            Debug.d("Loading sound file "+i+": "+word.sound);
            try {
                blockSounds.put(word.sound, SoundFactory.createSoundFromAsset(PhoeniciaContext.soundManager, PhoeniciaContext.context, word.sound));
            } catch (IOException e) {
                Debug.e("Failed to load letter sound file: "+word.sound);
                e.printStackTrace();
            }
        }
    }

    private void loadLocaleGames() throws IOException {
        List<Game> blockGames = locale.games;
        try {
            // Load game assets
            for (int i = 0; i < blockGames.size(); i++) {
                Game game = blockGames.get(i);
                Debug.d("Loading game sprite texture from " + game.sprite_texture);
                final AssetBitmapTexture gameSpriteTexture = new AssetBitmapTexture(PhoeniciaContext.textureManager, PhoeniciaContext.assetManager, game.sprite_texture);
                gameSpriteTexture.load();
                this.gameTextures.put(game, gameSpriteTexture);
                this.gameSprites.put(game, TextureRegionFactory.extractTiledFromTexture(gameSpriteTexture, 0, 0, GAME_SPRITE_WIDTH * GAME_SPRITE_COLS, GAME_SPRITE_HEIGHT * GAME_SPRITE_ROWS, GAME_SPRITE_COLS, GAME_SPRITE_ROWS));

                Debug.d("Loading game block texture from " + game.block_texture);
                final AssetBitmapTexture gameTexture = new AssetBitmapTexture(PhoeniciaContext.textureManager, PhoeniciaContext.assetManager, game.block_texture);
                gameTexture.load();
                int[] gameTileSize = GameTextures.calculateTileSize(game.columns, game.rows, game.height);
                Debug.d("Word "+game.name+" has size "+game.columns+"x"+game.rows+" and sprite size "+gameTileSize[0]+"x"+gameTileSize[1]);
                this.gameBlocks.put(game, TextureRegionFactory.extractTiledFromTexture(gameTexture, 0, 0, gameTileSize[0] * GAME_TEXTURE_COLS, gameTileSize[1] * GAME_TEXTURE_ROWS, GAME_TEXTURE_COLS, GAME_TEXTURE_ROWS));
            }

        } catch (final IOException e)
        {
            e.printStackTrace();
            throw e;
        }
    }

    private void loadLocaleDecorations() throws IOException {
        List<Decoration> blockDecorations = locale.decorations;
        try {
            // Load game assets
            for (int i = 0; i < blockDecorations.size(); i++) {
                Decoration decoration = blockDecorations.get(i);
                Debug.d("Loading decoration sprite texture from " + decoration.sprite_texture);
                final AssetBitmapTexture decorationSpriteTexture = new AssetBitmapTexture(PhoeniciaContext.textureManager, PhoeniciaContext.assetManager, decoration.sprite_texture);
                decorationSpriteTexture.load();
                this.decorationTextures.put(decoration, decorationSpriteTexture);
                this.decorationSprites.put(decoration, TextureRegionFactory.extractTiledFromTexture(decorationSpriteTexture, 0, 0, DECORATION_SPRITE_WIDTH * DECORATION_SPRITE_COLS, DECORATION_SPRITE_HEIGHT * DECORATION_SPRITE_ROWS, DECORATION_SPRITE_COLS, DECORATION_SPRITE_ROWS));

                Debug.d("Loading decoration block texture from " + decoration.block_texture);
                final AssetBitmapTexture decorationTexture = new AssetBitmapTexture(PhoeniciaContext.textureManager, PhoeniciaContext.assetManager, decoration.block_texture);
                decorationTexture.load();
                int[] decorationTileSize = GameTextures.calculateTileSize(decoration.columns, decoration.rows, decoration.height);
                Debug.d("Word "+decoration.name+" has size "+decoration.columns+"x"+decoration.rows+" and sprite size "+decorationTileSize[0]+"x"+decorationTileSize[1]);
                this.decorationBlocks.put(decoration, TextureRegionFactory.extractTiledFromTexture(decorationTexture, 0, 0, decorationTileSize[0] * DECORATION_TEXTURE_COLS, decorationTileSize[1] * DECORATION_TEXTURE_ROWS, DECORATION_TEXTURE_COLS, DECORATION_TEXTURE_ROWS));
            }

        } catch (final IOException e)
        {
            e.printStackTrace();
            throw e;
        }
    }

    private void loadLocaleLevels() {
        // Load level assets
        // TODO: Loading all level intros now is wasteful, need to hack SoundFactory to take a callback when loading finishes
        Debug.d("Loading intros");
        for (int i = 0; i < locale.levels.size(); i++) {
            Level level = locale.levels.get(i);
            Debug.d("Loading intros for level " + level.name);
            for (int j = 0; j < level.intro.size(); j++) {
                IntroPage page = level.intro.get(j);
                Debug.d("Loading level intro sound file " + page.sound);
                try {
                    levelSounds.put(page.sound, MusicFactory.createMusicFromAsset(PhoeniciaContext.musicManager, PhoeniciaContext.context, page.sound));
                } catch (IOException e) {
                    Debug.w("Failed to load level intro sound: "+page.sound);
                    e.printStackTrace();
                }
            }
        }

    }
    private void loadSession(ProgressDisplay progress) {
        progress.setProgress(0.90f);
        this.loadSessionDefaultTiles();
        progress.setProgress(0.91f);
        this.loadSessionLetters();
        progress.setProgress(0.94f);
        this.loadSessionWords();
        progress.setProgress(0.98f);
        this.loadSessionGames();
        progress.setProgress(0.99f);
        this.loadSessionDecorations();
        progress.setProgress(1.0f);
    }

    private void loadSessionDefaultTiles() {
        Debug.d("Loading inventory tile");
        try {
            final Filter inventoryFilter = new Filter();
            inventoryFilter.is("item_type", "inventory");
            final DefaultTile inventoryDefaultTile = DefaultTile.objects(PhoeniciaContext.context).filter(session.filter).filter(inventoryFilter).toList().get(0);
            inventoryDefaultTile.phoeniciaGame = this;
            this.createInventorySprite(inventoryDefaultTile);
            this.startCenterX = inventoryDefaultTile.sprite.getX();
            this.startCenterY = inventoryDefaultTile.sprite.getY();
        } catch (IndexOutOfBoundsException e) {
            this.createInventoryTile();
        }

        Debug.d("Loading market tile");
        try {
            final Filter marketFilter = new Filter();
            marketFilter.is("item_type", "market");
            final DefaultTile marketDefaultTile = DefaultTile.objects(PhoeniciaContext.context).filter(session.filter).filter(marketFilter).toList().get(0);
            marketDefaultTile.phoeniciaGame = this;
            this.createMarketSprite(marketDefaultTile);
        } catch (IndexOutOfBoundsException e) {
            this.createMarketTile();
        }
        Debug.d("Loading workshop tile");
        try {
            final Filter workshopFilter = new Filter();
            workshopFilter.is("item_type", "workshop");
            final DefaultTile workshopDefaultTile = DefaultTile.objects(PhoeniciaContext.context).filter(session.filter).filter(workshopFilter).toList().get(0);
            workshopDefaultTile.phoeniciaGame = this;
            this.createWorkshopSprite(workshopDefaultTile);
            Filter forWorkshopTile = new Filter();
            forWorkshopTile.is("tile", workshopDefaultTile);
            try {
                WorkshopBuilder workshopBuilder = WorkshopBuilder.objects(PhoeniciaContext.context).filter(forWorkshopTile).toList().get(0);
                workshopDefaultTile.setBuilder(workshopBuilder);
                if (workshopBuilder.status.get() != Builder.NONE) {
                    Debug.d("Workshop building word: " + workshopBuilder.item_name.get());
                    Word workshopWord = locale.word_map.get(workshopBuilder.item_name.get());
                    if (workshopWord != null) {
                        workshopBuilder.setUpdateHandler(this, workshopDefaultTile, workshopWord);
                        workshopBuilder.time.set(workshopWord.time);
                        // If builder is market complete, set the progress to the build time in case it was changed in the locale
                        if (workshopBuilder.status.get() == Builder.COMPLETE) {
                            workshopBuilder.progress.set(workshopWord.time);
                            workshopDefaultTile.setAttention(true);
                        }
                        workshopBuilder.save(PhoeniciaContext.context);
                        Debug.d("Found workshop builder with " + workshopBuilder.progress.get() + "/" + workshopBuilder.time.get() + " and status " + workshopBuilder.status.get());
                    }
                }
                this.addBuilder(workshopBuilder);
            } catch (IndexOutOfBoundsException e) {
                // No WorkshopBuilder found
            }
        } catch (IndexOutOfBoundsException e) {
            this.createWorkshopTile();
        }
    }

    private void loadSessionLetters() {
        Debug.d("Loading letter tiles");
        List<LetterTile> letterTiles = LetterTile.objects(PhoeniciaContext.context).filter(session.filter).toList();
        for (int i = 0; i < letterTiles.size(); i++) {
            LetterTile letterTile = letterTiles.get(i);
            Debug.d("Restoring tile "+letterTile.item_name.get());
            // In case a letter was removed from the locale
            if (!this.locale.letter_map.containsKey(letterTile.item_name.get())) continue;
            letterTile.letter = this.locale.letter_map.get(letterTile.item_name.get());
            letterTile.phoeniciaGame = this;
            LetterBuilder builder = letterTile.getBuilder(PhoeniciaContext.context);
            if (builder == null) {
                Debug.d("Adding new builder for tile "+letterTile.item_name.get());
                builder = new LetterBuilder(this.session, letterTile, letterTile.item_name.get(), letterTile.letter.time);
                builder.save(PhoeniciaContext.context);
                letterTile.setBuilder(builder);
                letterTile.save(PhoeniciaContext.context);
                builder.start();
            } else {
                builder.time.set(letterTile.letter.time);
                // If builder is market complete, set the progress to the build time in case it was changed in the locale3
                if (builder.status.get() == Builder.COMPLETE) {
                    builder.progress.set(letterTile.letter.time);
                }
                builder.save(PhoeniciaContext.context);
                Debug.d("Found builder with " + builder.progress.get() + "/" + builder.time.get() + " and status " + builder.status.get());
            }
            this.addBuilder(builder);
            this.createLetterSprite(letterTile);
        }
    }

    private void loadSessionWords() {
        Debug.d("Loading word tiles");
        List<WordTile> wordTiles = WordTile.objects(PhoeniciaContext.context).filter(session.filter).toList();
        for (int i = 0; i < wordTiles.size(); i++) {
            WordTile wordTile = wordTiles.get(i);
            Debug.d("Restoring tile "+wordTile.item_name.get());
            wordTile.word = this.locale.word_map.get(wordTile.item_name.get());
            // In case a word was removed from the locale
            if (!this.locale.word_map.containsKey(wordTile.item_name.get())) continue;
            wordTile.phoeniciaGame = this;
            WordTileBuilder builder = wordTile.getBuilder(PhoeniciaContext.context);
            if (builder == null) {
                Debug.d("Adding new builder for tile " + wordTile.item_name.get());
                builder = new WordTileBuilder(this.session, wordTile, wordTile.item_name.get(), wordTile.word.construct);
                builder.save(PhoeniciaContext.context);
                builder.start();
            } else {
                builder.time.set(wordTile.word.construct);
                // If builder is market complete, set the progress to the build time in case it was changed in the locale
                if (builder.status.get() == Builder.COMPLETE) {
                    builder.progress.set(wordTile.word.construct);
                }
                builder.save(PhoeniciaContext.context);
                Debug.d("Found builder with "+builder.progress.get()+"/"+builder.time.get()+" and status "+builder.status.get());
            }
            wordTile.setBuilder(builder);
            wordTile.save(PhoeniciaContext.context);
            this.addBuilder(builder);

            this.createWordSprite(wordTile);
            wordTile.restart(PhoeniciaContext.context);
        }
    }

    private void loadSessionGames() {
        Debug.d("Loading game tiles");
        Filter session_filter = new Filter();
        session_filter.is("session", session);
        List<GameTile> gameTiles = GameTile.objects(PhoeniciaContext.context).filter(session_filter).toList();
        for (int i = 0; i < gameTiles.size(); i++) {
            GameTile gameTile = gameTiles.get(i);
            Debug.d("Restoring tile "+gameTile.item_name.get());
            // In case a game was removed from the locale
            if (!this.locale.game_map.containsKey(gameTile.item_name.get())) continue;
            gameTile.game = this.locale.game_map.get(gameTile.item_name.get());
            gameTile.phoeniciaGame = this;
            GameTileBuilder builder = gameTile.getBuilder(PhoeniciaContext.context);
            if (builder == null) {
                Debug.d("Adding new builder for tile " + gameTile.item_name.get());
                builder = new GameTileBuilder(this.session, gameTile, gameTile.item_name.get(), gameTile.game.construct);
                builder.save(PhoeniciaContext.context);
                builder.start();
            } else {
                builder.time.set(gameTile.game.construct);
                // If builder is market complete, set the progress to the build time in case it was changed in the locale
                if (builder.status.get() == Builder.COMPLETE) {
                    builder.progress.set(gameTile.game.time);
                }
                builder.save(PhoeniciaContext.context);
                Debug.d("Found builder with "+builder.progress.get()+"/"+builder.time.get()+" and status "+builder.status.get());
            }
            gameTile.setBuilder(builder);
            gameTile.save(PhoeniciaContext.context);
            this.addBuilder(builder);

            this.createGameSprite(gameTile);
            gameTile.restart(PhoeniciaContext.context);
        }
    }

    private void loadSessionDecorations() {
        Debug.d("Loading decoration tiles");
        Filter session_filter = new Filter();
        session_filter.is("session", session);
        List<DecorationTile> decorationTiles = DecorationTile.objects(PhoeniciaContext.context).filter(session_filter).toList();
        for (int i = 0; i < decorationTiles.size(); i++) {
            DecorationTile decorationTile = decorationTiles.get(i);
            Debug.d("Restoring tile " + decorationTile.item_name.get());
            // In case a decoration was removed from the locale
            if (!this.locale.decoration_map.containsKey(decorationTile.item_name.get())) continue;
            decorationTile.decoration = this.locale.decoration_map.get(decorationTile.item_name.get());
            decorationTile.phoeniciaGame = this;
            decorationTile.save(PhoeniciaContext.context);

            this.createDecorationSprite(decorationTile);
        }
    }

    /**
     * Completely restarts a GameSession, deleting any and all saved and state data.
     */
    public void restart() {
        // Stop build queues
        this.builders.clear();
        // Detach sprites
        if (placedSprites != null) {
            for (int c = 0; c < placedSprites.length; c++) {
                for (int r = 0; r < placedSprites[c].length; r++) {
                    if (placedSprites[c][r] != null) {
                        scene.detachChild(placedSprites[c][r]);
                        scene.unregisterTouchArea(placedSprites[c][r]);
                        placedSprites[c][r] = null;
                    }
                }
            }
        }

        this.inventory.removeUpdateListener(this);
        this.bank.removeUpdateListener(this);

        // Delete DB records
        Market.getInstance().clear();
        Bank.getInstance().clear();
        InventoryItem.objects(PhoeniciaContext.context).filter(this.session.filter).delete(PhoeniciaContext.context);
        DefaultTile.objects(PhoeniciaContext.context).filter(this.session.filter).delete(PhoeniciaContext.context);
        LetterBuilder.objects(PhoeniciaContext.context).filter(this.session.filter).delete(PhoeniciaContext.context);
        LetterTile.objects(PhoeniciaContext.context).filter(this.session.filter).delete(PhoeniciaContext.context);
        WordBuilder.objects(PhoeniciaContext.context).filter(this.session.filter).delete(PhoeniciaContext.context);
        WordTileBuilder.objects(PhoeniciaContext.context).filter(this.session.filter).delete(PhoeniciaContext.context);
        WordTile.objects(PhoeniciaContext.context).filter(this.session.filter).delete(PhoeniciaContext.context);
        GameTile.objects(PhoeniciaContext.context).filter(this.session.session_filter).delete(PhoeniciaContext.context);
        GameTileBuilder.objects(PhoeniciaContext.context).filter(this.session.filter).delete(PhoeniciaContext.context);
        GameTileTimer.objects(PhoeniciaContext.context).filter(this.session.filter).delete(PhoeniciaContext.context);
        DecorationTile.objects(PhoeniciaContext.context).filter(this.session.session_filter).delete(PhoeniciaContext.context);

        this.inventory.addUpdateListener(this);
        this.bank.addUpdateListener(this);

        this.session.reset();
        this.session.save(PhoeniciaContext.context);
        this.current_level = this.locale.levels.get(0).name;
        this.createInventoryTile();
        this.createMarketTile();
        this.createWorkshopTile();
        this.changeLevel(this.locale.levels.get(0));

    }

    private void createInventoryTile() {
        // Create Inventory tile
        Debug.d("Creating DefaultTile for Inventory");
        final DefaultTile inventoryDefaultTile = new DefaultTile();
        inventoryDefaultTile.phoeniciaGame = this;
        inventoryDefaultTile.item_type.set("inventory");
        inventoryDefaultTile.game.set(this.session);
        inventoryDefaultTile.isoX.set(locale.inventoryBlock.mapCol);
        inventoryDefaultTile.isoY.set(locale.inventoryBlock.mapRow);
        this.createInventorySprite(inventoryDefaultTile);
        this.startCenterX = inventoryDefaultTile.sprite.getX();
        this.startCenterY = inventoryDefaultTile.sprite.getY();
        inventoryDefaultTile.save(PhoeniciaContext.context);
    }

    private void createInventorySprite(DefaultTile inventoryDefaultTile) {
        final TMXLayer tmxLayer = this.mTMXTiledMap.getTMXLayers().get(0);

        final TMXTile inventoryTile = tmxLayer.getTMXTile(inventoryDefaultTile.isoX.get(), inventoryDefaultTile.isoY.get());

        int[] tileSize = GameTextures.calculateTileSize(locale.inventoryBlock.columns, locale.inventoryBlock.rows, locale.inventoryBlock.height);
        float[] tilePos = GameTextures.calculateTilePosition(inventoryTile, tileSize, locale.inventoryBlock.columns, locale.inventoryBlock.rows);
        int inventoryZ = inventoryTile.getTileZ();

        Debug.d("Creating Sprite for Inventory");
        final MapBlockSprite inventorySprite = new MapBlockSprite(this, tilePos[0], tilePos[1], 4, inventoryTiles, PhoeniciaContext.vboManager);
        inventorySprite.setZIndex(inventoryZ);

        scene.attachChild(inventorySprite);

        for (int c = 0; c < locale.inventoryBlock.columns; c++) {
            for (int r = 0; r < locale.inventoryBlock.rows; r++) {
                placedSprites[inventoryDefaultTile.isoX.get()-c][inventoryDefaultTile.isoY.get()-r] = inventorySprite;
            }
        }
        inventorySprite.setOnClickListener(inventoryDefaultTile);
        inventorySprite.animate();
        scene.registerTouchArea(inventorySprite);

        inventoryDefaultTile.setSprite(inventorySprite);
    }

    private void createMarketTile() {
        // Create Market tile
        Debug.d("Creating DefaultTile for Market");
        final DefaultTile marketDefaultTile = new DefaultTile();
        marketDefaultTile.phoeniciaGame = this;
        marketDefaultTile.item_type.set("market");
        marketDefaultTile.game.set(this.session);
        marketDefaultTile.isoX.set(locale.marketBlock.mapCol);
        marketDefaultTile.isoY.set(locale.marketBlock.mapRow);
        this.createMarketSprite(marketDefaultTile);
        marketDefaultTile.save(PhoeniciaContext.context);
    }

    private void createMarketSprite(DefaultTile marketDefaultTile) {
        final TMXLayer tmxLayer = this.mTMXTiledMap.getTMXLayers().get(0);

        final TMXTile marketTile = tmxLayer.getTMXTile(marketDefaultTile.isoX.get(), marketDefaultTile.isoY.get());

        int[] tileSize = GameTextures.calculateTileSize(locale.marketBlock.columns, locale.marketBlock.rows, locale.marketBlock.height);
        float[] tilePos = GameTextures.calculateTilePosition(marketTile, tileSize, locale.marketBlock.columns, locale.marketBlock.rows);
        int marketZ = marketTile.getTileZ();

        Debug.d("Creating Sprite for market");
        final MapBlockSprite marketSprite = new MapBlockSprite(this, tilePos[0], tilePos[1], 4, marketTiles, PhoeniciaContext.vboManager);
        marketSprite.setZIndex(marketZ);

        scene.attachChild(marketSprite);

        for (int c = 0; c < locale.marketBlock.columns; c++) {
            for (int r = 0; r < locale.marketBlock.rows; r++) {
                placedSprites[marketDefaultTile.isoX.get()-c][marketDefaultTile.isoY.get()-r] = marketSprite;
            }
        }
        marketSprite.setOnClickListener(marketDefaultTile);
        marketSprite.animate();
        scene.registerTouchArea(marketSprite);

        marketDefaultTile.setSprite(marketSprite);

        scene.sortChildren();
    }

    private void createWorkshopTile() {
        // Create Workshop tile
        Debug.d("Creating DefaultTile for Workshop");
        final DefaultTile workshopDefaultTile = new DefaultTile();
        workshopDefaultTile.phoeniciaGame = this;
        workshopDefaultTile.item_type.set("workshop");
        workshopDefaultTile.game.set(this.session);
        workshopDefaultTile.isoX.set(locale.workshopBlock.mapCol);
        workshopDefaultTile.isoY.set(locale.workshopBlock.mapRow);
        this.createWorkshopSprite(workshopDefaultTile);
        workshopDefaultTile.save(PhoeniciaContext.context);
    }

    private void createWorkshopSprite(DefaultTile workshopDefaultTile) {
        final TMXLayer tmxLayer = this.mTMXTiledMap.getTMXLayers().get(0);

        final TMXTile workshopTile = tmxLayer.getTMXTile(workshopDefaultTile.isoX.get(), workshopDefaultTile.isoY.get());

        int[] tileSize = GameTextures.calculateTileSize(locale.workshopBlock.columns, locale.workshopBlock.rows, locale.workshopBlock.height);
        float[] tilePos = GameTextures.calculateTilePosition(workshopTile, tileSize, locale.workshopBlock.columns, locale.workshopBlock.rows);
        int workshopZ = workshopTile.getTileZ();

        Debug.d("Creating Sprite for workshop");
        final MapBlockSprite workshopSprite = new MapBlockSprite(this, tilePos[0], tilePos[1], 4, workshopTiles, PhoeniciaContext.vboManager);
        workshopSprite.setZIndex(workshopZ);

        scene.attachChild(workshopSprite);

        for (int c = 0; c < locale.workshopBlock.columns; c++) {
            for (int r = 0; r < locale.workshopBlock.rows; r++) {
                placedSprites[workshopDefaultTile.isoX.get()-c][workshopDefaultTile.isoY.get()-r] = workshopSprite;
            }
        }
        workshopSprite.setOnClickListener(workshopDefaultTile);
        workshopSprite.animate();
        scene.registerTouchArea(workshopSprite);

        workshopDefaultTile.setSprite(workshopSprite);

        scene.sortChildren();
    }

    /**
     * Start playing the game
     */
    public void start() {
        this.camera.setCenter(this.startCenterX, this.startCenterY);
        this.camera.setZoomFactor(2.0f);
        this.camera.setHUD(this.hudManager);
        this.hudManager.showDefault();
        if (this.music != null) {
            this.music.play();
        }

        double timediff = (double)System.currentTimeMillis() - session.last_timestamp.get();
        Debug.d("Restarting after " + (timediff / 1000) + " seconds");
        this.onUpdate((float) timediff / 1000);

        this.session.update();
        this.isStarted = true;
        this.isRunning = true;

        if (this.current_level == null || this.current_level == "") {
            this.changeLevel( this.locale.levels.get(0));
        } else if (this.current_level != this.session.current_level.get()) {
            this.changeLevel(this.locale.level_map.get(this.session.current_level.get()));
        }

    }

    public void pause() {
        if (!isStarted) return;
        this.isRunning = false;
        if (this.music != null) {
            this.music.pause();
        }
        this.saveBuilders();
        session.last_timestamp.set((double) this.currentTime);
        session.save(PhoeniciaContext.context);
    }

    public void resume() {
        if (!isStarted) return;

        double timediff = (double)System.currentTimeMillis() - session.last_timestamp.get();
        Debug.d("Resume after " + (timediff / 1000) + " seconds");
        this.onUpdate((float) timediff / 1000);

        this.isRunning = true;
        if (this.music != null) {
            this.music.resume();
        }
    }

    /**
     * Reset the game session's state, including inventory, account balance, and blocks placed on
     * the map
     */
    public void reset() {
        // IUpdateHandler.reset
    }

    /**
     * Update the game's builders
     * @param v
     */
    public void onUpdate(float v) {
        // update build queues
        this.hudManager.update(v);
        this.currentTime = System.currentTimeMillis();
        this.updateTime += v;
        if (this.updateTime > 1) {
            // Because this.builders might be modified based on updates to other builders
            // work from a copy of the set as it exists now
            final Set<Builder> updateBuilders = new HashSet<Builder>(this.builders);
            for (Builder builder : updateBuilders) {
                if (builder.status.get() == Builder.BUILDING) {
                    builder.update(this.updateTime);
                    //builder.save(PhoeniciaContext.context);
                    //Debug.d("Builder "+builder.item_name.get()+" saved");
                }
            }
            this.updateTime = 0;

        }
        // Save things every 5 minutes in case of a crash or force quit without pause
        if (this.currentTime - session.last_timestamp.get() > (5 * 60 * 1000)) { // timestamps are in milliseconds
            this.saveBuilders();
            session.last_timestamp.set((double) this.currentTime);
            session.save(PhoeniciaContext.context);

        }
    }

    private void saveBuilders() {
        // save status of all builders
        for (Builder builder : builders) {
            builder.save(PhoeniciaContext.context);
        }
    }
    /**
     * Add an new Builder instance to the list of builders updated every second
     * @param builder to be added
     */
    public void addBuilder(Builder builder) {
        this.builders.remove(builder);
        this.builders.add(builder);
    }

    /**
     * Remove a Builder instance from the list of builders updated every second
     * @param builder to be removed
     */
    public void removeBuilder(Builder builder) {
        this.builders.remove(builder);
    }

    /**
     * Handle Android back button presses by popping the current HUD off the stack
     */
    public void onBackPressed() {
        Debug.d("Back button pressed");
        this.activity.runOnUpdateThread(new Runnable() {
            @Override
            public void run() {
                hudManager.pop();
            }
        });

    }

    /**
     * Create a new sprite for the given LetterTile without waiting for user confirmation
     * @param tile source for the PlacedBlockSprite to create
     */
    public void createLetterSprite(LetterTile tile) {
        this.createLetterSprite(tile, null);
    }

    /**
     * Create a new sprite for the given LetterTile, waiting for user confirmation
     * @param tile source for the PlacedBlockSprite to create
     * @param callback handler to inform the calling code if the user completes or cancels placement
     */
    public void createLetterSprite(final LetterTile tile, final CreateLetterSpriteCallback callback) {
        final TMXLayer tmxLayer = this.mTMXTiledMap.getTMXLayers().get(0);
        final TMXTile tmxTile = tmxLayer.getTMXTile(tile.isoX.get(), tile.isoY.get());
        if (tmxTile == null) {
            Debug.d("Can't place blocks outside of map");
            return;
        }

        int[] tileSize = GameTextures.calculateTileSize(tile.letter.columns, tile.letter.rows, tile.letter.height);
        float[] tilePos = GameTextures.calculateTilePosition(tmxTile, tileSize, tile.letter.columns, tile.letter.rows);
        int tileZ = tmxTile.getTileZ();

        for (int c = 0; c < tile.letter.columns; c++) {
            for (int r = 0; r < tile.letter.rows; r++) {
                if (placedSprites[tmxTile.getTileColumn()-c][tmxTile.getTileRow()-r] != null && callback == null) {
                    Debug.d("Can't place blocks over existing blocks");
                    return;
                }
            }
        }

        Debug.d("Creating LetterSprite for "+tile.letter.name+" at "+tilePos[0]+"x"+tilePos[1]);
        final PlacedBlockSprite sprite = new PlacedBlockSprite(this, tilePos[0], tilePos[1], tile.letter.time, 4, letterBlocks.get(tile.letter), PhoeniciaContext.vboManager);
        sprite.setZIndex(tileZ);

        final LetterBuilder builder = tile.getBuilder(PhoeniciaContext.context);

        scene.attachChild(sprite);
        scene.sortChildren();

        if (callback != null) {
            this.hudManager.push(new SpriteMoveHUD(this, tmxTile, sprite, tile.letter.columns, tile.letter.rows, tile.letter.restriction, new SpriteMoveHUD.SpriteMoveHandler() {
                @Override
                public void onSpriteMoveCanceled(MapBlockSprite pSprite) {
                    scene.detachChild(sprite);
                    callback.onLetterSpriteCreationFailed(tile);
                }

                @Override
                public void onSpriteMoveFinished(MapBlockSprite pSprite, TMXTile newlocation) {
                    tile.isoX.set(newlocation.getTileColumn());
                    tile.isoY.set(newlocation.getTileRow());
                    for (int c = 0; c < tile.letter.columns; c++) {
                        for (int r = 0; r < tile.letter.rows; r++) {
                            placedSprites[newlocation.getTileColumn()-c][newlocation.getTileRow()-r] = sprite;
                        }
                    }

                    if (builder != null) {
                        sprite.setProgress(builder.progress.get(), tile.letter.time);
                    }
                    tile.setSprite(sprite);
                    sprite.setOnClickListener(tile);
                    sprite.animate();
                    scene.registerTouchArea(sprite);
                    callback.onLetterSpriteCreated(tile);
                }
            }));
        } else {
            for (int c = 0; c < tile.letter.columns; c++) {
                for (int r = 0; r < tile.letter.rows; r++) {
                    placedSprites[tmxTile.getTileColumn()-c][tmxTile.getTileRow()-r] = sprite;
                }
            }

            if (builder != null) {
                sprite.setProgress(builder.progress.get(), tile.letter.time);
            }
            tile.setSprite(sprite);
            sprite.setOnClickListener(tile);
            sprite.animate();
            scene.registerTouchArea(sprite);
        }
    }

    public interface CreateLetterSpriteCallback {
        public void onLetterSpriteCreated(LetterTile tile);
        public void onLetterSpriteCreationFailed(LetterTile tile);
    }

    /**
     * Create a new sprite for the given WordTile, without waiting for user confirmation
     * @param tile source for the PlacedBlockSprite to create
     */
    public void createWordSprite(WordTile tile) {
        this.createWordSprite(tile, null);
    }

    /**
     * Create a new sprite for the given WordTile, waiting for user confirmation
     * @param tile source for the PlacedBlockSprite to create
     * @param callback handler to inform the calling code if the user completes or cancels placement
     */
    public void createWordSprite(final WordTile tile, final CreateWordSpriteCallback callback) {
        final TMXLayer tmxLayer = this.mTMXTiledMap.getTMXLayers().get(0);
        final TMXTile tmxTile = tmxLayer.getTMXTile(tile.isoX.get(), tile.isoY.get());
        if (tmxTile == null) {
            Debug.d("Can't place blocks outside of map");
            return;
        }

        int[] tileSize = GameTextures.calculateTileSize(tile.word.columns, tile.word.rows, tile.word.height);
        float[] tilePos = GameTextures.calculateTilePosition(tmxTile, tileSize, tile.word.columns, tile.word.rows);
        int tileZ = tmxTile.getTileZ();

        for (int c = 0; c < tile.word.columns; c++) {
            for (int r = 0; r < tile.word.rows; r++) {
                if (placedSprites[tmxTile.getTileColumn()-c][tmxTile.getTileRow()-r] != null && callback == null) {
                    Debug.d("Can't place blocks over existing blocks");
                    return;
                }
            }
        }

        Debug.d("Creating WordSprite for " + tile.word.name + " at " +tilePos[0]+"x"+tilePos[1]);
        final PlacedBlockSprite sprite = new PlacedBlockSprite(this, tilePos[0], tilePos[1], tile.word.construct, 4, wordBlocks.get(tile.word), PhoeniciaContext.vboManager);
        sprite.setZIndex(tileZ);

        final WordTileBuilder builder = tile.getBuilder(PhoeniciaContext.context);

        scene.attachChild(sprite);
        scene.sortChildren();

        if (callback != null) {
            this.hudManager.push(new SpriteMoveHUD(this, tmxTile, sprite, tile.word.columns, tile.word.rows, tile.word.restriction, new SpriteMoveHUD.SpriteMoveHandler() {
                @Override
                public void onSpriteMoveCanceled(MapBlockSprite sprite) {
                    scene.detachChild(sprite);
                    callback.onWordSpriteCreationFailed(tile);
                }

                @Override
                public void onSpriteMoveFinished(MapBlockSprite pSprite, TMXTile newlocation) {
                    tile.isoX.set(newlocation.getTileColumn());
                    tile.isoY.set(newlocation.getTileRow());
                    for (int c = 0; c < tile.word.columns; c++) {
                        for (int r = 0; r < tile.word.rows; r++) {
                            placedSprites[newlocation.getTileColumn()-c][newlocation.getTileRow()-r] = sprite;
                        }
                    }

                    if (builder != null) {
                        sprite.setProgress(builder.progress.get(), tile.word.construct);
                    }
                    tile.setSprite(sprite);
                    sprite.setOnClickListener(tile);
                    sprite.animate();
                    scene.registerTouchArea(sprite);
                    callback.onWordSpriteCreated(tile);
                }
            }));
        } else {
            for (int c = 0; c < tile.word.columns; c++) {
                for (int r = 0; r < tile.word.rows; r++) {
                    placedSprites[tmxTile.getTileColumn()-c][tmxTile.getTileRow()-r] = sprite;
                }
            }

            if (builder != null) {
                sprite.setProgress(builder.progress.get(), tile.word.construct);
            }
            tile.setSprite(sprite);
            sprite.setOnClickListener(tile);
            sprite.animate();
            scene.registerTouchArea(sprite);
        }
    }

    public interface CreateWordSpriteCallback {
        public void onWordSpriteCreated(WordTile tile);
        public void onWordSpriteCreationFailed(WordTile tile);
    }

    /**
     * Create a new sprite for the given WordTile, without waiting for user confirmation
     * @param tile source for the PlacedBlockSprite to create
     */
    public void createGameSprite(GameTile tile) {
        this.createGameSprite(tile, null);
    }

    /**
     * Create a new sprite for the given WordTile, waiting for user confirmation
     * @param tile source for the PlacedBlockSprite to create
     * @param callback handler to inform the calling code if the user completes or cancels placement
     */
    public void createGameSprite(final GameTile tile, final CreateGameSpriteCallback callback) {
        final TMXLayer tmxLayer = this.mTMXTiledMap.getTMXLayers().get(0);
        final TMXTile tmxTile = tmxLayer.getTMXTile(tile.isoX.get(), tile.isoY.get());
        if (tmxTile == null) {
            Debug.d("Can't place blocks outside of map");
            return;
        }

        int[] tileSize = GameTextures.calculateTileSize(tile.game.columns, tile.game.rows, tile.game.height);
        float[] tilePos = GameTextures.calculateTilePosition(tmxTile, tileSize, tile.game.columns, tile.game.rows);
        int tileZ = tmxTile.getTileZ();

        for (int c = 0; c < tile.game.columns; c++) {
            for (int r = 0; r < tile.game.rows; r++) {
                if (placedSprites[tmxTile.getTileColumn()-c][tmxTile.getTileRow()-r] != null && callback == null) {
                    Debug.d("Can't place blocks over existing blocks");
                    return;
                }
            }
        }

        Debug.d("Creating GameSprite for " + tile.game.name + " at " +tilePos[0]+"x"+tilePos[1]);
        final PlacedBlockSprite sprite = new PlacedBlockSprite(this, tilePos[0], tilePos[1], tile.game.construct, 4, gameBlocks.get(tile.game), PhoeniciaContext.vboManager);
        sprite.setZIndex(tileZ);

        final GameTileBuilder builder = tile.getBuilder(PhoeniciaContext.context);

        scene.attachChild(sprite);
        scene.sortChildren();

        if (callback != null) {
            this.hudManager.push(new SpriteMoveHUD(this, tmxTile, sprite, tile.game.columns, tile.game.rows, tile.game.restriction, new SpriteMoveHUD.SpriteMoveHandler() {
                @Override
                public void onSpriteMoveCanceled(MapBlockSprite sprite) {
                    scene.detachChild(sprite);
                    callback.onGameSpriteCreationFailed(tile);
                }

                @Override
                public void onSpriteMoveFinished(MapBlockSprite pSprite, TMXTile newlocation) {
                    tile.isoX.set(newlocation.getTileColumn());
                    tile.isoY.set(newlocation.getTileRow());
                    for (int c = 0; c < tile.game.columns; c++) {
                        for (int r = 0; r < tile.game.rows; r++) {
                            placedSprites[newlocation.getTileColumn()-c][newlocation.getTileRow()-r] = sprite;
                        }
                    }

                    if (builder != null) {
                        sprite.setProgress(builder.progress.get(), tile.game.construct);
                    }
                    tile.setSprite(sprite);
                    sprite.setOnClickListener(tile);
                    sprite.animate();
                    scene.registerTouchArea(sprite);
                    callback.onGameSpriteCreated(tile);
                }
            }));
        } else {
            for (int c = 0; c < tile.game.columns; c++) {
                for (int r = 0; r < tile.game.rows; r++) {
                    placedSprites[tmxTile.getTileColumn()-c][tmxTile.getTileRow()-r] = sprite;
                }
            }

            if (builder != null) {
                sprite.setProgress(builder.progress.get(), tile.game.construct);
            }
            tile.setSprite(sprite);
            sprite.setOnClickListener(tile);
            sprite.animate();
            scene.registerTouchArea(sprite);
        }
    }

    public interface CreateGameSpriteCallback {
        public void onGameSpriteCreated(GameTile tile);
        public void onGameSpriteCreationFailed(GameTile tile);
    }

    /**
     * Create a new sprite for the given WordTile, without waiting for user confirmation
     * @param tile source for the PlacedBlockSprite to create
     */
    public void createDecorationSprite(DecorationTile tile) {
        this.createDecorationSprite(tile, null);
    }

    /**
     * Create a new sprite for the given WordTile, waiting for user confirmation
     * @param tile source for the PlacedBlockSprite to create
     * @param callback handler to inform the calling code if the user completes or cancels placement
     */
    public void createDecorationSprite(final DecorationTile tile, final CreateDecorationSpriteCallback callback) {
        final TMXLayer tmxLayer = this.mTMXTiledMap.getTMXLayers().get(0);
        final TMXTile tmxTile = tmxLayer.getTMXTile(tile.isoX.get(), tile.isoY.get());
        if (tmxTile == null) {
            Debug.d("Can't place blocks outside of map");
            return;
        }

        int[] tileSize = GameTextures.calculateTileSize(tile.decoration.columns, tile.decoration.rows, tile.decoration.height);
        float[] tilePos = GameTextures.calculateTilePosition(tmxTile, tileSize, tile.decoration.columns, tile.decoration.rows);
        int tileZ = tmxTile.getTileZ();

        for (int c = 0; c < tile.decoration.columns; c++) {
            for (int r = 0; r < tile.decoration.rows; r++) {
                if (placedSprites[tmxTile.getTileColumn()-c][tmxTile.getTileRow()-r] != null && callback == null) {
                    Debug.d("Can't place blocks over existing blocks");
                    return;
                }
            }
        }

        Debug.d("Creating Decoration sprite for " + tile.decoration.name + " at " + tilePos[0] + "x" + tilePos[1]);
        final PlacedBlockSprite sprite = new PlacedBlockSprite(this, tilePos[0], tilePos[1], 0, 4, decorationBlocks.get(tile.decoration), PhoeniciaContext.vboManager);
        sprite.setZIndex(tileZ);

        scene.attachChild(sprite);
        scene.sortChildren();

        if (callback != null) {
            this.hudManager.push(new SpriteMoveHUD(this, tmxTile, sprite, tile.decoration.columns, tile.decoration.rows, tile.decoration.restriction, new SpriteMoveHUD.SpriteMoveHandler() {
                @Override
                public void onSpriteMoveCanceled(MapBlockSprite sprite) {
                    scene.detachChild(sprite);
                    callback.onDecorationSpriteCreationFailed(tile);
                }

                @Override
                public void onSpriteMoveFinished(MapBlockSprite pSprite, TMXTile newlocation) {
                    tile.isoX.set(newlocation.getTileColumn());
                    tile.isoY.set(newlocation.getTileRow());
                    for (int c = 0; c < tile.decoration.columns; c++) {
                        for (int r = 0; r < tile.decoration.rows; r++) {
                            placedSprites[newlocation.getTileColumn()-c][newlocation.getTileRow()-r] = sprite;
                        }
                    }

                    tile.setSprite(sprite);
                    sprite.setOnClickListener(tile);
                    sprite.animate();
                    scene.registerTouchArea(sprite);
                    callback.onDecorationSpriteCreated(tile);
                }
            }));
        } else {
            for (int c = 0; c < tile.decoration.columns; c++) {
                for (int r = 0; r < tile.decoration.rows; r++) {
                    placedSprites[tmxTile.getTileColumn()-c][tmxTile.getTileRow()-r] = sprite;
                }
            }

            tile.setSprite(sprite);
            sprite.setOnClickListener(tile);
            sprite.animate();
            scene.registerTouchArea(sprite);
        }
    }

    public interface CreateDecorationSpriteCallback {
        public void onDecorationSpriteCreated(DecorationTile tile);
        public void onDecorationSpriteCreationFailed(DecorationTile tile);
    }

    /**
     * Find the ISO map tile under the given Scene coordinates.
     * @param x
     * @param y
     * @return the associated map tile under the given coordinates, or null if none is found
     */
    public TMXTile getTileAt(float x, float y) {
        final TMXLayer tmxLayer = this.mTMXTiledMap.getTMXLayers().get(0);
        return tmxLayer.getTMXTileAt(x, y);
    }

    public TMXTile getTileAtIso(int isoX, int isoY) {
        final TMXLayer tmxLayer = this.mTMXTiledMap.getTMXLayers().get(0);
        return tmxLayer.getTMXTile(isoX, isoY);
    }

    /**
     * Check if the player has placed a tile item at the given map tile location.
     * @param tmxTile
     * @return
     */
    public boolean hasTileAt(TMXTile tmxTile) {
        return this.hasTileAt(tmxTile.getTileColumn(), tmxTile.getTileRow());
    }

    /**
     * Check if the player has placed a tile at the given isometric location on the map..
     * @param isoX
     * @param isoY
     * @return
     */
    public boolean hasTileAt(int isoX, int isoY) {
        if (placedSprites[isoX][isoY] != null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Get a player-placed sprite at the given map tile location
     * @param tmxTile
     * @return
     */
    public Sprite getSpriteAt(TMXTile tmxTile) {
        return this.getSpriteAt(tmxTile.getTileColumn(), tmxTile.getTileRow());
    }

    /**
     * Get a player-placed sprite at the given isometric location on the map.
     * @param isoX
     * @param isoY
     * @return
     */
    public Sprite getSpriteAt(int isoX, int isoY) {
        return placedSprites[isoX][isoY];
    }

    /**
     * Play the given audio file
     * @param soundFile
     */
    public void playBlockSound(String soundFile) {

        Debug.d("Playing sound: " + soundFile);
        if (!this.isStarted || !this.isRunning) {
            Debug.d("Game hasn't started yet, not playing any sound");
            return;
        }
        if (this.blockSounds.containsKey(soundFile)) {
            this.blockSounds.get(soundFile).play();
        } else {
            Debug.d("No blockSounds: " + soundFile + "");
        }
    }

    /**
     * Play the given audio file
     * @param soundFile
     */
    public void playLevelSound(String soundFile, MediaPlayer.OnCompletionListener callback) {

        Debug.d("Playing sound: "+soundFile);
        if (!this.isStarted || !this.isRunning) {
            Debug.d("Game hasn't started yet, not playing any sound");
            return;
        }
        if (this.levelSounds.containsKey(soundFile)) {
            Music sound = this.levelSounds.get(soundFile);
            sound.setOnCompletionListener(callback);
            sound.play();
        } else {
            Debug.d("No levelSounds: " + soundFile + "");
        }
    }

    private void checkLevelRequirements() {
        Level current = this.locale.level_map.get(current_level);
        while (current.check(PhoeniciaContext.context) && current.next != null) {
            this.changeLevel(current.next);
            current = current.next;
        }
    }
    @Override
    public void onInventoryUpdated(InventoryItem[] item) {
        this.checkLevelRequirements();
    }

    @Override
    public void onBankAccountUpdated(int new_balance) {
        this.checkLevelRequirements();
    }

    @Override
    public void onExperienceChanged(int new_xp) {
        this.checkLevelRequirements();
    }

    @Override
    public void onAssetsUpdated() { this.checkLevelRequirements(); }

    /**
     * Called whenever the player advances to the next level.
     * @param next
     */
    public void changeLevel(Level next) {
        this.current_level = next.name;
        this.session.current_level.set(current_level);
        Bank.getInstance().credit(next.coinsEarned);
        this.session.addExperience(next.pointsEarned);
        this.session.save(PhoeniciaContext.context);

        Debug.d("Level changed to: " + current_level);
        for (int i = 0; i < this.levelListeners.size(); i++) {
            Debug.d("Calling update listener: "+this.levelListeners.get(i).getClass());
            this.levelListeners.get(i).onLevelChanged(next);
        }

        // Show what's new only for levels after the first one
        if (this.locale.levels.indexOf(next) >= 1) {
            this.hudManager.showNewLevel(next);
        } else {
            this.hudManager.showLevelIntro(next);
        }
        return;
    }

    public void addLevelListener(LevelChangeListener listener) {
        this.levelListeners.add(listener);
    }
    public void removeLevelListener(LevelChangeListener listener) {
        this.levelListeners.remove(listener);
    }
    public interface LevelChangeListener {
        public void onLevelChanged(Level newLevel);
    }
}
