package com.cosmicrover.cassini.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Mapper;
import com.artemis.systems.EntityProcessingSystem;
import com.artemis.utils.ImmutableBag;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.cosmicrover.cassini.EntityFactory;
import com.cosmicrover.cassini.components.CameraComponent;
import com.cosmicrover.cassini.components.LocationComponent;
import com.cosmicrover.cassini.components.MapComponent;
import com.cosmicrover.cassini.components.PropertyComponent;
import com.cosmicrover.cassini.components.RoverEventComponent;
import com.cosmicrover.cassini.components.SpriteComponent;
import com.cosmicrover.cassini.components.ViewportComponent;
import com.cosmicrover.cassini.managers.GroupManager;
import com.cosmicrover.core.GameManager;

public class RoverRenderSystem extends EntityProcessingSystem {
	@Mapper ComponentMapper<CameraComponent> cameraMapper;
	@Mapper	ComponentMapper<LocationComponent> locationMapper;
	@Mapper ComponentMapper<SpriteComponent> spriteMapper;
	@Mapper ComponentMapper<MapComponent> mapMapper;
	@Mapper ComponentMapper<PropertyComponent> propertyMapper;
	@Mapper ComponentMapper<RoverEventComponent> roverEventMapper;
    @Mapper ComponentMapper<ViewportComponent> viewportMapper;

	// Radius to use for movement path
	private static final int PATH_RADIUS = 2;

	// GameManager class for retrieving various game wide resources
	private final GameManager gameManager;
	
	// ShapeRenderer for drawing map borders and other things
	private final ShapeRenderer shapeRenderer;
	
	// SpriteBatch for drawing multiple sprites in the same draw call
	private final SpriteBatch spriteBatch;

	// GroupManager which is used to retrieve the sprites and masks to draw
	private GroupManager groupManager = null;

	// Bag of sprites to be drawn
	ImmutableBag<Entity> sprites = null;

	// Bag of masks to be drawn
	ImmutableBag<Entity> masks = null;
	
    @SuppressWarnings("unchecked")
	public RoverRenderSystem(GameManager gameManager) {
		super(Aspect.getAspectForAll(
				CameraComponent.class,
				LocationComponent.class,
				MapComponent.class,
				RoverEventComponent.class,
				ViewportComponent.class));
		this.gameManager = gameManager;
		this.spriteBatch = gameManager.getSpriteBatch();
		this.shapeRenderer = gameManager.getShapeRenderer();
    }

	@Override
	protected void initialize() {
		// Retrieve the GroupManager object now
		groupManager = world.getManager(GroupManager.class);
	}
	
	@Override
	protected void begin() {
		// Retrieve all the sprites and masks to be displayed here
		sprites = groupManager.getEntities(EntityFactory.SPRITE_GROUP);
		super.begin();
	}

    @Override
	protected void process(Entity theEntity) {
		//System.out.println("RoverRenderSystem:entity("+theEntity.getId()+")");
		
		CameraComponent camera = cameraMapper.get(theEntity);
		LocationComponent location = locationMapper.get(theEntity);
		MapComponent map = mapMapper.get(theEntity);
		RoverEventComponent roverEvent = roverEventMapper.get(theEntity);
		ViewportComponent viewport = viewportMapper.get(theEntity);

		// Create our viewport first
		Gdx.gl.glViewport(viewport.x, viewport.y, viewport.width, viewport.height);
	
		// Retrieve the mapRenderer from our renderer component above
		OrthogonalTiledMapRenderer mapRenderer = gameManager.getMapRenderer(map.tiledMap);

		if(mapRenderer != null) {
			// Render the background tiles
			mapRenderer.setView(camera.getWorldCamera());
			mapRenderer.render(map.mapBackgroundLayers);
	
			// Set our world camera for drawing our sprites on the map
			spriteBatch.setProjectionMatrix(camera.getWorldCamera().combined);
			spriteBatch.begin();
			for(int i = 0, s = sprites.size(); s > i; i++) {
				// Retrieve the Entity by index
				Entity anEntity = sprites.get(i);

				// Retrieve the location component
				LocationComponent spriteLocation = locationMapper.get(anEntity);
				
				// Does our spriteLocation's map match this entities location map? then render it now
				if(location.getMapName().equals(spriteLocation.getMapName())) {
					// Retrieve the components for this sprite
					SpriteComponent sprite = spriteMapper.get(anEntity);
				
					// Maybe the textureRegion comes from our map
					if(sprite.textureRegion == null) {
						PropertyComponent property = propertyMapper.getSafe(anEntity);
						if(property != null) {
							sprite.textureRegion = map.tiledMap.getTileSets().getTile(property.tileId).getTextureRegion();
						} else {
							Gdx.app.error("RoverRenderSystem:process", "Null texture region");
							// Skip this sprite and move on
							continue;
						}
					}
					
					// Skip this sprite if it doesn't match the map we are showing now
					// Set the tint color for this sprite
					spriteBatch.setColor(sprite.tint);
					spriteBatch.draw(sprite.textureRegion, spriteLocation.getLevel().x, spriteLocation.getLevel().y);
				}
			}
			spriteBatch.end();
			
			// Render the foreground tiles
			mapRenderer.setView(camera.getWorldCamera());
			mapRenderer.render(map.mapForegroundLayers);
			
			shapeRenderer.setProjectionMatrix(camera.getWorldCamera().combined);
			shapeRenderer.begin(ShapeType.Filled);
			shapeRenderer.setColor(0, 0, 0, 1);
			// Render the masks for this player
			masks = groupManager.getEntities(
					EntityFactory.MASK_GROUP+
					location.getContextTag()+
					theEntity.getUuid());
			for(int i = 0, s = masks.size(); s > i; i++) {
				// Retrieve the Entity by index
				Entity anEntity = masks.get(i);
				
				// Retrieve the position and sprite components for this sprite
				LocationComponent locationMask = locationMapper.get(anEntity);
	
				// Draw opaque black rectangle on top of this square
				shapeRenderer.rect(locationMask.getLevel().x, locationMask.getLevel().y,
						location.getLevelGrid().x, location.getLevelGrid().y);
			}
			
			// Draw horizontal, vertical, and connecting circles for rover path on top of any masks
			shapeRenderer.setColor(0, 0, 1, 1);
			Vector2 anPrevious = null;
			for(Vector2 anVector : roverEvent.movePath) {
				if(anPrevious != null) {
					// Draw the previous vector as a dot
					// Change in Y but same X? then draw horizontal bar
					if(anVector.x == anPrevious.x && anVector.y != anPrevious.y) {
						if(anVector.y < anPrevious.y) {
							shapeRenderer.rect(
									anVector.x*location.getLevelGrid().x+location.getLevelOffsetCenter().x-PATH_RADIUS,
									anVector.y*location.getLevelGrid().y+location.getLevelOffsetCenter().y,
									PATH_RADIUS*2, anVector.dst(anPrevious)*location.getLevelGrid().y);
						} else {
							shapeRenderer.rect(
									anPrevious.x*location.getLevelGrid().x+location.getLevelOffsetCenter().x-PATH_RADIUS,
									anPrevious.y*location.getLevelGrid().y+location.getLevelOffsetCenter().y,
									PATH_RADIUS*2, anVector.dst(anPrevious)*location.getLevelGrid().y);
						}
					}
					// Change in X but same Y? then draw vertical bar
					else if(anVector.x != anPrevious.x && anVector.y == anPrevious.y) {
						if(anVector.x < anPrevious.x) {
							shapeRenderer.rect(
									anVector.x*location.getLevelGrid().x+location.getLevelOffsetCenter().x,
									anVector.y*location.getLevelGrid().y+location.getLevelOffsetCenter().y-PATH_RADIUS,
									anVector.dst(anPrevious)*location.getLevelGrid().x, PATH_RADIUS*2);
						} else {
							shapeRenderer.rect(
									anPrevious.x*location.getLevelGrid().x+location.getLevelOffsetCenter().x,
									anPrevious.y*location.getLevelGrid().y+location.getLevelOffsetCenter().y-PATH_RADIUS,
									anVector.dst(anPrevious)*location.getLevelGrid().x, PATH_RADIUS*2);
						}
					}
					shapeRenderer.circle(anPrevious.x*location.getLevelGrid().x+location.getLevelOffsetCenter().x,
							anPrevious.y*location.getLevelGrid().y+location.getLevelOffsetCenter().y, PATH_RADIUS, 10);
				}
	
				// Always draw the current movement point last
				shapeRenderer.circle(anVector.x*location.getLevelGrid().x+location.getLevelOffsetCenter().x,
						anVector.y*location.getLevelGrid().y+location.getLevelOffsetCenter().y, PATH_RADIUS, 10);
	
				// Keep track of the previous vector for the next loop iteration
				anPrevious = anVector;
			}
			anPrevious = null;
			shapeRenderer.end();
	
			// Render white border around the map limits
			shapeRenderer.begin(ShapeType.Line);
			shapeRenderer.setColor(1, 1, 1, 1);
			shapeRenderer.rect(location.getLevelBounds().x-1, location.getLevelBounds().y-1,
					location.getLevelBounds().width+2, location.getLevelBounds().height+2);
			
			// Draw path diagonal lines
			shapeRenderer.setColor(0, 0, 1, 1);
			for(Vector2 anVector : roverEvent.movePath) {
				if(anPrevious != null) {
					// Change in X and Y? then draw diagonal bar
					if(anVector.x != anPrevious.x && anVector.y != anPrevious.y) {
						for(float i=-PATH_RADIUS-0.25f; i<=PATH_RADIUS+0.25f; i+=0.25f) {
							shapeRenderer.line(anVector.x*location.getLevelGrid().x+location.getLevelOffsetCenter().x,
									           anVector.y*location.getLevelGrid().x+location.getLevelOffsetCenter().y+i,
									           anPrevious.x*location.getLevelGrid().x+location.getLevelOffsetCenter().x,
									           anPrevious.y*location.getLevelGrid().x+location.getLevelOffsetCenter().y+i);
						}
					}
				}
				// Keep track of the previous vector for the next loop iteration
				anPrevious = anVector;
			}
			anPrevious = null;
	
			// Draw radar
			if(roverEvent.scanInProgress) {
				// Draw rotating green to black fading lines
				for(float green = 0.0f; green <= MathUtils.PI2; green += 0.005f) {
					shapeRenderer.setColor(0,green/(2*MathUtils.PI2),0,1f);
					shapeRenderer.line(location.getLevel().x+location.getLevelOffsetCenter().x,
							location.getLevel().y+location.getLevelOffsetCenter().y,
							location.getLevel().x+location.getLevelOffsetCenter().x +
								32*5*MathUtils.cos(green+roverEvent.scanAngle),
							location.getLevel().y+location.getLevelOffsetCenter().y +
								32*5*MathUtils.sin(green+roverEvent.scanAngle));
				}
				// Draw circles indicating distances away from rover for radar scan
				for(int i=1; i<5; i++) {
					shapeRenderer.setColor(0,0.8f, 0.1f, 1);
					shapeRenderer.circle(location.getLevel().x+location.getLevelOffsetCenter().x,
							location.getLevel().y+location.getLevelOffsetCenter().y, i*location.getLevelGrid().x);
				}
				// Draw cross hairs for radar scan
				shapeRenderer.setColor(0,0.8f, 0.1f, 1);
				shapeRenderer.line(location.getLevel().x+location.getLevelOffsetCenter().x-5*location.getLevelGrid().x,
						location.getLevel().y+location.getLevelOffsetCenter().y,
						location.getLevel().x+location.getLevelOffsetCenter().x+5*location.getLevelGrid().x,
						location.getLevel().y+location.getLevelOffsetCenter().y);
				shapeRenderer.line(location.getLevel().x+location.getLevelOffsetCenter().x,
						location.getLevel().y+location.getLevelOffsetCenter().y-5*location.getLevelGrid().y,
						location.getLevel().x+location.getLevelOffsetCenter().x,
						location.getLevel().y+location.getLevelOffsetCenter().y+5*location.getLevelGrid().y);
				// Draw circles indicating distances away from rover for radar scan
				for(int i=1; i<=5; i++) {
					// White border on the last circle
					if(i == 5) {
						shapeRenderer.setColor(1, 1, 1, 1);					
					}
					shapeRenderer.circle(location.getLevel().x+location.getLevelOffsetCenter().x,
							location.getLevel().y+location.getLevelOffsetCenter().y, i*location.getLevelGrid().x);
				}
			}
			shapeRenderer.end();
		} // if(mapRenderer != null)
	}

    @Override
	protected void end() {
		super.begin();
		sprites = null;
		masks = null;
	}
}
