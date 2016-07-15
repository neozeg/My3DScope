package com.example.my3dscope;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.gesture.GestureOverlayView;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class MyGLSurfView extends GLSurfaceView implements GLSurfaceView.Renderer{
	private final static String TAG = "MyGLSurfView";
	private final int NUMBER_OF_POINT = 256;
	private final int NUM_OF_CH = 6;
	private final static float TWOPI = (float)Math.PI;

	private final static float[] COLOR_ARRAY = {
			0.0f,1.0f,0.0f,1.0f,//Green
			0.0f,0.0f,1.0f,1.0f,//Blue
			1.0f,1.0f,1.0f,1.0f,//White
			1.0f,1.0f,0.0f,1.0f,//yellow
			1.0f,0.0f,0.0f,1.0f,//Red
			1.0f,0.5f,0.0f,1.0f,//orange
	};

	private GestureDetector mGestureDetector;

	private float Width,Height;
	private float mDepth;
	
	public MyGLSurfView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		setRenderer(this);
		setOnTouchListener(mOnTouchListener);
		mGestureDetector = new GestureDetector(context,mOGL);
	}
	
	private FloatBuffer[] mVertBuf,mVertColorBuf;
	private ShortBuffer[] mIndexBuf;

	//private float[] mCubeVert;
	//private float[] mCubeVertColor ;
	//private short[] mCubeVertIndex;
	private float[][] mWaveVert = new float[NUM_OF_CH][];
	private float[][] mWaveVertColor = new float[NUM_OF_CH][];
	private short[][] mWaveVertIndex = new short[NUM_OF_CH][];
	
	private float backColorR = 0.1f,
			backColorG = 0.1f,
			backColorB = 0.1f,
			backColorA = 0.0f;
	
	private float mYaw = 0f, mPitch=0f, mRoll=0f;
	private float mdX = 0f, mdY=0f, mdZ=0f;
	
	private void setup(){
		mVertBuf = new FloatBuffer[NUM_OF_CH];
		mVertColorBuf = new FloatBuffer[NUM_OF_CH];
		mIndexBuf = new ShortBuffer[NUM_OF_CH];
		for(int i=0;i<NUM_OF_CH;i++){
			mWaveVert[i] = new float[NUMBER_OF_POINT *3];
			mWaveVertColor[i] = new float[NUMBER_OF_POINT * 4];
			mWaveVertIndex[i] = new short[NUMBER_OF_POINT];
			for(int j=0;j<NUMBER_OF_POINT;j++){
				mWaveVert[i][j*3 + 0] = (float)(j-NUMBER_OF_POINT/2)/100;
				mWaveVert[i][j*3 + 1] = (float)Math.sin(4*Math.PI * j/NUMBER_OF_POINT)/5;
				mWaveVert[i][j*3 + 2] = (float)(i-NUM_OF_CH/2)/5;

				mWaveVertColor[i][j*4 + 0] = COLOR_ARRAY[i*4 + 0];
				mWaveVertColor[i][j*4 + 1] = COLOR_ARRAY[i*4 + 1];
				mWaveVertColor[i][j*4 + 2] = COLOR_ARRAY[i*4 + 2];
				mWaveVertColor[i][j*4 + 3] = COLOR_ARRAY[i*4 + 3];

				mWaveVertIndex[i][j] = (short)j;
			}

			ByteBuffer vertBuf = ByteBuffer.allocateDirect(4 * mWaveVert[i].length);
			vertBuf.order(ByteOrder.nativeOrder());
			mVertBuf[i] = vertBuf.asFloatBuffer();

			ByteBuffer vertColorBuf = ByteBuffer.allocateDirect(4 * mWaveVertColor[i].length);
			vertColorBuf.order(ByteOrder.nativeOrder());
			mVertColorBuf[i] = vertColorBuf.asFloatBuffer();

			ByteBuffer indexBuf = ByteBuffer.allocateDirect(2 * mWaveVertIndex[i].length);
			indexBuf.order(ByteOrder.nativeOrder());
			mIndexBuf[i] = indexBuf.asShortBuffer();

			mVertBuf[i].put(mWaveVert[i]);
			mVertColorBuf[i].put(mWaveVertColor[i]);
			mIndexBuf[i].put(mWaveVertIndex[i]);

			mVertBuf[i].position(0);
			mVertColorBuf[i].position(0);
			mIndexBuf[i].position(0);
		}

		
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		Width = getMeasuredWidth();
		Height = getMeasuredHeight();
		Log.v(TAG,"Width= "+ Width + " ,Height= " + Height);
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		// TODO Auto-generated method stub
		gl.glClearColor(backColorR, backColorG, backColorB, backColorA);
		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glEnable(GL10.GL_CULL_FACE);
		gl.glFrontFace(GL10.GL_CCW);
		gl.glCullFace(GL10.GL_BACK);
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
		gl.glLineWidth(10);

		
		setup();
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		// TODO Auto-generated method stub
		final float fNEAREST = .01f,
				fFAREST	= 100f,
				fVIEW_ANGLE = 45f;
		gl.glMatrixMode(GL10.GL_PROJECTION);
		float fViewWidth = fNEAREST * (float) Math.tan(Math.toRadians(fVIEW_ANGLE)/2);
		float aspectRatio = (float) width/ (float) height;
		gl.glFrustumf(-fViewWidth, fViewWidth, -fViewWidth/aspectRatio, fViewWidth/aspectRatio, fNEAREST, fFAREST);
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glViewport(0, 0, width, height);

		setup();
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		// TODO Auto-generated method stub

		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		for(int i = 0 ;i<NUM_OF_CH;i++){

			gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertBuf[i]);
			gl.glColorPointer(4, GL10.GL_FLOAT, 0, mVertColorBuf[i]);
			gl.glLoadIdentity();
			gl.glTranslatef(mdX, mdY, mdZ-6);
			float x = (float)Math.cos(Math.toRadians(-mYaw))*1.0f;
			float y = (float)Math.sin(Math.toRadians(-mYaw))*1.0f;
			float z = (float)Math.sin(Math.toRadians(-mPitch))*1.0f;
			gl.glRotatef(mYaw,0,0,1f);
			gl.glRotatef(mRoll, 0f, 1.0f, 0f);
			gl.glRotatef(mPitch, 1.0f, 0f, 0f);
			gl.glDrawElements(GL10.GL_LINE_STRIP, mWaveVertIndex[i].length, GL10.GL_UNSIGNED_SHORT, mIndexBuf[i]);
		}
		
	}
	
	public void setOrientation(float yaw, float pitch, float roll){
		mYaw = yaw;
		mPitch = pitch;
		mRoll = roll;
	}
	
	public void setTranslate(float ax, float ay, float az){
		mdX = ax;
		mdY = ay;
		mdZ = az;
	}

	private float posX,posY;
	private OnTouchListener mOnTouchListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			//Log.v(TAG,"Event="+event.getAction());
			switch (event.getAction()){
				case MotionEvent.ACTION_DOWN:
					posX = event.getX();
					posY = event.getY();
					break;
				case MotionEvent.ACTION_MOVE:
					mRoll += 360 * (event.getX() - posX)/Width;
					posX = event.getX();
					mPitch += 10 * (event.getY() - posY)/Height;
					posY = event.getY();
					break;
			}
			mGestureDetector.onTouchEvent(event);
			return true;
		}
	};

	private GestureDetector.OnGestureListener mOGL = new GestureDetector.OnGestureListener() {
		@Override
		public boolean onDown(MotionEvent e) {
			return false;
		}

		@Override
		public void onShowPress(MotionEvent e) {

		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			mRoll = 0;
			mPitch = 0;
			mYaw = 0;
			return true;
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			//Log.v(TAG,"MotionEvent = "+ e1.getX() + " , " + e2.getX());
			return false;
		}

		@Override
		public void onLongPress(MotionEvent e) {

		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			//Log.v(TAG,"MotionEvent = "+ e1.getX() + " , " + e2.getX());
			return false;
		}
	};
	
}
