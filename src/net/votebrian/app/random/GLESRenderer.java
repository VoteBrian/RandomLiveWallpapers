package net.votebrian.app.random;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.egl.EGLConfig;

import net.rbgrn.android.glwallpaperservice.*;

public class GLESRenderer
        implements GLWallpaperService.Renderer,
                   SharedPreferences.OnSharedPreferenceChangeListener {
    Context mCtx;
    Donut donut;

    //.....................................................
    private int mViewW, mViewH;
    private float mViewAngle = 10f;

    int mFrame = 0;

    private float mNearH = 0f;
    private float mNearW = 0f;
    private float mNearZ = 1f;
    private float mFarZ  = 30f;

    public final int SS_SUNLIGHT = GL10.GL_LIGHT0;

    private FloatBuffer mPositionBuffer;
    private FloatBuffer mDiffuseBuffer;

    //.....................................................

    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // Create models
        donut = new Donut(mCtx, gl);
    }

    public void onDrawFrame(GL10 gl) {
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        gl.glPushMatrix();
        // gl.glRotatef(5f, 1f, 0f, 0f);
        gl.glTranslatef( 0f, 0f, -17f);
        gl.glRotatef(30f, 1f, 0f, 0f);
        // cube.draw(gl);
        donut.draw(gl);

        // gl.glTranslatef( 0f, 0f, -17f);
        gl.glPopMatrix();
    }

    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mViewW = width;
        mViewH = height;

        // intializations
        initLighting(gl);
        setDisplayProperties(gl);
        setProjection(gl);
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
        Log.v("RENDERER", "Key: " + key);
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

        gl.glViewport(0, 0, mViewW, mViewH);

        // Define orthographic projection
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glFrustumf(-mNearW, mNearW, -mNearH, mNearH, mNearZ, mFarZ);

        gl.glMatrixMode(GL10.GL_MODELVIEW);
    }
}