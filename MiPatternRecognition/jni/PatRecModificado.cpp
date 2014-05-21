#include <jni.h>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d/features2d.hpp>
#include <vector>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "opencv2/nonfree/features2d.hpp"
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/calib3d/calib3d.hpp"
#include <android/log.h>

using namespace std;
using namespace cv;

extern "C" {


JNIEXPORT jobjectArray JNICALL Java_com_example_mipatternrecognition_ReconocimientoObjeto_FindFeatures(
		JNIEnv* env, jobject, jlong addrGray, jlong addrRgba, jlong addrDescriptores) {
	// ----------------------
	//Crear matrices y vector
	// ----------------------
	Mat& mGr = *(Mat*) addrGray;
	Mat& mRgb = *(Mat*) addrRgba;
	Mat& descriptores = *(Mat*) addrDescriptores;
	vector<KeyPoint> keyPoints;

	// ------------------
	//Inicializacion SURF
	// ------------------
	int minHessian = 400;
	SurfFeatureDetector detector_Surf(minHessian);
	SurfDescriptorExtractor extractor_Surf;

	// ----------------------------------------------------------------
	// Deteccion de keypoints y extraccion de caracteristicas del objeto
	// -----------------------------------------------------------------
	detector_Surf.detect(mGr, keyPoints);
	if (keyPoints.size() > 0){
		extractor_Surf.compute(mGr, keyPoints, descriptores);
		putText(mRgb, "Patron adquirido", Point2f(100, 100), FONT_HERSHEY_PLAIN, 2,
				Scalar(0, 0, 255, 150), 2);
	}

	// ----------------------------------------------
	// Creacion del objectArray a devolver (keyPoints)
	// ----------------------------------------------
	jclass cls = env->FindClass("org/opencv/features2d/KeyPoint"); // Get a class reference for java.lang.Integer
	//jmethodID midInit = env->GetMethodID(cls, "<init>", "(FFFFFII)V"); // Get the Method ID of the constructor which takes an int
	jmethodID midInit = env->GetMethodID(cls, "<init>", "(FFF)V"); // Get the Method ID of the constructor which takes an int
	jobjectArray newKeyPointArr = env->NewObjectArray(keyPoints.size(), cls,
			NULL);// Call back constructor to allocate a new instance

	for (unsigned int i = 0; i < keyPoints.size(); i++) {
//		jobject newKeyPoint = env->NewObject(cls, midInit, keyPoints[i].pt.x,
//				keyPoints[i].pt.y, keyPoints[i].size, keyPoints[i].angle,
//				keyPoints[i].response, keyPoints[i].octave,
//				keyPoints[i].class_id);
		jobject newKeyPoint = env->NewObject(cls, midInit, keyPoints[i].pt.x,
						keyPoints[i].pt.y, keyPoints[i].size);
		env->SetObjectArrayElement(newKeyPointArr, i, newKeyPoint);
	}


	// ------------------------------------
	// Se pintan los keyPoints en la imagen
	// ------------------------------------
	for (unsigned int i = 0; i < keyPoints.size(); i++) {
		const KeyPoint& kp = keyPoints[i];
		circle(mRgb, Point(kp.pt.x, kp.pt.y), 10, Scalar(255, 0, 0, 255));
	}

	return newKeyPointArr;
}

JNIEXPORT bool JNICALL Java_com_example_mipatternrecognition_ReconocimientoObjeto_FindObject(
		JNIEnv* env, jobject, jlong addrGray, jlong addrRgba,
		jobjectArray arrayKeyPoints, jlong addrDescriptores, jint cols, jint rows) {
	// ----------------------
	//Crear matrices y vector
	// ----------------------
	Mat& mGr = *(Mat*) addrGray;
	Mat& mRgb = *(Mat*) addrRgba;
	Mat& descriptores_obj = *(Mat*) addrDescriptores;
	Mat descriptores_esc;
	vector<KeyPoint> keyPoints_obj, keyPoints_esc;

	// ------------------
	//Inicializacion SURF
	// ------------------
	int minHessian = 400;
	SurfFeatureDetector detector_Surf(minHessian);
	SurfDescriptorExtractor extractor_Surf;

	// --------------------------------------------------------------------
	// Deteccion de keypoints y extraccion de caracteristicas del escenario
	// --------------------------------------------------------------------
	jint length = env->GetArrayLength(arrayKeyPoints);
	detector_Surf.detect(mGr, keyPoints_esc);
	if (keyPoints_esc.size() == 0 || length == 0)
		return false;
	extractor_Surf.compute(mGr, keyPoints_esc, descriptores_esc);
	if (descriptores_esc.rows == 0 || descriptores_obj.rows == 0)
		return false;


	// ------------------------------------------------------------------------
	// Creacion de los keyPoints extraidos del objeto (a partir del objectArray)
	// ------------------------------------------------------------------------
	jclass objClassKeyPoint = env->FindClass("org/opencv/features2d/KeyPoint"); // Get a class reference for KeyPoint
	assert(objClassKeyPoint != NULL);

	// Get the Fields of the KeyPoint
	//jfieldID myFieldClass = env->GetFieldID(objClassKeyPoint, "class_id", "I");
	//assert(myFieldClass != NULL);
	jfieldID myFieldSize = env->GetFieldID(objClassKeyPoint, "size", "F");
	assert(myFieldSize != NULL);
	//jfieldID myFieldAngle = env->GetFieldID(objClassKeyPoint, "angle", "F");
	//assert(myFieldAngle != NULL);
	//jfieldID myFieldOctave = env->GetFieldID(objClassKeyPoint, "octave", "I");
	//assert(myFieldOctave != NULL);
	//jfieldID myFieldResponse = env->GetFieldID(objClassKeyPoint, "response",
	//		"F");
	//assert(myFieldResponse != NULL);

	// Get the Fields of the values of the Point (inside the KeyPoints)
	jfieldID myFieldPoint = env->GetFieldID(objClassKeyPoint, "pt",
			"Lorg/opencv/core/Point;");
	assert(myFieldPoint != NULL);
	jclass objClassPoint = env->FindClass("org/opencv/core/Point");
	assert(objClassPoint != NULL);

	// Create KeyPoint Vector
	KeyPoint aux;
	for (unsigned int i = 0; i < length; i++) {
		jobject newKeyPoint = env->GetObjectArrayElement(arrayKeyPoints, i);
		assert(newKeyPoint != NULL);
		//aux.angle = env->GetFloatField(newKeyPoint, myFieldAngle);
		//aux.response = env->GetFloatField(newKeyPoint, myFieldResponse);
		aux.size = env->GetFloatField(newKeyPoint, myFieldSize);
		//aux.class_id = env->GetIntField(newKeyPoint, myFieldClass);
		//aux.octave = env->GetIntField(newKeyPoint, myFieldOctave);

		jobject point = env->GetObjectField(newKeyPoint, myFieldPoint);
		assert(point != NULL);
		jfieldID myFieldPointX = env->GetFieldID(objClassPoint, "x", "D");
		assert(myFieldPointX != NULL);
		jfieldID myFieldPointY = env->GetFieldID(objClassPoint, "y", "D");
		assert(myFieldPointY != NULL);
		aux.pt.x = env->GetDoubleField(point, myFieldPointX);
		aux.pt.y = env->GetDoubleField(point, myFieldPointY);
		keyPoints_obj.push_back(aux);
	}

	// ----------------------------------------------------------------------
	// Obtencion de los matches entre el objeto y el escenario mediante FLANN
	// ----------------------------------------------------------------------
	FlannBasedMatcher matcher;
	vector<vector<DMatch> > matches;
	try {
		matcher.knnMatch(descriptores_obj, descriptores_esc, matches, 2);

		//-- Draw only "good" matches (i.e. whose distance is less than 2*min_dist,
		//-- or a small arbitary value ( 0.02 ) in the event that min_dist is very
		//-- small)
		//-- PS.- radiusMatch can also be used here.
		vector<DMatch> good_matches;

		for (int i = 0; i < min(descriptores_obj.rows - 1, (int) matches.size());
				i++) //THIS LOOP IS SENSITIVE TO SEGFAULTS
				{
			if ((matches[i][0].distance < 0.6 * (matches[i][1].distance))
					&& ((int) matches[i].size() <= 2
							&& (int) matches[i].size() > 0)) {
				good_matches.push_back(matches[i][0]);
			}
		}

		if (good_matches.size() >= 4) {

			vector < Point2f > obj;
			vector < Point2f > scene;

			for (int i = 0; i < good_matches.size(); i++) {
				//-- Get the keypoints from the good matches
				obj.push_back(keyPoints_obj[good_matches[i].queryIdx].pt);
				scene.push_back(keyPoints_esc[good_matches[i].trainIdx].pt);
			}

			Mat H = findHomography(obj, scene, CV_RANSAC);

			vector<Point2f> obj_corners(4);
			obj_corners[0] = cvPoint(0, 0);
			obj_corners[1] = cvPoint(cols, 0);
			obj_corners[2] = cvPoint(cols, rows);
			obj_corners[3] = cvPoint(0, rows);
			vector<Point2f> scene_corners(4);

			perspectiveTransform(obj_corners, scene_corners, H);

			line(mRgb, scene_corners[0], scene_corners[1], Scalar(0, 255, 0),
					4);
			line(mRgb, scene_corners[1], scene_corners[2], Scalar(255, 0, 0),
					4);
			line(mRgb, scene_corners[2], scene_corners[3], Scalar(0, 0, 255),
					4);
			line(mRgb, scene_corners[3], scene_corners[0],
					Scalar(255, 255, 255), 4);

			for (unsigned int i = 0; i < scene.size(); i++) {
				const Point2f& kp = scene[i];
				circle(mRgb, Point(kp.x, kp.y), 10, Scalar(255, 0, 0, 255));
			}

			putText(mRgb, "Encontrado", Point2f(100, 100), FONT_HERSHEY_PLAIN,
					2, Scalar(0, 0, 255, 150), 2);

			return true;

		}
	} catch (Exception e) {
	}
	return false;
}

}
