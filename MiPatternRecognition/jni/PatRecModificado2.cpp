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

vector<Mat> vectorDescriptores;
vector<vector<KeyPoint> > vectorKeyPoints;
vector<int> listaCols;
vector<int> listaRows;


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

void freeObjects(){
	for (int i=0;i<listaCols.size();i++){
		vectorDescriptores.at(i).release();
		vectorKeyPoints.at(i).clear();
	}
	vectorDescriptores.clear();
	vectorKeyPoints.clear();
	listaCols.clear();
	listaRows.clear();
}

JNIEXPORT jint JNICALL Java_com_example_mipatternrecognition_ReconocimientoObjeto_RellenarObjetos(JNIEnv * env, jobject, jlongArray descriptors, jlongArray keyPoints, jintArray colsArray, jintArray rowsArray)
{
	freeObjects();
	jsize length = env->GetArrayLength(descriptors);
	if (length > 0){
		// ----------------------------
		// Inicialización de los arrays
		// ----------------------------
		jlong *descriptorsData = env->GetLongArrayElements(descriptors,0);
		jlong *keyPointsData = env->GetLongArrayElements(keyPoints,0);
		jint *colsData = env->GetIntArrayElements(colsArray,0);
		jint *rowsData = env->GetIntArrayElements(rowsArray,0);

		for(int k=0;k<length;k++)
		{
			// -------------------------------------------
			// Relleno del vector de vectores de KeyPoints
			// -------------------------------------------
			Mat & tempMatKeyPoint=*(Mat*)keyPointsData[k];
			vector<KeyPoint> tempVectorKeyPoint;
			Mat_to_vector_KeyPoint(tempMatKeyPoint,tempVectorKeyPoint);
			vectorKeyPoints.push_back(tempVectorKeyPoint);

			// ----------------------------------------------
			// Relleno del vector de matrices de descriptores
			// ----------------------------------------------
			Mat & tempDesctiptor=*(Mat*)descriptorsData[k];
			vectorDescriptores.push_back(tempDesctiptor);

			// ------------------------------------------------------------------------
			// Relleno de la anchura y largura del objeto (tamaño de la imagen inicial)
			// ------------------------------------------------------------------------
			listaCols.push_back(colsData[k]);
			listaRows.push_back(rowsData[k]);

		}

		// --------------------------------------------
		// Liberación de los arrays antes inicializados
		// --------------------------------------------
		env->ReleaseLongArrayElements(descriptors,descriptorsData,0);
		env->ReleaseLongArrayElements(keyPoints,keyPointsData,0);
		env->ReleaseIntArrayElements(colsArray,colsData,0);
		env->ReleaseIntArrayElements(colsArray,rowsData,0);
	}
	return length;
}

JNIEXPORT void JNICALL Java_com_example_mipatternrecognition_ReconocimientoObjeto_FindFeatures(
		JNIEnv* env, jobject, jlong addrGray, jlong addrRgba, jlong addrDescriptores, jlong addrKeyPoints) {
	// -----------------------
	// Crear matrices y vector
	// -----------------------
	Mat& mGr = *(Mat*) addrGray;
	Mat& mRgb = *(Mat*) addrRgba;
	Mat& descriptores = *(Mat*) addrDescriptores;
	Mat& key = *(Mat*) addrKeyPoints;
	vector<KeyPoint> keyPoints;

	// -------------------
	// Inicializacion SURF
	// -------------------
	int minHessian = 400;
	SurfFeatureDetector detector_Surf(minHessian);
	SurfDescriptorExtractor extractor_Surf;

	// -----------------------------------------------------------------
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

/**
 * @param mrGr imagen gris del frame actual (escenario)
 * @param mRgb imagen color del frame actual (escenario)
 * @param keyPoints_esc keyPoints obtenidos al procesar el escenario
 * @param descriptores_esc Descriptores obtenidos al procesar el escenario
 * @param nObjeto índice del objeto que se está buscando en el escenario
 * @return Devuelve si el objeto nObjeto se han encontrado en el escenario
 */
bool encuentraObjeto(Mat mrGr, Mat mRgb, vector<KeyPoint> keyPoints_esc, Mat descriptores_esc, int nObjeto) {
	// ---------------------------------------------------------------------------
	// Inicializar vector de KeyPoints y matriz de Descriptores del objeto nObjeto
	// ---------------------------------------------------------------------------
	Mat descriptores_obj = vectorDescriptores.at(nObjeto);
	vector<KeyPoint> keyPoints_obj = vectorKeyPoints.at(nObjeto);
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

		// ----------------------------------------------------------------------
		// Draw only "good" matches (i.e. whose distance is less than 2*min_dist,
		// or a small arbitary value ( 0.02 ) in the event that min_dist is very
		// small)
		// PS.- radiusMatch can also be used here.
		// ----------------------------------------------------------------------
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

		// -----------------------------------------------------------------------------------
		// Si se han encontrado más de cuatro coincidencias se ha encontrado el objeto nObjeto
		// -----------------------------------------------------------------------------------
		if (good_matches.size() >= 10) {

			vector < Point2f > obj;
			vector < Point2f > scene;

			for (int i = 0; i < good_matches.size(); i++) {
				//-- Get the keypoints from the good matches
				obj.push_back(keyPoints_obj[good_matches[i].queryIdx].pt);
				scene.push_back(keyPoints_esc[good_matches[i].trainIdx].pt);
			}

			// -------------------------------------------------------------------------------------------------------
			// Encontrar la homografía, para poder dibujar un rectángulo que englobe al objeto nObjeto en el escenario
			// -------------------------------------------------------------------------------------------------------
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

vector<KeyPoint> keyPoints_esc;
Mat descriptores_esc;

JNIEXPORT jint JNICALL Java_com_example_mipatternrecognition_ReconocimientoObjeto_LiberaEscenario(
		JNIEnv* env, jobject){
	keyPoints_esc.clear();
	descriptores_esc.release();
}

JNIEXPORT bool JNICALL Java_com_example_mipatternrecognition_ReconocimientoObjeto_InicializaEscenario(
		JNIEnv* env, jobject, jlong addrGray, jlong addrRgba){
	Mat& mGr = *(Mat*) addrGray;
	Mat& mRgb = *(Mat*) addrRgba;
	// -------------------
	// Inicializacion SURF
	// -------------------
	int minHessian = 400;
	SurfFeatureDetector detector_Surf(minHessian);
	SurfDescriptorExtractor extractor_Surf;

	// --------------------------------------------------------------------
	// Deteccion de keypoints y extraccion de caracteristicas del escenario
	// --------------------------------------------------------------------

	detector_Surf.detect(mGr, keyPoints_esc);
	if (keyPoints_esc.size() == 0){
		keyPoints_esc.clear();
		return false;
	}
	extractor_Surf.compute(mGr, keyPoints_esc, descriptores_esc);
	if (descriptores_esc.rows == 0){
		keyPoints_esc.clear();
		descriptores_esc.release();
		return false;
	}
	return true;
}

JNIEXPORT jint JNICALL Java_com_example_mipatternrecognition_ReconocimientoObjeto_FindObjects(
		JNIEnv* env, jobject, jlong addrGray, jlong addrRgba, jint i) {

	// ----------------------------
	// Inicializar variables a usar
	// ----------------------------
	jint nObjeto=-1;
	bool encontrado=false;
	Mat& mGr = *(Mat*) addrGray;
	Mat& mRgb = *(Mat*) addrRgba;

	if (keyPoints_esc.size() == 0 || descriptores_esc.rows == 0){
		keyPoints_esc.clear();
		descriptores_esc.release();
		return nObjeto;
	}

	// -----------------------------------------------------------------------------------------------
	// Bucle que terminar si se encuentra el objeto en el escenario o si no hay más objetos que buscar
	// -----------------------------------------------------------------------------------------------
	//for(int i=0;i<listaCols.size() && !encontrado;i++){
		encontrado = encuentraObjeto(mGr, mRgb, keyPoints_esc, descriptores_esc, i);
		if (encontrado) nObjeto=i;
	//}

	return nObjeto;
}

JNIEXPORT jint JNICALL Java_com_example_mipatternrecognition_ReconocimientoObjeto_LiberaObjetos(){
	freeObjects();
}

}
