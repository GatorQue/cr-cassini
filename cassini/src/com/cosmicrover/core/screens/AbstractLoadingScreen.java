package com.cosmicrover.core.screens;

import com.badlogic.gdx.Gdx;
import com.cosmicrover.core.GameData;
import com.cosmicrover.core.GameManager;

public abstract class AbstractLoadingScreen extends AbstractScreen {
	/// The screenId to switch to when Loading screen completes (static to make
	/// it possible to change without having an address to AbstractLoadingScreen)
	private static int nextScreenId = GameData.EXIT_GAME_SCREEN;

	/// Constants used internally for delays (in seconds)
	/// Minimum 
	private static final float DEFAULT_LAG_DELAY_S = 0.2f;

	/// Lag delay time accumulator to wait before changing screens
	private float lagAccumulator_s = 0.0f;

	/// Set when changeScreen is called which indicates derived class thinks
	/// we are done loading everything. Lag delay begins when this is true
	private boolean done = false;
	
	/// The minimum amount of time to display the loading screen
	private float lagDelay_s = DEFAULT_LAG_DELAY_S;
	
	/**
	 * Creates a AbstractLoadingScreen with default lag delay of 0.2 seconds to
	 * wait before switching to the next screen (see setNextScreenId) so the
	 * user can see the 100% complete screen.
	 * @param gameManager to use for switching screens
	 */
	public AbstractLoadingScreen(GameManager gameManager) {
		this(gameManager, DEFAULT_LAG_DELAY_S);
	}

	/**
	 * Creates a AbstractLoadingScreen with a lag delay to wait after loading
	 * has completed before switching to the next screen specified using the
	 * setNextScreenId method below. A minimum delay to wait after loading has
	 * finished is helpful to allow the user to read the screen (e.g. a splash
	 * screen that is masking as the loading screen) or see the loading bar
	 * reach 100%.
	 * 
	 * @param gameManager to use for switching screens
	 * @param lagDelay_s in seconds to wait before switching screens
	 */
	public AbstractLoadingScreen(GameManager gameManager, float lagDelay_s) {
		super(gameManager);

		// Lag delay after loading completes to wait before switching to next screen
		this.lagDelay_s = lagDelay_s;
		
		// Note the creation of each screen in our debug log
		Gdx.app.debug(this.getClass().getSimpleName(), "Creating loading screen with a lag delay=" + lagDelay_s);
	}
	
	/**
	 * Sets the screenId to use after the Loading screen finishes loading. This
	 * method is static to make it possible to change without having access to
	 * the AbstractLoadingScreen objects address.
	 * @param screenId to switch to after data is loaded (defaults to exit)
	 */
	public static final void setNextScreenId(int screenId) {
		Gdx.app.log("AbstractLoadingScreen", "Setting next screenId="+screenId);
		nextScreenId = screenId;
	}

	@Override
	public final void render(float delta) {
		// Only add to our lag delay accumulator if we are done
		if(done) {
			// Add to our accumulator to keep track of loading times
			lagAccumulator_s += delta;

			// Do we have a lag delay and are we greater than it now? then switch to the next screen
			if (lagDelay_s >= 0.0f && lagAccumulator_s > lagDelay_s) {
				changeScreen();
			}
		}

		// Call our derived class implementation for rendering
		handleRender(delta);
	}

	/**
	 * Derived classes should call reportDone when loading is complete to begin
	 * the lag timer. When the lag timer has finished the base class will call
	 * changeScreen method for you. If you want to change screens immediately
	 * and ignore the lag delay then call the changeScreen method instead.
	 */
	protected final void reportDone() {
		done = true;
	}
	
	protected final void changeScreen() {
		// Make sure to call reportDone first
		reportDone();
		
		// Switch to the next screen
		gameManager.setScreen(nextScreenId);
	}

	/**
	 * Derived class implementation for rendering the loading screen to the display. 
	 * @param delta value since the last time handleRender was called
	 */
	protected abstract void handleRender(float delta);

	@Override
	public void show() {
		// Disable using the Back button for this screen
		gameManager.setBackScreenHandling(false);
		
		// Call our base class implementation which will set the Back button screenId
		super.show();
		
		// Reset our accumulator value
		lagAccumulator_s = 0.0f;

		// Reset our done flag
		done = false;
		
		// Indicate the start of loading data to be restored
		Gdx.app.log(this.getClass().getSimpleName(), "Loading data please wait...");
	}

	@Override
	public void hide() {
		// Enable using the Back button for this screen
		gameManager.setBackScreenHandling(true);
		
		// Call our base class implementation
		super.hide();
		
		// Indicate the average loading time to our log file
		Gdx.app.log(this.getClass().getSimpleName(), "Loading data completed");
	}
}
