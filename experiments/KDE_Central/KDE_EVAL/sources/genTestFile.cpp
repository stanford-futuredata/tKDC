/*
 * Copyright (c) 2012-2013 University of Utah.
 * All Rights Reserved.
 *
 * University of Utah grants permission to use, copy, modify, and distribute this software and
 * its documentation for NON-COMMERCIAL purposes and without fee, provided that
 * this copyright notice appears in all copies.
 *
 * University of Utah provides this software "as is," without representations or warranties of
 * any kind, either expressed or implied, including but not limited to the
 * implied warranties of merchantability, fitness for a particular purpose, and
 * noninfringement.  University of Utah shall not be liable for any damages arising from any
 * use of this software.
 *
 * Author: Yan Zheng (yanzheng@cs.utah.edu)
 */

#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <time.h>
#include <string.h>

#define ERROR_OUTPUT stderr

#define FAILIF(b) {if (b) {fprintf(ERROR_OUTPUT, "FAILIF triggered on line %d, file %s.\n", __LINE__, __FILE__); exit(1);}}

#define MALLOC(amount) ((amount > 0) ? malloc(amount) : NULL)

#define ASSERT(b) {if (!(b)) {fprintf(ERROR_OUTPUT, "ASSERT failed on line %d, file %s.\n", __LINE__, __FILE__); exit(1);}}

#ifndef PI
#define PI 3.14159265358979323846 
#endif

//structure for storing the data
typedef struct _PointT {
  int index; // the index of this point in the dataset list of points
  float *coordinates;
  int tag;
} PointT, *PPointT;

//function that is used for generate random int data
int genRandomInt(int rangeStart, int rangeEnd){
  ASSERT(rangeStart <= rangeEnd);
  int r;
  r = rangeStart + (int)((rangeEnd - rangeStart + 1.0) * rand() / (RAND_MAX + 1.0));
  ASSERT(r >= rangeStart && r <= rangeEnd);
  return r;
}

// Generate a random real distributed uniformly in [rangeStart,
// rangeEnd]. Input must satisfy: rangeStart <= rangeEnd. The
// granularity of generated random reals is given by RAND_MAX.
float genUniformRandom(float rangeStart, float rangeEnd){
  ASSERT(rangeStart <= rangeEnd);
  float r;
  r = rangeStart + ((rangeEnd - rangeStart) * (float)random() / (float)RAND_MAX);
  ASSERT(r >= rangeStart && r <= rangeEnd);
  return r;
}

// Generate a random real from normal distribution N(0,1).
float genGaussianRandom(float stdv){
  // Use Box-Muller transform to generate a point from normal
  // distribution.
  float x1, x2;
  do{
    x1 = genUniformRandom(0.0, 1.0);
  } while (x1 == 0); // cannot take log of 0.
  x2 = genUniformRandom(0.0, 1.0);
  float z;
  z = sqrt(-2.0 * log(x1)) * cos(2.0 * PI * x2);
  z = z * stdv;
  return z;
}

//gaussion function
float gauss(float* x, float* p, int pDimension, float stdv){
    float result = 0;
    float d = 0;
	double coeff = 1.0/(stdv * stdv * 2.0*PI);
    for(int i = 0; i<pDimension; i++){
        d += (x[i] - p[i]) * (x[i] - p[i]);
    }
	result = coeff * exp(-d / (2* stdv * stdv));
    return result;
}

//generate test data in original dataset
float genTest1(FILE* fp, PPointT *dataSet, int pDimension, int nPoints, int nEval, float stdv){
    float err = 0;
	
    float* x;
    float kdep = 0;
    float kdes = 0;
    printf("nTest = %d\n", nEval);
    srandom(time(0));
	for(int i = 0; i<nEval; i++){
        int randIndex = genRandomInt(0,nPoints-1);
        x = dataSet[randIndex]->coordinates;
		for(int j = 0; j<pDimension; j++){
			fprintf(fp,"%.4f ",x[j]);
		}
    	fprintf(fp, "\n");

	}
    return err;
}

//generate test data from domain
float genTest2(FILE* fp, PPointT *dataSet, int pDimension, int nPoints, int nEval, float** minmaxdata, float stdv){
    float err = 0;

    float* x;
    float kdep = 0;
    float kdes = 0;
    
	FAILIF(NULL == (x = (float*)MALLOC(pDimension * sizeof(float))));

	for(int j = 0 ; j<pDimension; j++){
    	printf("dimension %d, [min, max] = [%f, %f]\n",j, minmaxdata[0][j], minmaxdata[1][j]);
    }
	printf("nTest = %d\n",nEval);
	
    srandom(time(0));
    for(int i = 0; i<nEval; i++){
		for(int j = 0 ; j<pDimension; j++){
			x[j] = genUniformRandom(minmaxdata[0][j], minmaxdata[1][j]);
			fprintf(fp,"%.4f ",x[j]);
		}
		fprintf(fp, "\n");

    }
	free(x);
    return err;
}

//generate test data in a combine way
float genTestCombine(FILE* fp, PPointT *dataSet, int pDimension, int nPoints, int nEval,float** minmaxdata, float stdv){
    float err = 0;
    float* x;
    float kdep = 0;
    float kdes = 0;
	float randtag = 0;
	int randIndex;
	float gaussnoise;
    FAILIF(NULL == (x = (float*)MALLOC(pDimension * sizeof(float))));

    for(int j = 0 ; j<pDimension; j++){
        printf("dimension %d, [min, max] = [%f, %f]\n",j, minmaxdata[0][j], minmaxdata[1][j]);
    }
    printf("nTest = %d\n",nEval);

    srandom(time(0));
    for(int i = 0; i<nEval; i++){
		randtag = genUniformRandom(0,1);
		if(randtag <0.8){
			//type 1
			randIndex = genRandomInt(0,nPoints-1);
			for(int j = 0 ; j<pDimension; j++){
                x[j] = dataSet[randIndex]->coordinates[j];
				fprintf(fp,"%.4f ",x[j]);
            }
			fprintf(fp,"\n");
		}else{
			//type 2
        	for(int j = 0 ; j<pDimension; j++){
            	x[j] = genUniformRandom(minmaxdata[0][j], minmaxdata[1][j]);
				fprintf(fp,"%.4f ",x[j]);
        	}
			fprintf(fp,"\n");
		}
    }
    free(x);
    return err;
}

//generate test data from original data plus gaussian noise
float genTest3(FILE* fp, PPointT *dataSet, int pDimension, int nPoints, int nEval, float stdv){
    float err = 0;

    float* x;
    float kdep = 0;
    float kdes = 0;
	float gaussnoise;
    printf("nTest = %d",nEval);
    srandom(time(0));
	FAILIF(NULL == (x = (float*)MALLOC(pDimension * sizeof(float))));
    for(int i = 0; i<nEval; i++){
        int randIndex = genRandomInt(0,nPoints-1);
		for(int j = 0 ; j<pDimension; j++){
            gaussnoise = genGaussianRandom(stdv);	
			x[j] = dataSet[randIndex]->coordinates[j]+gaussnoise;
			fprintf(fp, "%.4f ",x[j]);
		}
		fprintf(fp, "\n");
		
    }
	free(x);
    return err;
}

//read a single point from file
inline PPointT readPoint(FILE *fileHandle, int pointsDimension){
  PPointT p;
  float sqrLength = 0;
  FAILIF(NULL == (p = (PPointT)MALLOC(sizeof(PointT))));
  FAILIF(NULL == (p->coordinates = (float*)MALLOC(pointsDimension * sizeof(float))));
  char str[80];
  int dummy = fscanf(fileHandle, "%f %f", &(p->coordinates[0]),&(p->coordinates[1]));
  return p;
}

// Reads in the data set points from <filename> in the array
// <dataSetPoints>. Each point get a unique number in the field
// <index> to be easily indentifiable. 
//The index and tag is not useful for the error estimate purpose
void readDataSetFromFile(char *filename,PPointT *dataSetPoints, int d,int nPoints){
  FILE *f = fopen(filename, "rt");
  FAILIF(f == NULL);
  for(int i = 0; i < nPoints; i++){
    dataSetPoints[i] = readPoint(f,d);
    dataSetPoints[i]->index = i;
    dataSetPoints[i]->tag = 0;
  }
}

//usage method of the program
void usage(char *programName){
  printf("Usage: %s <data_set_file> <test_file>  #dimension #pts_in_data_set #pts_in_test_set #stdv of the kernel #way of generate test points  \n", programName);
}

//free dataset
void freeDataPoints(PPointT* dataSetPoints, int nPoints){
    PPointT p;
    for(int i = 0; i<nPoints; i++){
	   p = dataSetPoints[i];
        if(p){
            free(p->coordinates);
            free(p);
        }
    }
    free(dataSetPoints);	
	return;
}

float** minmax(PPointT *p, int nPoints, int pDimension){
	float** minmaxdata;    
	FAILIF(NULL == (minmaxdata = (float**)MALLOC(pDimension * sizeof(float*))));

	//min data stored in minmaxdata[0]   max data stored in minmaxdata[1]
  	for(int i = 0; i < pDimension; i++){
    	FAILIF(NULL == (minmaxdata[i] = (float*)MALLOC(pDimension * sizeof(float))));
  	}

	for(int i = 0; i<nPoints; i++){
        float* cor = p[i]->coordinates;
        if(i == 0){
            for(int j = 0; j<pDimension; j++){
                minmaxdata[0][j] = cor[j];
				minmaxdata[1][j] = cor[j];
            }
        }else{
            for(int j = 0; j<pDimension; j++){
                if(cor[j] < minmaxdata[0][j]){
                    minmaxdata[0][j] = cor[j];
					//printf("min i = %d, j = %d, minmaxdata[0][j] = %f\n",i,j,minmaxdata[0][j]);
                }
				if(cor[j] > minmaxdata[1][j]){
                    minmaxdata[1][j] = cor[j];
					//printf("max i = %d, j = %d minmaxdata[0][j]=%f\n",i,j,minmaxdata[1][j]);
                }
            }
        }
    }
    return minmaxdata;
}

int main(int nargs, char **args){
  if(nargs < 7){
    usage(args[0]);
    exit(1);
  }
  srandom(time(0));
  PPointT *dataSetPoints = NULL;
  int nTestPoints = 1000;
  int tag = 0; //mix three ways to generate the test points

  // Parse part of the command-line parameters.
  int pointsDimension = atoi(args[3]);
  int nPoints = atoi(args[4]);
  nTestPoints = atoi(args[5]);
  float** minmaxdata;
  float stdv = atof(args[6]);
  if(nargs >= 8) tag = atoi(args[7]);
  
  //tag == 1 generate test data points from original dataset
  //tag == 2 generate test data points from domain
  //tag == 3 generate test data points from origial dataset plus gaussian noise

  FAILIF(NULL == (dataSetPoints = (PPointT*)MALLOC(nPoints * sizeof(PPointT))));
  readDataSetFromFile(args[1],dataSetPoints,pointsDimension,nPoints);
  
  float err;
  FILE* fp = fopen(args[2],"w"); 
 if(tag == 0){
	printf("generate %d testdata from randomly choosen from combine of the first two ways\n",nTestPoints);
    minmaxdata = minmax(dataSetPoints, nPoints, pointsDimension);
	err = genTestCombine(fp,dataSetPoints,pointsDimension,nPoints,nTestPoints, minmaxdata, stdv);
  }else if(tag == 1){
	printf("generate %d testdata from randomly choosen from original dataset\n",nTestPoints);  
	err = genTest1(fp, dataSetPoints,pointsDimension,nPoints,nTestPoints,stdv);
  }else if(tag == 2){
	printf("generate %d testdata from randomly choosen from minmax\n",nTestPoints);
	minmaxdata = minmax(dataSetPoints, nPoints, pointsDimension);
	err = genTest2(fp,dataSetPoints,pointsDimension,nPoints,nTestPoints, minmaxdata, stdv);
  }else if(tag == 3){
    printf("generate %d testdata from randomly choosen from original dataset with gaussian noise\n",nTestPoints);
    err = genTest3(fp,dataSetPoints,pointsDimension,nPoints,nTestPoints, stdv);
  }	
  freeDataPoints(dataSetPoints,nPoints);
  if(tag == 0 || tag == 2){
  	for(int i = 0; i < pointsDimension; i++){    
 		free(minmaxdata[i]);
  	}
  	free(minmaxdata);
  }
  return 0;
}
