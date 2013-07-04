package net.votebrian.app.random;

import android.content.Context;
import android.util.Log;
import java.lang.Math;
import javax.microedition.khronos.opengles.GL10;

public class Flag {
    private Context mCtx;
    private GL10 mGl;

    private int VERTS_PER_TRIANGLE = 3;
    private int DIMS_PER_VERT = 3;
    private int POINTS_PER_LINE = 2;

    private Model[] mCircles;
    private Model[] mDots;
    private float[] mVertices;
    private float[] mTempVertices;

    private int[] mIndices;

    private float mWidth;
    private float mHeight;

    private float mScreenWidth = 2;
    private float mScreenHeight = 2;

    private float mOffsetX, mOffsetY;

    private int counter = 0;
    private int delay = 60;

    private int mNumRows = 0;
    private int mNumCols = 30;

    private int mNumAtomPoints = 16;
    private int mNumDotPoints = 8;

    private float mAtomRadius = 0.2f;
    private float mDotRadius  = 0.02f;
    private float mDeltaPhase = 0.05f;

    private int row, col;

    // private float[] outlineAtomColor = {0.8f, 0.8f, 0.8f, 0.3f};
    private float[] outlineAtomColor = {0.9f, 0.9f, 0.9f, 0.3f};
    private float[] outlineDotColor = {0.8f, 0.8f, 0.8f, 0.5f};

    private float[] redDotColor   = {0.8f, 0.2f, 0.2f, 0.8f};
    private float[] whiteDotColor = {0.9f, 0.9f, 0.9f, 0.8f};
    private float[] blueDotColor  = {0.2f, 0.2f, 0.8f, 0.8f};

    public Flag(Context context, GL10 gl) {
        mCtx    = context;
        mGl     = gl;

        initializeWallpaper();
    }

    public void updateWallpaper(float screenWidth, float screenHeight) {
        mScreenWidth = 1.25f * screenWidth;
        mScreenHeight = 0.7f * screenHeight;

        mAtomRadius = 2 * mScreenWidth / (0.9f + (1.1f * mNumCols));
        mDotRadius = mAtomRadius/5;
        // mNumRows = (int)Math.floor(2*mScreenHeight/mAtomRadius);
        mNumRows = 13;

        initializeWallpaper();
    }

    private void initializeWallpaper() {

        mCircles = new Model[mNumRows * mNumCols];
        mDots    = new Model[mNumRows * mNumCols];

        for(row = 0; row < mNumRows; row++) {
            for(col = 0; col < mNumCols; col++) {
                int index = row*mNumCols + col;

                mCircles[index] = new Model(mCtx, mGl);
                mCircles[index].setVertices(initializeCircle());
                mCircles[index].setPosition( (-1f*(mScreenWidth-mAtomRadius)) + (float)(1.1 * mAtomRadius * col), (mScreenHeight-mAtomRadius) - (float)(1.1 * mAtomRadius * row), 0f);
                mCircles[index].setModelColor(outlineAtomColor);

                mCircles[index].setOutlineColor(outlineAtomColor);
                mCircles[index].setOutlineIndices(initializeCircleIndices());
                mCircles[index].enableOutline();

                mDots[index] = new Model(mCtx, mGl);
                mOffsetX = (float)mAtomRadius * (float)Math.cos( (2*Math.PI) * (mDeltaPhase * (row + col)));
                mOffsetY = (float)mAtomRadius * (float)Math.sin( (2*Math.PI) * (mDeltaPhase * (row + col)));
                mDots[index].setVertices(initializeDot(mOffsetX, mOffsetY));
                mDots[index].setPosition( (-1f*(mScreenWidth-mAtomRadius)) + (float)(1.1 * mAtomRadius * col), (0.75f*mScreenHeight-mAtomRadius)  - (float)(1.1 * mAtomRadius * row), 0f);

                if(row < 7 && col < 7) {
                    mDots[index].setModelColor(blueDotColor);
                } else if((row%2) == 1) {
                    mDots[index].setModelColor(whiteDotColor);
                } else {
                    mDots[index].setModelColor(redDotColor);
                }
                // mDots[index].setModelColor(outlineDotColor);
            }
        }
    }

    private float[] initializeCircle() {
        mVertices = new float[mNumAtomPoints*DIMS_PER_VERT];

        for(int point = 0; point < mNumAtomPoints; point++) {
            int index = point * DIMS_PER_VERT;
            mVertices[index] = (float)mAtomRadius * (float)Math.cos( (float)(2*Math.PI) * (float)point/mNumAtomPoints);
            mVertices[index+1] = (float)mAtomRadius * (float)Math.sin( (float)(2*Math.PI) * (float)point/mNumAtomPoints);
            mVertices[index+2] = 0;
        }

        return mVertices;
    }

    private int[] initializeCircleIndices() {
        int nextPoint, index;

        mIndices = new int[mNumAtomPoints * POINTS_PER_LINE];

        for(int point = 0; point < mNumAtomPoints; point++) {
            index = point * POINTS_PER_LINE;
            nextPoint = point + 1;
            if(nextPoint == mNumAtomPoints) {
                nextPoint = 0;
            }
            mIndices[index] = point;
            mIndices[index+1] = nextPoint;
        }

        return mIndices;
    }

    private float[] initializeDot(float offsetX, float offsetY) {
        int index, nextPoint;

        mVertices = new float[VERTS_PER_TRIANGLE * DIMS_PER_VERT * mNumDotPoints];
        for(int point = 0; point < mNumDotPoints; point++) {
            nextPoint = point + 1;
            if(nextPoint == mNumDotPoints) {
                nextPoint = 0;
            }

            index = VERTS_PER_TRIANGLE * DIMS_PER_VERT * point;

            mVertices[index] = offsetX + (float)mDotRadius * (float)Math.cos( (float)(2*Math.PI) * (float)point/mNumDotPoints);
            mVertices[index+1] = offsetY + (float)mDotRadius * (float)Math.sin( (float)(2*Math.PI) * (float)point/mNumDotPoints);
            mVertices[index+2] = 0;

            mVertices[index+3] = offsetX;
            mVertices[index+4] = offsetY;
            mVertices[index+5] = 0;

            mVertices[index+6] = offsetX + (float)mDotRadius * (float)Math.cos( (float)(2*Math.PI) * (float)nextPoint/mNumDotPoints);
            mVertices[index+7] = offsetY + (float)mDotRadius * (float)Math.sin( (float)(2*Math.PI) * (float)nextPoint/mNumDotPoints);
            mVertices[index+8] = 0;
        }

        return mVertices;
    }

    public void draw(GL10 gl) {
        for(row = 0; row < mNumRows; row++) {
            for(col = 0; col < mNumCols; col++) {
                //mCircles[row*mNumCols + col].draw(gl);

                mDots[row*mNumCols + col].setRotation(0, 0, (float)-1*360*counter/delay);
                mDots[row*mNumCols + col].draw(gl);
            }
        }

        // counter for animation
        counter++;
        if(counter > delay) {
            counter = 0;
        }
    }
}