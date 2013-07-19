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

    private Model[] mDots;
    private Model   mPole;
    private float[] mVertices;
    private float[] mTempVertices;

    private int[] mIndices;

    private float mWidth;
    private float mHeight;

    private float mScreenWidth = 2;
    private float mScreenHeight = 2;

    private float mOffsetX, mOffsetY;

    private int counter = 0;
    private int delay = 40;

    private int mNumRows = 0;
    private int mNumCols = 30;

    private int mNumAtomPoints = 16;
    private int mNumDotPoints = 8;

    private float mAtomRadius = 0.2f;
    private float mDotRadius  = 0.02f;
    private float mDeltaPhase = 0.05f;

    private int row, col;

    private float[] redDotColor   = {0.8f, 0.2f, 0.2f, 0.8f};
    private float[] whiteDotColor = {0.9f, 0.9f, 0.9f, 0.8f};
    private float[] blueDotColor  = {0.2f, 0.2f, 0.8f, 0.8f};
    private float[] greyDotColor  = {0.6f, 0.6f, 0.6f, 0.8f};

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

        mNumRows = 13;

        initializeWallpaper();
    }

    private void initializeWallpaper() {
        mDots    = new Model[mNumRows * mNumCols];

        for(row = 0; row < mNumRows; row++) {
            for(col = 0; col < mNumCols; col++) {
                int index = row*mNumCols + col;

                mDots[index] = new Model(mCtx, mGl);
                mOffsetX = (float)(mAtomRadius * col/mNumCols) * (float)Math.cos( (2*Math.PI) * (mDeltaPhase * (row + col)));
                mOffsetY = (float)(mAtomRadius * col/mNumCols) * (float)Math.sin( (2*Math.PI) * (mDeltaPhase * (row + col)));
                mDots[index].setVertices(initializeDot(mOffsetX, mOffsetY));
                mDots[index].setPosition( (-1f*(mScreenWidth-mAtomRadius)) + (float)(1.1 * mAtomRadius * col), (0.75f*mScreenHeight-mAtomRadius)  - (float)(1.1 * mAtomRadius * row), 0f);

                if(row < 7 && col < 7) {
                    mDots[index].setModelColor(blueDotColor);
                } else if((row%2) == 1) {
                    mDots[index].setModelColor(whiteDotColor);
                } else {
                    mDots[index].setModelColor(redDotColor);
                }
            }
        }

        mPole = new Model(mCtx, mGl);
        mPole.setVertices(initializeDot(0f, 0f));
        mPole.setModelColor(greyDotColor);
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
                mDots[row*mNumCols + col].setRotation(0, 0, (float)-1*360*counter/delay);
                mDots[row*mNumCols + col].draw(gl);
            }
        }

        for(row = mNumRows; row < mNumRows + 10; row++) {
            mPole.setPosition((-1f*(mScreenWidth-mAtomRadius)), (0.75f*mScreenHeight-mAtomRadius)  - (float)(1.1 * mAtomRadius * row), 0f);
            mPole.draw(gl);
        }

        // counter for animation
        counter++;
        if(counter > delay) {
            counter = 0;
        }
    }
}