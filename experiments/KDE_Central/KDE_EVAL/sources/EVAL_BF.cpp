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
 * Part of the code from IFGT project
 */

#include <time.h>
#include <math.h>
#include <stdio.h>
#include <stdlib.h>
#include "ImprovedFastGaussTransform.h"
#include "ImprovedFastGaussTransformChooseParameters.h"
#include "ImprovedFastGaussTransformChooseTruncationNumber.h"
#include "KCenterClustering.h"
#include "GaussTransform.h"

#define PI 3.1415926535

void usage(char *programName){
  printf("Usage: %s data_set_file test_file #pts_in_data_set #pts_in_test_file dimension stdv outfile  \n", programName);
}
void readDataSetFromFile(char *filename, double* dataSetPoints, int d,int nPoints){
  FILE *f = fopen(filename, "rt");
  for(int i = 0; i < nPoints; i++){
	for(int j = 0; j < d; j++){
   		 int dummy = fscanf(f, "%lf", &(dataSetPoints[i*d+j]));
  	}
  }
  fclose(f);
}

void scaleTest(double *ds, double *minmax, int size, int dim, double fact){
    int i;
    for(i = 0; i<size; i++){
        ds[dim*i] = (ds[dim*i] - minmax[0])/fact;
        ds[dim*i+1] = (ds[dim*i+1] - minmax[2])/fact;
    }
}

int main(int nargs, char **args){

	clock_t begin;
    clock_t end;

	if(nargs < 8){
		usage(args[0]);
		exit(1);
	}
	int NSources = atoi(args[3]);
	int MTargets = atoi(args[4]);

	double *pSources;
	double *pTargets;


	//get from ImprovedFastGaussTransformChooseParameters
    double CutoffRadius;
	int MaxTruncNumber;
    int NumClusters;

	double *pGaussTransform; //output
	
	int dim = atoi(args[5]);
	double Bandwidth = atof(args[6])*sqrt(2);
	//MaxTruncNumber = atoi(args[7]);

	pSources = new double[NSources*dim];
	pTargets = new double[MTargets*dim];

	begin = clock();
	readDataSetFromFile(args[1],pSources,dim,NSources);
    readDataSetFromFile(args[2],pTargets,dim,MTargets);

   	FILE* fout = fopen(args[7], "w");


	//evalue bf
	double *pWeights =  new double[NSources];
    for(int i = 0; i<NSources; i++){
        pWeights[i] = (double)((double)1.0/double(NSources));
    }

	double* pbfGaussTransform = new double[MTargets];
    for(int i = 0; i<MTargets; i++){
        pbfGaussTransform[i] = 0;
    }
	GaussTransform* bfgauss = new GaussTransform(dim, NSources, MTargets, pSources, Bandwidth, pWeights, pTargets,pbfGaussTransform);
	bfgauss->Evaluate();

    for(int i = 0; i<MTargets; i++){
		fprintf(fout, "%lf \n",pbfGaussTransform[i]);
    }	
	free(bfgauss);
	free(pbfGaussTransform);
	fclose(fout);

	free(pSources);
	free(pTargets);
	free(pWeights);
	return 0;
}
