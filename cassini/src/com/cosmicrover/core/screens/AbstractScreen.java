package com.cosmicrover.core.screens;

import com.artemis.World;
import com.badlogic.gdx.Screen;
import com.cosmicrover.cassini.managers.ViewportManager;
import com.cosmicrover.cassini.utils.WorldData;
import com.cosmicrover.core.GameData;
import com.cosmicrover.core.GameManager;

public class AbstractScreen implements Screen {
	/// Our GameDataService object to make game changes with
	protected final GameManager gameManager;
	
	/// Our World object that holds our Components, Managers, and Systems
	protected final World world;
	
	/// Viewport manager for managing viewports for each player
	private final ViewportManager viewportManager;
	
	/// Indicates this screen has never been shown yet.
	private boolean firstTime = true; 
	
	/// Which screen to switch to on Back button (default is to exit game)
	private int backScreenId = GameData.EXIT_GAME_SCREEN;
	
	public AbstractScreen(GameManager gameManager) {
		this(gameManager, GameData.EXIT_GAME_SCREEN);
	}

	public AbstractScreen(GameManager gameManager, int screenId) {
		this.gameManager = gameManager;
		this.world = gameManager.getData(WorldData.class).getWorld();
		firstTime = true;
		setBackScreenId(screenId);

		// Attempt to retrieve the ViewportManager here
		viewportManager = this.world.getManager(ViewportManager.class);
	}
	
	protected boolean isFirstTime() {
		return firstTime;
	}
	
	protected void clearFirstTime() {
		firstTime = false;
	}
	
	protected int getBackScreenId() {
		return backScreenId;
	}

	/**
	 * Sets the screenId to use when the back button is pressed.
	 * @param screenId to use when the Back button is pressed
	 */
	protected void setBackScreenId(int screenId) {
		this.backScreenId = screenId;
	}

	/////////////////////////////////////////////////////////////////////////
	// Screen interface methods
	/////////////////////////////////////////////////////////////////////////
	@Override
	public void render(float delta) {
		// TODO: Add rendering logic here
	}

	@Override
	public void resize(int width, int height) {
		// TODO: Call classes that need resize information

		// Update our viewports when screen is resized if we have a ViewportManager
		if(viewportManager != null) {
			viewportManager.updateViewports();
		}
	}

	@Override
	public void show() {
		// TODO: Enable or insert entities specific to this Screen

		// Set our Back button screenId value now
		gameManager.setBackScreen(backScreenId);
	}

	@Override
	public void hide() {
		// TODO: Disable or remove entities created by show method above.
	}

	@Override
	public void pause() {
		// TODO: Add serialize of any custom data here. (usually done by gameService)
	}

	@Override
	public void resume() {
		// TODO: Not sure what might be needed here, only called on Android
	}

	@Override
	public void dispose() {
		// TODO: Add dispose calls for anything specific to this Screen
	}
}
