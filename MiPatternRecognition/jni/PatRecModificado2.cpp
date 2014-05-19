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

vector<long> listaDescriptores;
vector<long> listaKeyPoints;
vector<Mat> listaMatDes;
vector<Mat> listaMatKey;
vector<int> listaCols;
vector<int> listaRows;
char au[80], ptn[40];


void Mat_to_vector_KeyPoint(Mat& mat, vector<KeyPoint>& v_kp)
{
    v_kp.clear();
    if (mat.type()==CV_32FC(7) && mat.cols==1)
		for(int i=0; i<mat.rows; i++)
		{
			Vec<float, 7> v = mat.at< Vec<float, 7> >(i, 0);
			KeyPoint kp(v[0], v[1], v[2], v[3], v[4], (int)v[5], (int)v[6]);
			v_kp.push_back(kp);
		}
    return;
}
void vector_KeyPoint_to_Mat(vector<KeyPoint>& v_kp, Mat& mat)
{
    int count = (int)v_kp.size();
    mat.create(count, 1, CV_32FC(7));
    for(int i=0; i<count; i++)
    {
        KeyPoint kp = v_kp[i];
        mat.at< Vec<float, 7> >(i, 0) = Vec<float, 7>(kp.pt.x, kp.pt.y, kp.size, kp.angle, kp.response, (float)kp.octave, (float)kp.class_id);
    }
}

JNIEXPORT jint JNICALL Java_com_example_mipatternrecognition_ReconocimientoObjeto_TrainDescriptors(JNIEnv * env, jobject, jlongArray descriptors, jlongArray keyPoints, jintArray colsArray, jintArray rowsArray)
{
	jsize a_len = env->GetArrayLength(descriptors);
	jlong *descriptorsData = env->GetLongArrayElements(descriptors,0);
	jlong *keyPointsData = env->GetLongArrayElements(keyPoints,0);
	jint *colsData = env->GetIntArrayElements(colsArray,0);
	jint *rowsData = env->GetIntArrayElements(rowsArray,0);

	for(int k=0;k<a_len;k++)
	{
		Mat & newimage=*(Mat*)descriptorsData[k];
		listaMatDes.push_back(newimage);

		Mat & newimage2=*(Mat*)keyPointsData[k];
		listaMatKey.push_back(newimage2);

		listaCols.push_back(colsData[k]);
		listaRows.push_back(rowsData[k]);

	}
	// do the required manipulation on the images;
	env->ReleaseLongArrayElements(descriptors,descriptorsData,0);
	env->ReleaseLongArrayElements(keyPoints,keyPointsData,0);
	env->ReleaseIntArrayElements(colsArray,colsData,0);
	env->ReleaseIntArrayElements(colsArray,rowsData,0);
	return a_len;
}

JNIEXPORT void JNICALL Java_com_example_mipatternrecognition_ReconocimientoObjeto_FindFeatures(
		JNIEnv* env, jobject, jlong addrGray, jlong addrRgba, jlong addrDescriptores, jlong addrKeyPoints) {
	// ----------------------
	//Crear matrices y vector
	// ----------------------
	Mat& mGr = *(Mat*) addrGray;
	Mat& mRgb = *(Mat*) addrRgba;
	Mat& descriptores = *(Mat*) addrDescriptores;
	Mat& key = *(Mat*) addrKeyPoints;
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
		vector_KeyPoint_to_Mat(keyPoints,key);
		extractor_Surf.compute(mGr, keyPoints, descriptores);
		putText(mRgb, "Patron adquirido", Point2f(100, 100), FONT_HERSHEY_PLAIN, 2,
				Scalar(0, 0, 255, 150), 2);
	}
	// ------------------------------------
	// Se pintan los keyPoints en la imagen
	// ------------------------------------
	for (unsigned int i = 0; i < keyPoints.size(); i++) {
		const KeyPoint& kp = keyPoints[i];
		circle(mRgb, Point(kp.pt.x, kp.pt.y), 10, Scalar(255, 0, 0, 255));
	}


}


void rellenarObjetos(jlong* keyPoints, jlong* descriptors, jint* cols, jint* rows, int length){

	listaCols.resize(length);
	listaRows.resize(length);
	listaDescriptores.resize(length);
	listaKeyPoints.resize(length);

	Mat* aux;

	for(int i=0; i<length; i++){
		listaCols.at(i)=cols[i];
		listaRows.at(i)=rows[i];
		listaDescriptores.at(i)=descriptors[i];
		listaKeyPoints.at(i)=keyPoints[i];

		sprintf(au, "EEEEEEEEEEE");
		Mat* aux_des=(Mat*)descriptors[i];
		sprintf(au, "EEEEEEEEEEE %i", aux_des->rows);
		__android_log_write(ANDROID_LOG_ERROR, "Tag", au);
		listaMatDes.push_back(aux_des->clone());

		__android_log_write(ANDROID_LOG_ERROR, "Tag", au);
		Mat* aux_key=(Mat*)keyPoints[i];
		listaMatKey.push_back((*aux_key));
	}

}
bool encuentraObjeto(Mat mrGr, Mat mRgb, vector<KeyPoint> keyPoints_esc, Mat descriptores_esc, int nObjeto) {
	// -------------------------------------------
	//Crear matrices y vector (objeto y escenario)
	// -------------------------------------------
	Mat descriptores_obj = listaMatDes.at(nObjeto);
	Mat keyPoints_obj_Mat = listaMatKey.at(nObjeto);
	vector<KeyPoint> keyPoints_obj;
	Mat_to_vector_KeyPoint(keyPoints_obj_Mat, keyPoints_obj);

	int cols = listaCols.at(nObjeto);
	int rows = listaRows.at(nObjeto);

	if(keyPoints_obj.size() == 0 || descriptores_obj.rows == 0)
		return false;


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

JNIEXPORT jint JNICALL Java_com_example_mipatternrecognition_ReconocimientoObjeto_FindObjects(
		JNIEnv* env, jobject, jlong addrGray, jlong addrRgba) {

	jint nObjeto=-1;

	bool encontrado=false;

	Mat& mGr = *(Mat*) addrGray;
	Mat& mRgb = *(Mat*) addrRgba;
	vector<KeyPoint> keyPoints_esc;
	Mat descriptores_esc;

	// ------------------
	//Inicializacion SURF
	// ------------------
	int minHessian = 400;
	SurfFeatureDetector detector_Surf(minHessian);
	SurfDescriptorExtractor extractor_Surf;

	// --------------------------------------------------------------------
	// Deteccion de keypoints y extraccion de caracteristicas del escenario
	// --------------------------------------------------------------------
	detector_Surf.detect(mGr, keyPoints_esc);
	if (keyPoints_esc.size() == 0){
		keyPoints_esc.clear();
		return nObjeto;
	}
	extractor_Surf.compute(mGr, keyPoints_esc, descriptores_esc);
	if (descriptores_esc.rows == 0){
		keyPoints_esc.clear();
		descriptores_esc.release();
		return nObjeto;
	}


	for(int i=0;i<listaCols.size() && !encontrado;i++){
		encontrado = encuentraObjeto(mGr, mRgb, keyPoints_esc, descriptores_esc, i);
		if (encontrado) nObjeto=i;
	}

	return nObjeto;
}

/*
vector<KeyPoint> keyPoints_1, keyPoints_2;
long objeto_long;

JNIEXPORT bool JNICALL Java_com_example_mipatternrecognition_ReconocimientoObjeto_FindFeatures2(
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

JNIEXPORT bool JNICALL Java_com_example_mipatternrecognition_ReconocimientoObjeto_FindObject2(
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
*/

}
