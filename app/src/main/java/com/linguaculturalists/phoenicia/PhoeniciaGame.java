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
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.linguaculturalists.phoenicia.locale.Letter;
import com.linguaculturalists.phoenicia.models.GameSession;
import com.linguaculturalists.phoenicia.models.Inventory;
import com.linguaculturalists.phoenicia.models.PlacedBlock;
import com.linguaculturalists.phoenicia.ui.BlockPlacementHUD;
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

    public AssetBitmapTexture terrainTexture;
    public ITiledTextureRegion terrainTiles;

    public Sprite[][] placedSprites;

    private TMXTiledMap mTMXTiledMap;

    private Map<String, Sound> blockSounds;

    private HUDManager hudManager;

    private final String tst_startLevel = "3";

    public PhoeniciaGame(GameActivity activity, final ZoomCamera camera) {
        FontFactory.setAssetBasePath("fonts/");

        this.activity = activity;
        this.textureManager = activity.getTextureManager();
        this.assetManager = activity.getAssets();
        this.vboManager = activity.getVertexBufferObjectManager();
        this.soundManager = activity.getSoundManager();
        this.camera = camera;
        scene = new Scene();
        scene.setBackground(new Background(new Color(0, 0, 0)));

        this.blockSounds = new HashMap<String, Sound>();

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
            @Override
            public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent) {
                mPinchZoomDetector.onTouchEvent(pSceneTouchEvent);

                switch(pSceneTouchEvent.getAction()) {
                    case TouchEvent.ACTION_DOWN:
                        addBlock((int) pSceneTouchEvent.getX(), (int) pSceneTouchEvent.getY());
                        break;
                    case TouchEvent.ACTION_UP:
                        //MainActivity.this.mSmoothCamera.setZoomFactor(1.0f);
                        break;
                    case TouchEvent.ACTION_MOVE:
                        MotionEvent motion = pSceneTouchEvent.getMotionEvent();
                        if(motion.getHistorySize() > 0){
                            for(int i = 1, n = motion.getHistorySize(); i < n; i++){
                                int calcX = (int) motion.getHistoricalX(i) - (int) motion.getHistoricalX(i-1);
                                int calcY = (int) motion.getHistoricalY(i) - (int) motion.getHistoricalY(i-1);
                                //Debug.d("diffX: "+calcX+", diffY: "+calcY);

                                camera.setCenter(camera.getCenterX() - calcX, camera.getCenterY() + calcY);
                            }
                        }
                        return true;
                }
                return false;
            }
        });
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
        this.terrainTexture = new AssetBitmapTexture(this.textureManager, this.assetManager, this.locale.letter_src);
        terrainTexture.load();
        this.terrainTiles = TextureRegionFactory.extractTiledFromTexture(this.terrainTexture, 0, 0, 640, 1024, 10, 16);

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

        // Load Level data
        try {
            Debug.d("Loading level "+this.tst_startLevel+" letters");
            List<Letter> blockLetters = locale.level_map.get(this.tst_startLevel).letters;
            blockSounds = new HashMap<String, Sound>();
            for (int i = 0; i < blockLetters.size(); i++) {
                Letter letter = blockLetters.get(i);
                Debug.d("Loading sound file "+i+": "+letter.sound);
                blockSounds.put(letter.sound, SoundFactory.createSoundFromAsset(this.soundManager, this.activity, letter.sound));
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        // Load game state
        this.syncDB();
        this.restart();
        Debug.d("Loading saved blocks");
        List<PlacedBlock> savedBlocks = PlacedBlock.objects(this.activity.getApplicationContext()).all().toList();
        for (int i = 0; i < savedBlocks.size(); i++) {
            PlacedBlock block = savedBlocks.get(i);
            Debug.d("Restoring tile "+block.sprite_tile.get()+" at "+block.isoX.get()+"x"+block.isoY.get());
            //this.setBlockAtIso(block.isoX.get(), block.isoY.get(), block.getLetter(locale));
        }
    }
    private void syncDB() {
        List<Class<? extends Model>> models = new ArrayList<Class<? extends Model>>();
        models.add(GameSession.class);
        models.add(PlacedBlock.class);
        models.add(Inventory.class);

        DatabaseAdapter.setDatabaseName("game_db");
        DatabaseAdapter adapter = new DatabaseAdapter(this.activity.getApplicationContext());
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
        DatabaseAdapter adapter = new DatabaseAdapter(this.activity.getApplicationContext());
        adapter.drop();
        this.syncDB();
    }
    public void start(Camera camera) {
        camera.setCenter(400, -400);
        this.hudManager = new HUDManager(this, locale.level_map.get(tst_startLevel));
        camera.setHUD(this.hudManager);
        this.hudManager.show(HUDManager.BlockPlacement);
    }

    public void reset() {
        // IUpdateHandler.reset
    }
    public void onUpdate(float v) {
        // TODO: update build queues
    }

    public HUD getBlockPlacementHUD() {
        return new HUDManager(this, locale.level_map.get(tst_startLevel));
    }

    public void addBlock(int x, int y) {
        TMXTile blockTile = this.placeBlock(x+64, y, BlockPlacementHUD.getPlaceBlock());
        if (blockTile != null) {
            PlacedBlock newBlock = new PlacedBlock();
            newBlock.isoX.set(blockTile.getTileColumn());
            newBlock.isoY.set(blockTile.getTileRow());
            newBlock.sprite_tile.set(BlockPlacementHUD.getPlaceBlock().tile);
            newBlock.item_name.set(BlockPlacementHUD.getPlaceBlock().chars);
            Debug.d("Saving block "+newBlock.sprite_tile.get()+" at "+newBlock.isoX.get()+"x"+newBlock.isoY.get());
            if (newBlock.save(this.activity.getApplicationContext())) {
                Debug.d("Save successful");
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
        int tileX = (int)tmxTile.getTileX() - 32;// tiles are 64px wide, assume the touch is targeting the middle
        int tileY = (int)tmxTile.getTileY() - 32 ;// tiles are 64px wide, assume the touch is targeting the middle
        int tileZ = tmxTile.getTileZ();

        ITextureRegion blockRegion = terrainTiles.getTextureRegion(placeBlock.tile);
        ButtonSprite block = new ButtonSprite(tileX, tileY, blockRegion, vboManager);
        block.setOnClickListener(new ButtonSprite.OnClickListener() {
            @Override
            public void onClick(ButtonSprite buttonSprite, float v, float v2) {
                Debug.d("Clicked block: "+placeBlock.chars);
                playBlockSound(placeBlock.sound);
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

        ITextureRegion blockRegion = terrainTiles.getTextureRegion(placeBlock.tile);
        ButtonSprite block = new ButtonSprite(tileX, tileY, blockRegion, vboManager);
        block.setOnClickListener(new ButtonSprite.OnClickListener() {
            @Override
            public void onClick(ButtonSprite buttonSprite, float v, float v2) {
                Debug.d("Clicked block: "+placeBlock.chars);
                playBlockSound(placeBlock.sound);
            }
        });
        block.setZIndex(tileZ);
        placedSprites[tmxTile.getTileColumn()][tmxTile.getTileRow()] = block;
        scene.registerTouchArea(block);
        scene.attachChild(block);

        scene.sortChildren();
        return tmxTile;
    }

    void playBlockSound(String soundFile) {

        Debug.d("Playing sound: "+soundFile);

        if (this.blockSounds.containsKey(soundFile)) {
            this.blockSounds.get(soundFile).play();
        } else {
            Debug.d("No blockSounds: "+soundFile+"");
        }
    }
}
