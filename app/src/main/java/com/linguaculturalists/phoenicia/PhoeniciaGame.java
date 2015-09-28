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
import org.andengine.entity.sprite.AnimatedSprite;
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
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.bitmap.AssetBitmapTexture;
import org.andengine.opengl.texture.region.ITextureRegion;
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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.linguaculturalists.phoenicia.components.PlacedBlockSprite;
import com.linguaculturalists.phoenicia.locale.Letter;
import com.linguaculturalists.phoenicia.locale.Word;
import com.linguaculturalists.phoenicia.models.BuildQueue;
import com.linguaculturalists.phoenicia.models.GameSession;
import com.linguaculturalists.phoenicia.models.Inventory;
import com.linguaculturalists.phoenicia.models.InventoryItem;
import com.linguaculturalists.phoenicia.models.PlacedBlock;
import com.linguaculturalists.phoenicia.ui.LetterPlacementHUD;
import com.linguaculturalists.phoenicia.ui.HUDManager;
import com.linguaculturalists.phoenicia.util.LocaleLoader;
import com.orm.androrm.DatabaseAdapter;
import com.orm.androrm.Model;

/**
 * Created by mhall on 3/22/15.
 */
public class PhoeniciaGame implements IUpdateHandler {
    public Locale locale;
    public GameActivity activity;
    private TextureManager textureManager;
    private AssetManager assetManager;
    private VertexBufferObjectManager vboManager;
    private SoundManager soundManager;
    public Scene scene;
    public Camera camera;

    private float mPinchZoomStartedCameraZoomFactor;
    private PinchZoomDetector mPinchZoomDetector;

    public ITexture fontTexture;
    public Font defaultFont;

    //public AssetBitmapTexture terrainTexture;
    //public ITiledTextureRegion terrainTiles;

    public AssetBitmapTexture lettersTexture;
    public ITiledTextureRegion letterTiles;

    public AssetBitmapTexture wordsTexture;
    public ITiledTextureRegion wordTiles;

    public Sprite[][] placedSprites;
    private Letter placeLetter;
    private Word placeWord;

    private TMXTiledMap mTMXTiledMap;

    private Map<String, Sound> blockSounds;

    public HUDManager hudManager;
    public Inventory inventory;

    private Map<PlacedBlock, BuildQueue> builders;
    private float updateTime;

    private final String tst_startLevel = "3";

    public PhoeniciaGame(GameActivity activity, final ZoomCamera camera) {
        FontFactory.setAssetBasePath("fonts/");

        this.activity = activity;
        this.textureManager = activity.getTextureManager();
        this.assetManager = activity.getAssets();
        this.vboManager = activity.getVertexBufferObjectManager();
        this.soundManager = activity.getSoundManager();
        this.camera = camera;
        Inventory.init(this);
        this.inventory = Inventory.getInstance();
        scene = new Scene();
        scene.setBackground(new Background(new Color(0, 0, 0)));

        this.blockSounds = new HashMap<String, Sound>();
        this.builders = new HashMap<PlacedBlock, BuildQueue>();
        this.updateTime = 0;

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
                            addBlock((int) pSceneTouchEvent.getX(), (int) pSceneTouchEvent.getY());
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
        // Load locale pack
        final String locale_pack_manifest = "locales/en_us_rural/manifest.xml";
        LocaleLoader localeLoader = new LocaleLoader();
        try {
            this.locale = localeLoader.load(assetManager.open(locale_pack_manifest));
            Debug.d("Locale map: "+locale.map_src);
        } catch (final IOException e) {
            Debug.e("Error loading Locale from "+locale_pack_manifest, e);
        }

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

        // Load letter assets
        Debug.d("Loading letters texture from "+this.locale.letter_src);
        this.lettersTexture = new AssetBitmapTexture(this.textureManager, this.assetManager, this.locale.letter_src);
        this.lettersTexture.load();
        this.letterTiles = TextureRegionFactory.extractTiledFromTexture(this.lettersTexture, 0, 0, 64*8, this.locale.letters.size()*64, 8, this.locale.letters.size());

        // Load word assets
        this.wordsTexture = new AssetBitmapTexture(this.textureManager, this.assetManager, this.locale.word_src);
        wordsTexture.load();
        this.wordTiles = TextureRegionFactory.extractTiledFromTexture(this.wordsTexture, 0, 0, 640, 1024, 10, 16);

        // Load Level data
        try {
            SoundFactory.setAssetBasePath("locales/en_us_rural/");
            blockSounds = new HashMap<String, Sound>();
            Debug.d("Loading level "+this.tst_startLevel+" letters");
            List<Letter> blockLetters = locale.level_map.get(this.tst_startLevel).letters;
            for (int i = 0; i < blockLetters.size(); i++) {
                Letter letter = blockLetters.get(i);
                Debug.d("Loading sound file "+i+": "+letter.sound);
                blockSounds.put(letter.sound, SoundFactory.createSoundFromAsset(this.soundManager, this.activity, letter.sound));
            }
            Debug.d("Loading level "+this.tst_startLevel+" words");
            List<Word> blockWords = locale.level_map.get(this.tst_startLevel).words;
            for (int i = 0; i < blockWords.size(); i++) {
                Word word = blockWords.get(i);
                Debug.d("Loading sound file "+i+": "+word.sound);
                blockSounds.put(word.sound, SoundFactory.createSoundFromAsset(this.soundManager, this.activity, word.sound));
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        // Load game state
        this.syncDB();
        //this.restart();
        Debug.d("Loading saved blocks");
        List<PlacedBlock> savedBlocks = PlacedBlock.objects(this.activity.getApplicationContext()).all().toList();
        for (int i = 0; i < savedBlocks.size(); i++) {
            PlacedBlock block = savedBlocks.get(i);
            Debug.d("Restoring tile "+block.item_name.get()+" at "+block.isoX.get()+"x"+block.isoY.get());
            if (block.sprite_type.get() == PlacedBlock.TYPE_LETTER) {
                this.setBlockAtIso(block.isoX.get(), block.isoY.get(), block.getLetter(locale));
            } else if (block.sprite_type.get() == PlacedBlock.TYPE_WORD) {
                this.setBlockAtIso(block.isoX.get(), block.isoY.get(), block.getWord(locale));
            }
        }

        Debug.d("Loading build queues");
        List<BuildQueue> buildObjects = BuildQueue.objects(this.activity.getApplicationContext()).all().toList();
        for (int i = 0; i < buildObjects.size(); i++) {
            BuildQueue builder = buildObjects.get(i);
            PlacedBlock tile = builder.tile.get(this.activity.getApplicationContext());
            Debug.d("Restoring builder "+builder.item_name.get());
            if (tile == null) {
                Debug.w("Builder "+builder.item_name.get()+" has no tile");
                builder.delete(this.activity.getApplicationContext());
                continue;
            }
            this.builders.put(tile, builder);
            Debug.d("Setting builder "+builder.item_name.get()+" to tile "+tile.item_name.get());
            builder.setUpdateHandler(tile);
        }
    }

    private void syncDB() {
        List<Class<? extends Model>> models = new ArrayList<Class<? extends Model>>();
        models.add(GameSession.class);
        models.add(PlacedBlock.class);
        models.add(InventoryItem.class);
        models.add(BuildQueue.class);

        DatabaseAdapter.setDatabaseName("game_db");
        DatabaseAdapter adapter = DatabaseAdapter.getInstance(this.activity.getApplicationContext());
        adapter.setModels(models);
    }

    public void restart() {
        // Detach sprites
        for (int c = 0; c < placedSprites.length; c++) {
            for (int r = 0; r < placedSprites[c].length; r++) {
                if (placedSprites[c][r] != null) {
                    scene.detachChild(placedSprites[c][r]);
                    scene.unregisterTouchArea(placedSprites[c][r]);
                    placedSprites[c][r] = null;
                }
            }
        }
        // Delete DB records
        Inventory.getInstance().clear();
        DatabaseAdapter adapter = DatabaseAdapter.getInstance(this.activity.getApplicationContext());
        adapter.drop();
        this.syncDB();
    }
    public void start(Camera camera) {
        camera.setCenter(50, -500);
        camera.setHUD(this.hudManager);
        this.hudManager.showDefault(locale.level_map.get(tst_startLevel));
    }

    public void reset() {
        // IUpdateHandler.reset
    }
    public void onUpdate(float v) {
        // update build queues
        this.updateTime += v;
        if (this.updateTime > 1) {
            this.updateTime = 0;
            for (BuildQueue builder : this.builders.values()) {
                if (builder.status.get() == BuildQueue.BUILDING) {
                    builder.update();
                    builder.save(this.activity.getApplicationContext());
                }
            }
        }
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
        TMXTile blockTile = null;
        if (this.placeLetter != null) {
            blockTile = this.placeBlock(x, y, this.placeLetter);
        } else if (this.placeWord != null) {
            blockTile = this.placeBlock(x, y, this.placeWord);
        }
        if (blockTile != null) {
            PlacedBlock newBlock = new PlacedBlock();
            newBlock.isoX.set(blockTile.getTileColumn());
            newBlock.isoY.set(blockTile.getTileRow());
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
                BuildQueue builder = new BuildQueue(newBlock, newBlock.item_name.get(), 100); // TODO: don't use magic number
                builder.setUpdateHandler(newBlock);
                builder.start();
                builder.save(this.activity.getApplicationContext());
                this.builders.put(newBlock, builder);
            }
        }
    }
    public TMXTile setBlockAtIso(int tileColumn, int tileRow, final Letter placeBlock) {
        final TMXLayer tmxLayer = this.mTMXTiledMap.getTMXLayers().get(1);
        final TMXTile tmxTile = tmxLayer.getTMXTile(tileColumn, tileRow);
        if (tmxTile == null) {
            Debug.d("Can't place blocks outside of map");
            return null;
        }
        int tileX = (int)tmxTile.getTileX() - 32;// tiles are 64px wide and anchors in the center
        int tileY = (int)tmxTile.getTileY() - 32 ;// tiles are 64px wide and anchors in the center
        int tileZ = tmxTile.getTileZ();

        PlacedBlockSprite block = new PlacedBlockSprite(tileX, tileY, placeBlock.tile, letterTiles, vboManager);
        block.setOnClickListener(new PlacedBlockSprite.OnClickListener() {
            @Override
            public void onClick(PlacedBlockSprite buttonSprite, float v, float v2) {
                Debug.d("Clicked block: "+placeBlock.chars);
                playBlockSound(placeBlock.sound);
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
    }

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

        ButtonSprite block = new ButtonSprite(tileX, tileY, wordTiles.getTextureRegion(placeBlock.tile), vboManager);
        block.setOnClickListener(new ButtonSprite.OnClickListener() {
            @Override
            public void onClick(ButtonSprite buttonSprite, float v, float v2) {
                Debug.d("Clicked block: "+placeBlock.chars);
                hudManager.showWordBuilder(locale.level_map.get(tst_startLevel), placeBlock);
            }
        });
        block.setZIndex(tileZ);
        placedSprites[tmxTile.getTileColumn()][tmxTile.getTileRow()] = block;
        scene.registerTouchArea(block);
        scene.attachChild(block);

        scene.sortChildren();
        return tmxTile;
    }
    public TMXTile placeBlock(int x, int y, final Letter placeBlock) {
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

        PlacedBlockSprite block = new PlacedBlockSprite(tileX, tileY, placeBlock.tile, this.letterTiles, vboManager);
        block.setOnClickListener(new PlacedBlockSprite.OnClickListener() {
            @Override
            public void onClick(PlacedBlockSprite buttonSprite, float v, float v2) {
                Debug.d("Clicked block: "+placeBlock.chars);
                playBlockSound(placeBlock.sound);
                Inventory.getInstance().add(placeBlock.name);
                //BlockPlacementHUD.getInstance().onInventoryUpdated(items);
            }
        });
        block.setZIndex(tileZ);
        block.animate();
        placedSprites[tmxTile.getTileColumn()][tmxTile.getTileRow()] = block;
        scene.registerTouchArea(block);
        scene.attachChild(block);

        scene.sortChildren();
        return tmxTile;
    }

    public TMXTile placeBlock(int x, int y, final Word placeBlock) {
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

        ITextureRegion blockRegion = this.wordTiles.getTextureRegion(placeBlock.tile);
        ButtonSprite block = new ButtonSprite(tileX, tileY, blockRegion, vboManager);
        block.setOnClickListener(new ButtonSprite.OnClickListener() {
            @Override
            public void onClick(ButtonSprite buttonSprite, float v, float v2) {
                Debug.d("Clicked block: "+placeBlock.chars);
                //playBlockSound(placeBlock.sound);
                //Inventory.getInstance().add(placeBlock.name);
                hudManager.showWordBuilder(locale.level_map.get(tst_startLevel), placeBlock);
            }
        });
        block.setZIndex(tileZ);
        placedSprites[tmxTile.getTileColumn()][tmxTile.getTileRow()] = block;
        scene.registerTouchArea(block);
        scene.attachChild(block);

        scene.sortChildren();
        return tmxTile;
    }

    public void playBlockSound(String soundFile) {

        Debug.d("Playing sound: "+soundFile);

        if (this.blockSounds.containsKey(soundFile)) {
            this.blockSounds.get(soundFile).play();
        } else {
            Debug.d("No blockSounds: "+soundFile+"");
        }
    }
}
