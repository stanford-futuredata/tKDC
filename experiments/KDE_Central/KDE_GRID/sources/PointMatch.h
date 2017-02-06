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

#ifndef POINTMATCH_INCLUDED
#define POINTMATCH_INCLUDED

typedef struct _PairPointsT{
	int* index;
	int chPointIndex;
	_PairPointsT *nextPair;	
}PairPointsT, *PPairPointsT;

typedef struct _PointMatchStructT{

 	PUHashStructureT pmHashStruct;
	
	//finest grid length
	float t;
	int nPoints;
	int pDimension;
	PPairPointsT matchedPoint;
	int* sampleIndex;
	int nMPoints;
	float* minCoor;
	float* maxCoor;

}PointMatchStructT, *PPointMatchStructT;

//get min and max and construct hash
PPointMatchStructT newPMatchStructure(int nPoints, int pDimension, float t);

//construct the hash for each round
void constructHash(int round, PPointMatchStructT pmatch, PPointT *dataSet, float* minArr, float* maxArr);

//Match pair of points in the same bucket
void getMatchPairs(PPointMatchStructT pmatch, PPointT *dataSet);

//get the one point from each pair as the data for the next round
PPointT *  getSamplePoint(PPointMatchStructT pmatch, PPointT *dataSet, int pDimension);

//free the matching structure
void freePMatchStructure(PPointMatchStructT pmatch);

//match point arbitrarily after certain round
void matchArb(PPointMatchStructT pmatch,PPointT *dataSet);
#endif
