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
const int FAST_DETECTION = 2; //FastFeatureDetector
const int STAR_DETECTION = 3; //StarFeatureDetector
const int ORB_DETECTION = 4; //ORB
const int MSER_DETECTION = 5; //MSER
const int GFTT_DETECTION = 6; //GoodFeaturesToTrackDetector
const int HARRIS_DETECTION = 7; //GoodFeaturesToTrackDetector with Harris detector enabled

vector<KeyPoint> keyPoints_1, keyPoints_2;
Mat descriptors_1, descriptors_2;
long objeto_long;

JNIEXPORT bool JNICALL Java_com_example_mipatternrecognition_MainActivity_FindFeatures(
		JNIEnv*, jobject, jlong addrGray, jlong addrRgba, jint TypeDetection) {
	Mat& mGr = *(Mat*) addrGray;
	Mat& mRgb = *(Mat*) addrRgba;
	objeto_long = addrGray;
	int minHessian = 500;
	SurfFeatureDetector detector_Surf(minHessian);
	SiftFeatureDetector detector_Sift(minHessian);

	FastFeatureDetector detector_Fast(50);

	OrbFeatureDetector detector_Orb(500, 1.2f, 8, 14, 0, 2, 0, 14);

	MserFeatureDetector detector_Mser(5, 60, 14400, 0.25, 0.2, 200, 1.01, 0.003,
			5);

	int maxCorners = 1000;
	double qualityLevel = 0.01;
	double minDistance = 1.;
	int blockSize = 3;
	bool useHarrisDetector;
	double k = 0.04;
	useHarrisDetector = false;
	GoodFeaturesToTrackDetector detector_Gftt(maxCorners, qualityLevel,
			minDistance, blockSize, useHarrisDetector, k);
	useHarrisDetector = true;
	GoodFeaturesToTrackDetector detector_Harris(maxCorners, qualityLevel,
			minDistance, blockSize, useHarrisDetector, k);

	int maxSize = 45;
	int responseThreshold = 30;
	int lineThresholdProjected = 10;
	int lineThresholdBinarized = 8;
	int suppressNonmaxSize = 5;
	StarFeatureDetector detector_Star(maxSize, responseThreshold,
			lineThresholdProjected, lineThresholdBinarized, suppressNonmaxSize);

	//http://stackoverflow.com/questions/14808429/classification-of-detectors-extractors-and-matchers

	SurfDescriptorExtractor extractor_Surf;
	SiftDescriptorExtractor extractor_Sift;
	//FastDescriptorExtractor extractor_Fast;
	OrbDescriptorExtractor extractor_Orb;
	FREAK extractor_Freak;
	//MserDescriptorExtractor extractor_Mser;
	//GFTTDescriptorExtractor extractor_GFTT;
	//HarrisDescriptorExtractor extractor_Harris;
	//StarDescriptorExtractor extractor_Star;

	switch (TypeDetection) {
	case SURF_DETECTION:
		detector_Surf.detect(mGr, keyPoints_1);
		extractor_Surf.compute(mGr, keyPoints_1, descriptors_1);
		break;
	case SIFT_DETECTION:
		detector_Sift.detect(mGr, keyPoints_1);
		extractor_Sift.compute(mGr, keyPoints_1, descriptors_1);
		break;
	case FAST_DETECTION:
		detector_Fast.detect(mGr, keyPoints_1);
		extractor_Freak.compute(mGr, keyPoints_1, descriptors_1);
		break;
	case ORB_DETECTION:
		detector_Orb.detect(mGr, keyPoints_1);
		extractor_Orb.compute(mGr, keyPoints_1, descriptors_1);
		break;
	case MSER_DETECTION:
		detector_Mser.detect(mGr, keyPoints_1);
		extractor_Surf.compute(mGr, keyPoints_1, descriptors_1);
		break;
	case GFTT_DETECTION:
		detector_Gftt.detect(mGr, keyPoints_1);
		extractor_Sift.compute(mGr, keyPoints_1, descriptors_1);
		break;
	case HARRIS_DETECTION:
		detector_Harris.detect(mGr, keyPoints_1);
		extractor_Orb.compute(mGr, keyPoints_1, descriptors_1);
		break;
	case STAR_DETECTION:
		detector_Star.detect(mGr, keyPoints_1);
		extractor_Orb.compute(mGr, keyPoints_1, descriptors_1);
		break;
	}

	for (unsigned int i = 0; i < keyPoints_1.size(); i++) {
		const KeyPoint& kp = keyPoints_1[i];
		circle(mRgb, Point(kp.pt.x, kp.pt.y), 10, Scalar(255, 0, 0, 255));
	}

	return true;
}

JNIEXPORT bool JNICALL Java_com_example_mipatternrecognition_MainActivity_FindObject(
		JNIEnv*, jobject, jlong addrGray, jlong addrRgba, jint TypeDetection) {
	Mat& mGr = *(Mat*) addrGray;
	Mat& mRgb = *(Mat*) addrRgba;
	Mat& objeto = *(Mat*) objeto_long;
	int minHessian = 500;
	SurfFeatureDetector detector_Surf(minHessian);
	SiftFeatureDetector detector_Sift(minHessian);

	FastFeatureDetector detector_Fast(50);

	OrbFeatureDetector detector_Orb(500, 1.2f, 8, 14, 0, 2, 0, 14);

	MserFeatureDetector detector_Mser(5, 60, 14400, 0.25, 0.2, 200, 1.01, 0.003,
			5);

	int maxCorners = 1000;
	double qualityLevel = 0.01;
	double minDistance = 1.;
	int blockSize = 3;
	bool useHarrisDetector;
	double k2 = 0.04;
	useHarrisDetector = false;
	GoodFeaturesToTrackDetector detector_Gftt(maxCorners, qualityLevel,
			minDistance, blockSize, useHarrisDetector, k2);
	useHarrisDetector = true;
	GoodFeaturesToTrackDetector detector_Harris(maxCorners, qualityLevel,
			minDistance, blockSize, useHarrisDetector, k2);

	int maxSize = 45;
	int responseThreshold = 30;
	int lineThresholdProjected = 10;
	int lineThresholdBinarized = 8;
	int suppressNonmaxSize = 5;
	StarFeatureDetector detector_Star(maxSize, responseThreshold,
			lineThresholdProjected, lineThresholdBinarized, suppressNonmaxSize);

	//http://stackoverflow.com/questions/14808429/classification-of-detectors-extractors-and-matchers

	SurfDescriptorExtractor extractor_Surf;
	SiftDescriptorExtractor extractor_Sift;
	OrbDescriptorExtractor extractor_Orb;
	FREAK extractor_Freak;

	switch (TypeDetection) {
	case SURF_DETECTION:
		detector_Surf.detect(mGr, keyPoints_2);
		extractor_Surf.compute(mGr, keyPoints_2, descriptors_2);
		break;
	case SIFT_DETECTION:
		detector_Sift.detect(mGr, keyPoints_2);
		extractor_Sift.compute(mGr, keyPoints_2, descriptors_2);
		break;
	case FAST_DETECTION:
		detector_Fast.detect(mGr, keyPoints_2);
		extractor_Freak.compute(mGr, keyPoints_2, descriptors_2);
		break;
	case ORB_DETECTION:
		detector_Orb.detect(mGr, keyPoints_2);
		extractor_Orb.compute(mGr, keyPoints_2, descriptors_2);
		break;
	case MSER_DETECTION:
		detector_Mser.detect(mGr, keyPoints_2);
		extractor_Surf.compute(mGr, keyPoints_2, descriptors_2);
		break;
	case GFTT_DETECTION:
		detector_Gftt.detect(mGr, keyPoints_2);
		extractor_Sift.compute(mGr, keyPoints_2, descriptors_2);
		break;
	case HARRIS_DETECTION:
		detector_Harris.detect(mGr, keyPoints_2);
		extractor_Orb.compute(mGr, keyPoints_2, descriptors_2);
		break;
	case STAR_DETECTION:
		detector_Star.detect(mGr, keyPoints_2);
		extractor_Orb.compute(mGr, keyPoints_2, descriptors_2);
		break;
	}

	if (descriptors_2.rows == 0 || descriptors_1.rows == 0
			|| keyPoints_2.size() == 0 || keyPoints_1.size() == 0) {
		return false;
	}

	FlannBasedMatcher matcher;
	vector<DMatch> matches;
	matcher.match(descriptors_1, descriptors_2, matches);

	double max_dist = 0;
	double min_dist = 100;

	//-- Quick calculation of max and min distances between keypoints
	for (int i = 0; i < descriptors_1.rows; i++) {
		double dist = matches[i].distance;
		if (dist < min_dist)
			min_dist = dist;
		if (dist > max_dist)
			max_dist = dist;
	}

	//-- Draw only "good" matches (i.e. whose distance is less than 2*min_dist,
	//-- or a small arbitary value ( 0.02 ) in the event that min_dist is very
	//-- small)
	//-- PS.- radiusMatch can also be used here.
	vector<DMatch> good_matches;

	for (int i = 0; i < descriptors_1.rows; i++) {
		if (matches[i].distance <= 3 * min_dist) {
			good_matches.push_back(matches[i]);
		}
	}

	vector<Point2f> obj;
	vector<Point2f> scene;

	for (int i = 0; i < good_matches.size(); i++) {
		//-- Get the keypoints from the good matches
		obj.push_back(keyPoints_1[good_matches[i].queryIdx].pt);
		scene.push_back(keyPoints_2[good_matches[i].trainIdx].pt);
	}

	try {
		Mat H = findHomography(obj, scene, CV_RANSAC);

		vector<Point2f> obj_corners(4);
		obj_corners[0] = cvPoint(0, 0);
		obj_corners[1] = cvPoint(objeto.cols, 0);
		obj_corners[2] = cvPoint(objeto.cols, objeto.rows);
		obj_corners[3] = cvPoint(0, objeto.rows);
		vector<Point2f> scene_corners(4);

		perspectiveTransform(obj_corners, scene_corners, H);

		line(mRgb, scene_corners[0], scene_corners[1], Scalar(0, 255, 0), 4);
		line(mRgb, scene_corners[1], scene_corners[2], Scalar(255, 0, 0), 4);
		line(mRgb, scene_corners[2], scene_corners[3], Scalar(0, 0, 255), 4);
		line(mRgb, scene_corners[3], scene_corners[0], Scalar(255, 255, 255),
				4);

		for (unsigned int i = 0; i < scene.size(); i++) {
			const Point2f& kp = scene[i];
			circle(mRgb, Point(kp.x, kp.y), 10, Scalar(255, 0, 0, 255));
		}

		for (int i = 0; i < 4; i++) {
			char aux[80];
			strcpy(aux, "Punto ");
			char ptn[10], cVal[32], cVal2[32];
			sprintf(ptn, "%i", i);
			strcat(aux, ptn);
			strcat(aux, " ");
			sprintf(cVal, "%f", scene_corners[i].x);
			strcat(aux, " ");
			strcat(aux, cVal);
			sprintf(cVal2, "%f", scene_corners[i].y);
			strcat(aux, " ");
			strcat(aux, cVal2);
			//aux.append("Punto");
			__android_log_write(ANDROID_LOG_ERROR, "Tag", aux);

		}

	} catch (Exception e) {
		return false;
	}

//else
	return false;
}

}

