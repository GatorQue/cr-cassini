package com.cosmicrover.cassini;

import com.badlogic.gdx.Game;
import com.cosmicrover.cassini.utils.WorldData;
import com.cosmicrover.core.GameEnvironment;
import com.cosmicrover.core.GameManager;

public class CassiniGame extends Game {
	/// Our persistence service class
	private final GameManager gameManager;
	
	public CassiniGame(GameEnvironment gameEnvironment) {
		this.gameManager = new GameManager(this, gameEnvironment, "cassini.dat");
	}
	
	@Override
	public void create() {
		// Step 1: Add any additional details to our environment (graphics,
		// Gamepad controllers, etc)
		// @TODO: Add detection of game environment information
		// gameManager.getEnvironment().setJoysticks(joystickCount);

		// Step 2: Initialize the game data
		gameManager.initData(WorldData.class);
		
		// Step 3: Set our first screen object and return to caller
		setScreen(gameManager.getData().getInitialScreen());
	}

	@Override
	public void pause() {
		// Call our super class first
		super.pause();

		// Persist our game data settings to disk
		gameManager.saveData();
	}

	@Override
	public void dispose() {
		// Call our super class first
		super.dispose();
		
		// Give our GameManager a chance to dispose itself
		gameManager.dispose();
	}
}
