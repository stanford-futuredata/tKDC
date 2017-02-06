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
#include <sys/times.h>
#include "headers.h"
#include "mtimer.h"

// The data set containing all the points.
PPointT *dataSetPoints = NULL;
// Number of points in the data set.
int nPoints = 0;
// The dimension of the points.
int pointsDimension = 0;

PPointMatchStructT pmStruct;

//Prints the usage
void usage(char *programName){
	printf("Usage: %s #pts_in_data_set dimension data_set_file epsilon outfile stdv\n", programName);
}

//read a point from file
inline PPointT readPoint(FILE *fileHandle){
  PPointT p;
  float sqrLength = 0;
  FAILIF(NULL == (p = (PPointT)MALLOC(sizeof(PointT))));
  FAILIF(NULL == (p->coordinates = (float*)MALLOC(pointsDimension * sizeof(float))));
  for(int d = 0; d < pointsDimension; d++){
  	int rv = fscanf(fileHandle, "%f", &(p->coordinates[d]));
  }
  p->index = -1;
  return p;
}

//do random sample of a file base on the sample size
//use Reservoir sampling technique
float** randSample(char *filename, long long unsigned nRSample){
	float** sample;
	FILE *f = fopen(filename, "r");
  	FAILIF(f == NULL);
	FAILIF(NULL == (sample = (float**)MALLOC(sizeof(float*)*nRSample)));
	for(int i = 0; i < nRSample; i++){
		FAILIF(NULL == (sample[i] = (float*)MALLOC(sizeof(float)*pointsDimension)));
    	int dummy = fscanf(f, "%f %f", &sample[i][0], &sample[i][1]);
  	}
	float data1, data2;
	
for(int i = nRSample; i < nPoints; i++){
    int dummy = fscanf(f, "%f %f", &data1, &data2);
	int r = genRandomInt(0,i);
    if(r<nRSample){
    	sample[r][0] = data1;
		sample[r][1] = data2;
	}
  }
  return sample;

}

//read the data from file and at the same time to store the min and max in each dimension
void readDataSetFromFile(char *filename, float* minarr, float* maxarr){
  FILE *f = fopen(filename, "rt");
  FAILIF(f == NULL);
  float* cor;
  FAILIF(NULL == (dataSetPoints = (PPointT*)MALLOC(nPoints * sizeof(PPointT))));
  for(int i = 0; i < nPoints; i++){
    dataSetPoints[i] = readPoint(f);
    dataSetPoints[i]->index = i;
	cor = dataSetPoints[i]->coordinates;
    if(i == 0){
            for(int j = 0; j<pointsDimension; j++){
                minarr[j] = cor[j];
                maxarr[j] = cor[j];
            }
    }else{
            for(int j = 0; j<pointsDimension; j++){
                if(cor[j] < minarr[j]){
                    minarr[j] = cor[j];
                }
                if(cor[j] > maxarr[j]){
                    maxarr[j] = cor[j];
                }
            }
    }
  }
}


//free space
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
}

//output the sample to a file
void getFloatArrSample( PPointT *dataSet,int nPoints, int dim, char* file){
    FILE *fp = fopen(file,"w");
    for(int i = 0; i<nPoints; i++){
        for(int j = 0; j<dim; j++){
            fprintf(fp,"%.1f ",dataSet[i]->coordinates[j]);
        }
        fprintf(fp,"\n");
    }
    fclose(fp);
}

//read one point from a array
inline PPointT readPointFloat(float *singledata){
    PPointT p;
    FAILIF(NULL == (p = (PPointT)MALLOC(sizeof(PointT))));
    p->coordinates = singledata;
    p->index = -1;
    return p;
}

//read data from a array and get the min and max in each dimension
void readDataSetFromArray(float** data, int nRSample, float* minarr, float* maxarr){
	float* cor;
    FAILIF(NULL == (dataSetPoints = (PPointT*)MALLOC(nRSample * sizeof(PPointT))));
    for(int i = 0; i < nRSample; i++){
    	//printf("i = %d\n",i); 
	   dataSetPoints[i] = readPointFloat(data[i]);
        dataSetPoints[i]->index = i;
        dataSetPoints[i]->tag = 0;
		cor = data[i];
        if(i == 0){
            for(int j = 0; j<pointsDimension; j++){
                minarr[j] = cor[j];
                maxarr[j] = cor[j];
            }
        }else{
            for(int j = 0; j<pointsDimension; j++){
                if(cor[j] < minarr[j]){
                    minarr[j] = cor[j];
                }
                if(cor[j] > maxarr[j]){
                    maxarr[j] = cor[j];
                }
            }
        }
     }
}


int main(int nargs, char **args){
  	if(nargs < 7){
    	usage(args[0]);
    	exit(1);
 	 }
  	FILE *fp;
	FILE *tfp; 	

  	// Parse part of the command-line parameters.
  	nPoints = atoi(args[1]);
	pointsDimension = atoi(args[2]);
    float eps = atof(args[4]);
	float stdv = atof(args[6]);

	//default failure probability
	float delta = 0.001;
	long long unsigned nRSample = ceil( ( 1.0 / (eps*eps) ) * log( 1.0 / delta ) );

	//to count the running time
	MTimer verybegin;
    verybegin.go();
	
	initRandom();
	int originalnPoints;
	float* minarr;
    float* maxarr;
    FAILIF(NULL == (minarr = (float*)MALLOC(sizeof(float)*pointsDimension)));
    FAILIF(NULL == (maxarr = (float*)MALLOC(sizeof(float)*pointsDimension)));
	float** data;
	
	if(nRSample > nPoints){
		printf("Don't need to do random sampling\n");
		readDataSetFromFile(args[3],minarr,maxarr);	
		originalnPoints = nPoints;
	}else{
		data = randSample(args[3],nRSample);
	    readDataSetFromArray(data,nRSample,minarr,maxarr);
		nPoints = nRSample;
		originalnPoints = nRSample;
	}

	PPointT *originalDataSet = dataSetPoints;
	int k = ceil(4/eps*sqrt(log(4/eps))*log(4/eps));
	if(k > nRSample){
 	   printf("If we want to achieve eps, we can't do reduce, just use the original data\n");
	}
	
	//get the number of round we need to run the algorithm
	int nofr = log(nPoints/k)/log(2);
	float stderror = 0;
	float acc_err = 0;
	dataSetPoints = originalDataSet;

	for(int i = 0; i < nPoints; i++){
		dataSetPoints[i]->tag = 0;
	}
	stderror = 0;
	acc_err = 0;
	for(int v = 0; v<nofr;v++){
  		
		pmStruct = newPMatchStructure(nPoints, pointsDimension,stdv * eps * 100);

		//run log(1/eps)+2 times and each time match point in the grid
  		for(int i = 0; i<log(1/eps)+2; i++){
            constructHash(i,pmStruct,dataSetPoints,minarr,maxarr);
            getMatchPairs(pmStruct,dataSetPoints);
		}

		//match arbitrarily after log(1/t)+2 times
  		matchArb(pmStruct,dataSetPoints);  
		if(v != 0){
			free(dataSetPoints);
		}

		//get the one point from each pair as the data for the next round
		dataSetPoints =  getSamplePoint(pmStruct,originalDataSet,pointsDimension);
  		nPoints = pmStruct->nMPoints;
  	
		//in the last round just output the sample to file	
		if(v == nofr-1){
			verybegin.stop();
        	verybegin.update();
			printf("%f\n",verybegin.elapsed);
			getFloatArrSample(dataSetPoints,nPoints,pointsDimension,args[5]);
   		}
  		freePMatchStructure(pmStruct);
	}
 	freeDataPoints(originalDataSet,originalnPoints);
	free(minarr);
    free(maxarr);
  	return 0;
}
