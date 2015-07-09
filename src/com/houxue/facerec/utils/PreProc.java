package com.houxue.facerec.utils;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import android.util.Log;

/**
 * ͼƬԤ����������ת��Ϊ�Ҷ�ͼ��ֱ��ͼ���⻯�����루��һ�����У�
 * 
 * @author houxue
 * @version 1.1
 * @date 2015.5.11
 */
public class PreProc {

	private static final String TAG = "PreProc.class";

	private String haar_face = null;

	public PreProc(String classifier) {
		this.haar_face = classifier;
	}

	/**
	 * ͼ��ҶȻ���ֱ��ͼ���⻯
	 * 
	 * @param srcMat
	 *            ԭʼ��ʽ��ͼ��
	 * 
	 * @return dstMat ������ͼ��
	 */
	public Mat cvtColHist(Mat srcMat) {
		Mat tmpMat = new Mat();
		Mat dstMat = new Mat();

		Imgproc.cvtColor(srcMat, tmpMat, Imgproc.COLOR_BGR2GRAY);
		Imgproc.equalizeHist(tmpMat, dstMat);

		return dstMat;
	}

	/**
	 * ����λ�ü��
	 * 
	 * @param srcMat
	 *            �ҶȻ��;��⻯���ͼ��
	 * 
	 * @return feature ��������
	 */
	public FeaturePosi FeatureDetect(Mat srcMat) {
		FeaturePosi feature = new FeaturePosi();

		CascadeClassifier faceClassifier = new CascadeClassifier(haar_face);

		if (faceClassifier.empty()) {
			Log.e(TAG, "Failed to load cascade classifier");
		} else {
			Log.i(TAG, "Loaded cascade classifier from " + haar_face);
		}
		MatOfRect faceRect = new MatOfRect();
		faceClassifier.detectMultiScale(srcMat, faceRect);
		if (faceRect.toArray().length != 0) {
			feature.setFace(faceRect.toArray()[0]);
		} else {
			Log.e(TAG, "Did not detect face.");
		}

		return feature;
	}

	/**
	 * ͼƬ����
	 * 
	 * @param srcMat
	 *            �ҶȻ��;��⻯�������ͼ��
	 * @param fPosi
	 *            ���������������������۾������ӣ���͵�λ��
	 * 
	 * @return dstMat ���к������ͼ��
	 */
	public Mat CropImg(Mat srcMat, FeaturePosi fPosi) {
		Mat dstMat = new Mat();

		dstMat = srcMat.submat(fPosi.getFace());

		return dstMat;
	}

	/**
	 * ͼ���׼��
	 * 
	 * @param srcMat
	 *            �ҶȻ��;��⻯�������ͼ��
	 * 
	 * @return dstMat ��׼�����ͼ��
	 */
	public Mat NormalImg(Mat srcMat) {
		Mat dstMat = new Mat();
		Mat result = new Mat();

		Mat tmpMat = cvtColHist(srcMat);
		FeaturePosi fp = FeatureDetect(tmpMat);

		if (fp.getFace() != null) {
			dstMat = CropImg(tmpMat, fp);
			Imgproc.resize(dstMat, result, new Size(200, 200));
		} else {
			// ���δ��⵽�������򷵻�null
			return null;
		}

		return result;
	}

}
