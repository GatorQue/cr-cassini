package com.cosmicrover.cassini;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.cosmicrover.core.GameEnvironment;
import com.cosmicrover.core.GameEnvironment.Platform;

public class MainActivity extends AndroidApplication {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
        cfg.useGL20 = false;
        
        initialize(new CassiniGame(new GameEnvironment(Platform.Android)), cfg);
    }
}