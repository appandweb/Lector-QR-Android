package com.appandweb.lectorqr;

import java.io.ByteArrayOutputStream;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.annotation.SuppressLint;
import android.app.Activity;

public class QrActivity extends Activity implements SurfaceHolder.Callback{

	private SurfaceView surfaceView;
	private SurfaceHolder surfaceHolder;	
	private Camera camara;
    private Handler autoFocusHandler;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_qr);
		
		autoFocusHandler = new Handler();
		
		this.surfaceView = (SurfaceView) findViewById(R.id.sVCamara);
		this.surfaceHolder = this.surfaceView.getHolder();
		this.surfaceHolder.addCallback(this);
		this.surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		// TODO Auto-generated method stub
		if (this.surfaceView.getHolder() == null){
		      return;
	    }
		
		try {
			this.camara.stopPreview();
	    } catch (Exception e){
	      // ignore: tried to stop a non-existent preview
	    }
		
		if (this.camara != null){
			try {
				this.camara.setDisplayOrientation(90);
				this.camara.setPreviewCallback(previewBarCode);
		        this.camara.setPreviewDisplay(this.surfaceHolder);
		        this.camara.startPreview();
				this.camara.autoFocus(autoFocusCB);							
	        } catch (Exception e){
	            Log.d("DBG", "Error iniciando camara ");
	        }
		}
	}
	
	 PreviewCallback previewBarCode = new PreviewCallback(){

			@SuppressLint("NewApi")
			@Override
			public void onPreviewFrame(byte[] data, Camera camera) {
				// TODO Auto-generated method stub
				Size previewSize = camera.getParameters().getPreviewSize(); 
				YuvImage yuvimage=new YuvImage(data, ImageFormat.NV21, previewSize.width, previewSize.height, null);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				yuvimage.compressToJpeg(new Rect(0, 0, previewSize.width, previewSize.height), 80, baos);
				byte[] jdata = baos.toByteArray();

				// Convert to Bitmap
				Bitmap bitmap = BitmapFactory.decodeByteArray(jdata, 0, jdata.length);
				if (bitmap != null){
					int width = bitmap.getWidth(), height = bitmap.getHeight();
		        	int[] pixels = new int[width * height];
		            bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
		            bitmap.recycle();
		            bitmap = null;
		            RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);
		            BinaryBitmap bBitmap = new BinaryBitmap(new HybridBinarizer(source));
		            MultiFormatReader reader = new MultiFormatReader();
		            try
		            {
		                Result result = reader.decode(bBitmap);
		                Log.v("QR", result.getText());
		            }
		            catch (NotFoundException e){}
				}else{
				}
			}
		};

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		this.camara = Camera.open();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		this.camara.stopPreview();
		this.camara.setPreviewCallback(null);
		this.camara.release();
		this.camara = null;
	}
	
	private Runnable doAutoFocus = new Runnable() {
        public void run() {
            camara.autoFocus(autoFocusCB);
        }
    };
    
    AutoFocusCallback autoFocusCB = new AutoFocusCallback() {
            public void onAutoFocus(boolean success, Camera camera) {
                autoFocusHandler.postDelayed(doAutoFocus, 1000);
            }
        };

	public SurfaceView getSurfaceView() {
		return surfaceView;
	}

	public void setSurfaceView(SurfaceView surfaceView) {
		this.surfaceView = surfaceView;
	}

	public SurfaceHolder getSurfaceHolder() {
		return surfaceHolder;
	}

	public void setSurfaceHolder(SurfaceHolder surfaceHolder) {
		this.surfaceHolder = surfaceHolder;
	}

	public Camera getCamara() {
		return camara;
	}

	public void setCamara(Camera camara) {
		this.camara = camara;
	}
}
