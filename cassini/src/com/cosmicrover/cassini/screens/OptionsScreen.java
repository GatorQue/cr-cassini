package com.cosmicrover.cassini.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.cosmicrover.core.GameManager;
import com.cosmicrover.core.screens.AbstractScreen;

public class OptionsScreen extends AbstractScreen {
	/// Scene2d used by this Screen
	private Stage stage = null;
	private Image background = null;
	private Table table = null;
	private Label title = null;
	private TextButton abandonButton = null;
	private TextButton backButton = null;

	public OptionsScreen(GameManager gameManager, int screenId) {
		super("OptionsScreen", gameManager, screenId);
		
		// Note the creation of each screen in our debug log
		Gdx.app.debug("SettingsScreen", "Creating Options screen");
	}
	@Override
	public void render(float delta) {
		// Use a basic gray color around the edges of the map
		Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1);
		Gdx.gl10.glClear(GL10.GL_COLOR_BUFFER_BIT);
		
		// Set the delta time since the last time render was called
		world.setDelta(delta);

		// Allow our world to perform updates and render to the screen
		world.process();
		
		// Handle our passive systems here
		
		// Handle our Stage2d processing here
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
        
        // Draw debug lines for table
        Table.drawDebug(stage);
	}

	@Override
	public void show() {
		// Call our base class implementation (sets our Back button screen)
		super.show();
		
		if(isFirstTime()) {
			// Clear our first time flag
			clearFirstTime();
			
			// Create our stage object and buttons
			stage = new Stage(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true, gameManager.getSpriteBatch());
			
			// Create a background image and have it fill the screen/stage
			background = new Image();
			background.setFillParent(true);

			// Create a single layout table and have it fill the screen/stage
	        table = new Table();
	        table.setFillParent(true);
	        stage.addActor(background);
	        stage.addActor(table);
	        
	        // Retrieve the font we will use for text messages
			Texture fontTexture = new Texture(Gdx.files.internal("textures/font_normal.png"));
			fontTexture.setFilter(TextureFilter.Linear, TextureFilter.MipMapLinearLinear);
			TextureRegion fontRegion = new TextureRegion(fontTexture);
			BitmapFont labelFont = new BitmapFont(Gdx.files.internal("fonts/normal.fnt"), fontRegion, false);
			BitmapFont buttonFont = new BitmapFont(Gdx.files.internal("fonts/normal.fnt"), fontRegion, false);
			buttonFont.setUseIntegerPositions(false);

			// Create our LabelStyle
			LabelStyle labelStyle = new LabelStyle();
			labelStyle.font = labelFont;
			labelStyle.fontColor = Color.CYAN;

			// Create and add our Title Label
			title = new Label("Settings", labelStyle);
			table.center().add(title).expandX();
			table.row();

			// Create our TextButtonStyle
			TextButtonStyle buttonStyle = new TextButtonStyle();
			//style.up = new TextureRegionDrawable(upRegion);
			//style.down = new TextureRegionDrawable(downRegion);
			buttonStyle.font = buttonFont;
			buttonStyle.fontColor = Color.WHITE;
			buttonStyle.overFontColor = Color.YELLOW;
			
			// Create our button listener
			ButtonListener buttonListener = new ButtonListener();
			
			// Add our back button last of all
			if(!gameManager.getData().isNewGame()) {
				abandonButton = new TextButton("Abandon Game", buttonStyle);
				abandonButton.addListener(buttonListener);
				table.right().add(abandonButton).expandY().fill();
				table.row();
			}

			// Add our back button last of all
			backButton = new TextButton("Back", buttonStyle);
			backButton.addListener(buttonListener);
			table.right().add(backButton).expandY().fill();

			// Enable table debug lines
			table.debug();
		} else {
			// TODO: Enable existing entities used only for this Screen
		}

		// Add our Scene2d as an input processor
		gameManager.getInputMultiplexer().addProcessor(stage);
	}

	@Override
	public void hide() {
		// TODO: Disable or remove entities created by show method above.

		// Remove our Scene2d as an input processor
		gameManager.getInputMultiplexer().removeProcessor(stage);
	}

	@Override
	public void resize(int width, int height) {
		// Handle any resizing needed here
		stage.setViewport(width, height, true);
	}

	@Override
	public void dispose() {
		// Remove our Scene2d as an input processor
		gameManager.getInputMultiplexer().removeProcessor(stage);
		
		// Give stage a chance to dispose itself
		stage.dispose();
	}
	
	/**
	 * ButtonListener is a general purpose button listener for all the buttons
	 * in the MainMenuScreen.
	 */
	private class ButtonListener extends ChangeListener {
		@Override
		public void changed(ChangeEvent event, Actor actor) {
			if(actor.equals(abandonButton)) {
				Gdx.app.log("OptionsScreen:ButtonListener", "Abandon Game");
				gameManager.getData().resetGame();
				actor.remove();
			} else if(actor.equals(backButton)) {
				Gdx.app.log("OptionsScreen:ButtonListener", "Back");
				gameManager.setScreen(getBackScreenId());
			}
		}
	}
}
