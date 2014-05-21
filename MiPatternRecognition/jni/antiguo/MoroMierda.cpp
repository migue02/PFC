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
vector<Point2f> obj;
vector<Point2f> scene;

bool refineMatchesWithHomography(
		const std::vector<cv::KeyPoint>& queryKeypoints,
		const std::vector<cv::KeyPoint>& trainKeypoints,
		float reprojectionThreshold, std::vector<cv::DMatch>& matches,
		cv::Mat& homography) {
	const int minNumberMatchesAllowed = 8;
	if (matches.size() < minNumberMatchesAllowed)
		return false;
	// Prepare data for cv::findHomography
	//std::vector<cv::Point2f> srcPoints(matches.size());
	//std::vector<cv::Point2f> dstPoints(matches.size());
	obj.clear();
	scene.clear();
	for (size_t i = 0; i < matches.size(); i++) {
		obj[i] = trainKeypoints[matches[i].trainIdx].pt;
		scene[i] = queryKeypoints[matches[i].queryIdx].pt;
	}
	// Find homography matrix and get inliers mask
	std::vector<unsigned char> inliersMask(obj.size());
	homography = cv::findHomography(obj, scene, CV_FM_RANSAC,
			reprojectionThreshold, inliersMask);
	std::vector<cv::DMatch> inliers;
	for (size_t i = 0; i < inliersMask.size(); i++) {
		if (inliersMask[i])
			inliers.push_back(matches[i]);
	}
	matches.swap(inliers);
	return matches.size() > minNumberMatchesAllowed;
}

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
	vector<vector<DMatch> > matches;
	matcher.knnMatch(descriptors_1, descriptors_2, matches, 2);

	//-- Draw only "good" matches (i.e. whose distance is less than 2*min_dist,
	//-- or a small arbitary value ( 0.02 ) in the event that min_dist is very
	//-- small)
	//-- PS.- radiusMatch can also be used here.
	vector<DMatch> good_matches;

	for (int i = 0; i < min(descriptors_1.rows - 1, (int) matches.size()); i++) //THIS LOOP IS SENSITIVE TO SEGFAULTS
			{
		if ((matches[i][0].distance < 0.6 * (matches[i][1].distance))
				&& ((int) matches[i].size() <= 2 && (int) matches[i].size() > 0)) {
			good_matches.push_back(matches[i][0]);
		}
	}

	char cVal[50];
	sprintf(cVal, "%i", good_matches.size());
	putText(mRgb, cVal, Point2f(110, 100), FONT_HERSHEY_PLAIN, 2,
			Scalar(0, 255, 0, 255), 2);

	Mat H;
	float reprojectionThreshold = 3;
	try {
		bool encontrado = refineMatchesWithHomography(keyPoints_1, keyPoints_2,
				reprojectionThreshold, good_matches, H);

		if (encontrado) {

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
		} else {
			char cVal[50];
			sprintf(cVal, "%i", good_matches.size());
			putText(mRgb, cVal, Point2f(100, 100), FONT_HERSHEY_PLAIN, 2,
					Scalar(0, 0, 255, 255), 2);
		}

	} catch (Exception e) {

	}
//else
	return false;
}

}

