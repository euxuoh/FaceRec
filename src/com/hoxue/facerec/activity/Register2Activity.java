package com.hoxue.facerec.activity;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.PreviewCallback;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.SurfaceHolder.Callback;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;

/**
 * @Title: Register2Activity.java
 * @Package: com.houxue.facerec.activity
 * @Description: �û�ע��ʱ��ȡ������Ϣ�Ľ���
 * @author Hou Xue
 * @Date 2015.4.17
 * @version 1.0
 */
public class Register2Activity extends Activity implements Callback,
		PreviewCallback {
	
	private static final String TAG = "Register2Activity.";

	// Ԥ����
	SurfaceView camerasurface = null;
	Camera camera = null;

	// ǰ������ͷlayout�Ƕ�
	private int orientionOfCamera;

	private TextView tips_edtx = null;
	private Button enter_btn = null;

	// ����ͼ���·��
	private String strCaptureFilePath = Environment
			.getExternalStorageDirectory() + "/frec/";

	// ͼ������
	private Bitmap newBitmap = null;
	
	// �ж�����ͼ���Ƿ�ϸ�
	private static final int JUDGE = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register2);

		initView();
	}

	// �����ʼ��
	public void initView() {
		camerasurface = (SurfaceView) findViewById(R.id.surfaceview_register2);
		LayoutParams para = new LayoutParams(800, 1000);
		para.addRule(RelativeLayout.CENTER_IN_PARENT);
		camerasurface.setLayoutParams(para);
		camerasurface.getHolder().addCallback(this);
		camerasurface.setKeepScreenOn(true);

		tips_edtx = (TextView) findViewById(R.id.register2_tips);
		tips_edtx.setText("����������ͷ...");

		// ȷ������
		enter_btn = (Button) this.findViewById(R.id.register2_ent);
		enter_btn.setOnClickListener(new View.OnClickListener() {

			public void onClick(View arg0) {
				camera.takePicture(null, null, takePictureCallback);
			}
		});
	}

	@SuppressLint("NewApi")
	@Override
	protected void onResume() {
		super.onResume();
		// �������ͷ�Ƿ�ռ��
		if (checkCamera(this)) {
			// ���һ������ͷʵ��
			camera = getCameraInstance(1);
		}
		CameraInfo info = new CameraInfo();
		orientionOfCamera = info.orientation;
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (camera != null) {
			camera.setPreviewCallback(null);
			camera.stopPreview();
			camera.release();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (resultCode == RESULT_OK) {
			String result = data.getExtras().getString("result");
			if (result.equals("OK")) {
				// ����ǰ���ش�����
				setResult(RESULT_OK);
				finish();
			}
		}
	}

	/**
	 * �������ͷ�Ƿ����
	 * 
	 * @param context
	 *            ��Activity������
	 * 
	 * @return true ����ͷ���� false ����ͷ������
	 */
	private boolean checkCamera(Context context) {
		if (context.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA)) {
			Log.i(TAG + "checkCamera()", "����ͷ����");
			return true;
		} else {
			Log.w(TAG + "checkCamera()", "����ͷ������");
			return false;
		}
	}

	/**
	 * ��ȡһ������ͷʵ��
	 * 
	 * @param int ����ͷ���
	 * 
	 * @return Camera һ������ͷʵ��
	 */
	@SuppressLint("NewApi")
	public Camera getCameraInstance(int parmInt) {
		Camera camera = null;

		try {
			// 0���򿪺�������ͷ��1����ǰ������ͷ
			camera = Camera.open(parmInt);
		} catch (Exception e) {
			Log.w(TAG + "getCameraInstance()", "����ͷ��ռ��");
		}

		return camera;
	}

	// ��takepicture�е��õĻص�����֮һ������jpeg��ʽ��ͼ��
	private PictureCallback takePictureCallback = new PictureCallback() {
		public void onPictureTaken(byte[] _data, Camera _camera) {
			try {
				// ȡ����Ƭ
				Bitmap bm = BitmapFactory.decodeByteArray(_data, 0,
						_data.length);

				// ��������ͷ����ת�Ƕ�����BitMap
				float scale = Math.min(1,
						Math.min(600f / bm.getWidth(), 600f / bm.getHeight()));
				Matrix matrix = new Matrix();
				matrix.setRotate(orientionOfCamera - 90);
				matrix.postScale(scale, scale);
				newBitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(),
						bm.getHeight(), matrix, true);

				// �����ļ�Ŀ¼
				File myCaptureFile = new File(strCaptureFilePath);
				if (!myCaptureFile.exists()) {
					myCaptureFile.mkdirs();
				}
				// �����ļ�
				File imgPath = new File(strCaptureFilePath, "img.jpg");
				BufferedOutputStream bos = new BufferedOutputStream(
						new FileOutputStream(imgPath));

				// ����ѹ��ת������
				newBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);

				// ����flush()����������BufferStream
				bos.flush();

				// ����OutputStream
				bos.close();
				
				// ����Ƭ��ʾ2��
				tips_edtx.setText("���ճɹ�...");
				Thread.sleep(1000);

				Intent intent = new Intent(Register2Activity.this,
						InfoSetActivity.class);
				intent.putExtra("imgPath", strCaptureFilePath + "img.jpg");
				startActivityForResult(intent, JUDGE);
			} catch (Exception e) {
				Log.e(TAG + "takePictureCallback()", e.getMessage());
			}
		}
	};

	@Override
	public void onPreviewFrame(final byte[] data, Camera camera) {
		// TODO Auto-generated method stub
		camera.setPreviewCallback(null);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		try {
			camera.setPreviewDisplay(holder);
		} catch (IOException e) {
			e.printStackTrace();
		}
		camera.setDisplayOrientation(90);
		camera.startPreview();
		camera.setPreviewCallback(this);

	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		// TODO Auto-generated method stub
	}

}
