package com.cosmicrover.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.ExternalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Base64Coder;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;

public class GameDataLoader<T extends GameData> extends AsynchronousAssetLoader<T, GameDataLoader.Parameters<T>> {
	public GameDataLoader() {
		super(new ExternalFileHandleResolver());
	}

	/** Creates loader
	 * 
	 * @param resolver */
	public GameDataLoader (FileHandleResolver resolver) {
		super(resolver);
	}

	@Override
	public void loadAsync(AssetManager manager, String fileName,
			Parameters<T> parameter) {
		// Make sure a gameData object was provided for us to load into
		if(parameter.gameData != null) {
			FileHandle gameDataFile = resolve(fileName);
	
	        // check if the game data file exists
	        if( gameDataFile.exists() ) {
	        	Gdx.app.log( "GameDataLoader:loadAsync",
	        			"Retrieving data from '" + fileName + "'" );
	
	            // load the game data from the file
	            try {
	            	// Read data and convert it from Base64 back to text and parse
	            	parameter.gameData.read(new Json(), new JsonReader().parse(
	            			Base64Coder.decodeString(gameDataFile.readString())));
	            } catch( Exception e ) {
	                // log the exception
	            	Gdx.app.error( "GameDataLoader:loadAsync",
	            			"Error while parsing data from '" + fileName + "'", e);
	
	            	// Indicate this is a new game
		        	parameter.gameData.setNewGame(true);
	            }
	        } else {
	        	// Indicate that the game data file doesn't exist
	        	Gdx.app.log( "GameDataLoader:loadAsync",
	        			"Data file '" + fileName + "' doesn't exist" );
	
	        	// Indicate this is a new game
	        	parameter.gameData.setNewGame(true);
	        }
		} else {
			Gdx.app.error("GameDataLoader:loadAsync",
					"GameData parameter missing");
		}
	}

	@Override
	public T loadSync(AssetManager manager, String fileName,
			Parameters<T> parameter) {
		return parameter.gameData;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Array<AssetDescriptor> getDependencies(String fileName,
			Parameters<T> parameter) {
		// TODO: Replace this with a list of maps, textures, or other asset
		// resources that need to be loaded too
//		Array<AssetDescriptor> dependencies = new Array<AssetDescriptor>();
//		try {
//			FileHandle tmxFile = resolve(fileName);
//			root = xml.parse(tmxFile);
//			boolean generateMipMaps = (parameter != null ? parameter.generateMipMaps : false);
//			TextureLoader.TextureParameter texParams = new TextureParameter();
//			texParams.genMipMaps = generateMipMaps;
//			if (parameter != null) {
//				texParams.minFilter = parameter.textureMinFilter;
//				texParams.magFilter = parameter.textureMagFilter;
//			}
//			for (FileHandle image : loadTilesets(root, tmxFile)) {
//				dependencies.add(new AssetDescriptor(image.path(), Texture.class, texParams));
//			}
//			return dependencies;
//		} catch (IOException e) {
//			throw new GdxRuntimeException("Couldn't load tilemap '" + fileName + "'", e);
//		}
		return null;
	}

	public static class Parameters<T extends GameData> extends AssetLoaderParameters<T> {
		private T gameData = null;
		public Parameters(T gameData) {
			this.gameData = gameData;
		}
	}
}
