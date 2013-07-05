package net.votebrian.app.random;

import android.content.Context;
import android.util.Log;
import javax.microedition.khronos.opengles.GL10;

public class Squares {
    private Context mCtx;
    private GL10 mGl;

    private Model[] mSquares;

    private int mNumSquares = 50;

    private float[] mVertices;
    private float mWidth = 10f;

    private final int TRIANGLES_PER_FACE = 2;
    private final int VERTICES_PER_TRIANGLE = 3;
    private final int DIMS_PER_VERTEX = 3;

    // private float[] outlineColor = {0.984f, 0.973f, 0.807f, 1.0f};
    private float[] outlineColor = {0.1f, 0.1f, 0.15f, 1.0f};
    private float[] backgroundColor = {0.0f, 0.0f, 0.0f, 1.0f};

    private int counter = 0;
    private int delay = 2500;

    public Squares(Context context, GL10 gl) {
        mCtx = context;
        mGl  = gl;

        initializeSquares();
    }

    public void updateWallpaper(float width, float height) {
        if(width > height) {
            mWidth = width;
        } else {
            mWidth = height;
        }

        initializeSquares();
    }

    private void initializeSquares() {
        mSquares = new Model[mNumSquares];
        mVertices = new float[TRIANGLES_PER_FACE * VERTICES_PER_TRIANGLE * DIMS_PER_VERTEX];

        for(int count = 0; count < mNumSquares; count++) {
            mSquares[count] = new Model(mCtx, mGl);
            mSquares[count].setVertices(createSquare(count));
            mSquares[count].setPosition(0, 0, -0.01f * count);

            if((count % 2) == 0) {
                mSquares[count].setModelColor(outlineColor);
            } else {
                mSquares[count].setModelColor(backgroundColor);
            }
        }
    }

    private float[] createSquare(int count) {
        mVertices[0] =  1.0f * mWidth * (1f/mNumSquares) * count;
        mVertices[1] =  1.0f * mWidth * (1f/mNumSquares) * count;
        mVertices[2] =  0f;

        mVertices[3] = -1.0f * mWidth * (1f/mNumSquares) * count;
        mVertices[4] = -1.0f * mWidth * (1f/mNumSquares) * count;
        mVertices[5] =  0f;

        mVertices[6] = -1.0f * mWidth * (1f/mNumSquares) * count;
        mVertices[7] =  1.0f * mWidth * (1f/mNumSquares) * count;
        mVertices[8] =  0f;


        mVertices[9]  =  1.0f * mWidth * (1f/mNumSquares) * count;
        mVertices[10] = -1.0f * mWidth * (1f/mNumSquares) * count;
        mVertices[11] =  0f;

        mVertices[12] = -1.0f * mWidth * (1f/mNumSquares) * count;
        mVertices[13] = -1.0f * mWidth * (1f/mNumSquares) * count;
        mVertices[14] =  0f;

        mVertices[15] =  1.0f * mWidth * (1f/mNumSquares) * count;
        mVertices[16] =  1.0f * mWidth * (1f/mNumSquares) * count;
        mVertices[17] =  0f;

        return mVertices;
    }

    public void setColor(float[] color) {
        backgroundColor = color;
        initializeSquares();
    }

    public void draw(GL10 gl) {
        for(int count = 0; count < mNumSquares; count++) {
            mSquares[count].setRotation(0f, 0f, 90f * count * counter / delay );
            mSquares[count].draw(gl);
        }

        // counter for animation
        counter++;
        if(counter > delay) {
            counter = 0;
        }
    }
}