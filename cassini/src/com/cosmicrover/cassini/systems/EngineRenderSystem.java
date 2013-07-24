package com.cosmicrover.cassini.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Mapper;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.cosmicrover.cassini.components.CameraComponent;
import com.cosmicrover.cassini.components.ViewportComponent;

public class EngineRenderSystem extends EntityProcessingSystem {
	@Mapper ComponentMapper<CameraComponent> cameraMapper;
	@Mapper ComponentMapper<ViewportComponent> viewportMapper;

	// Batch up multiple sprite and font draw calls
	private SpriteBatch spriteBatch;
	// Font to use to display information
	private BitmapFont font;

	@SuppressWarnings("unchecked")
	public EngineRenderSystem(SpriteBatch spriteBatch) {
		super(Aspect.getAspectForAll(
				CameraComponent.class,
				ViewportComponent.class));
		this.spriteBatch = spriteBatch;
	}

	@Override
	protected void initialize() {
		// Retrieve the font we will use for text messages
		Texture fontTexture = new Texture(Gdx.files.internal("textures/font_normal.png"));
		fontTexture.setFilter(TextureFilter.Linear, TextureFilter.MipMapLinearLinear);
		TextureRegion fontRegion = new TextureRegion(fontTexture);
		font = new BitmapFont(Gdx.files.internal("fonts/normal.fnt"), fontRegion, false);
		font.setUseIntegerPositions(false);
	}

	@Override
	protected void process(Entity theEntity) {
		CameraComponent camera = cameraMapper.get(theEntity);
		ViewportComponent viewport = viewportMapper.get(theEntity);

		// Create our viewport first
		Gdx.gl.glViewport(viewport.x, viewport.y, viewport.width, viewport.height);

		// Render our Heads Up Display
		spriteBatch.setProjectionMatrix(camera.getHudCamera().combined);
		spriteBatch.begin();
		spriteBatch.setColor(1, 1, 1, 1);
		font.draw(spriteBatch, "FPS: " + Gdx.graphics.getFramesPerSecond(),
				-(viewport.width / 2) + 20, viewport.height / 2 - 20);
		font.draw(spriteBatch, "Active entities: " + world.getEntityManager().getActiveEntityCount(),
				-(viewport.width / 2) + 20, viewport.height / 2 - 40);
		font.draw(spriteBatch, "Total created: " + world.getEntityManager().getTotalCreated(),
				-(viewport.width / 2) + 20, viewport.height / 2 - 60);
		font.draw(spriteBatch, "Total deleted: " + world.getEntityManager().getTotalDeleted(),
				-(viewport.width / 2) + 20, viewport.height / 2 - 80);
		spriteBatch.end();
	}
}
