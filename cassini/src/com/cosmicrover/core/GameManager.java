package com.cosmicrover.core;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.Base64Coder;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Json;
import com.cosmicrover.core.GameEnvironment.Platform;

public class GameManager implements Disposable {
	public static final String DATA_DIRECTORY = ".cosmicrover/";
	
    private final AssetManager assetManager;
	private final Game game;
    private final GameEnvironment gameEnvironment;
	private final InputMultiplexer inputMultiplexer;
	private SpriteBatch spriteBatch = null;
	private ShapeRenderer shapeRenderer = null;
	private OrthogonalTiledMapRenderer mapRenderer = null;
    private GameData gameData = null;
	private BackButtonHandler backButtonHandler = null;
    private String dataFilename;
    
    /**
     * Creates the GameManager class that is responsible for providing
     * game data persistence services (save/load of game data information).
     * @param[in] the Game derived parent class needed to change Screens
     * @param[in] the GameEnvironment class used to provide environment information
     */
    public GameManager(Game game, GameEnvironment gameEnvironment) {
    	this(game, gameEnvironment, "default.dat");
    }
    
    /**
     * Creates the persistence service.
     */
    public GameManager(Game game, GameEnvironment gameEnvironment, String gameFilename) {
    	this.assetManager = new AssetManager();
    	this.game = game;
    	this.gameEnvironment = gameEnvironment;
    	this.inputMultiplexer = new InputMultiplexer();
    	setDataFilename(gameFilename);
    }
    
    /**
	 * @return the assetManager
	 */
	public final AssetManager getAssetManager() {
		return assetManager;
	}

	public final InputMultiplexer getInputMultiplexer() {
		return inputMultiplexer;
	}

	/**
	 * @return the spriteBatch
	 */
	public final SpriteBatch getSpriteBatch() {
		if(spriteBatch == null) {
			spriteBatch = new SpriteBatch();
		}
		return spriteBatch;
	}
	
	/**
	 * @return the shapeRenderer
	 */
	public final ShapeRenderer getShapeRenderer() {
		if(shapeRenderer == null) {
			shapeRenderer = new ShapeRenderer();
		}
		return shapeRenderer;
	}
	
	/**
	 * @return the OrthogonalTiledMapRenderer
	 */
	public final OrthogonalTiledMapRenderer getMapRenderer(TiledMap tiledMap) {
		if(tiledMap != null) {
			if(mapRenderer == null) {
				mapRenderer = new OrthogonalTiledMapRenderer(tiledMap, getSpriteBatch());
			} else {
				// Change the tiledMap to use for our renderer
				mapRenderer.setMap(tiledMap);
			}
		}
		return mapRenderer;
	}

	/**
     * Returns the GameEnvironment class provided at construction time.
     * @return GameEnvironment class used to determine the runtime environment
     */
    public final GameEnvironment getEnvironment() {
    	return gameEnvironment;
    }

    /**
     * Changes the current screen being displayed to the screenId specified.
     * @param screenId to change to
     */
    public final void setScreen(int screenId) {
    	// Does the user want to exit the application? then schedule an exit now
    	if(GameData.EXIT_GAME_SCREEN == screenId) {
    		// Log the application exit request and schedule an exit now
        	Gdx.app.log( "GameManager:setScreen()", "Exit application requested");
    		Gdx.app.exit();
    	} else {
    		// Log the change of screens and change screens
        	Gdx.app.log( "GameManager:setScreen()", "Changing to screenId="+screenId);
    		game.setScreen(gameData.getScreen(screenId));
    	}
    }
    
    /**
     * Sets the screenId to use when the Back button is pressed.
     * @param screenId to use on Back button
     */
    public final void setBackScreen(int screenId) {
    	if(backButtonHandler != null) {
    		backButtonHandler.setScreenId(screenId);
    	}
    }
    
    /**
     * Sets the back button handling to the value of enabled. Sometimes you
     * want to disable the back button (loading screen, credits, etc) from
     * changing screens. Other times you want to handle the back button on
     * your own. This class allows you to disable the default back button
     * handler.
     * @param enabled for back button handling
     */
    public final void setBackScreenHandling(boolean enabled) {
    	if(backButtonHandler != null) {
    		backButtonHandler.setEnabled(enabled);
    	}
    }

    /**
     * This method is responsible for setting theFilename specified as the
     * filename to save the game data information into.
     * @param theFilename to use for saving data
     */
    public final void setDataFilename(String theFilename) {
    	this.dataFilename = theFilename;
    }
    
    /**
     * This method will delete the existing game data file. This is typically
     * called when the game or level has ended and you don't want the
     * application to restore the game back to the last saved point before the
     * game ended.
     */
    public final void deleteData() {
    	FileHandle gameDataFile = Gdx.files.external(dataFilename);
    	if(gameDataFile.exists()) {
        	Gdx.app.log( "GameManager:deleteData()",
        			"Deleting data file '" + dataFilename + "'" );

        	// Delete the game data file
    		gameDataFile.delete();

    		// Set our new game flag on our game data and call the resetGame method
    		gameData.setNewGame(true);
    		
    		// Indicate that we need to create a new game
    		gameData.resetGame();
    	}
    }

    /**
     * This method is responsible for returning the GameData version of the
     * derived class registered earlier.
     * @return GameData derived class loaded from a file or newly created.
     */
    public final GameData getData() {
    	return getData(GameData.class);
    }

    /**
     * This method is responsible for returning the GameData derived class that
     * is either read from the file or created using the createGameData method
     * above.
     * @return GameData derived class, type, loaded from a file or newly created.
     */
	public final <T extends GameData> T getData(Class<T> type) {
        // Return the gameData previously restored or the gameData created above
		return type.cast(gameData);
    }
    
    /**
     * This method is responsible for creating the GameData derived class type
     * specified and calling its initGame() method.
     * 
     * @param[in] type of GameData derived class to create
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public final <T extends GameData> void initData(Class<T> type) {
	    // if the game data is already loaded, just return it
        if( gameData == null ) {
        	// Log the creation of the GameData derived class
        	Gdx.app.log( "GameManager:initData("+type.getName()+")", "Creating GameData object...");

        	// Attempt to create a new instance of gameDataType
        	try {
        		// Create an instance of the class type specified
    			gameData = type.newInstance();
    			
    			// Do we need a back button handler?
            	if(Platform.Android == gameEnvironment.getPlatform() ||
            	   Platform.iOS == gameEnvironment.getPlatform()) {
            		// Create our Back button handler
            		backButtonHandler = new BackButtonHandler(this);
            		
            		// Register our handler with our InputMultiplexer
            		inputMultiplexer.addProcessor(backButtonHandler);

            		// Make sure we catch the Back key (Android/iOS) input events
            		Gdx.app.getInput().setCatchBackKey(true);
            	}
            	
    			// Call the init method on this new instance
    			gameData.init(this);
    			
    			// Add ourselves as a handler for the GameData class
    			assetManager.setLoader(type, new GameDataLoader<T>());

    			// Add the data to our assetMaanger to load next
    	    	assetManager.load(DATA_DIRECTORY+dataFilename, type,
    	    			new GameDataLoader.Parameters(gameData));
    		} catch (InstantiationException ie) {
    			// Log the exception
            	Gdx.app.error( "GameManager:initData(" + type.getName() + ")",
            			"Unable to create game data object", ie);
    		} catch (IllegalAccessException iae) {
    			// Log the exception
            	Gdx.app.error( "GameManager:initData(" + type.getName() + ")",
            			"Unable to create game data object", iae);
    		}
        } else {
			// Log the exception
        	Gdx.app.error( "GameManager:initData(" + type.getName() + ")",
        			"Called twice");
        }

        // Make our inputMultiplexer the primary input listener
	    Gdx.input.setInputProcessor(this.inputMultiplexer);
    }
 
    /**
     * Determines if the background thread has finished loading the game data
     * from a file.
     * @return true if the background thread has completed, false otherwise
     */
    public final boolean isDataLoaded() {
    	return assetManager.isLoaded(DATA_DIRECTORY + dataFilename);
    }
 
    /**
     * This method is responsible for saving the game data previously
     * created or restored above.
     */
    public final void saveData() {
    	// Call our overloaded method with the current default filename
    	saveData(DATA_DIRECTORY + dataFilename);
    }

    /**
     * This method is responsible for saving the game data previously created
     * or restored above.
     * @param filename and path to where the data will be saved.
     */
    public final void saveData(String filename) {
    	if(gameData != null) {
    		// Do we have a game in progress to save?
    		if(gameData.isInProgress()) {
	        	Gdx.app.log( "GameManager:saveData", "Saving data to '" + filename + "'" );
	
	            // create the JSON utility object
	            Json json = new Json();
	
	            // create the handle for the profile data file
	            FileHandle gameDataFile = Gdx.files.external(filename );
	
	            // Attempt to compress and save the game data
	            try {
	                // Retrieve data and encode as Base64 and write it to a file
	                gameDataFile.writeString(Base64Coder.encodeString(json.toJson(gameData)), false);
	    		} catch (Exception e) {
	                // log the exception
	            	Gdx.app.error( "GameManager:saveData",
	            			"Unable to save data file '" + filename + "'", e);
	    		}
    		} else {
    			Gdx.app.debug("GameManager:saveData", "Game not in progress, skipping save");
    		}
    	} else {
        	Gdx.app.log( "GameManager:saveData",
        			"Data missing, did you forget to call initData first?" );
    	}
    }

	@Override
	public void dispose() {
	    assetManager.dispose();
	    if(spriteBatch != null) {
	    	spriteBatch.dispose();
	    }
	    if(shapeRenderer != null) {
			shapeRenderer.dispose();
	    }
	    if(mapRenderer != null) {
			mapRenderer.dispose();
	    }
	    if(gameData != null) {
	    	gameData = null;
	    }
	};

	protected final class BackButtonHandler implements InputProcessor {
    	/// The parent class responsible for handling changing of screens
    	private final GameManager gameManager;
    	
    	/// The screenId to use when back button is pressed (defaults to exit game)
    	private int screenId = GameData.EXIT_GAME_SCREEN;
    	
    	/// Is the back button response currently enabled?
    	private boolean enabled = true;
    	
    	public BackButtonHandler(GameManager gameManager) {
    		this.gameManager = gameManager;
    	}
    	
    	/**
    	 * Set the screenId to switch to when the back button is pressed (use
    	 * GameData.EXIT_GAME_SCREEN if you want the program to exit instead).
    	 * @param screenId to change to when Back button is pressed
    	 */
    	public void setScreenId(int screenId) {
    		this.screenId = screenId;
    	}

    	/**
    	 * Enable or disable the handling of the back button by this input
    	 * processor class.
    	 * @param enabled value to use
    	 */
    	public void setEnabled(boolean enabled) {
    		this.enabled = enabled;
    	}

    	/////////////////////////////////////////////////////////////////////////
    	// Keyboard inputs
    	/////////////////////////////////////////////////////////////////////////
		@Override
		public boolean keyDown(int keycode) {
			return false;
		}

		@Override
		public boolean keyUp(int keycode) {
			boolean consumed = false;

			if(keycode == Input.Keys.BACK && enabled) {
				// Set our screen to the backScreenId set earlier
				gameManager.setScreen(screenId);
				
				// Set our consumed flag to true
				consumed = true;
			}

			// Return the consumed flag set above
			return consumed;
		}

		@Override
		public boolean keyTyped(char character) {
			return false;
		}

		/////////////////////////////////////////////////////////////////////////
		// Mouse/Touch inputs
		/////////////////////////////////////////////////////////////////////////
		@Override
		public boolean touchDown(int screenX, int screenY, int pointer,
				int button) {
			return false;
		}

		@Override
		public boolean touchUp(int screenX, int screenY, int pointer, int button) {
			return false;
		}

		@Override
		public boolean touchDragged(int screenX, int screenY, int pointer) {
			return false;
		}

		@Override
		public boolean mouseMoved(int screenX, int screenY) {
			return false;
		}

		@Override
		public boolean scrolled(int amount) {
			return false;
		}
    }
}
