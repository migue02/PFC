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

vector<KeyPoint> keyPoints_1, keyPoints_2;

long objeto_long;

JNIEXPORT bool JNICALL Java_com_example_mipatternrecognition_Reconocimiento_FindFeatures2(
		JNIEnv*, jobject, jlong addrGray, jlong addrRgba, jlong addrKeyPoints, jlong addrDescriptores) {
	Mat& mGr = *(Mat*) addrGray;
	Mat& mRgb = *(Mat*) addrRgba;
	Mat& keyPoints = *(Mat*) addrKeyPoints;
	Mat& descriptores = *(Mat*) addrDescriptores;

	//vector<KeyPoint> keyPoints_1;

	objeto_long = addrGray;
	int minHessian = 500;
	SurfFeatureDetector detector_Surf(minHessian);

	//http://stackoverflow.com/questions/14808429/classification-of-detectors-extractors-and-matchers

	SurfDescriptorExtractor extractor_Surf;

	detector_Surf.detect(mGr, keyPoints_1);
	extractor_Surf.compute(mGr, keyPoints_1, descriptores);

	putText(mRgb, "Encontrado", Point2f(100, 100), FONT_HERSHEY_PLAIN,
						2, Scalar(0, 0, 255, 150), 2);

	std::vector<cv::Point2f> points;
	std::vector<cv::KeyPoint>::iterator it;

	for( it= keyPoints_1.begin(); it!= keyPoints_1.end();it++)
	{
	    points.push_back(it->pt);
	}

	Mat pointmatrix(points);

	keyPoints = pointmatrix;

	for (unsigned int i = 0; i < keyPoints_1.size(); i++) {
		const KeyPoint& kp = keyPoints_1[i];
		circle(mRgb, Point(kp.pt.x, kp.pt.y), 10, Scalar(255, 0, 0, 255));
	}

	return true;
}

JNIEXPORT bool JNICALL Java_com_example_mipatternrecognition_Reconocimiento_FindObject2(
		JNIEnv*, jobject, jlong addrGray, jlong addrRgba, jlong addrKeyPoints, jlong addrDescriptores) {
	Mat& mGr = *(Mat*) addrGray;
	Mat& mRgb = *(Mat*) addrRgba;
	Mat& objeto = *(Mat*) objeto_long;
	Mat& keyPoints = *(Mat*) addrKeyPoints;
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
