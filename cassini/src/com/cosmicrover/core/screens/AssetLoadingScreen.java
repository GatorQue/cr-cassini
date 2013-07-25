package com.cosmicrover.core.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.cosmicrover.core.GameManager;

public class AssetLoadingScreen extends AbstractLoadingScreen {

	/// Default Lag delay for this Loading screen
	private static final float DEFAULT_LAG_DELAY_S = 0.0f;

	/// The Stage2d stage object used to render actors
    private Stage stage = null;
    /// The logo image to be rendered
    private Image logo = null;
    /// The loading frame image to be rendered
    private Image loadingFrame = null;
    /// The loading bar hidden image to be rendered
    private Image loadingBarHidden = null;
    /// The background screen image to be rendered
    private Image screenBg = null;
    /// The loading background image to be rendered
    private Image loadingBg = null;
    /// The loading bar image to be rendered
    private Actor loadingBar = null;
    
    // The startX position and endX position for scaling the loading bar
    private float startX, endX;

    public AssetLoadingScreen(GameManager gameManager) {
		this(gameManager, DEFAULT_LAG_DELAY_S);
	}

	public AssetLoadingScreen(GameManager gameManager, float lagDelay_s) {
		super("AssetLoadingScreen", gameManager, lagDelay_s);

		// Load the assets we need for displaying the loading screen immediately
		gameManager.getAssetManager().load("textures/loading.pack", TextureAtlas.class);
        gameManager.getAssetManager().finishLoading();
	}

	@Override
	protected void handleRender(float delta) {
        // Clear the screen
        Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
        
		// Update our asset loading progress
		float percentComplete = gameManager.getAssetManager().getProgress();
		
		// Check our asset loading progress (returns true if assets are done loading)
		if(gameManager.getAssetManager().update()) {
    		// Report that we are done loading
    		reportDone();

    		// Fix our percent to 1.0
			percentComplete = 1.0f;
		}
		
        // Update positions (and size) to match the percentage
        loadingBarHidden.setX(startX + endX * percentComplete);
        loadingBg.setX(loadingBarHidden.getX() + 30);
        loadingBg.setWidth(450 - 450 * percentComplete);
        loadingBg.invalidate();

        // Show the loading screen
        stage.act();
        stage.draw();
        
        // Log the percent complete
        Gdx.app.debug("AssetLoadingScreen", "Loading percent=" + percentComplete);
	}

	@Override
	public void show() {
		// Call our parent class implementation
		super.show();

        // Initialize the stage where we will place everything
        stage = new Stage();

        // Get our TextureAtlas from the manager
        TextureAtlas atlas = gameManager.getAssetManager().get("textures/loading.pack", TextureAtlas.class);

        // Grab the regions from the atlas and create some images
        logo = new Image(atlas.findRegion("libgdx-logo"));
        loadingFrame = new Image(atlas.findRegion("loading-frame"));
        loadingBarHidden = new Image(atlas.findRegion("loading-bar-hidden"));
        screenBg = new Image(atlas.findRegion("screen-bg"));
        loadingBg = new Image(atlas.findRegion("loading-frame-bg"));

        // Add the loading bar animation
        Animation anim = new Animation(0.05f, atlas.findRegions("loading-bar-anim") );
        anim.setPlayMode(Animation.LOOP_REVERSED);
        loadingBar = new LoadingBar(anim);

        // Or if you only need a static bar, you can do
        // loadingBar = new Image(atlas.findRegion("loading-bar1"));

        // Add all the actors to the stage
        stage.addActor(screenBg);
        stage.addActor(loadingBar);
        stage.addActor(loadingBg);
        stage.addActor(loadingBarHidden);
        stage.addActor(loadingFrame);
        stage.addActor(logo);
    }

	@Override
	public void resize(int width, int height) {
		// Call our parent class implementation
		super.resize(width, height);

        // Set our screen to always be XXX x 480 in size
        width = 480 * width / height;
        height = 480;
        stage.setViewport(width , height, false);

        // Make the background fill the screen
        screenBg.setSize(width, height);

        // Place the logo in the middle of the screen and 100 px up
        logo.setX((width - logo.getWidth()) / 2);
        logo.setY((height - logo.getHeight()) / 2 + 100);

        // Place the loading frame in the middle of the screen
        loadingFrame.setX((stage.getWidth() - loadingFrame.getWidth()) / 2);
        loadingFrame.setY((stage.getHeight() - loadingFrame.getHeight()) / 2);

        // Place the loading bar at the same spot as the frame, adjusted a few px
        loadingBar.setX(loadingFrame.getX() + 15);
        loadingBar.setY(loadingFrame.getY() + 5);

        // Place the image that will hide the bar on top of the bar, adjusted a few px
        loadingBarHidden.setX(loadingBar.getX() + 35);
        loadingBarHidden.setY(loadingBar.getY() - 3);

        // The start position and how far to move the hidden loading bar
        startX = loadingBarHidden.getX();
        endX = 440;

        // The rest of the hidden bar
        loadingBg.setSize(450, 50);
        loadingBg.setX(loadingBarHidden.getX() + 30);
        loadingBg.setY(loadingBarHidden.getY() + 3);
    }

	@Override
	public void hide() {
		// Remove our stage object
		stage.dispose();
		
        // Call our parent class implementation last
		super.hide();
	}
	
	private class LoadingBar extends Actor {
	    private Animation animation;
	    private TextureRegion textureRegion;
	    private float stateTime;

	    public LoadingBar(Animation animation) {
	        this.animation = animation;
	        textureRegion = animation.getKeyFrame(0);
	    }

	    @Override
	    public void act(float delta) {
	        stateTime += delta;
	        textureRegion = animation.getKeyFrame(stateTime);
	    }

	    @Override
	    public void draw(SpriteBatch batch, float parentAlpha) {
	        batch.draw(textureRegion, getX(), getY());
	    }
	};
}
