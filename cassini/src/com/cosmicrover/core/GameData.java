package com.cosmicrover.core;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.utils.Json.Serializable;

public abstract class GameData implements Serializable {
	/// Special screen ID indicating that the program should exit
	public static final int EXIT_GAME_SCREEN = 0;
	
	/// Flag that indicates that a new game should be started
	private boolean newGame = false;
	
	/// Flag that indicates that a game is in progress
	private boolean inProgress = false;
	
	/// Screen manager for managing Screen's to switch between
	protected final ScreenManager screenManager = new ScreenManager();
	
	public final boolean isNewGame() {
		return newGame;
	}
	
	public final boolean isInProgress() {
		return inProgress;
	}

	/**
	 * Sets the new game indicator so that when restoreWorld is called, a new
	 * game can be created instead.
	 */
	public final void setNewGame( final boolean newGame ) {
		this.newGame = newGame;
	}
	
	/**
	 * Sets the in progress indicator so that game data will be saved on pause
	 * or exit of the application.
	 * @param[in] inProgress value to set indicator to
	 */
	public final void setInProgress( final boolean inProgress ) {
		this.inProgress = inProgress;
	}
	
	/**
	 * Called when the game is being created and gives the GameData object an
	 * opportunity to create and initialize any objects it might need.
	 * Typically this is called by the GameService.init() method but before
	 * the GameService.restoreGame() method has been called.
	 * @param[in] the GameService parent class that created this GameData derived object
	 */
	public abstract void init(GameManager gameService);
	
	/**
	 * Called by the game class to retrieve the initial screen to be shown
	 * after the initGame and restoreGame methods have been called above.
	 * @return a AbstractScreen derived class to be displayed first
	 */
	public abstract Screen getInitialScreen();
	
	/**
	 * Retrieve the screenId for the settings screen (or EXIT_GAME_SCREEN if
	 * there isn't a settings screen to display).
	 */
	public int getOptionsScreen() {
		return EXIT_GAME_SCREEN;
	}

	/**
	 * Retrieve the screenId value for the Screen value provided.
	 * @param screen to find screenId value for
	 * @return the screenId found or 0 if Screen wasn't found.
	 */
	public final int getScreenId(Screen screen) {
		return screenManager.getScreenId(screen);
	}

	/**
	 * Retrieve the Screen object using the screenId provided.
	 * @param screenId to use to retrieve the Screen object
	 * @return the Screen object associated with screenId or null if not found.
	 */
	public final Screen getScreen(int screenId) {
		return screenManager.getScreen(screenId);
	}
	
	/**
	 * Called when a new game should be started (typically as a result of the
	 * Play Game button being pressed on the MainMenuScreen. The derived class
	 * is responsible for the creation or loading of the entities in a new game
	 * and changing to the correct screen for a new game.
	 */
	public abstract void newGame();

	/**
	 * Called when a game in progress should be resumed (typically as a result
	 * of the Resume Game button being pressed on the MainMenuScreen. The
	 * derived class is responsible for the restoring of the game from this
	 * GameData object that was previously loaded and changing to the correct
	 * screen for the resumed game.
	 */
	public abstract void resumeGame();

	/**
	 * Called when the game in progress should be reset (e.g. player needs to
	 * replay the current level). The derived class is responsible for
	 * resetting the game world state back in time and changing to the correct
	 * screen after reseting the game.
	 */
	public abstract void resetGame();
}
