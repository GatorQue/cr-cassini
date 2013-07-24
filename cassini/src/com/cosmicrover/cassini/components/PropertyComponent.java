package com.cosmicrover.cassini.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.cosmicrover.core.components.AbstractComponent;

public class PropertyComponent extends AbstractComponent {
	public enum Type {
		Unknown,
		Artifact,
		Gem,
		Glass,
		Ice,
		Life,
		Liquid,
		Mineral,
		Radioactive,
		Rock,
		Thing,
	};
	
	public enum Color {
		Unknown,
		Black,
		Blue,
		Bronze,
		Brown,
		Copper,
		Flashy,
		Gold,
		Gray,
		Green,
		Multicolor,
		Orange,
		Pink,
		Purple,
		Red,
		Silver,
		Transparent,
		Violet,
		White,
		Yellow,
	};
	
	public enum Intensity {
		Unknown,
		Bright,
		Dim,
		Low,
		High,
	};

	public enum Shape {
		Unknown,
		Circular,
		Cylindrical,
		Flat,
		Globular,
		Irregular,
		Rectangular,
		Spherical,
		Symmetrical,
	};
	
	public enum Size {
		Unknown,
		Small,
		Large,
		Medium,
	};
	
	public enum Surface {
		Unknown,
		Shiny,
		Reflective,
		Metallic
	};
	
	public enum Sound {
		Unknown,
		TickTock,
		Exterminate,
		BarkBark,
		HissHiss,
	};
	
	public Type type;
	public float mass;
	public float volume;
	public int worth;
	public Color color;
	public Intensity intensity;
	public Shape shape;
	public Size size;
	public Sound sound;
	
	public PropertyComponent() {
		type = Type.Unknown;
		mass = 0.0f;
		volume = 0.0f;
		worth = 0;
		color = Color.Unknown;
		intensity = Intensity.Unknown;
		shape = Shape.Unknown;
		size = Size.Unknown;
		sound = Sound.Unknown;
	}

	@Override
	public void write(Json json) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void read(Json json, JsonValue jsonData) {
		// TODO Auto-generated method stub
		
	}
}
