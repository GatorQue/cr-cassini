package com.cosmicrover.cassini;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.cosmicrover.core.GameEnvironment;
import com.cosmicrover.core.GameEnvironment.Platform;

public class Main {
	public static void main(String[] args) {
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "cassini";
		cfg.useGL20 = false;
		cfg.width = 480;
		cfg.height = 320;
		
		new LwjglApplication(new CassiniGame(new GameEnvironment(Platform.Desktop)), cfg);
	}
}
