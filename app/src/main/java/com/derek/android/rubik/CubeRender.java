package com.derek.android.rubik;

import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;
import android.os.Bundle;

import com.derek.android.rubik.bean.UserPerspective;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CubeRender implements Renderer {
    public static final float XANGLE = -30.0f;
    public static final float YANGLE = -25.0f;
    public static final float MIN_XANGLE = -80;
    public static final float MAX_XANGLE = 80;
    public static final float viewDistance = 4;

    private UserPerspective perspective;
    public static final String PERSPECTIVE_SAVE_KEY = "CubeRender.perspective";

    public float[] rotation = new float[]{0,0,viewDistance};

    private float[] eye = new float[]{0,0,viewDistance};
    private float[] center = new float[]{0,0,0};
    private float[] upVector = new float[]{0,1,0};//y轴向上

    private final Animation animation;

    public CubeRender(Animation animation) {
        this.animation = animation;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (animation != null) {
            animation.nextFrame();
        }

        gl.glClearColor(0.5f, 0.5f, 0.5f, 1);
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();

        GLU.gluLookAt(gl, eye[0], eye[1], eye[2], center[0], center[1], center[2], upVector[0], upVector[1], upVector[2]);

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

        gl.glShadeModel(GL10.GL_FLAT);
        gl.glFrontFace(GL10.GL_SMOOTH);
        gl.glEnable(GL10.GL_DEPTH_TEST);

        Cube3.getInstance().draw(gl);

        gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        float ratio;
        if(width < height){
            ratio = (float) width/height;
            gl.glFrustumf(-ratio, ratio, -1, 1, 1, 5);
        }
        else {
            ratio = (float) height / width;
            gl.glFrustumf(-1, 1, -ratio, ratio, 1, 5);
        }
        gl.glDisable(GL10.GL_DITHER);
        gl.glActiveTexture(GL10.GL_TEXTURE0);
        Cube3.getInstance().setViewPort(new int[]{0,0,width,height});
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Cube3.getInstance().setGL(gl);
    }

    /**
     *
     * @param x Δx
     * @param y Δy
     */
    public void rorate(float x, float y){
        float target = perspective.xAngle + x;
        if(target < MAX_XANGLE){
            if(target < MIN_XANGLE){
                perspective.xAngle = MIN_XANGLE;
            }
            else {
                perspective.xAngle += x;
            }
        }
        else{
            perspective.xAngle = MAX_XANGLE;
        }
        perspective.yAngle += y;
        applyRotation();
    }

    public void resetCam(){
        perspective.xAngle = XANGLE;
        perspective.yAngle = YANGLE;
        applyRotation();
    }

    private void applyRotation(){
        Matrix3 rx = Matrix3.rotate(perspective.xAngle/180*Math.PI,0);
        Matrix3 ry = Matrix3.rotate(perspective.yAngle/180*Math.PI,1);
        rx.multiply(ry).convert(rotation,eye);
    }

    public void onSaveInstanceState(Bundle out){
        out.putSerializable(PERSPECTIVE_SAVE_KEY,perspective);
    }

    public void onRestoreInstanceState(Bundle in){
        if (in!=null) {
            perspective = (UserPerspective)in.getSerializable(PERSPECTIVE_SAVE_KEY);
            applyRotation();
        }
        else {
            perspective = new UserPerspective();
            resetCam();
        }
    }


}
