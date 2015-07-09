package com.hoxue.facerec.activity;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

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
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * @Title: RegisterActivity.java
 * @Package: com.houxue.facerec.activity
 * @Description: �û�ע��ʱ��ȡ������Ϣ�Ľ���
 * @author Hou Xue
 * @Date 2015.4.17
 * @version 1.0
 */
public class RegisterActivity extends Activity {
	
	private static final String TAG = "RegisterActivity.";

	// ����ͷ���
	private final int FRONT_CAMERA = 1;
	// private final int REAR_CAMERA = 0;

	// ����ͷʵ��
	private Camera mCamera = null;
	// ����ͷԤ��ʵ��
	private CameraPreview mPreview = null;
	// ǰ������ͷlayout�Ƕ�
	private int orientionOfCamera;

	private TextView tips_edtx = null;
	private Button enter_btn = null;

	// ����ͼ���·��
	private String strCaptureFilePath = Environment
			.getExternalStorageDirectory() + "/frec/";

	// ͼ������
	private Bitmap newBitmap = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register);

		tips_edtx = (TextView) findViewById(R.id.Register_tips);
		tips_edtx.setText("����������ͷ...");

		// ȷ������
		enter_btn = (Button) this.findViewById(R.id.Register_ent);
		enter_btn.setOnClickListener(new View.OnClickListener() {

			public void onClick(View arg0) {
				mCamera.takePicture(null, null, takePictureCallback);
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		// �������ͷ�Ƿ�ռ��
		if (checkCamera(this)) {
			// ���һ������ͷʵ��
			mCamera = getCameraInstance(FRONT_CAMERA);
		}

		// ��������ͷ�ĽǶ�
		setCameraDisplayOrientation(FRONT_CAMERA, mCamera);

		// �������ͷԤ��ʵ��
		mPreview = new CameraPreview(this, mCamera);
		// ��Ԥ������layout��
		FrameLayout previewFrameLayout = (FrameLayout) findViewById(R.id.camera_preview_reg);
		previewFrameLayout.addView(mPreview);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if(mCamera != null) {
			mCamera.stopPreview();
			mCamera.release();
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

	/**
	 * ��������ͷ�ķ���һ��Ĭ���Ǻ����
	 * 
	 * @param int ����ͷ��� Camera ǰ�Ļ�ȡ������ͷʵ��
	 * 
	 * @return void
	 */
	@SuppressLint("NewApi")
	public void setCameraDisplayOrientation(int paramInt, Camera paramCamera) {
		CameraInfo info = new CameraInfo();
		Camera.getCameraInfo(paramInt, info);
		// �����ʾ�����Ƕ�
		int rotation = ((WindowManager) getSystemService("window"))
				.getDefaultDisplay().getRotation();
		int degrees = 0;
		Log.i(TAG + "setCameraDisplayOrientation()",
				"getRotation's rotation is " + String.valueOf(rotation));
		switch (rotation) {
		case Surface.ROTATION_0:
			degrees = 0;
			break;
		case Surface.ROTATION_90:
			degrees = 90;
			break;
		case Surface.ROTATION_180:
			degrees = 180;
			break;
		case Surface.ROTATION_270:
			degrees = 270;
			break;
		}

		// �������ͷ�İ�װ��ת�Ƕ�
		orientionOfCamera = info.orientation;
		int result;
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			result = (info.orientation + degrees) % 360;
			// ������
			result = (360 - result) % 360;
		} else {
			// ��������ͷ
			result = (info.orientation - degrees + 360) % 360;
		}
		// ע��ǰ���õĴ���ǰ����ӳ���棬�ö���SDK�ĵ��ı�׼DEMO
		paramCamera.setDisplayOrientation(result);
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
				matrix.setRotate(orientionOfCamera - 360);
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

				// �ͷ����
				releaseCamera();

				// ����Ƭ��ʾ2��
				tips_edtx.setText("���ճɹ�...");
				Thread.sleep(2000);

				Intent intent = new Intent(RegisterActivity.this,
						InfoSetActivity.class);
				intent.putExtra("imgPath", strCaptureFilePath + "img.jpg");
				startActivity(intent);

			} catch (Exception e) {
				Log.e(TAG + "takePictureCallback()", e.getMessage());
			}
		}
	};

	/**
	 * �ͷ�����ͷ
	 * 
	 * @param void
	 * 
	 * @return void
	 */
	public void releaseCamera() {
		if (mCamera != null) {
			mCamera.release();
			mCamera = null;
		}
	}

}
