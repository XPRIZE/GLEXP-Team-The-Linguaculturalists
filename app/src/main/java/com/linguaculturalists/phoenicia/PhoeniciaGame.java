package com.linguaculturalists.phoenicia;

import android.graphics.Typeface;
import android.view.MotionEvent;

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
import org.andengine.opengl.texture.bitmap.AssetBitmapTexture;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.opengl.texture.region.TextureRegionFactory;
import org.andengine.util.adt.color.Color;
import org.andengine.extension.tmx.TMXLoader;
import org.andengine.extension.tmx.TMXTile;
import org.andengine.extension.tmx.TMXTiledMap;
import org.andengine.extension.tmx.util.exception.TMXLoadException;
import org.andengine.util.debug.Debug;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.linguaculturalists.phoenicia.components.MapBlockSprite;
import com.linguaculturalists.phoenicia.components.PlacedBlockSprite;
import com.linguaculturalists.phoenicia.locale.IntroPage;
import com.linguaculturalists.phoenicia.locale.Letter;
import com.linguaculturalists.phoenicia.locale.Level;
import com.linguaculturalists.phoenicia.locale.Locale;
import com.linguaculturalists.phoenicia.locale.Word;
import com.linguaculturalists.phoenicia.models.Bank;
import com.linguaculturalists.phoenicia.models.Builder;
import com.linguaculturalists.phoenicia.models.DefaultTile;
import com.linguaculturalists.phoenicia.models.LetterBuilder;
import com.linguaculturalists.phoenicia.models.GameSession;
import com.linguaculturalists.phoenicia.models.Inventory;
import com.linguaculturalists.phoenicia.models.InventoryItem;
import com.linguaculturalists.phoenicia.models.LetterTile;
import com.linguaculturalists.phoenicia.models.WordBuilder;
import com.linguaculturalists.phoenicia.models.WordTile;
import com.linguaculturalists.phoenicia.ui.HUDManager;
import com.linguaculturalists.phoenicia.ui.SpriteMoveHUD;
import com.linguaculturalists.phoenicia.locale.LocaleLoader;
import com.linguaculturalists.phoenicia.util.PhoeniciaContext;
import com.orm.androrm.DatabaseAdapter;
import com.orm.androrm.Filter;
import com.orm.androrm.Model;

/**
 * The main class for managing a game.
 */
public class PhoeniciaGame implements IUpdateHandler, Inventory.InventoryUpdateListener, Bank.BankUpdateListener {
    public static final int LETTER_TEXTURE_COLS = 4;
    public static final int LETTER_TEXTURE_ROWS = 6;
    public static final int LETTER_TILE_WIDTH = 64;
    public static final int LETTER_TILE_HEIGHT = 64;
    public static final int WORD_TEXTURE_COLS = 4;
    public static final int WORD_TEXTURE_ROWS = 6;
    public static final int WORD_TILE_WIDTH = 64;
    public static final int WORD_TILE_HEIGHT = 64;
    public Locale locale; /**< the locale used by the game session */
    public Scene scene;
    public ZoomCamera camera;
    private float startCenterX;
    private float startCenterY;
    private GameActivity activity;

    private float mPinchZoomStartedCameraZoomFactor;
    private PinchZoomDetector mPinchZoomDetector;

    public ITexture fontTexture;
    public Font defaultFont;

    public AssetBitmapTexture shellTexture;
    public ITiledTextureRegion shellTiles; /**< Tile regions for building the game shell */

    public AssetBitmapTexture inventoryTexture;
    public ITiledTextureRegion inventoryTiles; /**< Tile regions for the inventory block */

    public AssetBitmapTexture marketTexture;
    public ITiledTextureRegion marketTiles; /**< Tile regions for the market block */

    public Map<Letter, AssetBitmapTexture> letterTextures;
    public Map<Letter, ITiledTextureRegion> letterTiles; /**< Tile regions depicting letters */

    public Map<Word, AssetBitmapTexture> wordTextures;
    public Map<Word, ITiledTextureRegion> wordTiles; /**< Tile regions depicting words */

    public Sprite[][] placedSprites; /**< active map tiles arranged according to the ISO map grid */
    public String[][] mapRestrictions; /**< map tile class types arranged according to the ISO map grid */
    public Map<Integer, String> mapTileClass;
    private Letter placeLetter;
    private Word placeWord;

    private TMXTiledMap mTMXTiledMap;

    private Map<String, Sound> blockSounds;

    public HUDManager hudManager; /**< The HUD stack manager for this game */
    public Inventory inventory; /**< The Inventory manager for this game */
    public Bank bank; /**< The Bank account manager for this game */

    public GameSession session; /**< The saved GameSession being run */
    //public Filter sessionFilter; /**< AndrOrm query filter to limit results to this GameSession */
    private Set<Builder> builders;
    private float updateTime;

    public String current_level = ""; /**< The current level the player has reached */
    private List<LevelChangeListener> levelListeners;

    public PhoeniciaGame(GameActivity activity, final ZoomCamera camera) {
        FontFactory.setAssetBasePath("fonts/");

        // Prime the static context utility
        PhoeniciaContext.activity = activity;
        PhoeniciaContext.context = activity.getApplicationContext();
        PhoeniciaContext.textureManager = activity.getTextureManager();
        PhoeniciaContext.assetManager = activity.getAssets();
        PhoeniciaContext.vboManager = activity.getVertexBufferObjectManager();
        PhoeniciaContext.soundManager = activity.getSoundManager();
        PhoeniciaContext.fontManager = activity.getFontManager();

        this.activity = activity;
        this.camera = camera;

        this.levelListeners = new ArrayList<LevelChangeListener>();

        scene = new Scene();
        scene.setBackground(new Background(new Color(0, 0, 0)));

        this.blockSounds = new HashMap<String, Sound>();
        this.builders = new HashSet<Builder>();
        this.updateTime = 0;

        this.letterTextures = new HashMap<Letter, AssetBitmapTexture>();
        this.letterTiles = new HashMap<Letter, ITiledTextureRegion>();

        this.wordTextures = new HashMap<Word, AssetBitmapTexture>();
        this.wordTiles = new HashMap<Word, ITiledTextureRegion>();

        final float minZoomFactor = 1.0f;
        final float maxZoomFactor = 5.0f;
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
                                int calcX = (int) motion.getHistoricalX(i) - (int) motion.getHistoricalX(i - 1);
                                int calcY = (int) motion.getHistoricalY(i) - (int) motion.getHistoricalY(i - 1);
                                //Debug.d("diffX: "+calcX+", diffY: "+calcY);
                                final float zoom = camera.getZoomFactor();

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
    public void load() throws IOException {
        this.syncDB();

        // Load locale pack
        final String locale_pack_manifest = "locales/en_us_rural/manifest.xml";
        LocaleLoader localeLoader = new LocaleLoader();
        try {
            this.locale = localeLoader.load(PhoeniciaContext.assetManager.open(locale_pack_manifest));
            Debug.d("Locale map: "+locale.map_src);
        } catch (final IOException e) {
            Debug.e("Error loading Locale from "+locale_pack_manifest, e);
        }

        // Load phoeniciaGame session
        try {
            this.session = GameSession.objects(PhoeniciaContext.context).all().toList().get(0);
            this.current_level = this.session.current_level.get();
        } catch (IndexOutOfBoundsException e) {
            this.session = GameSession.start(this.locale);
        }
        this.session.save(PhoeniciaContext.context);

        // Start the Inventory for this session
        Inventory.init(this.session);
        this.inventory = Inventory.getInstance();
        this.inventory.addUpdateListener(this);

        // Start the Bank for this session
        Bank.init(this.session);
        this.bank = Bank.getInstance();
        this.bank.addUpdateListener(this);

        // Load font assets
        this.defaultFont = FontFactory.create(PhoeniciaContext.fontManager, PhoeniciaContext.textureManager, 256, 256, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 32, Color.RED_ARGB_PACKED_INT);
        this.defaultFont.load();

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
                                        Debug.d("Found map restriction '"+prop.getValue()+"' at "+r+"x"+c);
                                        mapTileClass.put(tile.getGlobalTileID(), prop.getValue());
                                    }
                                }
                            } catch (IllegalArgumentException e) {
                                // Do nothing, it just means there are no properties for this tile
                            }
                        }
                        mapRestrictions[r][c] = mapTileClass.get(tile.getGlobalTileID());
                    }
                }
            }

            // Initiate array for holding references to block sprites on the map
            placedSprites = new Sprite[this.mTMXTiledMap.getTileColumns()][this.mTMXTiledMap.getTileRows()];

            // Lock the camera to the map's boundaries
            TMXLayer baseLayer = this.mTMXTiledMap.getTMXLayers().get(0);
            this.camera.setBoundsEnabled(true);
            this.camera.setBounds((-baseLayer.getWidth()/2)+32, -baseLayer.getHeight(), (baseLayer.getWidth()/2)+32, 32);
        } catch (final TMXLoadException e) {
            Debug.e("Error loading map at "+this.locale.map_src, e);
        }

        try {
            shellTexture = new AssetBitmapTexture(PhoeniciaContext.textureManager, PhoeniciaContext.assetManager, this.locale.shell_src);
            shellTexture.load();
            shellTiles = TextureRegionFactory.extractTiledFromTexture(shellTexture, 0, 0, 64 * 8, 64 * 8, 8, 8);

        } catch (final IOException e) {
            e.printStackTrace();
            throw e;
        }

        try {
            inventoryTexture = new AssetBitmapTexture(PhoeniciaContext.textureManager, PhoeniciaContext.assetManager, this.locale.inventoryBlock.texture_src);
            inventoryTexture.load();
            inventoryTiles = TextureRegionFactory.extractTiledFromTexture(inventoryTexture, 0, 0, 64 * 8, 64 * 8, 8, 8);

            marketTexture = new AssetBitmapTexture(PhoeniciaContext.textureManager, PhoeniciaContext.assetManager, this.locale.marketBlock.texture_src);
            marketTexture.load();
            marketTiles = TextureRegionFactory.extractTiledFromTexture(marketTexture, 0, 0, 64 * 8, 64 * 8, 8, 8);

        } catch (final IOException e) {
            e.printStackTrace();
            throw e;
        }

        List<Letter> blockLetters = locale.letters;
        List<Word> blockWords = locale.words;
        SoundFactory.setAssetBasePath("locales/en_us_rural/");
        try {
            // Load letter assets
            for (int i = 0; i < blockLetters.size(); i++) {
                Letter letter = blockLetters.get(i);
                Debug.d("Loading letter texture from " + letter.texture_src);
                final AssetBitmapTexture letterTexture = new AssetBitmapTexture(PhoeniciaContext.textureManager, PhoeniciaContext.assetManager, letter.texture_src);
                letterTexture.load();
                this.letterTextures.put(letter, letterTexture);
                this.letterTiles.put(letter, TextureRegionFactory.extractTiledFromTexture(letterTexture, 0, 0, LETTER_TILE_WIDTH * LETTER_TEXTURE_COLS, LETTER_TILE_HEIGHT * LETTER_TEXTURE_ROWS, LETTER_TEXTURE_COLS, LETTER_TEXTURE_ROWS));
            }

            // Load word assets
            for (int i = 0; i < blockWords.size(); i++) {
                Word word = blockWords.get(i);
                Debug.d("Loading word texture from " + word.texture_src);
                final AssetBitmapTexture wordTexture = new AssetBitmapTexture(PhoeniciaContext.textureManager, PhoeniciaContext.assetManager, word.texture_src);
                wordTexture.load();
                this.wordTextures.put(word, wordTexture);
                this.wordTiles.put(word, TextureRegionFactory.extractTiledFromTexture(wordTexture, 0, 0, WORD_TILE_WIDTH * WORD_TEXTURE_COLS, WORD_TILE_HEIGHT * WORD_TEXTURE_ROWS, WORD_TEXTURE_COLS, WORD_TEXTURE_ROWS));
            }

        } catch (final IOException e)
        {
            e.printStackTrace();
            throw e;
        }

        // Load sound data
        try {
            SoundFactory.setAssetBasePath("locales/en_us_rural/");
            blockSounds = new HashMap<String, Sound>();
            Debug.d("Loading  letters");
            for (int i = 0; i < blockLetters.size(); i++) {
                Letter letter = blockLetters.get(i);
                Debug.d("Loading sound file "+i+": "+letter.sound);
                blockSounds.put(letter.sound, SoundFactory.createSoundFromAsset(PhoeniciaContext.soundManager, PhoeniciaContext.activity, letter.sound));
                blockSounds.put(letter.phoneme, SoundFactory.createSoundFromAsset(PhoeniciaContext.soundManager, PhoeniciaContext.activity, letter.phoneme));
            }
            Debug.d("Loading words");
            for (int i = 0; i < blockWords.size(); i++) {
                Word word = blockWords.get(i);
                Debug.d("Loading sound file "+i+": "+word.sound);
                blockSounds.put(word.sound, SoundFactory.createSoundFromAsset(PhoeniciaContext.soundManager, PhoeniciaContext.activity, word.sound));
            }
            // Load level assets
            // TODO: Loading all level intros now is wasteful, need to hack SoundFactory to take a callback when loading finishes
            Debug.d("Loading intros");
            for (int i = 0; i < locale.levels.size(); i++) {
                Level level = locale.levels.get(i);
                Debug.d("Loading intros for level " + level.name);
                for (int j = 0; j < level.intro.size(); j++) {
                    IntroPage page = level.intro.get(j);
                    Debug.d("Loading intro sound file " + page.sound);
                    try {
                        blockSounds.put(page.sound, SoundFactory.createSoundFromAsset(PhoeniciaContext.soundManager, PhoeniciaContext.activity, page.sound));
                    } catch (IOException e) {
                        Debug.w("Failed to load level intro sound: "+page.sound);
                    }
                }
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        Debug.d("Loading inventory tile");
        try {
            final Filter inventoryFilter = new Filter();
            inventoryFilter.is("item_type", "inventory");
            final DefaultTile inventoryDefaultTile = DefaultTile.objects(PhoeniciaContext.context).filter(inventoryFilter).toList().get(0);
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
            final DefaultTile marketDefaultTile = DefaultTile.objects(PhoeniciaContext.context).filter(marketFilter).toList().get(0);
            marketDefaultTile.phoeniciaGame = this;
            this.createMarketSprite(marketDefaultTile);
        } catch (IndexOutOfBoundsException e) {
            this.createMarketTile();
        }

        Debug.d("Loading letter tiles");
        List<LetterTile> letterTiles = LetterTile.objects(PhoeniciaContext.context).filter(session.filter).toList();
        for (int i = 0; i < letterTiles.size(); i++) {
            LetterTile letterTile = letterTiles.get(i);
            Debug.d("Restoring tile "+letterTile.item_name.get());
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
                builder.save(PhoeniciaContext.context);
                Debug.d("Found builder with "+builder.progress.get()+"/"+builder.time.get()+" and status "+builder.status.get());
            }
            this.addBuilder(builder);
            this.createLetterSprite(letterTile);
        }

        Debug.d("Loading word tiles");
        List<WordTile> wordTiles = WordTile.objects(PhoeniciaContext.context).filter(session.filter).toList();
        for (int i = 0; i < wordTiles.size(); i++) {
            WordTile wordTile = wordTiles.get(i);
            Debug.d("Restoring tile "+wordTile.item_name.get());
            wordTile.word = this.locale.word_map.get(wordTile.item_name.get());
            wordTile.phoeniciaGame = this;
            WordBuilder builder = wordTile.getBuilder(PhoeniciaContext.context);
            if (builder == null) {
                Debug.d("Adding new builder for tile "+wordTile.item_name.get());
                builder = new WordBuilder(this.session, wordTile, wordTile.item_name.get(), wordTile.word.construct);
                builder.save(PhoeniciaContext.context);
                wordTile.setBuilder(builder);
                wordTile.save(PhoeniciaContext.context);
                builder.start();
            } else {
                builder.time.set(wordTile.word.construct);
                builder.save(PhoeniciaContext.context);
                Debug.d("Found builder with "+builder.progress.get()+"/"+builder.time.get()+" and status "+builder.status.get());
            }
            this.addBuilder(builder);
            this.createWordSprite(wordTile);
        }

    }

    /**
     * Tell AndOrm about the Models that will be used to read and write to the database.
     */
    private void syncDB() {
        List<Class<? extends Model>> models = new ArrayList<Class<? extends Model>>();
        models.add(GameSession.class);
        models.add(InventoryItem.class);
        models.add(DefaultTile.class);
        models.add(LetterTile.class);
        models.add(WordTile.class);
        models.add(LetterBuilder.class);
        models.add(WordBuilder.class);

        DatabaseAdapter.setDatabaseName("game_db");
        DatabaseAdapter adapter = DatabaseAdapter.getInstance(PhoeniciaContext.context);
        adapter.setModels(models);
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
        // Delete DB records
        Inventory.getInstance().clear();
        Bank.getInstance().clear();
        DatabaseAdapter adapter = DatabaseAdapter.getInstance(PhoeniciaContext.context);
        adapter.drop();
        this.syncDB();
        this.session = GameSession.start(this.locale);
        this.session.save(PhoeniciaContext.context);
        this.current_level = this.session.current_level.get();
        this.createInventoryTile();
        this.createMarketTile();
        this.changeLevel(this.locale.level_map.get(this.current_level));

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
        inventoryDefaultTile.save(PhoeniciaContext.context);
    }

    private void createInventorySprite(DefaultTile inventoryDefaultTile) {
        final TMXLayer tmxLayer = this.mTMXTiledMap.getTMXLayers().get(0);

        final TMXTile inventoryTile = tmxLayer.getTMXTile(inventoryDefaultTile.isoX.get(), inventoryDefaultTile.isoY.get());

        int inventoryX = (int)inventoryTile.getTileX() + 32;// tiles are 64px wide and anchors in the center
        int inventoryY = (int)inventoryTile.getTileY() + 32;// tiles are 64px wide and anchors in the center
        int inventoryZ = inventoryTile.getTileZ();

        Debug.d("Creating Sprite for Inventory");
        final MapBlockSprite inventorySprite = new MapBlockSprite(inventoryX, inventoryY, 0, inventoryTiles, PhoeniciaContext.vboManager);
        inventorySprite.setZIndex(inventoryZ);

        scene.attachChild(inventorySprite);

        placedSprites[inventoryDefaultTile.isoX.get()][inventoryDefaultTile.isoY.get()] = inventorySprite;
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
        int marketX = (int)marketTile.getTileX() + 32;// tiles are 64px wide and anchors in the center
        int marketY = (int)marketTile.getTileY() + 32;// tiles are 64px wide and anchors in the center
        int marketZ = marketTile.getTileZ();

        Debug.d("Creating Sprite for market");
        final PlacedBlockSprite marketSprite = new PlacedBlockSprite(marketX, marketY, 0, 0, marketTiles, PhoeniciaContext.vboManager);
        marketSprite.setZIndex(marketZ);

        scene.attachChild(marketSprite);

        placedSprites[marketDefaultTile.isoX.get()][marketDefaultTile.isoY.get()] = marketSprite;
        marketSprite.setOnClickListener(marketDefaultTile);
        marketSprite.animate();
        scene.registerTouchArea(marketSprite);

        marketDefaultTile.setSprite(marketSprite);

        scene.sortChildren();
    }

    /**
     * Start playing the game
     */
    public void start() {
        this.session.update();
        this.camera.setCenter(this.startCenterX, this.startCenterY);
        this.camera.setZoomFactor(2.0f);
        this.camera.setHUD(this.hudManager);
        this.hudManager.showDefault();
        if (this.current_level != this.session.current_level.get()) {
            this.changeLevel(this.locale.level_map.get(this.session.current_level.get()));
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

        this.updateTime += v;
        if (this.updateTime > 1) {
            this.updateTime = 0;
            for (Builder builder : this.builders) {
                if (builder.status.get() == Builder.BUILDING) {
                    builder.update();
                    builder.save(PhoeniciaContext.context);
                    //Debug.d("Builder "+builder.item_name.get()+" saved");
                }
            }
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

        int tileX = (int)tmxTile.getTileX() + 32;// tiles are 64px wide and anchors in the center
        int tileY = (int)tmxTile.getTileY() + 32;// tiles are 64px wide and anchors in the center
        int tileZ = tmxTile.getTileZ();

        if (placedSprites[tmxTile.getTileColumn()][tmxTile.getTileRow()] != null && callback == null) {
            Debug.d("Can't place blocks over existing blocks");
            return;
        }

        Debug.d("Creating LetterSprite for "+tile.letter.name+" at "+tile+"x"+tileY);
        final PlacedBlockSprite sprite = new PlacedBlockSprite(tileX, tileY, tile.letter.time, 8, letterTiles.get(tile.letter), PhoeniciaContext.vboManager);
        sprite.setZIndex(tileZ);

        final LetterBuilder builder = tile.getBuilder(PhoeniciaContext.context);

        scene.attachChild(sprite);
        scene.sortChildren();

        if (callback != null) {
            this.hudManager.push(new SpriteMoveHUD(this, tmxTile, sprite, tile.letter.restriction, new SpriteMoveHUD.SpriteMoveHandler() {
                @Override
                public void onSpriteMoveCanceled(MapBlockSprite pSprite) {
                    scene.detachChild(sprite);
                    callback.onLetterSpriteCreationFailed(tile);
                }

                @Override
                public void onSpriteMoveFinished(MapBlockSprite pSprite, TMXTile newlocation) {
                    tile.isoX.set(newlocation.getTileColumn());
                    tile.isoY.set(newlocation.getTileRow());
                    placedSprites[newlocation.getTileColumn()][newlocation.getTileRow()] = sprite;

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
            placedSprites[tmxTile.getTileColumn()][tmxTile.getTileRow()] = sprite;

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

        int tileX = (int)tmxTile.getTileX() + 32;// tiles are 64px wide and anchors in the center
        int tileY = (int)tmxTile.getTileY() + 32;// tiles are 64px wide and anchors in the center
        int tileZ = tmxTile.getTileZ();

        if (placedSprites[tmxTile.getTileColumn()][tmxTile.getTileRow()] != null && callback == null) {
            Debug.d("Can't place blocks over existing blocks");
            return;
        }

        Debug.d("Creating WordSprite for " + tile.word.name + " at " +tile+"x"+tileY);
        final PlacedBlockSprite sprite = new PlacedBlockSprite(tileX, tileY, tile.word.construct, 8, wordTiles.get(tile.word), PhoeniciaContext.vboManager);
        sprite.setZIndex(tileZ);

        final WordBuilder builder = tile.getBuilder(PhoeniciaContext.context);

        scene.sortChildren();

        if (callback != null) {
            this.hudManager.push(new SpriteMoveHUD(this, tmxTile, sprite, tile.word.restriction, new SpriteMoveHUD.SpriteMoveHandler() {
                @Override
                public void onSpriteMoveCanceled(MapBlockSprite sprite) {
                    scene.detachChild(sprite);
                    callback.onWordSpriteCreationFailed(tile);
                }

                @Override
                public void onSpriteMoveFinished(MapBlockSprite pSprite, TMXTile newlocation) {
                    tile.isoX.set(newlocation.getTileColumn());
                    tile.isoY.set(newlocation.getTileRow());
                    placedSprites[newlocation.getTileColumn()][newlocation.getTileRow()] = sprite;

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
            placedSprites[tmxTile.getTileColumn()][tmxTile.getTileRow()] = sprite;

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

        Debug.d("Playing sound: "+soundFile);

        if (this.blockSounds.containsKey(soundFile)) {
            this.blockSounds.get(soundFile).play();
        } else {
            Debug.d("No blockSounds: " + soundFile + "");
        }
    }

    @Override
    public void onInventoryUpdated(InventoryItem[] item) {
        final Level current = this.locale.level_map.get(current_level);
        if (current.check(PhoeniciaContext.context) && this.locale.levels.size() > this.locale.levels.indexOf(current)+1) {
            final Level next = this.locale.levels.get(this.locale.levels.indexOf(current)+1);
            this.changeLevel(next);
        }
    }

    @Override
    public void onBankAccountUpdated(int new_balance) {
        // TODO: do something
    }

    /**
     * Called whenever the player advances to the next level.
     * @param next
     */
    public void changeLevel(Level next) {
        this.current_level = next.name;
        this.session.current_level.set(current_level);
        this.session.save(PhoeniciaContext.context);

        Debug.d("Level changed to: " + current_level);
        for (int i = 0; i < this.levelListeners.size(); i++) {
            Debug.d("Calling update listener: "+this.levelListeners.get(i).getClass());
            this.levelListeners.get(i).onLevelChanged(next);
        }
        this.hudManager.showLevelIntro(this.locale.level_map.get(current_level));
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
