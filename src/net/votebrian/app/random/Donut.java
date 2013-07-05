package net.votebrian.app.random;

import android.content.Context;
import android.util.Log;
import java.lang.Math;
import javax.microedition.khronos.opengles.GL10;

public class Donut {
    private Model   model;
    private Model[] mBands;
    private Context mCtx;
    private float[] mRing;
    private float[] mBandVertices;
    private int[]   mOutline;

    private final int TRIANGLES_PER_QUAD = 2;
    private final int VERTS_PER_TRIANGLE = 3;
    private final int DIMS_PER_VERT      = 3;
    private final int INDICES_PER_LINE   = 2;
    //.....................................................

    private int mNumRings = 4;
    private int mNumPoints = 90;
    private float mDonutRadius = 0.5f;
    private float mRingRadius = 1.5f;
    private float mBaseZ = 17f;
    private float mBandOffset = 0.02f;
    private float mBandScale = 1.02f;

    private int counter = 0;
    private int delay = 240;

    private float[] mTempVertices, mTempBandVertices;

    private float[] mPtVertices = {-0.1f, -0.1f, 0f,
                                    0.0f,  0.1f, 0f,
                                    0.1f, -0.1f, 0f};

    private float[] outlineColor = {0.984f, 0.973f, 0.807f, 1.0f};
    private float[] donutColor = {0.0f, 0.0f, 0.0f, 1.0f};

    //.....................................................

    float heightA, heightB;

    float xAngle;

    int mCurrRing, mCurrPoint;
    int mNextRing, mNextPoint;
    float mRadiusA, mRadiusB;
    float mHeightA, mHeightB;

    int index;
    int x;

    public Donut(Context context, GL10 gl) {
        model = new Model(context, gl);

        initializeArrays();
        initializeVertices();


        mBands = new Model[mNumRings];
        for(int x = 0; x < mNumRings; x++) {
            mBands[x] = new Model(context, gl);
            initializeBand(x);
        }
    }

    public void draw(GL10 gl) {
        calculateVertices();
        //initializeVertices();

        model.draw(gl);

        for(int x = 0; x < mNumRings; x++) {
            updateBand(x);
            mBands[x].draw(gl);
        }

        // counter for animation
        counter++;
        if(counter > delay) {
            counter = 0;
        }
    }

    public void setColor(float[] color) {
        donutColor = color;
    }

    private void initializeArrays() {
        mTempVertices = new float[DIMS_PER_VERT * mNumPoints * mNumRings];
        mTempBandVertices = new float[DIMS_PER_VERT * mNumPoints * 2];
        mRing = new float[DIMS_PER_VERT * VERTS_PER_TRIANGLE * TRIANGLES_PER_QUAD * mNumPoints * mNumRings];
        mBandVertices = new float[DIMS_PER_VERT * VERTS_PER_TRIANGLE * TRIANGLES_PER_QUAD * mNumPoints * 4];
        mOutline = new int[INDICES_PER_LINE * mNumPoints * mNumRings];
    }

    private void initializeBand(int mCurrRing) {
        mRadiusA = mRingRadius + (mBandScale*mDonutRadius)*(float)Math.cos( (2*Math.PI) * ((float)mCurrRing/mNumRings + (float)counter/delay + mBandOffset) );
        mRadiusB = mRingRadius + (mBandScale*mDonutRadius)*(float)Math.cos( (2*Math.PI) * ((float)mCurrRing/mNumRings + (float)counter/delay - mBandOffset) );

        mHeightA = (mBandScale*mDonutRadius)*(float)Math.sin( (2*Math.PI) * ((float)mCurrRing/mNumRings + (float)counter/delay) + mBandOffset );
        mHeightB = (mBandScale*mDonutRadius)*(float)Math.sin( (2*Math.PI) * ((float)mCurrRing/mNumRings + (float)counter/delay) - mBandOffset );

        for(mCurrPoint = 0; mCurrPoint < mNumPoints; mCurrPoint++) {
            index = mCurrPoint * DIMS_PER_VERT;
            mTempBandVertices[index]     = mRadiusA*(float)Math.cos(2*Math.PI * mCurrPoint/mNumPoints);
            mTempBandVertices[index + 1] = mHeightA;
            mTempBandVertices[index + 2] = mRadiusA*(float)Math.sin(2*Math.PI * mCurrPoint/mNumPoints);

            index = (mCurrPoint + mNumPoints) * DIMS_PER_VERT;

            mTempBandVertices[index]     = mRadiusB*(float)Math.cos(2*Math.PI * mCurrPoint/mNumPoints);
            mTempBandVertices[index + 1] = mHeightB;
            mTempBandVertices[index + 2] = mRadiusB*(float)Math.sin(2*Math.PI * mCurrPoint/mNumPoints);
        }

        for(mCurrPoint = 0; mCurrPoint < mNumPoints; mCurrPoint++) {
            if(mCurrPoint == mNumPoints -1) {
                mNextPoint = 0;
            } else {
                mNextPoint = mCurrPoint + 1;
            }

            index = (DIMS_PER_VERT * VERTS_PER_TRIANGLE * TRIANGLES_PER_QUAD) * mCurrPoint * 2;

            mBandVertices[index]      = mTempBandVertices[mCurrPoint * DIMS_PER_VERT];
            mBandVertices[index + 1]  = mTempBandVertices[mCurrPoint * DIMS_PER_VERT + 1];
            mBandVertices[index + 2]  = mTempBandVertices[mCurrPoint * DIMS_PER_VERT + 2];

            mBandVertices[index + 3]  = mTempBandVertices[(mNumPoints + mCurrPoint) * DIMS_PER_VERT];
            mBandVertices[index + 4]  = mTempBandVertices[(mNumPoints + mCurrPoint) * DIMS_PER_VERT + 1];
            mBandVertices[index + 5]  = mTempBandVertices[(mNumPoints + mCurrPoint) * DIMS_PER_VERT + 2];

            mBandVertices[index + 6]  = mTempBandVertices[mNextPoint * DIMS_PER_VERT];
            mBandVertices[index + 7]  = mTempBandVertices[mNextPoint * DIMS_PER_VERT + 1];
            mBandVertices[index + 8]  = mTempBandVertices[mNextPoint * DIMS_PER_VERT + 2];


            mBandVertices[index + 9]  = mTempBandVertices[(mNumPoints + mCurrPoint) * DIMS_PER_VERT];
            mBandVertices[index + 10] = mTempBandVertices[(mNumPoints + mCurrPoint) * DIMS_PER_VERT + 1];
            mBandVertices[index + 11] = mTempBandVertices[(mNumPoints + mCurrPoint) * DIMS_PER_VERT + 2];

            mBandVertices[index + 12] = mTempBandVertices[(mNumPoints + mNextPoint) * DIMS_PER_VERT];
            mBandVertices[index + 13] = mTempBandVertices[(mNumPoints + mNextPoint) * DIMS_PER_VERT + 1];
            mBandVertices[index + 14] = mTempBandVertices[(mNumPoints + mNextPoint) * DIMS_PER_VERT + 2];

            mBandVertices[index + 15] = mTempBandVertices[mNextPoint * DIMS_PER_VERT];
            mBandVertices[index + 16] = mTempBandVertices[mNextPoint * DIMS_PER_VERT + 1];
            mBandVertices[index + 17] = mTempBandVertices[mNextPoint * DIMS_PER_VERT + 2];



            mBandVertices[index + 18]  = mTempBandVertices[mNextPoint * DIMS_PER_VERT];
            mBandVertices[index + 19]  = mTempBandVertices[mNextPoint * DIMS_PER_VERT + 1];
            mBandVertices[index + 20]  = mTempBandVertices[mNextPoint * DIMS_PER_VERT + 2];

            mBandVertices[index + 21]  = mTempBandVertices[(mNumPoints + mCurrPoint) * DIMS_PER_VERT];
            mBandVertices[index + 22]  = mTempBandVertices[(mNumPoints + mCurrPoint) * DIMS_PER_VERT + 1];
            mBandVertices[index + 23]  = mTempBandVertices[(mNumPoints + mCurrPoint) * DIMS_PER_VERT + 2];

            mBandVertices[index + 24]  = mTempBandVertices[mCurrPoint * DIMS_PER_VERT];
            mBandVertices[index + 25]  = mTempBandVertices[mCurrPoint * DIMS_PER_VERT + 1];
            mBandVertices[index + 26]  = mTempBandVertices[mCurrPoint * DIMS_PER_VERT + 2];


            mBandVertices[index + 27] = mTempBandVertices[mNextPoint * DIMS_PER_VERT];
            mBandVertices[index + 28] = mTempBandVertices[mNextPoint * DIMS_PER_VERT + 1];
            mBandVertices[index + 29] = mTempBandVertices[mNextPoint * DIMS_PER_VERT + 2];

            mBandVertices[index + 30] = mTempBandVertices[(mNumPoints + mNextPoint) * DIMS_PER_VERT];
            mBandVertices[index + 31] = mTempBandVertices[(mNumPoints + mNextPoint) * DIMS_PER_VERT + 1];
            mBandVertices[index + 32] = mTempBandVertices[(mNumPoints + mNextPoint) * DIMS_PER_VERT + 2];

            mBandVertices[index + 33]  = mTempBandVertices[(mNumPoints + mCurrPoint) * DIMS_PER_VERT];
            mBandVertices[index + 34] = mTempBandVertices[(mNumPoints + mCurrPoint) * DIMS_PER_VERT + 1];
            mBandVertices[index + 35] = mTempBandVertices[(mNumPoints + mCurrPoint) * DIMS_PER_VERT + 2];
        }

        mBands[mCurrRing].setVertices(mBandVertices);

        mBands[mCurrRing].enableModel();
        mBands[mCurrRing].setModelColor(outlineColor);
    }

    private void updateBand(int mCurrRing) {
        mRadiusA = mRingRadius + (mBandScale*mDonutRadius)*(float)Math.cos( (2*Math.PI) * ((float)mCurrRing/mNumRings + (float)counter/delay + mBandOffset) );
        mRadiusB = mRingRadius + (mBandScale*mDonutRadius)*(float)Math.cos( (2*Math.PI) * ((float)mCurrRing/mNumRings + (float)counter/delay - mBandOffset) );

        mHeightA = (mBandScale*mDonutRadius)*(float)Math.sin( (2*Math.PI) * ((float)mCurrRing/mNumRings + (float)counter/delay) + mBandOffset );
        mHeightB = (mBandScale*mDonutRadius)*(float)Math.sin( (2*Math.PI) * ((float)mCurrRing/mNumRings + (float)counter/delay) - mBandOffset );

        for(mCurrPoint = 0; mCurrPoint < mNumPoints; mCurrPoint++) {
            index = mCurrPoint * DIMS_PER_VERT;
            mTempBandVertices[index]     = mRadiusA*(float)Math.cos(2*Math.PI * mCurrPoint/mNumPoints);
            mTempBandVertices[index + 1] = mHeightA;
            mTempBandVertices[index + 2] = mRadiusA*(float)Math.sin(2*Math.PI * mCurrPoint/mNumPoints);

            index = (mCurrPoint + mNumPoints) * DIMS_PER_VERT;

            mTempBandVertices[index]     = mRadiusB*(float)Math.cos(2*Math.PI * mCurrPoint/mNumPoints);
            mTempBandVertices[index + 1] = mHeightB;
            mTempBandVertices[index + 2] = mRadiusB*(float)Math.sin(2*Math.PI * mCurrPoint/mNumPoints);
        }

        for(mCurrPoint = 0; mCurrPoint < mNumPoints; mCurrPoint++) {
            if(mCurrPoint == mNumPoints -1) {
                mNextPoint = 0;
            } else {
                mNextPoint = mCurrPoint + 1;
            }

            index = (DIMS_PER_VERT * VERTS_PER_TRIANGLE * TRIANGLES_PER_QUAD) * mCurrPoint * 2;

            mBandVertices[index]      = mTempBandVertices[mCurrPoint * DIMS_PER_VERT];
            mBandVertices[index + 1]  = mTempBandVertices[mCurrPoint * DIMS_PER_VERT + 1];
            mBandVertices[index + 2]  = mTempBandVertices[mCurrPoint * DIMS_PER_VERT + 2];

            mBandVertices[index + 3]  = mTempBandVertices[(mNumPoints + mCurrPoint) * DIMS_PER_VERT];
            mBandVertices[index + 4]  = mTempBandVertices[(mNumPoints + mCurrPoint) * DIMS_PER_VERT + 1];
            mBandVertices[index + 5]  = mTempBandVertices[(mNumPoints + mCurrPoint) * DIMS_PER_VERT + 2];

            mBandVertices[index + 6]  = mTempBandVertices[mNextPoint * DIMS_PER_VERT];
            mBandVertices[index + 7]  = mTempBandVertices[mNextPoint * DIMS_PER_VERT + 1];
            mBandVertices[index + 8]  = mTempBandVertices[mNextPoint * DIMS_PER_VERT + 2];


            mBandVertices[index + 9]  = mTempBandVertices[(mNumPoints + mCurrPoint) * DIMS_PER_VERT];
            mBandVertices[index + 10] = mTempBandVertices[(mNumPoints + mCurrPoint) * DIMS_PER_VERT + 1];
            mBandVertices[index + 11] = mTempBandVertices[(mNumPoints + mCurrPoint) * DIMS_PER_VERT + 2];

            mBandVertices[index + 12] = mTempBandVertices[(mNumPoints + mNextPoint) * DIMS_PER_VERT];
            mBandVertices[index + 13] = mTempBandVertices[(mNumPoints + mNextPoint) * DIMS_PER_VERT + 1];
            mBandVertices[index + 14] = mTempBandVertices[(mNumPoints + mNextPoint) * DIMS_PER_VERT + 2];

            mBandVertices[index + 15] = mTempBandVertices[mNextPoint * DIMS_PER_VERT];
            mBandVertices[index + 16] = mTempBandVertices[mNextPoint * DIMS_PER_VERT + 1];
            mBandVertices[index + 17] = mTempBandVertices[mNextPoint * DIMS_PER_VERT + 2];



            mBandVertices[index + 18]  = mTempBandVertices[mNextPoint * DIMS_PER_VERT];
            mBandVertices[index + 19]  = mTempBandVertices[mNextPoint * DIMS_PER_VERT + 1];
            mBandVertices[index + 20]  = mTempBandVertices[mNextPoint * DIMS_PER_VERT + 2];

            mBandVertices[index + 21]  = mTempBandVertices[(mNumPoints + mCurrPoint) * DIMS_PER_VERT];
            mBandVertices[index + 22]  = mTempBandVertices[(mNumPoints + mCurrPoint) * DIMS_PER_VERT + 1];
            mBandVertices[index + 23]  = mTempBandVertices[(mNumPoints + mCurrPoint) * DIMS_PER_VERT + 2];

            mBandVertices[index + 24]  = mTempBandVertices[mCurrPoint * DIMS_PER_VERT];
            mBandVertices[index + 25]  = mTempBandVertices[mCurrPoint * DIMS_PER_VERT + 1];
            mBandVertices[index + 26]  = mTempBandVertices[mCurrPoint * DIMS_PER_VERT + 2];


            mBandVertices[index + 27] = mTempBandVertices[mNextPoint * DIMS_PER_VERT];
            mBandVertices[index + 28] = mTempBandVertices[mNextPoint * DIMS_PER_VERT + 1];
            mBandVertices[index + 29] = mTempBandVertices[mNextPoint * DIMS_PER_VERT + 2];

            mBandVertices[index + 30] = mTempBandVertices[(mNumPoints + mNextPoint) * DIMS_PER_VERT];
            mBandVertices[index + 31] = mTempBandVertices[(mNumPoints + mNextPoint) * DIMS_PER_VERT + 1];
            mBandVertices[index + 32] = mTempBandVertices[(mNumPoints + mNextPoint) * DIMS_PER_VERT + 2];

            mBandVertices[index + 33]  = mTempBandVertices[(mNumPoints + mCurrPoint) * DIMS_PER_VERT];
            mBandVertices[index + 34] = mTempBandVertices[(mNumPoints + mCurrPoint) * DIMS_PER_VERT + 1];
            mBandVertices[index + 35] = mTempBandVertices[(mNumPoints + mCurrPoint) * DIMS_PER_VERT + 2];
        }

        mBands[mCurrRing].updateVertices(mBandVertices);

        mBands[mCurrRing].enableModel();
        mBands[mCurrRing].updateModelColor(outlineColor);
    }

    private void initializeVertices() {
        for(mCurrRing = 0; mCurrRing < mNumRings; mCurrRing++) {
            // calculate the current ring radius
            mRadiusA = mRingRadius + mDonutRadius*(float)Math.cos( (2*Math.PI) * ((float)mCurrRing/mNumRings + (float)counter/delay) );
            mHeightA = mDonutRadius*(float)Math.sin( (2*Math.PI) * ((float)mCurrRing/mNumRings + (float)counter/delay) );

            for(mCurrPoint = 0; mCurrPoint < mNumPoints; mCurrPoint++) {
                index = (mCurrRing * mNumPoints + mCurrPoint) * DIMS_PER_VERT;
                mTempVertices[index]     = mRadiusA*(float)Math.cos(2*Math.PI * mCurrPoint/mNumPoints);
                mTempVertices[index + 1] = mHeightA;
                mTempVertices[index + 2] = mRadiusA*(float)Math.sin(2*Math.PI * mCurrPoint/mNumPoints);
            }
        }

        for(mCurrRing = 0; mCurrRing < mNumRings; mCurrRing++) {
            if(mCurrRing == mNumRings -1) {
                mNextRing = 0;
            } else {
                mNextRing = mCurrRing + 1;
            }

            for(mCurrPoint = 0; mCurrPoint < mNumPoints; mCurrPoint++) {
                if(mCurrPoint == mNumPoints -1) {
                    mNextPoint = 0;
                } else {
                    mNextPoint = mCurrPoint + 1;
                }

                index = (DIMS_PER_VERT * VERTS_PER_TRIANGLE * TRIANGLES_PER_QUAD) * (mCurrRing * mNumPoints + mCurrPoint);

                mRing[index]      = mTempVertices[(mCurrRing * mNumPoints + mCurrPoint) * DIMS_PER_VERT];
                mRing[index + 1]  = mTempVertices[(mCurrRing * mNumPoints + mCurrPoint) * DIMS_PER_VERT + 1];
                mRing[index + 2]  = mTempVertices[(mCurrRing * mNumPoints + mCurrPoint) * DIMS_PER_VERT + 2];

                mRing[index + 3]  = mTempVertices[(mNextRing * mNumPoints + mCurrPoint) * DIMS_PER_VERT];
                mRing[index + 4]  = mTempVertices[(mNextRing * mNumPoints + mCurrPoint) * DIMS_PER_VERT + 1];
                mRing[index + 5]  = mTempVertices[(mNextRing * mNumPoints + mCurrPoint) * DIMS_PER_VERT + 2];

                mRing[index + 6]  = mTempVertices[(mCurrRing * mNumPoints + mNextPoint) * DIMS_PER_VERT];
                mRing[index + 7]  = mTempVertices[(mCurrRing * mNumPoints + mNextPoint) * DIMS_PER_VERT + 1];
                mRing[index + 8]  = mTempVertices[(mCurrRing * mNumPoints + mNextPoint) * DIMS_PER_VERT + 2];


                mRing[index + 9]  = mTempVertices[(mNextRing * mNumPoints + mCurrPoint) * DIMS_PER_VERT];
                mRing[index + 10] = mTempVertices[(mNextRing * mNumPoints + mCurrPoint) * DIMS_PER_VERT + 1];
                mRing[index + 11] = mTempVertices[(mNextRing * mNumPoints + mCurrPoint) * DIMS_PER_VERT + 2];

                mRing[index + 12] = mTempVertices[(mNextRing * mNumPoints + mNextPoint) * DIMS_PER_VERT];
                mRing[index + 13] = mTempVertices[(mNextRing * mNumPoints + mNextPoint) * DIMS_PER_VERT + 1];
                mRing[index + 14] = mTempVertices[(mNextRing * mNumPoints + mNextPoint) * DIMS_PER_VERT + 2];

                mRing[index + 15] = mTempVertices[(mCurrRing * mNumPoints + mNextPoint) * DIMS_PER_VERT];
                mRing[index + 16] = mTempVertices[(mCurrRing * mNumPoints + mNextPoint) * DIMS_PER_VERT + 1];
                mRing[index + 17] = mTempVertices[(mCurrRing * mNumPoints + mNextPoint) * DIMS_PER_VERT + 2];
            }
        }

        model.setVertices(mRing);

        model.enableModel();
        model.setModelColor(donutColor);
    }

    private void calculateVertices() {
        for(mCurrRing = 0; mCurrRing < mNumRings; mCurrRing++) {
            // calculate the current ring radius
            mRadiusA = mRingRadius + mDonutRadius*(float)Math.cos( (2*Math.PI) * ((float)mCurrRing/mNumRings + (float)counter/delay) );
            mHeightA = mDonutRadius*(float)Math.sin( (2*Math.PI) * ((float)mCurrRing/mNumRings + (float)counter/delay) );

            for(mCurrPoint = 0; mCurrPoint < mNumPoints; mCurrPoint++) {
                index = (mCurrRing * mNumPoints + mCurrPoint) * DIMS_PER_VERT;
                mTempVertices[index]     = mRadiusA*(float)Math.cos(2*Math.PI * mCurrPoint/mNumPoints);
                mTempVertices[index + 1] = mHeightA;
                mTempVertices[index + 2] = mRadiusA*(float)Math.sin(2*Math.PI * mCurrPoint/mNumPoints);
            }
        }

        for(mCurrRing = 0; mCurrRing < mNumRings; mCurrRing++) {
            if(mCurrRing == mNumRings -1) {
                mNextRing = 0;
            } else {
                mNextRing = mCurrRing + 1;
            }

            for(mCurrPoint = 0; mCurrPoint < mNumPoints; mCurrPoint++) {
                if(mCurrPoint == mNumPoints -1) {
                    mNextPoint = 0;
                } else {
                    mNextPoint = mCurrPoint + 1;
                }

                index = (DIMS_PER_VERT * VERTS_PER_TRIANGLE * TRIANGLES_PER_QUAD) * (mCurrRing * mNumPoints + mCurrPoint);

                mRing[index]      = mTempVertices[(mCurrRing * mNumPoints + mCurrPoint) * DIMS_PER_VERT];
                mRing[index + 1]  = mTempVertices[(mCurrRing * mNumPoints + mCurrPoint) * DIMS_PER_VERT + 1];
                mRing[index + 2]  = mTempVertices[(mCurrRing * mNumPoints + mCurrPoint) * DIMS_PER_VERT + 2];

                mRing[index + 3]  = mTempVertices[(mNextRing * mNumPoints + mCurrPoint) * DIMS_PER_VERT];
                mRing[index + 4]  = mTempVertices[(mNextRing * mNumPoints + mCurrPoint) * DIMS_PER_VERT + 1];
                mRing[index + 5]  = mTempVertices[(mNextRing * mNumPoints + mCurrPoint) * DIMS_PER_VERT + 2];

                mRing[index + 6]  = mTempVertices[(mCurrRing * mNumPoints + mNextPoint) * DIMS_PER_VERT];
                mRing[index + 7]  = mTempVertices[(mCurrRing * mNumPoints + mNextPoint) * DIMS_PER_VERT + 1];
                mRing[index + 8]  = mTempVertices[(mCurrRing * mNumPoints + mNextPoint) * DIMS_PER_VERT + 2];


                mRing[index + 9]  = mTempVertices[(mNextRing * mNumPoints + mCurrPoint) * DIMS_PER_VERT];
                mRing[index + 10] = mTempVertices[(mNextRing * mNumPoints + mCurrPoint) * DIMS_PER_VERT + 1];
                mRing[index + 11] = mTempVertices[(mNextRing * mNumPoints + mCurrPoint) * DIMS_PER_VERT + 2];

                mRing[index + 12] = mTempVertices[(mNextRing * mNumPoints + mNextPoint) * DIMS_PER_VERT];
                mRing[index + 13] = mTempVertices[(mNextRing * mNumPoints + mNextPoint) * DIMS_PER_VERT + 1];
                mRing[index + 14] = mTempVertices[(mNextRing * mNumPoints + mNextPoint) * DIMS_PER_VERT + 2];

                mRing[index + 15] = mTempVertices[(mCurrRing * mNumPoints + mNextPoint) * DIMS_PER_VERT];
                mRing[index + 16] = mTempVertices[(mCurrRing * mNumPoints + mNextPoint) * DIMS_PER_VERT + 1];
                mRing[index + 17] = mTempVertices[(mCurrRing * mNumPoints + mNextPoint) * DIMS_PER_VERT + 2];
            }
        }

        model.updateVertices(mRing);

        model.enableModel();
        model.updateModelColor(donutColor);
    }
}