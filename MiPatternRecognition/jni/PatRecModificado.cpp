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

const int SURF_DETECTION = 0; //SURF (nonfree module)
const int SIFT_DETECTION = 1; //SIFT (nonfree module)



long objeto_long;

JNIEXPORT jobjectArray JNICALL Java_com_example_mipatternrecognition_Reconocimiento_FindFeatures(
		JNIEnv* env, jobject, jlong addrGray, jlong addrRgba, jlong addrDescriptores) {
	Mat& mGr = *(Mat*) addrGray;
	Mat& mRgb = *(Mat*) addrRgba;
	Mat& descriptores = *(Mat*) addrDescriptores;
	vector<KeyPoint> keyPoints;

	objeto_long = addrGray;
	int minHessian = 500;
	SurfFeatureDetector detector_Surf(minHessian);

	//http://stackoverflow.com/questions/14808429/classification-of-detectors-extractors-and-matchers

	SurfDescriptorExtractor extractor_Surf;

	detector_Surf.detect(mGr, keyPoints);
	extractor_Surf.compute(mGr, keyPoints, descriptores);

	putText(mRgb, "Patron adquirido", Point2f(100, 100), FONT_HERSHEY_PLAIN, 2,
			Scalar(0, 0, 255, 150), 2);

	// Get a class reference for java.lang.Integer
	jclass cls = env->FindClass("org/opencv/features2d/KeyPoint");
	// Get the Method ID of the constructor which takes an int
	jmethodID midInit = env->GetMethodID(cls, "<init>", "(FFFFFII)V");
	// Call back constructor to allocate a new instance
	jobjectArray newKeyPointArr = env->NewObjectArray(keyPoints.size(), cls,
			NULL);

	for (unsigned int i = 0; i < keyPoints.size(); i++) {
		jobject newKeyPoint = env->NewObject(cls, midInit, keyPoints[i].pt.x,
				keyPoints[i].pt.y, keyPoints[i].size, keyPoints[i].angle,
				keyPoints[i].response, keyPoints[i].octave,
				keyPoints[i].class_id);
		env->SetObjectArrayElement(newKeyPointArr, i, newKeyPoint);
	}

	for (unsigned int i = 0; i < keyPoints.size(); i++) {
		const KeyPoint& kp = keyPoints[i];
		circle(mRgb, Point(kp.pt.x, kp.pt.y), 10, Scalar(255, 0, 0, 255));
	}

	return newKeyPointArr;
}

JNIEXPORT bool JNICALL Java_com_example_mipatternrecognition_Reconocimiento_FindObject(
		JNIEnv* env, jobject, jlong addrGray, jlong addrRgba,
		jobjectArray arrayKeyPoints, jlong addrDescriptores) {
	Mat& mGr = *(Mat*) addrGray;
	Mat& mRgb = *(Mat*) addrRgba;
	Mat& objeto = *(Mat*) objeto_long;
	Mat& descriptores_obj = *(Mat*) addrDescriptores;
	Mat descriptores_esc;
	vector<KeyPoint> keyPoints_obj, keyPoints_esc;

	int minHessian = 500;
	SurfFeatureDetector detector_Surf(minHessian);

	SurfDescriptorExtractor extractor_Surf;

	jint length = env->GetArrayLength(arrayKeyPoints);

	// Get a class reference for java.lang.Integer
	jclass objClassKeyPoint = env->FindClass("org/opencv/features2d/KeyPoint");
	assert(objClassKeyPoint != NULL);

	// Get the Fields of the KeyPoint
	jfieldID myFieldClass = env->GetFieldID(objClassKeyPoint, "class_id", "I");
	assert(myFieldClass != NULL);
	jfieldID myFieldSize = env->GetFieldID(objClassKeyPoint, "size", "F");
	assert(myFieldSize != NULL);
	jfieldID myFieldAngle = env->GetFieldID(objClassKeyPoint, "angle", "F");
	assert(myFieldAngle != NULL);
	jfieldID myFieldOctave = env->GetFieldID(objClassKeyPoint, "octave", "I");
	assert(myFieldOctave != NULL);
	jfieldID myFieldResponse = env->GetFieldID(objClassKeyPoint, "response",
			"F");
	assert(myFieldResponse != NULL);

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
		aux.angle = env->GetFloatField(newKeyPoint, myFieldAngle);
		aux.response = env->GetFloatField(newKeyPoint, myFieldResponse);
		aux.size = env->GetFloatField(newKeyPoint, myFieldSize);
		aux.class_id = env->GetIntField(newKeyPoint, myFieldClass);
		aux.octave = env->GetIntField(newKeyPoint, myFieldOctave);

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

	char au[100],ptn[10];
	strcpy(au, "GetArrayLenght = ");
	sprintf(ptn, "%i", keyPoints_obj.size());
	strcat(au, ptn);
	__android_log_write(ANDROID_LOG_ERROR, "Tag", au);

	detector_Surf.detect(mGr, keyPoints_esc);
	extractor_Surf.compute(mGr, keyPoints_esc, descriptores_esc);

	if (descriptores_esc.rows == 0 || descriptores_obj.rows == 0
			|| keyPoints_esc.size() == 0 || keyPoints_obj.size() == 0) {
		return false;
	}

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
			obj_corners[1] = cvPoint(objeto.cols, 0);
			obj_corners[2] = cvPoint(objeto.cols, objeto.rows);
			obj_corners[3] = cvPoint(0, objeto.rows);
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

		}
	} catch (Exception e) {
	}
	return false;
}

vector<KeyPoint> keyPoints_1, keyPoints_2;

JNIEXPORT bool JNICALL Java_com_example_mipatternrecognition_Reconocimiento_FindFeatures2(
		JNIEnv*, jobject, jlong addrGray, jlong addrRgba, jlong addrDescriptores) {
	Mat& mGr = *(Mat*) addrGray;
	Mat& mRgb = *(Mat*) addrRgba;
	Mat& descriptores = *(Mat*) addrDescriptores;

	//vector<KeyPoint> keyPoints_1;

	objeto_long = addrGray;
	int minHessian = 500;
	SurfFeatureDetector detector_Surf(minHessian);

	//http://stackoverflow.com/questions/14808429/classification-of-detectors-extractors-and-matchers

	SurfDescriptorExtractor extractor_Surf;

	detector_Surf.detect(mGr, keyPoints_1);
	extractor_Surf.compute(mGr, keyPoints_1, descriptores);

	putText(mRgb, "Patron adquirido", Point2f(100, 100), FONT_HERSHEY_PLAIN, 2,
			Scalar(0, 0, 255, 150), 2);

	for (unsigned int i = 0; i < keyPoints_1.size(); i++) {
		const KeyPoint& kp = keyPoints_1[i];
		circle(mRgb, Point(kp.pt.x, kp.pt.y), 10, Scalar(255, 0, 0, 255));
	}

	return true;
}

JNIEXPORT bool JNICALL Java_com_example_mipatternrecognition_Reconocimiento_FindObject2(
		JNIEnv*, jobject, jlong addrGray, jlong addrRgba, jlong addrDescriptores) {
	Mat& mGr = *(Mat*) addrGray;
	Mat& mRgb = *(Mat*) addrRgba;
	Mat& objeto = *(Mat*) objeto_long;
	Mat& descriptores = *(Mat*) addrDescriptores;
	Mat descriptors_2;
	int minHessian = 500;
	SurfFeatureDetector detector_Surf(minHessian);

	SurfDescriptorExtractor extractor_Surf;

	detector_Surf.detect(mGr, keyPoints_2);
	extractor_Surf.compute(mGr, keyPoints_2, descriptors_2);

	if (descriptors_2.rows == 0 || descriptores.rows == 0
			|| keyPoints_2.size() == 0 || keyPoints_1.size() == 0) {
		return false;
	}

	FlannBasedMatcher matcher;
	vector<vector<DMatch> > matches;
	try {
		matcher.knnMatch(descriptores, descriptors_2, matches, 2);

		//-- Draw only "good" matches (i.e. whose distance is less than 2*min_dist,
		//-- or a small arbitary value ( 0.02 ) in the event that min_dist is very
		//-- small)
		//-- PS.- radiusMatch can also be used here.
		vector<DMatch> good_matches;

		for (int i = 0; i < min(descriptores.rows - 1, (int) matches.size());
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
				obj.push_back(keyPoints_1[good_matches[i].queryIdx].pt);
				scene.push_back(keyPoints_2[good_matches[i].trainIdx].pt);
			}

			Mat H = findHomography(obj, scene, CV_RANSAC);

			vector<Point2f> obj_corners(4);
			obj_corners[0] = cvPoint(0, 0);
			obj_corners[1] = cvPoint(objeto.cols, 0);
			obj_corners[2] = cvPoint(objeto.cols, objeto.rows);
			obj_corners[3] = cvPoint(0, objeto.rows);
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

		}
	} catch (Exception e) {
	}
	return false;
}

}
