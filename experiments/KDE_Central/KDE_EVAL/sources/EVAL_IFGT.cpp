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

//Calculate the running time in each step
inline double diffclock(clock_t clock1,clock_t clock2)
{
    double diffticks=clock1-clock2;
    double diffms=(diffticks)/CLOCKS_PER_SEC;
    return diffms;
}

void usage(char *programName){
	printf("Usage: %s data_set_file test_file #pts_in_data_set #pts_in_test_file dimension stdv eps outfile\n",programName);

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

//get the min and max in each dimension to do normalization afterwards
void MinMax(double *minmax, double *ds, int size, int dim){
    int i, j;
    for(i = 0; i<size; i++){
        if(i == 0){
            minmax[0] = ds[dim*i];
            minmax[1] = ds[dim*i];

            minmax[2] = ds[dim*i+1];
            minmax[3] = ds[dim*i+1];
        }
        if(ds[dim*i] < minmax[0]){
            minmax[0] = ds[dim*i];
        }
        if(ds[dim*i] > minmax[1]){
            minmax[1] = ds[dim*i];
        }
        if(ds[dim*i+1] < minmax[2]){
            minmax[2] = ds[dim*i+1];
        }
        if(ds[dim*i+1] > minmax[3]){
            minmax[3] = ds[dim*i+1];
        }
    }
	//printf("minx = %lf, maxx = %lf, diffx = %lf\n",minmax[0],minmax[1],minmax[1]-minmax[0]);
}

double getFactor(double *ds, double *minmax, int size, int dim){
    int i;
    double diff = minmax[1]-minmax[0];
    if(diff < minmax[3] - minmax[2]){
        diff = minmax[3] - minmax[2];
    }
    //printf("minx = %lf, maxx = %lf, diffx = %lf\n",minmax[0],minmax[1],minmax[1]-minmax[0]);
    //printf("miny = %lf, maxy = %lf, diffy = %lf\n",minmax[2],minmax[3],minmax[3] - minmax[2]);
    //printf("diff = %lf\n",diff);
	return diff;
}

void scale(double *ds, double *minmax, int size, int dim, double fact){
    int i;
    for(i = 0; i<size; i++){
        ds[dim*i] = (ds[dim*i] - minmax[0])/fact;
        ds[dim*i+1] = (ds[dim*i+1] - minmax[2])/fact;
    }
}

int main(int nargs, char **args){

	clock_t begin;
    clock_t end;

	if(nargs < 6){
		usage(args[0]);
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

	pSources = new double[NSources*dim];
	pTargets = new double[MTargets*dim];

	begin = clock();
	readDataSetFromFile(args[1],pSources,dim,NSources);
    readDataSetFromFile(args[2],pTargets,dim,MTargets);


	//normalize the data
	double *minmax  = (double*) malloc(sizeof(double) * 4);
	MinMax(minmax, pSources,NSources,dim);
	double fact = getFactor(pSources, minmax, NSources, dim);
	scale(pSources,minmax,NSources,dim,fact);
	scale(pTargets,minmax,MTargets,dim,fact);
	free(minmax);

	int maxNumCluster;
    double epsilon = atof(args[7]);

	//this area is for their code for automatically choose parameter
	int R = 1;
	Bandwidth = Bandwidth/fact;
    //maxNumCluster=round(20*R/Bandwidth);
   	maxNumCluster=round(0.2*sqrt(dim)*100/Bandwidth);
	//get K, r, p_max
    ImprovedFastGaussTransformChooseParameters* chPara = new ImprovedFastGaussTransformChooseParameters(dim, Bandwidth, epsilon, maxNumCluster);

    NumClusters = chPara->K;
    MaxTruncNumber = chPara->p_max;
    CutoffRadius = chPara->r;

	int *pClusterIndex = new int[NSources];
	KCenterClustering*  kcenclust = new KCenterClustering(dim, NSources, pSources, pClusterIndex, NumClusters);
	kcenclust->Cluster();	
	double* pClusterCenters = new double[NumClusters*dim];
	int* pNumPoints = new int[NumClusters];
	double* pClusterRadii = new double[NumClusters];
	kcenclust->ComputeClusterCenters(NumClusters, pClusterCenters, pNumPoints,pClusterRadii);


	//this area is for their code for automatically choose parameter
   	//get p_max again
	double MaxClusterRadius = kcenclust->MaxClusterRadius;
	MaxClusterRadius = 0.01;
    ImprovedFastGaussTransformChooseTruncationNumber* chTrunc = new ImprovedFastGaussTransformChooseTruncationNumber(dim, Bandwidth, epsilon, MaxClusterRadius);
    MaxTruncNumber = chTrunc->p_max;

	double *pWeights =  new double[NSources];
	for(int i = 0; i<NSources; i++){
		pWeights[i] = (double)((double)1.0/double(NSources));
	}
	pGaussTransform = new double[MTargets];
	for(int i = 0; i<MTargets; i++){
		pGaussTransform[i] = 0;
	}
	ImprovedFastGaussTransform* transform = new ImprovedFastGaussTransform(dim, NSources, MTargets, pSources, Bandwidth, pWeights, pTargets, MaxTruncNumber, NumClusters, pClusterIndex, pClusterCenters, pClusterRadii, CutoffRadius, epsilon, pGaussTransform);

	end = clock();
    double diff_build = (double)(diffclock(end,begin));

	begin = clock();
	transform->Evaluate(); 
	end = clock();
	double diff_eval = (double)(diffclock(end,begin));
    //out put the sample    

   	FILE* fout = fopen(args[8], "w");

	for(int i = 0; i<MTargets; i++){
		fprintf(fout, "%lf \n",pGaussTransform[i]);
	}

	//printf("%lf %lf %lf\n", diff_build, diff_eval,diff_build+diff_eval);
	free(transform);
	free(pGaussTransform);

	free(pSources);
	free(pTargets);
	free(pWeights);
	free(pClusterIndex);
	free(kcenclust);
	free(pClusterCenters);
	free(pNumPoints);
	free(pClusterRadii);
	return 0;
}
