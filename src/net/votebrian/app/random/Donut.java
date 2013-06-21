package net.votebrian.app.random;

import android.content.Context;
import android.util.Log;
import java.lang.Math;
import javax.microedition.khronos.opengles.GL10;

public class Donut {
    private Model   model;
    private Model[] mPt;
    private Context mCtx;
    private float[] mRing;
    private int[]   mOutline;

    //.....................................................

    private int mNumRings = 4;
    private int mNumPoints = 32;
    private float mDonutRadius = 0.5f;
    private float mRingRadius = 1.5f;
    private float mBaseZ = 17f;

    private int counter = 0;
    private int delay = 240;

    private float[] mPtVertices = {-0.1f, -0.1f, 0f,
                                    0.0f,  0.1f, 0f,
                                    0.1f, -0.1f, 0f};

    private float[] outlineColor = {0.984f, 0.973f, 0.807f, 1.0f};
    private float[] backgroundColor = {0.66f, 0.12f, 0.13f, 0.5f};

    //.....................................................

    float radiusA, radiusB;
    float heightA, heightB;

    float xAngle;

    int curr_ring, ring_2, curr_point;
    int index;
    int x;

    public Donut(Context context, GL10 gl) {
        model = new Model(context, gl);

        mPt = new Model[mNumPoints*mNumRings];
        for(int x = 0; x < mNumPoints*mNumRings; x++) {
            mPt[x] = new Model(context, gl);
            mPt[x].setVertices(mPtVertices);
            mPt[x].enableModel();
            mPt[x].setModelColor( new float[]{0.0f, 1.0f, 0.0f, 1.0f} );
        }

        initializeArrays();
        initializeVertices();
        // calculateVertices();
    }

    public void draw(GL10 gl) {
        calculateVertices();

        model.draw(gl);

        for(x = 0; x < mNumPoints*mNumRings; x++) {
            // mPt[x].draw(gl);
        }

        counter++;

        if(counter > delay) {
            counter = 0;
        }
    }

    private void initializeArrays() {
        mRing = new float[6*3*mNumPoints*mNumRings];
        mOutline = new int[2*mNumPoints*mNumRings];
    }

    private void initializeVertices() {

        for(curr_ring=0; curr_ring < mNumRings; curr_ring++) {
            ring_2 = curr_ring+1;
            if(ring_2 == mNumRings) {
                ring_2 = 0;
            }

            radiusA = mRingRadius + mDonutRadius*(float)Math.cos(2*curr_ring*Math.PI/mNumRings + 2*Math.PI*counter/delay);
            radiusB = mRingRadius + mDonutRadius*(float)Math.cos(2*ring_2*Math.PI/mNumRings + 2*Math.PI*counter/delay);

            heightA = mDonutRadius*(float)Math.sin(2*curr_ring*Math.PI/mNumRings + 2*Math.PI*counter/delay);
            heightB = mDonutRadius*(float)Math.sin(2*ring_2*Math.PI/mNumRings + 2*Math.PI*counter/delay);

            for(curr_point=0; curr_point < mNumPoints; curr_point++) {
                index = (6*3*curr_ring*mNumPoints) + (6*3*curr_point);

                // TRIANGLE 1
                // First point
                mRing[index]   = radiusA * (float)Math.cos(2*curr_point*Math.PI/mNumPoints);
                mRing[index+1] = heightA;
                mRing[index+2] = radiusA * (float)Math.sin(2*curr_point*Math.PI/mNumPoints);

                // Second point
                mRing[index+3] = radiusA * (float)Math.cos(2*(curr_point+1)*Math.PI/mNumPoints);
                mRing[index+4] = heightA;
                mRing[index+5] = radiusA * (float)Math.sin(2*(curr_point+1)*Math.PI/mNumPoints);

                // Third point
                mRing[index+6] = radiusB * (float)Math.cos(2*curr_point*Math.PI/mNumPoints);
                mRing[index+7] = heightB;
                mRing[index+8] = radiusB * (float)Math.sin(2*curr_point*Math.PI/mNumPoints);

                // TRIANGLE 2
                // First point
                mRing[index+9]  = radiusA * (float)Math.cos(2*(curr_point+1)*Math.PI/mNumPoints);
                mRing[index+10] = heightA;
                mRing[index+11] = radiusA * (float)Math.sin(2*(curr_point+1)*Math.PI/mNumPoints);

                // Second point
                mRing[index+12] = radiusB * (float)Math.cos(2*(curr_point+1)*Math.PI/mNumPoints);
                mRing[index+13] = heightB;
                mRing[index+14] = radiusB * (float)Math.sin(2*(curr_point+1)*Math.PI/mNumPoints);

                // Third point
                mRing[index+15] = radiusB * (float)Math.cos(2*curr_point*Math.PI/mNumPoints);
                mRing[index+16] = heightB;
                mRing[index+17] = radiusB * (float)Math.sin(2*curr_point*Math.PI/mNumPoints);

                // OUTLINE INDICES
                mOutline[(2*curr_ring*mNumPoints) + (2*curr_point)] = 6*curr_ring*mNumPoints + 6*curr_point;
                if( curr_point == mNumPoints-1 ) {
                    mOutline[(2*curr_ring*mNumPoints)+(2*curr_point)+1] = 6*curr_ring*mNumPoints;
                } else {
                    mOutline[(2*curr_ring*mNumPoints)+(2*curr_point)+1] = 6*curr_ring*mNumPoints + 6*curr_point + 1;
                }

                // triangles
                // float xAngle = -1*360/mNumRings*j; // - 360*counter/delay
                xAngle = 0f;
                mPt[curr_ring*mNumPoints + curr_point].setRotation(xAngle, -1*360/mNumPoints*curr_point+90, 0f);
                mPt[curr_ring*mNumPoints + curr_point].setPosition(mRing[index], mRing[index+1], mRing[index+2]);
            }
        }

        model.setVertices(mRing);

        model.enableOutline();
        model.setOutlineIndices(mOutline);
        model.setOutlineColor(outlineColor);

        model.enableModel();
        model.setModelColor(backgroundColor);
        // model.setModelColor( new float[]{0.0f, 0.0f, 0.0f, 1.0f} );

        model.enableOutline();
    }

    private void calculateVertices() {

        for(curr_ring=0; curr_ring < mNumRings; curr_ring++) {
            ring_2 = curr_ring+1;
            if(ring_2 == mNumRings) {
                ring_2 = 0;
            }

            radiusA = mRingRadius + mDonutRadius*(float)Math.cos(2*curr_ring*Math.PI/mNumRings + 2*Math.PI*counter/delay);
            radiusB = mRingRadius + mDonutRadius*(float)Math.cos(2*ring_2*Math.PI/mNumRings + 2*Math.PI*counter/delay);

            heightA = mDonutRadius*(float)Math.sin(2*curr_ring*Math.PI/mNumRings + 2*Math.PI*counter/delay);
            heightB = mDonutRadius*(float)Math.sin(2*ring_2*Math.PI/mNumRings + 2*Math.PI*counter/delay);

            for(curr_point=0; curr_point < mNumPoints; curr_point++) {
                index = (6*3*curr_ring*mNumPoints) + (6*3*curr_point);

                // TRIANGLE 1
                // First point
                mRing[index]   = radiusA * (float)Math.cos(2*curr_point*Math.PI/mNumPoints);
                mRing[index+1] = heightA;
                mRing[index+2] = radiusA * (float)Math.sin(2*curr_point*Math.PI/mNumPoints);

                // Second point
                mRing[index+3] = radiusA * (float)Math.cos(2*(curr_point+1)*Math.PI/mNumPoints);
                mRing[index+4] = heightA;
                mRing[index+5] = radiusA * (float)Math.sin(2*(curr_point+1)*Math.PI/mNumPoints);

                // Third point
                mRing[index+6] = radiusB * (float)Math.cos(2*curr_point*Math.PI/mNumPoints);
                mRing[index+7] = heightB;
                mRing[index+8] = radiusB * (float)Math.sin(2*curr_point*Math.PI/mNumPoints);

                // TRIANGLE 2
                // First point
                mRing[index+9]  = radiusA * (float)Math.cos(2*(curr_point+1)*Math.PI/mNumPoints);
                mRing[index+10] = heightA;
                mRing[index+11] = radiusA * (float)Math.sin(2*(curr_point+1)*Math.PI/mNumPoints);

                // Second point
                mRing[index+12] = radiusB * (float)Math.cos(2*(curr_point+1)*Math.PI/mNumPoints);
                mRing[index+13] = heightB;
                mRing[index+14] = radiusB * (float)Math.sin(2*(curr_point+1)*Math.PI/mNumPoints);

                // Third point
                mRing[index+15] = radiusB * (float)Math.cos(2*curr_point*Math.PI/mNumPoints);
                mRing[index+16] = heightB;
                mRing[index+17] = radiusB * (float)Math.sin(2*curr_point*Math.PI/mNumPoints);

                // OUTLINE INDICES
                mOutline[(2*curr_ring*mNumPoints) + (2*curr_point)] = 6*curr_ring*mNumPoints + 6*curr_point;
                if( curr_point == mNumPoints-1 ) {
                    mOutline[(2*curr_ring*mNumPoints)+(2*curr_point)+1] = 6*curr_ring*mNumPoints;
                } else {
                    mOutline[(2*curr_ring*mNumPoints)+(2*curr_point)+1] = 6*curr_ring*mNumPoints + 6*curr_point + 1;
                }

                // triangles
                // float xAngle = -1*360/mNumRings*j; // - 360*counter/delay
                xAngle = 0f;
                mPt[curr_ring*mNumPoints + curr_point].setRotation(xAngle, -1*360/mNumPoints*curr_point+90, 0f);
                mPt[curr_ring*mNumPoints + curr_point].setPosition(mRing[index], mRing[index+1], mRing[index+2]);
            }
        }

        model.updateVertices(mRing);

        // model.enableOutline();
        model.updateOutlineIndices(mOutline);
        // model.updateOutlineColor(outlineColor);

        // model.enableModel();
        // model.updateModelColor(backgroundColor);
        // model.setModelColor( new float[]{0.0f, 0.0f, 0.0f, 1.0f} );

        // model.enableOutline();
    }
}