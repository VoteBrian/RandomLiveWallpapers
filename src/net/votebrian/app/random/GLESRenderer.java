package net.votebrian.app.random;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.egl.EGLConfig;

import net.rbgrn.android.glwallpaperservice.*;

public class GLESRenderer
        implements GLWallpaperService.Renderer,
                   SharedPreferences.OnSharedPreferenceChangeListener/*,
                   SensorEventListener*/ {
    private Context mCtx;
    private Donut donut;
    private Atoms atoms;
    private Flag  flag;
    private Squares squares;
    private Tunnel tunnel;

    //.....................................................
    private SensorManager mSensorManager;
    private Sensor        mSensor;

    private int mViewW, mViewH;
    private float mViewAngle = 10f;

    int mFrame = 0;

    private int mWallpaperSelection = 0;

    private float mNearH = 0f;
    private float mNearW = 0f;
    private float mNearZ = 1f;
    private float mFarH  = 0f;
    private float mFarW  = 0f;
    private float mFarZ  = 30f;

    private float mSceneAngle = 0f;
    private final float mSceneAngleFactor = 110f;

    private float mAngleOffset = 1.0f;
    private float mSensorX = 0f;
    private float mSensorY = 0f;
    private float mSensorZ = 0f;

    public final int SS_SUNLIGHT = GL10.GL_LIGHT0;

    private FloatBuffer mPositionBuffer;
    private FloatBuffer mDiffuseBuffer;

    private SharedPreferences mPrefs;

    private float[] mBlack      = {0.00f, 0.00f, 0.00f, 1.0f};
    private float[] mBlue       = {(float)0/255,
                                   (float)153/255,
                                   (float)204/255,
                                   (float)255/255};
    private float[] mPurple     = {(float)153/255,
                                   (float)50/255,
                                   (float)204/255,
                                   (float)255/255};
    private float[] mGreen      = {(float)102/255,
                                   (float)153/255,
                                   (float)0/255,
                                   (float)255/255};
    private float[] mOrange     = {(float)255/255,
                                   (float)136/255,
                                   (float)0/255,
                                   (float)255/255};
    private float[] mRed        = {(float)204/255,
                                   (float)0/255,
                                   (float)0/255,
                                   (float)255/255};
    private float[] mWhite      = {(float)251/255,
                                   (float)248/255,
                                   (float)206/255,
                                   (float)255/255};
    private float[] mClearColor = {0.00f, 0.00f, 0.00f, 1.0f};

    private final int ATOMS   = 0;
    private final int DONUT   = 1;
    private final int FLAG    = 2;
    private final int SQUARES = 3;
    private final int TUNNEL  = 4;

    //.....................................................

    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // Create models
        donut   = new Donut(mCtx, gl);
        atoms   = new Atoms(mCtx, gl);
        flag    = new Flag(mCtx, gl);
        squares = new Squares(mCtx, gl);
        tunnel  = new Tunnel(mCtx, gl);

        mPrefs = mCtx.getSharedPreferences(RandomService.SHARED_PREFS_NAME, 0);
        mPrefs.registerOnSharedPreferenceChangeListener(this);
        onSharedPreferenceChanged(mPrefs, null);

        // mSensorManager = (SensorManager) mCtx.getSystemService(Context.SENSOR_SERVICE);
        // mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        // mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_UI);
    }

    public void onDrawFrame(GL10 gl) {
        gl.glClearColor(mClearColor[0], mClearColor[1], mClearColor[2], mClearColor[3]);
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        gl.glPushMatrix();

        switch(mWallpaperSelection) {
            case ATOMS:
                gl.glTranslatef(0f, 0f, -10f);
                // gl.glRotatef(-mSceneAngle, 0f, 1f, 0f);
                atoms.draw(gl);
                break;
            case DONUT:
                gl.glTranslatef( 0f, 0f, -17f);

                gl.glRotatef(30f - mSensorX*mAngleOffset, 1f, 0f, 0f);
                gl.glRotatef(mSensorZ*mAngleOffset + mSceneAngle, 0f, 1f, 0f);
                gl.glRotatef(-mSensorY*mAngleOffset, 0f, 0f, 1f);

                donut.draw(gl);
                break;
            case FLAG:
                gl.glTranslatef(0f, 0f, -6f);
                gl.glRotatef(15f, 0f, 0f, 1f);
                gl.glRotatef(-60f, 0f, 1f, 0f);
                flag.draw(gl);
                break;
            case SQUARES:
                gl.glTranslatef(0f, 0f, -9f);
                squares.draw(gl);
                break;
            case TUNNEL:
                // gl.glTranslatef(0f, 0f, -30f);
                tunnel.draw(gl);
                break;
        }

        gl.glPopMatrix();
    }

    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mViewW = width;
        mViewH = height;

        // intializations
        initLighting(gl);
        setDisplayProperties(gl);
        setProjection(gl);

        float screenHeight = 10f * (float)Math.tan(Math.toRadians(mViewAngle));
        float screenWidth = screenHeight * width / height;

        atoms.updateWallpaper(screenWidth, screenHeight);
        flag.updateWallpaper(screenWidth, screenHeight);
        squares.updateWallpaper(screenWidth, screenHeight);

        float radiusInner, radiusOuter;
        radiusInner = (float)Math.sqrt(Math.pow(mNearH, 2) + Math.pow(mNearW, 2));
        radiusOuter = (float)Math.sqrt(Math.pow(mFarH, 2) + Math.pow(mFarW, 2));
        tunnel.initialize(radiusInner, radiusOuter, mNearZ, mFarZ);
    }

    public void setContext(Context context) {
        mCtx = context;
    }

    /**
     * Called when the engine is destroyed. Do any necessary clean up because
     * at this point your renderer instance is now done for.
     */
    public void release() {
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        String color = sharedPreferences.getString("settings_bg_color", "black");

        if("white".equals(color)) {
            mClearColor = mWhite;
        } else if("black".equals(color)) {
            mClearColor = mBlack;
        } else if("blue".equals(color)) {
            mClearColor = mBlue;
        } else if("purple".equals(color)) {
            mClearColor = mPurple;
        } else if("green".equals(color)) {
            mClearColor = mGreen;
        } else if("orange".equals(color)) {
            mClearColor = mOrange;
        } else if("red".equals(color)) {
            mClearColor = mRed;
        }

        donut.setColor(mClearColor);
        squares.setColor(mClearColor);


        String wallpaper = sharedPreferences.getString("settings_wallpaper", "atoms");

        if("atoms".equals(wallpaper)) {
            mWallpaperSelection = ATOMS;
        } else if("donut".equals(wallpaper)) {
            mWallpaperSelection = DONUT;
        } else if("flag".equals(wallpaper)) {
            mWallpaperSelection = FLAG;
        } else if("squares".equals(wallpaper)) {
            mWallpaperSelection = SQUARES;
        } else if("tunnel".equals(wallpaper)) {
            mWallpaperSelection = TUNNEL;
        }
    }

    //.....................................................

    private void initLighting(GL10 gl) {
        float[] diffuse = { 1.0f, 1.0f, 1.0f, 1.0f };
        ByteBuffer mDbb = ByteBuffer.allocateDirect( diffuse.length * 4 );
        mDbb.order(ByteOrder.nativeOrder());
        mDiffuseBuffer = mDbb.asFloatBuffer();
        mDiffuseBuffer.put(diffuse);
        mDiffuseBuffer.position(0);

        float[] pos = { -5.0f, 0.0f, 0.0f, 1.0f };
        ByteBuffer mPbb = ByteBuffer.allocateDirect( pos.length * 4 );
        mPbb.order(ByteOrder.nativeOrder());
        mPositionBuffer = mPbb.asFloatBuffer();
        mPositionBuffer.put(pos);
        mPositionBuffer.position(0);

        gl.glLightfv(SS_SUNLIGHT, GL10.GL_POSITION, mPositionBuffer);
        gl.glLightfv(SS_SUNLIGHT, GL10.GL_DIFFUSE, mDiffuseBuffer);
        gl.glShadeModel(GL10.GL_FLAT);
        // gl.glEnable(GL10.GL_LIGHTING);
        gl.glDisable(GL10.GL_LIGHTING);
        // gl.glEnable(SS_SUNLIGHT);
    }

    private void setDisplayProperties(GL10 gl) {
        // Set background color
        // gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        gl.glClearColor(0.66f, 0.12f, 0.13f, 1.0f);
        // gl.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

        // Set to remove CW triangles
        gl.glEnable(GL10.GL_CULL_FACE);
        gl.glFrontFace(GL10.GL_CCW);
        gl.glCullFace(GL10.GL_FRONT);

        gl.glClearDepthf(1.0f);
        gl.glEnable(GL10.GL_DEPTH_TEST);
        gl.glDepthFunc(GL10.GL_LEQUAL);

        // set blend parameter
        gl.glEnable(GL10.GL_BLEND);
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

        gl.glDisable(GL10.GL_COLOR_MATERIAL);
    }

    private void setProjection(GL10 gl) {
        float ratio = (float) mViewW / (float) mViewH;

        // determine the "half-width" and "half-height" of our view at the near cutoff Z value stuff
        // stuff stuff
        mNearH = (float) (mNearZ * (Math.tan(Math.toRadians(mViewAngle))));
        mNearW = mNearH * ratio;

        mFarH = (float) (mFarZ * (Math.tan(Math.toRadians(mViewAngle))));
        mFarW = mFarH * ratio;

        gl.glViewport(0, 0, mViewW, mViewH);

        // Define orthographic projection
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glFrustumf(-mNearW, mNearW, -mNearH, mNearH, mNearZ, mFarZ);

        gl.glMatrixMode(GL10.GL_MODELVIEW);
    }

    public void updateAngle(float xOffset) {
        mSceneAngle = mSceneAngleFactor * xOffset;
    }

/*
    @Override
    public void onSensorChanged(SensorEvent event) {
        mSensorX = event.values[0];
        mSensorY = event.values[1];
        mSensorZ = event.values[2];
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int x) {
        // stuff
    }
*/
}