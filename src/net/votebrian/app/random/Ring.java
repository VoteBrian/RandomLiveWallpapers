package net.votebrian.app.random;

import android.content.Context;
import android.util.Log;
import java.lang.Math;
import javax.microedition.khronos.opengles.GL10;
import java.util.Random;

public class Ring extends Model {
    private float mRadiusInner;
    private float mRadiusOuter;

    private float mRotation;
    private final float MAX_ROTATION = 30f;

    private float mOffsetX;
    private final float MAX_OFFSET = 0.5f;

    private float mOffsetY;

    private float mOffsetZ;

    private int mNumPoints = 7;

    private Random mRandom;

    public float[] mTempArray;
    public int[]   mTempIndices;

    private int mFrame = 0;

    private int mXFreq = 25;
    private int mYFreq = 40;

    private final int TRIANGLES_PER_POINT = 2;
    private final int VERTICES_PER_TRIANGLE = 3;
    private final int DIMS_PER_VERTICES = 3;
    private final int POINTS_PER_LINE = 2;

    private float[] ringColor = {0.984f, 0.973f, 0.807f, 0.15f};
    private float[] outlineColor = {0.1f, 0.1f, 0.12f, 0.8f};

    public Ring(Context context, GL10 gl) {
        super(context, gl);

        mRandom = new Random();

        initialize(0.02f, 1.0f);
    }

    public void initialize(float radiusInner, float radiusOuter) {
        setRadii(radiusInner, radiusOuter);

        mRotation = MAX_ROTATION * (mRandom.nextFloat() - 0.5f);
        // mOffsetX = MAX_OFFSET * (mRandom.nextFloat() - 0.5f);
        // mOffsetY = MAX_OFFSET * (mRandom.nextFloat() - 0.5f);

        buildVertices();
    }

    private void setRadii(float radiusInner, float radiusOuter) {
        mRadiusInner = radiusInner;
        mRadiusOuter = radiusOuter;
    }

    private void buildVertices() {
        int nextPoint = 0;
        int index = 0;

        mTempArray = new float[mNumPoints * TRIANGLES_PER_POINT * VERTICES_PER_TRIANGLE * DIMS_PER_VERTICES];
        mTempIndices = new int[mNumPoints * POINTS_PER_LINE];

        for(int point = 0; point < mNumPoints; point++) {
            index = point * TRIANGLES_PER_POINT * VERTICES_PER_TRIANGLE * DIMS_PER_VERTICES;

            nextPoint = point + 1;
            if(nextPoint == mNumPoints) {
                nextPoint = 0;
            }

            mTempArray[index] = mRadiusInner * (float)Math.cos( 2f * Math.PI * point / mNumPoints);
            mTempArray[index + 1] = mRadiusInner * (float)Math.sin( 2f * Math.PI * point / mNumPoints);
            mTempArray[index + 2] = 0f;

            mTempArray[index + 3] = mRadiusInner * (float)Math.cos( 2f * Math.PI * nextPoint / mNumPoints);
            mTempArray[index + 4] = mRadiusInner * (float)Math.sin( 2f * Math.PI * nextPoint / mNumPoints);
            mTempArray[index + 5] = 0f;

            mTempArray[index + 6] = mRadiusOuter * (float)Math.cos( 2f * Math.PI * point / mNumPoints);
            mTempArray[index + 7] = mRadiusOuter * (float)Math.sin( 2f * Math.PI * point / mNumPoints);
            mTempArray[index + 8] = 0f;


            mTempArray[index + 9] = mRadiusOuter * (float)Math.cos( 2f * Math.PI * point / mNumPoints);
            mTempArray[index + 10] = mRadiusOuter * (float)Math.sin( 2f * Math.PI * point / mNumPoints);
            mTempArray[index + 11] = 0f;

            mTempArray[index + 12] = mRadiusInner * (float)Math.cos( 2f * Math.PI * nextPoint / mNumPoints);
            mTempArray[index + 13] = mRadiusInner * (float)Math.sin( 2f * Math.PI * nextPoint / mNumPoints);
            mTempArray[index + 14] = 0f;

            mTempArray[index + 15] = mRadiusOuter * (float)Math.cos( 2f * Math.PI * nextPoint / mNumPoints);
            mTempArray[index + 16] = mRadiusOuter * (float)Math.sin( 2f * Math.PI * nextPoint / mNumPoints);
            mTempArray[index + 17] = 0f;

            mTempIndices[(point * POINTS_PER_LINE)] = TRIANGLES_PER_POINT * VERTICES_PER_TRIANGLE * point;
            mTempIndices[(point * POINTS_PER_LINE) + 1] = TRIANGLES_PER_POINT * VERTICES_PER_TRIANGLE * point + 1;
        }

        super.setVertices(mTempArray);
        super.setModelColor(ringColor);

        super.setOutlineIndices(mTempIndices);
        super.setOutlineColor(outlineColor);
        super.enableOutline();
    }

    @Override
    public void draw(GL10 gl) {
        gl.glTranslatef(mOffsetX, mOffsetY, mOffsetZ);
        gl.glRotatef(0f, 0f, 1f, mRotation);
        super.draw(gl);
    }

    public void setZOffset(float z) {
        mOffsetZ = z;
    }

    public float getZOffset(float z) {
        return mOffsetZ;
    }

    public void setFrame(int frame) {
        mFrame = frame;
    }

    public int getFrame() {
        return mFrame;
    }

    public void setOffsets(int count) {
        mOffsetX = 0.12f * (float)Math.sin(2*Math.PI*count/mXFreq);
        mOffsetY = 0.15f * (float)Math.cos(2*Math.PI*count/mYFreq);
    }
}