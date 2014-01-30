package edu.esimecu.keyframe;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.Core;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import edu.esimecu.keyframe.R;
import android.view.MotionEvent;
import android.view.View.OnTouchListener;

public class Camera extends Activity implements OnTouchListener, CvCameraViewListener2 {
    private static final String TAG = "Camera::Activity";

    private CameraBridgeViewBase mOpenCvCameraView;
    private Mat					 mRgb;
    private Mat 	 		 	 mat;
    private int 				 mHeight;
    private int				     mWidth;
    private MatOfPoint 	 		 mPoint;
    private int					 cols;
    private int					 rows;
    private int					 xOffset;
    private int					 yOffset;
    private int					 x;
    private int					 y;
    private Rect 			     touchedRect;
    private Mat					 touchedRegionRgba;
    private Point[]  			 mPuntito;
    private Scalar				 rojo;

    
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
            case LoaderCallbackInterface.SUCCESS:
            {
                Log.i(TAG, "OpenCV loaded successfully");
                mOpenCvCameraView.enableView();
                mOpenCvCameraView.setOnTouchListener(Camera.this);
            } 
            break;
            default:
            {
                super.onManagerConnected(status);
            } break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_camera);
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.activity_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        
    }
    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_2, this, mLoaderCallback);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }
    
    public void onCameraViewStarted(int width, int height) {
    	mWidth = width;
        mHeight = height;
        mRgb = new Mat(mHeight, mWidth, CvType.CV_8UC1);
    }

    public void onCameraViewStopped() {
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        return puntos(inputFrame.rgba());
    }
    
    public boolean onTouch(View v, MotionEvent event) {
        cols = mRgb.cols();
        rows = mRgb.rows();

        xOffset = (mOpenCvCameraView.getWidth() - cols) / 2;
        yOffset = (mOpenCvCameraView.getHeight() - rows) / 2;

        x = (int)event.getX() - xOffset;
        y = (int)event.getY() - yOffset;

        Log.i(TAG, "Touch image coordinates: (" + x + ", " + y + ")");

        if ((x < 0) || (y < 0) || (x > cols) || (y > rows)) return false;

        touchedRect = new Rect();

        touchedRect.x = (x>4) ? x-4 : 0;
        touchedRect.y = (y>4) ? y-4 : 0;

        touchedRect.width = (x+4 < cols) ? x + 4 - touchedRect.x : cols - touchedRect.x;
        touchedRect.height = (y+4 < rows) ? y + 4 - touchedRect.y : rows - touchedRect.y;

        touchedRegionRgba = mRgb.submat(touchedRect);

        touchedRegionRgba.release();

        return false; // don't need subsequent touch events
    }
    
    public Mat puntos(Mat inputFrame){
    	 int blockSize = 3;
    	  boolean useHarrisDetector = false;
    	  double k = 0.04;
    	mPoint = new MatOfPoint();
    	mat = inputFrame;
    	mRgb = mat.clone();
    	rojo = new Scalar(255,0,0);
    	Imgproc.cvtColor(mat, mRgb, Imgproc.COLOR_RGBA2GRAY,4);
    	Imgproc.goodFeaturesToTrack(mRgb, mPoint, 50, 0.01, 10, mRgb, blockSize, useHarrisDetector, k);
    	mPuntito = mPoint.toArray(); 
    	for (int i = 0; i < mPuntito.length; i++){
    	Core.circle(mRgb, mPuntito[i], 2, rojo, -1, 8, 0);}
    	return mRgb;
    }

		protected void onStop() {
			super.onStop();
			if (mOpenCvCameraView != null)
	            mOpenCvCameraView.disableView();

		}
		
		@Override
		protected void onRestart() {
			// TODO Auto-generated method stub
			super.onRestart();
			OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_2, this, mLoaderCallback);
		}
}