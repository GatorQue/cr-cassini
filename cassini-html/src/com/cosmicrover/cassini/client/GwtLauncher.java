package com.cosmicrover.cassini.client;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import com.cosmicrover.cassini.CassiniGame;
import com.cosmicrover.core.GameEnvironment;
import com.cosmicrover.core.GameEnvironment.Platform;

public class GwtLauncher extends GwtApplication {
	@Override
	public GwtApplicationConfiguration getConfig () {
		GwtApplicationConfiguration cfg = new GwtApplicationConfiguration(480, 320);
		return cfg;
	}

	@Override
	public ApplicationListener getApplicationListener () {
		return new CassiniGame(new GameEnvironment(Platform.HTML));
	}
}