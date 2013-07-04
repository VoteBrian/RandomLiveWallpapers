package net.votebrian.app.random;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import net.rbgrn.android.glwallpaperservice.*;

// Original code provided by Robert Green
// http://www.rbgrn.net/content/354-glsurfaceview-adapted-3d-live-wallpapers
public class RandomService extends GLWallpaperService {
    Context mCtx;

    public static final String SHARED_PREFS_NAME = "random_settings";

    public RandomService() {
        super();
    }

    public Engine onCreateEngine() {
        MyEngine engine = new MyEngine();
        return engine;
    }

    class MyEngine extends GLEngine
            implements SharedPreferences.OnSharedPreferenceChangeListener {
        GLESRenderer renderer;

        public MyEngine() {
            super();

            // Application Context needed for InputStream openRawResource
            mCtx = getApplicationContext();

            // handle prefs, other initialization
            renderer = new GLESRenderer();
            setRenderer(renderer);
            renderer.setContext(mCtx);
            setRenderMode(RENDERMODE_CONTINUOUSLY);
        }

        public void onDestroy() {
            super.onDestroy();
            if (renderer != null) {
                renderer.release();
            }
            renderer = null;
        }

        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            // stuff
        }

        public void onOffsetsChanged (float xOffset, float yOffset,
                                      float xOffsetStep, float yOffsetStep,
                                      int xPixelOffset, int yPixelOffset) {
            renderer.updateAngle(xOffset - 0.5f);  // 0 offset at center screen
        }
    }
}