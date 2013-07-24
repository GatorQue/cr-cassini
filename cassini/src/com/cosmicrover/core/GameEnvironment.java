package com.cosmicrover.core;

public class GameEnvironment {
	public enum Platform {
		Unknown,
		Android,
		Applet,
		Desktop,
		HTML,
		iOS,
	};
	
	private Platform platform = Platform.Unknown;
	
	public GameEnvironment(Platform platform) {
		this.platform = platform;
	}

	public Platform getPlatform() {
		return platform;
	}
}
