package com.cosmicrover.cassini.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.cosmicrover.cassini.WorldData;
import com.cosmicrover.cassini.systems.RoverEventSystem;
import com.cosmicrover.cassini.systems.RoverInputSystem;
import com.cosmicrover.cassini.systems.EngineRenderSystem;
import com.cosmicrover.cassini.systems.RoverRenderSystem;
import com.cosmicrover.cassini.systems.MapSystem;
import com.cosmicrover.core.GameManager;
import com.cosmicrover.core.screens.AbstractLoadingScreen;
import com.cosmicrover.core.screens.AbstractScreen;

public class PlanetMapScreen extends AbstractScreen {
	/// Passive systems used only by this Screen
	private MapSystem mapSystem = null;
	private RoverEventSystem eventQueueSystem = null;
	private RoverInputSystem roverInputSystem = null;
	private RoverRenderSystem roverRenderSystem = null;
	private EngineRenderSystem engineRenderSystem = null;

	public PlanetMapScreen(GameManager gameManager, int screenId) {
		super("PlanetMapScreen", gameManager, screenId);

		// Retrieve our passive MapSystem here
		mapSystem = world.getSystem(MapSystem.class);

		// Create passive systems for any systems specific to this Screen 
		roverInputSystem = world.setSystem(new RoverInputSystem(), true);
		eventQueueSystem = world.setSystem(new RoverEventSystem(), true);
		roverRenderSystem = world.setSystem(new RoverRenderSystem(gameManager), true);
		//healthRenderSystem = world.setSystem(new HealthRenderSystem(screenWidth, screenHeight));
		engineRenderSystem = world.setSystem(new EngineRenderSystem(gameManager.getSpriteBatch()), true);
		
		// Note the creation of each screen in our debug log
		Gdx.app.debug("PlanetMapScreen", "Creating Planet Map screen");
	}

	@Override
	public void render(float delta) {
		// Use a basic gray color around the edges of the map
		Gdx.gl.glClearColor(0.5f, 0.5f, 0.5f, 1);
		Gdx.gl10.glClear(GL10.GL_COLOR_BUFFER_BIT);
		
		// Set the delta time since the last time render was called
		world.setDelta(delta);

		// Allow our world to perform updates and render to the screen
		world.process();
		
		// Handle our passive systems here
		roverInputSystem.process();     // Rover input processing
		eventQueueSystem.process();     // Event queue processing
		roverRenderSystem.process();    // Rover rendering
		engineRenderSystem.process();   // Engine statistics rendering
		mapSystem.process();            // Map loading processing
		
		// Switch to AssetDataLoadingScreen?
		if(mapSystem.isLoadingRequired()) {
			// Switch back to a full screen viewport
			Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			// Let our loading screen know which screen to switch back to
			AbstractLoadingScreen.setNextScreenId(WorldData.PLANET_MAP_SCREEN);
			// Switch to our Asset Loading Screen
			gameManager.setScreen(WorldData.ASSET_LOADING_SCREEN);
		}
	}

	@Override
	public void show() {
		// Call our base class implementation (sets the Back button screen)
		super.show();
		
		if(isFirstTime()) {
			// Clear our first time flag
			clearFirstTime();
		} else {
			// TODO: Enable existing entities used only for this Screen
		}
		
		// Add our InputProcessors to the InputMultiplexer
		gameManager.getInputMultiplexer().addProcessor(roverInputSystem);
	}

	@Override
	public void hide() {
		// TODO: Disable or remove entities created by show method above.

		// Remove our InputProcessors from the InputMultiplexer
		gameManager.getInputMultiplexer().removeProcessor(roverInputSystem);
	}

	@Override
	public void dispose() {
		// Remove our InputProcessors from the InputMultiplexer
		gameManager.getInputMultiplexer().removeProcessor(roverInputSystem);
	}
}
