package net.votebrian.app.random;

import android.content.Context;
import android.util.Log;
import javax.microedition.khronos.opengles.GL10;

public class Tunnel {
    private Context mCtx;
    private GL10 mGl;

    private Ring[] mRings;
    private float mNearZ;
    private float mFarZ;
    private float mRange;

    private int mNumRings = 10;

    private int mDelay = 0;
    private int mDelta = 10;

    private int mCounter = 0;

    public Tunnel(Context context, GL10 gl) {
        mCtx = context;
        mGl = gl;

        mDelay = mDelta * mNumRings;

        mRings = new Ring[mNumRings];
        for(int x = 0; x < mNumRings; x++) {
            mRings[x] = new Ring(mCtx, gl);
            mRings[x].setFrame(x * mDelta);
            mRings[x].setOffsets(mCounter);
            mCounter++;
        }
    }

    public void draw(GL10 gl) {
        int currFrame;
        float offsetZ = 0f;

        for(int x = 0; x < mNumRings; x++) {
            currFrame = mRings[x].getFrame();

            gl.glPushMatrix();
            gl.glTranslatef(0f, 0f, -1 * (mFarZ - (mRange * currFrame / mDelay)));
            mRings[x].draw(gl);
            gl.glPopMatrix();

            currFrame++;
            if(currFrame == mDelay) {
                currFrame = 0;
                mRings[x].setOffsets(mCounter);
                mCounter++;
            }

            mRings[x].setFrame(currFrame);
        }
    }

    public void initialize(float radiusInner, float radiusOuter, float nearZ, float farZ) {
        mNearZ = nearZ;
        mFarZ = farZ;
        mRange = mFarZ - mNearZ;

        for(int x = 0; x < mNumRings; x++) {
            mRings[x].initialize(radiusInner, radiusOuter);
        }
    }
}