package com.linguaculturalists.phoenicia;

import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.view.MotionEvent;

import org.andengine.audio.sound.Sound;
import org.andengine.audio.sound.SoundFactory;
import org.andengine.audio.sound.SoundManager;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.camera.ZoomCamera;
import org.andengine.engine.camera.hud.HUD;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.extension.tmx.TMXLayer;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.detector.PinchZoomDetector;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.TextureManager;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.bitmap.AssetBitmapTexture;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.opengl.texture.region.TextureRegionFactory;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
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

import com.linguaculturalists.phoenicia.components.PlacedBlockSprite;
import com.linguaculturalists.phoenicia.locale.Letter;
import com.linguaculturalists.phoenicia.locale.Level;
import com.linguaculturalists.phoenicia.locale.Word;
import com.linguaculturalists.phoenicia.models.BuildQueue;
import com.linguaculturalists.phoenicia.models.GameSession;
import com.linguaculturalists.phoenicia.models.Inventory;
import com.linguaculturalists.phoenicia.models.InventoryItem;
import com.linguaculturalists.phoenicia.models.LetterTile;
import com.linguaculturalists.phoenicia.models.PlacedBlock;
import com.linguaculturalists.phoenicia.ui.HUDManager;
import com.linguaculturalists.phoenicia.util.LocaleLoader;
import com.orm.androrm.DatabaseAdapter;
import com.orm.androrm.Filter;
import com.orm.androrm.Model;

/**
 * Created by mhall on 3/22/15.
 */
public class PhoeniciaGame implements IUpdateHandler, Inventory.InventoryUpdateListener {
    public Locale locale;
    public GameActivity activity;
    public TextureManager textureManager;
    public AssetManager assetManager;
    public VertexBufferObjectManager vboManager;
    public SoundManager soundManager;
    public Scene scene;
    public Camera camera;

    private float mPinchZoomStartedCameraZoomFactor;
    private PinchZoomDetector mPinchZoomDetector;

    public ITexture fontTexture;
    public Font defaultFont;

    public AssetBitmapTexture shellTexture;
    public ITiledTextureRegion shellTiles;

    // To be deprecated
    //public AssetBitmapTexture lettersTexture;
    //public ITiledTextureRegion letterTiles;

    public Map<Letter, AssetBitmapTexture> letterTextures;
    public Map<Letter, ITiledTextureRegion> letterTiles;

    // To be deprecated
    //public AssetBitmapTexture wordsTexture;
    //public ITiledTextureRegion wordTiles;

    public Map<Word, AssetBitmapTexture> wordTextures;
    public Map<Word, ITiledTextureRegion> wordTiles;

    public Sprite[][] placedSprites;
    private Letter placeLetter;
    private Word placeWord;

    private TMXTiledMap mTMXTiledMap;

    private Map<String, Sound> blockSounds;

    public HUDManager hudManager;
    public Inventory inventory;

    public GameSession session;
    public Filter sessionFilter;
    private Set<BuildQueue> builders;
    private float updateTime;

    public String current_level = "";
    private List<LevelChangeListener> levelListeners;

    public PhoeniciaGame(GameActivity activity, final ZoomCamera camera) {
        FontFactory.setAssetBasePath("fonts/");

        this.activity = activity;
        this.textureManager = activity.getTextureManager();
        this.assetManager = activity.getAssets();
        this.vboManager = activity.getVertexBufferObjectManager();
        this.soundManager = activity.getSoundManager();
        this.camera = camera;
        Inventory.init(this);
        Inventory.getInstance().addUpdateListener(this);

        this.levelListeners = new ArrayList<LevelChangeListener>();

        this.inventory = Inventory.getInstance();
        scene = new Scene();
        scene.setBackground(new Background(new Color(0, 0, 0)));

        this.blockSounds = new HashMap<String, Sound>();
        this.builders = new HashSet<BuildQueue>();
        this.updateTime = 0;

        this.letterTextures = new HashMap<Letter, AssetBitmapTexture>();
        this.letterTiles = new HashMap<Letter, ITiledTextureRegion>();

        this.wordTextures = new HashMap<Word, AssetBitmapTexture>();
        this.wordTiles = new HashMap<Word, ITiledTextureRegion>();

        mPinchZoomDetector = new PinchZoomDetector(new PinchZoomDetector.IPinchZoomDetectorListener() {
            @Override
            public void onPinchZoomStarted(final PinchZoomDetector pPinchZoomDetector, final TouchEvent pTouchEvent) {
                mPinchZoomStartedCameraZoomFactor = camera.getZoomFactor();
            }

            @Override
            public void onPinchZoom(final PinchZoomDetector pPinchZoomDetector, final TouchEvent pTouchEvent, final float pZoomFactor) {
                camera.setZoomFactor(mPinchZoomStartedCameraZoomFactor * pZoomFactor);
            }

            @Override
            public void onPinchZoomFinished(final PinchZoomDetector pPinchZoomDetector, final TouchEvent pTouchEvent, final float pZoomFactor) {
                camera.setZoomFactor(mPinchZoomStartedCameraZoomFactor * pZoomFactor);
            }
        });
        scene.setOnSceneTouchListener(new IOnSceneTouchListener() {
            private boolean pressed = false;
            @Override
            public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent) {
                mPinchZoomDetector.onTouchEvent(pSceneTouchEvent);

                switch(pSceneTouchEvent.getAction()) {
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
                        if(motion.getHistorySize() > 0){
                            for(int i = 1, n = motion.getHistorySize(); i < n; i++){
                                int calcX = (int) motion.getHistoricalX(i) - (int) motion.getHistoricalX(i-1);
                                int calcY = (int) motion.getHistoricalY(i) - (int) motion.getHistoricalY(i-1);
                                //Debug.d("diffX: "+calcX+", diffY: "+calcY);
                                final float zoom = camera.getZoomFactor();

                                camera.setCenter(camera.getCenterX() - (calcX/zoom), camera.getCenterY() + (calcY/zoom));
                            }
                        }
                        return true;
                }
                return false;
            }
        });

        this.hudManager = new HUDManager(this);

    }

    public void load() throws IOException {
        this.syncDB();

        // Load locale pack
        final String locale_pack_manifest = "locales/en_us_rural/manifest.xml";
        LocaleLoader localeLoader = new LocaleLoader();
        try {
            this.locale = localeLoader.load(assetManager.open(locale_pack_manifest));
            Debug.d("Locale map: "+locale.map_src);
        } catch (final IOException e) {
            Debug.e("Error loading Locale from "+locale_pack_manifest, e);
        }

        // Load phoeniciaGame session
        try {
            this.session = GameSession.objects(activity).all().toList().get(0);
            this.current_level = this.session.current_level.get();
        } catch (IndexOutOfBoundsException e) {
            this.session = GameSession.start(this.locale);
        }
        this.session.save(activity.getApplicationContext());
        this.sessionFilter = new Filter();
        sessionFilter.is("game", this.session);

        // Load font assets
        this.defaultFont = FontFactory.create(this.activity.getFontManager(), this.activity.getTextureManager(), 256, 256, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 32, Color.RED_ARGB_PACKED_INT);
        this.defaultFont.load();

        // Load map assets
        try {
            final TMXLoader tmxLoader = new TMXLoader(assetManager, textureManager, TextureOptions.BILINEAR_PREMULTIPLYALPHA, vboManager);
            this.mTMXTiledMap = tmxLoader.loadFromAsset(this.locale.map_src);
            for (TMXLayer tmxLayer : this.mTMXTiledMap.getTMXLayers()){
                scene.attachChild(tmxLayer);
            }
            placedSprites = new Sprite[this.mTMXTiledMap.getTileColumns()][this.mTMXTiledMap.getTileRows()];
        } catch (final TMXLoadException e) {
            Debug.e("Error loading map at "+this.locale.map_src, e);
        }

        try {
            shellTexture = new AssetBitmapTexture(this.textureManager, this.assetManager, this.locale.shell_src);
            shellTexture.load();
            shellTiles = TextureRegionFactory.extractTiledFromTexture(shellTexture, 0, 0, 64 * 8, 64 * 8, 8, 8);

        } catch (final IOException e) {
            e.printStackTrace();
            throw e;
        }
        List<Letter> blockLetters = locale.letters;
        List<Word> blockWords = locale.words;
        try {
            // Load letter assets
            for (int i = 0; i < blockLetters.size(); i++) {
                Letter letter = blockLetters.get(i);
                Debug.d("Loading letter texture from " + letter.texture_src);
                final AssetBitmapTexture letterTexture = new AssetBitmapTexture(this.textureManager, this.assetManager, letter.texture_src);
                letterTexture.load();
                this.letterTextures.put(letter, letterTexture);
                this.letterTiles.put(letter, TextureRegionFactory.extractTiledFromTexture(letterTexture, 0, 0, 64 * 4, 64 * 5, 4, 5));
            }

            // Load word assets
            for (int i = 0; i < blockWords.size(); i++) {
                Word word = blockWords.get(i);
                Debug.d("Loading word texture from " + word.texture_src);
                final AssetBitmapTexture wordTexture = new AssetBitmapTexture(this.textureManager, this.assetManager, word.texture_src);
                wordTexture.load();
                this.wordTextures.put(word, wordTexture);
                this.wordTiles.put(word, TextureRegionFactory.extractTiledFromTexture(wordTexture, 0, 0, 64 * 4, 64 * 5, 4, 5));
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
            Debug.d("Loading level "+this.current_level+" letters");
            for (int i = 0; i < blockLetters.size(); i++) {
                Letter letter = blockLetters.get(i);
                Debug.d("Loading sound file "+i+": "+letter.sound);
                blockSounds.put(letter.sound, SoundFactory.createSoundFromAsset(this.soundManager, this.activity, letter.sound));
            }
            Debug.d("Loading level "+this.current_level+" words");
            for (int i = 0; i < blockWords.size(); i++) {
                Word word = blockWords.get(i);
                Debug.d("Loading sound file "+i+": "+word.sound);
                blockSounds.put(word.sound, SoundFactory.createSoundFromAsset(this.soundManager, this.activity, word.sound));
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        // Load phoeniciaGame state
        Debug.d("Loading saved blocks");
        List<PlacedBlock> savedBlocks = PlacedBlock.objects(this.activity.getApplicationContext()).filter(sessionFilter).toList();
        for (int i = 0; i < savedBlocks.size(); i++) {
            PlacedBlock block = savedBlocks.get(i);
            Debug.d("Restoring tile "+block.item_name.get()+" at "+block.isoX.get()+"x"+block.isoY.get());
            if (block.sprite_type.get() == PlacedBlock.TYPE_LETTER) {
                //this.setBlockAtIso(block.isoX.get(), block.isoY.get(), block.getLetter(locale));
            } else if (block.sprite_type.get() == PlacedBlock.TYPE_WORD) {
                this.setBlockAtIso(block.isoX.get(), block.isoY.get(), block.getWord(locale));
            }
        }

        Debug.d("Loading letter tiles");
        List<LetterTile> letterTiles = LetterTile.objects(this.activity.getApplicationContext()).filter(sessionFilter).toList();
        for (int i = 0; i < letterTiles.size(); i++) {
            LetterTile letterTile = letterTiles.get(i);
            Debug.d("Restoring tile "+letterTile.item_name.get());
            letterTile.letter = this.locale.letter_map.get(letterTile.item_name.get());
            letterTile.phoeniciaGame = this;
            BuildQueue builder = letterTile.getBuilder(this.activity.getApplicationContext());
            if (builder == null) {
                Debug.d("Adding new builder for tile "+letterTile.item_name.get());
                builder = new BuildQueue(this.session, letterTile, letterTile.item_name.get(), letterTile.letter.time);
                builder.save(this.activity.getApplicationContext());
                letterTile.setBuilder(builder);
                letterTile.save(this.activity.getApplicationContext());
                builder.start();
            } else {
                builder.time.set(letterTile.letter.time);
                builder.save(this.activity.getApplicationContext());
                Debug.d("Found builder with "+builder.progress.get()+"/"+builder.time.get()+" and status "+builder.status.get());
            }
            this.addBuilder(builder);
            this.createLetterSprite(letterTile);
        }

    }

    private void syncDB() {
        List<Class<? extends Model>> models = new ArrayList<Class<? extends Model>>();
        models.add(GameSession.class);
        models.add(LetterTile.class);
        models.add(PlacedBlock.class);
        models.add(InventoryItem.class);
        models.add(BuildQueue.class);

        DatabaseAdapter.setDatabaseName("game_db");
        DatabaseAdapter adapter = DatabaseAdapter.getInstance(this.activity.getApplicationContext());
        adapter.setModels(models);
    }

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
        DatabaseAdapter adapter = DatabaseAdapter.getInstance(this.activity.getApplicationContext());
        adapter.drop();
        this.syncDB();
        this.session = GameSession.start(this.locale);
        this.session.save(activity);
        this.current_level = this.session.current_level.get();
        this.changeLevel(this.locale.level_map.get(this.current_level));
        this.sessionFilter = new Filter();
        this.sessionFilter.is("game", this.session);
    }
    public void start() {
        this.camera.setCenter(50, -500);
        this.camera.setHUD(this.hudManager);
        this.hudManager.showDefault(locale.level_map.get(this.current_level));
        if (this.current_level != this.session.current_level.get()) {
            this.changeLevel(this.locale.level_map.get(this.session.current_level.get()));
        }
    }

    public void reset() {
        // IUpdateHandler.reset
    }
    public void onUpdate(float v) {
        // update build queues
        this.updateTime += v;
        if (this.updateTime > 1) {
            this.updateTime = 0;
            for (BuildQueue builder : this.builders) {
                if (builder.status.get() == BuildQueue.BUILDING) {
                    builder.update();
                    builder.save(this.activity.getApplicationContext());
                    Debug.d("Builder "+builder.item_name.get()+" saved");
                }
            }
        }
    }

    public void addBuilder(BuildQueue builder) {
        this.builders.remove(builder);
        this.builders.add(builder);
    }

    public HUD getHUD() {
        return this.hudManager;
    }

    public void onBackPressed() {
        Debug.d("Back button pressed");
        this.hudManager.pop();
    }

    public boolean setPlaceBlock(Letter activateLetter) {
        if (this.placeLetter != null && this.placeLetter.name == activateLetter.name) {
            this.placeLetter = null;
            this.placeWord = null;
            return false;
        } else {
            this.placeLetter = activateLetter;
            this.placeWord = null;
            return true;
        }
    }
    public boolean setPlaceBlock(Word activateWord) {
        if (this.placeWord != null && this.placeWord.name == activateWord.name) {
            this.placeWord = null;
            this.placeLetter = null;
            return false;
        } else {
            this.placeWord = activateWord;
            this.placeLetter = null;
            return true;
        }
    }

    public void addBlock(int x, int y) {
        Debug.d("Don't use PhoeniciaGame.addBlock anymore");
        return;

/*
        final TMXLayer tmxLayer = this.mTMXTiledMap.getTMXLayers().get(1);
        final TMXTile tmxTile = tmxLayer.getTMXTileAt(x, y);
        if (tmxTile == null) {
            Debug.d("Can't place blocks outside of map");
            return;
        }
        PlacedBlockSprite blockTile = null;
        if (this.placeLetter != null) {
            blockTile = this.placeBlock(tmxTile, this.placeLetter);
        } else if (this.placeWord != null) {
            blockTile = this.placeBlock(x, y, this.placeWord);
        }
        if (blockTile != null) {
            PlacedBlock newBlock = new PlacedBlock();
            newBlock.phoeniciaGame.set(this.session);
            newBlock.isoX.set(tmxTile.getTileColumn());
            newBlock.isoY.set(tmxTile.getTileRow());
            newBlock.sprite = blockTile;
            if (this.placeLetter != null) {
                newBlock.sprite_type.set(PlacedBlock.TYPE_LETTER);
                newBlock.item_name.set(this.placeLetter.chars);
            } else if (this.placeWord != null) {
                newBlock.sprite_type.set(PlacedBlock.TYPE_WORD);
                newBlock.item_name.set(this.placeWord.chars);
            }
            Debug.d("Saving block "+newBlock.item_name.get()+" at "+newBlock.isoX.get()+"x"+newBlock.isoY.get());
            if (newBlock.save(this.activity.getApplicationContext())) {
                Debug.d("Save successful");
            }

            if (newBlock.sprite_type.get() == PlacedBlock.TYPE_LETTER) {
                BuildQueue builder = new BuildQueue(this.session, null, "", this.placeLetter.time); // TODO: don't use magic number
                builder.setUpdateHandler(newBlock);
                builder.start();
                builder.save(this.activity.getApplicationContext());
                this.builders.add(builder);
            }
        }
*/
    }

    public void createLetterSprite(LetterTile tile) {
        final TMXLayer tmxLayer = this.mTMXTiledMap.getTMXLayers().get(1);
        final TMXTile tmxTile = tmxLayer.getTMXTile(tile.isoX.get(), tile.isoY.get());
        int tileX = (int)tmxTile.getTileX() + 32;// tiles are 64px wide and anchors in the center
        int tileY = (int)tmxTile.getTileY() + 32 ;// tiles are 64px wide and anchors in the center
        int tileZ = tmxTile.getTileZ();

        Debug.d("Creating LetterSprite for "+tile.letter.name+" at "+tile+"x"+tileY);
        final PlacedBlockSprite sprite = new PlacedBlockSprite(tileX, tileY, 4, letterTiles.get(tile.letter), vboManager);
        sprite.setOnClickListener(tile);
        sprite.setZIndex(tileZ);

        BuildQueue builder = tile.getBuilder(this.activity.getApplicationContext());
        if (builder != null) {
            sprite.setProgress(builder.progress.get(), tile.letter.time);
        }
        sprite.animate();
        tile.setSprite(sprite);

        placedSprites[tmxTile.getTileColumn()][tmxTile.getTileRow()] = sprite;

        scene.registerTouchArea(sprite);
        scene.attachChild(sprite);
        scene.sortChildren();
    }
    /*public TMXTile setBlockAtIso(int tileColumn, int tileRow, final Letter placeBlock) {
        final TMXLayer tmxLayer = this.mTMXTiledMap.getTMXLayers().get(1);
        final TMXTile tmxTile = tmxLayer.getTMXTile(tileColumn, tileRow);
        if (tmxTile == null) {
            Debug.d("Can't place blocks outside of map");
            return null;
        }
        int tileX = (int)tmxTile.getTileX() - 32;// tiles are 64px wide and anchors in the center
        int tileY = (int)tmxTile.getTileY() - 32 ;// tiles are 64px wide and anchors in the center
        int tileZ = tmxTile.getTileZ();

        final PlacedBlockSprite block = new PlacedBlockSprite(tileX, tileY, 4, letterTiles.get(placeBlock), vboManager);
        block.setOnClickListener(new PlacedBlockSprite.OnClickListener() {
            @Override
            public void onClick(PlacedBlockSprite buttonSprite, float v, float v2) {
                Debug.d("Clicked block: "+placeBlock.chars);
                playBlockSound(placeBlock.phoneme);
                Inventory.getInstance().add(placeBlock.name);
            }
        });
        block.setZIndex(tileZ);
        block.animate();
        placedSprites[tmxTile.getTileColumn()][tmxTile.getTileRow()] = block;
        scene.registerTouchArea(block);
        scene.attachChild(block);

        scene.sortChildren();
        return tmxTile;
    }*/

    public TMXTile setBlockAtIso(int tileColumn, int tileRow, final Word placeBlock) {
        final TMXLayer tmxLayer = this.mTMXTiledMap.getTMXLayers().get(1);
        final TMXTile tmxTile = tmxLayer.getTMXTile(tileColumn, tileRow);
        if (tmxTile == null) {
            Debug.d("Can't place blocks outside of map");
            return null;
        }
        int tileX = (int)tmxTile.getTileX() - 32;// tiles are 64px wide and anchors in the center
        int tileY = (int)tmxTile.getTileY() - 32 ;// tiles are 64px wide and anchors in the center
        int tileZ = tmxTile.getTileZ();

        ButtonSprite block = new ButtonSprite(tileX, tileY, wordTiles.get(placeBlock).getTextureRegion(4), vboManager);
        block.setOnClickListener(new ButtonSprite.OnClickListener() {
            @Override
            public void onClick(ButtonSprite buttonSprite, float v, float v2) {
                Debug.d("Clicked block: "+placeBlock.chars);
                hudManager.showWordBuilder(locale.level_map.get(current_level), placeBlock);
            }
        });
        block.setZIndex(tileZ);
        placedSprites[tmxTile.getTileColumn()][tmxTile.getTileRow()] = block;
        scene.registerTouchArea(block);
        scene.attachChild(block);

        scene.sortChildren();
        return tmxTile;
    }

    public TMXTile getTileAt(float x, float y) {
        final TMXLayer tmxLayer = this.mTMXTiledMap.getTMXLayers().get(1);
        return tmxLayer.getTMXTileAt(x, y);
    }

    public boolean hasTileAt(TMXTile tmxTile) {
        return this.hasTileAt(tmxTile.getTileColumn(), tmxTile.getTileRow());
    }
    public boolean hasTileAt(int isoX, int isoY) {
        if (placedSprites[isoX][isoY] != null) {
            return true;
        } else {
            return false;
        }
    }
    public Sprite getSpriteAt(TMXTile tmxTile) {
        return this.getSpriteAt(tmxTile.getTileColumn(), tmxTile.getTileRow());
    }
    public Sprite getSpriteAt(int isoX, int isoY) {
        return placedSprites[isoX][isoY];
    }

/*    public PlacedBlockSprite placeBlock(final TMXTile tmxTile, final Letter placeLetter) {
        if (placeLetter == null) {
            Debug.d("No active letter to place");
            return null;
        }
        if (tmxTile == null) {
            Debug.d("Can't place blocks outside of map");
            return null;
        }
        int tileX = (int)tmxTile.getTileX() + 32;// map tiles are offset by 32px (half tile)
        int tileY = (int)tmxTile.getTileY() + 32;// map tiles are offset by 32px (half tile)
        int tileZ = tmxTile.getTileZ();

        if (placedSprites[tmxTile.getTileColumn()][tmxTile.getTileRow()] != null) {
            Debug.d("Sprite already exists at this location");
            return null;
        }

        final PlacedBlockSprite block = new PlacedBlockSprite(tileX, tileY, 4, this.letterTiles.get(placeLetter), vboManager);
        block.setZIndex(tileZ);
        block.animate();
        placedSprites[tmxTile.getTileColumn()][tmxTile.getTileRow()] = block;
        scene.registerTouchArea(block);
        scene.attachChild(block);

        scene.sortChildren();
        return block;
    }*/

    public PlacedBlockSprite placeBlock(int x, int y, final Word placeBlock) {
        if (placeBlock == null) {
            Debug.d("No active block to place");
            return null;
        }
        final TMXLayer tmxLayer = this.mTMXTiledMap.getTMXLayers().get(1);
        final TMXTile tmxTile = tmxLayer.getTMXTileAt(x, y);
        if (tmxTile == null) {
            Debug.d("Can't place blocks outside of map");
            return null;
        }
        int tileX = (int)tmxTile.getTileX() - 32;// tiles are 64px wide, assume the touch is targeting the middle
        int tileY = (int)tmxTile.getTileY() - 32;// tiles are 64px wide, assume the touch is targeting the middle
        int tileZ = tmxTile.getTileZ();

        if (placedSprites[tmxTile.getTileColumn()][tmxTile.getTileRow()] != null) {
            scene.detachChild(placedSprites[tmxTile.getTileColumn()][tmxTile.getTileRow()]);
            scene.unregisterTouchArea(placedSprites[tmxTile.getTileColumn()][tmxTile.getTileRow()]);
            placedSprites[tmxTile.getTileColumn()][tmxTile.getTileRow()] = null;
        }

        final PlacedBlockSprite block = new PlacedBlockSprite(tileX, tileY, 4, this.wordTiles.get(placeBlock), vboManager);
        block.setOnClickListener(new PlacedBlockSprite.OnClickListener() {
            @Override
            public void onClick(PlacedBlockSprite buttonSprite, float v, float v2) {
                Debug.d("Clicked block: "+placeBlock.chars);
                //playBlockSound(placeBlock.sound);
                //Inventory.getInstance().add(placeBlock.name);
                hudManager.showWordBuilder(locale.level_map.get(current_level), placeBlock);
            }
        });
        block.setZIndex(tileZ);
        placedSprites[tmxTile.getTileColumn()][tmxTile.getTileRow()] = block;
        scene.registerTouchArea(block);
        scene.attachChild(block);

        scene.sortChildren();
        return block;
    }

    public void playBlockSound(String soundFile) {

        Debug.d("Playing sound: "+soundFile);

        if (this.blockSounds.containsKey(soundFile)) {
            this.blockSounds.get(soundFile).play();
        } else {
            Debug.d("No blockSounds: "+soundFile+"");
        }
    }

    @Override
    public void onInventoryUpdated(InventoryItem[] item) {
        final Level current = this.locale.level_map.get(current_level);
        if (current.check(activity) && this.locale.levels.size() > this.locale.levels.indexOf(current)+1) {
            final Level next = this.locale.levels.get(this.locale.levels.indexOf(current)+1);
            this.changeLevel(next);
        }
    }

    public void changeLevel(Level next) {
        this.current_level = next.name;
        this.session.current_level.set(current_level);
        this.session.save(activity);

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
