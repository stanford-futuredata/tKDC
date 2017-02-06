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

#include "headers.h"

//construct new pointmatch structure
PPointMatchStructT newPMatchStructure(int nPoints, int pDimension, float t){
	PPointMatchStructT pmatch;
	FAILIF(NULL == (pmatch = (PPointMatchStructT)MALLOC(sizeof(PointMatchStructT))));

	pmatch->t = t;
	pmatch->nPoints = nPoints;
	pmatch->pDimension = pDimension;
	pmatch->matchedPoint = NULL;
	pmatch->pmHashStruct = NULL;
    pmatch->nMPoints = 0;
	return pmatch;
}

//construct hash for each round, we only care about the data with tag!= -1
void constructHash(int round, PPointMatchStructT pmatch, PPointT *dataSet, float* minArr, float* maxArr){
	
	int nPoints = pmatch->nPoints; 
    int pDimension = pmatch->pDimension;
	pmatch->minCoor = minArr;
	pmatch->maxCoor = maxArr;

	if(pmatch->pmHashStruct == NULL){
		pmatch->pmHashStruct = newUHashStructure(nPoints, pmatch->t);

	}

	PUHashStructureT hashstruct = pmatch->pmHashStruct;
	int num=0;
	for(int i = 0; i<nPoints; i++){
		if(dataSet[i]->tag == 0){
			addBucketEntry(i,round,hashstruct,dataSet[i], pmatch->minCoor,pmatch->maxCoor, pDimension,nPoints);
		}

	}
}

//after certain rounds, just arbitrarily match points
void matchArb(PPointMatchStructT pmatch,PPointT *dataSet){
    int nPoints = pmatch->nPoints;
    int pDimension = pmatch->pDimension;
    PPairPointsT mp = NULL;
    int num = 0;
	int lasttag = 0;
    for(int i = 0; i<nPoints; i++){
        if(dataSet[i]->tag == 0){
            num++;
            if(num > 0 && num % 2 == 0){

                    PPairPointsT mp;
                    FAILIF(NULL == (mp = (PPairPointsT)MALLOC(sizeof(PairPointsT))));
                    FAILIF(NULL == (mp->index = (int*)MALLOC(sizeof(int)*2)));
                    mp->index[0] = dataSet[i]->index;
                    mp->index[1] = dataSet[lasttag]->index;
                    if(pmatch->matchedPoint == NULL){
                        pmatch->matchedPoint = mp;
                        mp->nextPair = NULL;
                    }else{
                          //link right after the first pair
                          PPairPointsT temp = pmatch->matchedPoint->nextPair;
                          pmatch->matchedPoint->nextPair = mp;
                          mp->nextPair = temp;
                    }

                    //then mark the datasets
                    dataSet[i]->tag = -1;
                    dataSet[lasttag]->tag = -1;

                    pmatch->nMPoints++;
            }else{
                lasttag = i;
            }
        }
    }
}

//get pairs of points in the same bucket 
void getMatchPairs(PPointMatchStructT pmatch,PPointT *dataSet){
    int nPoints = pmatch->nPoints;
    int pDimension = pmatch->pDimension;
    PUHashStructureT uhash = pmatch->pmHashStruct;
    PBucketEntryT bucketEntry;
    PBucketEntryT compEntry;
    PBucketEntryT matchedEntry;
    for(int i = 0; i < nPoints; i++){

        PGBucketT bucket = uhash->llHashTable[i];
        while(bucket != NULL){
            //for each buck's entry, find the pair points.
            //so go through each entry, find the any entry in the same bucket
            //market the pointIndex to -1 when it is got matched
            //and the go to the next entry with pointindex!= -1
            bucketEntry = &(bucket->firstEntry);
            while(bucketEntry != NULL){
                
				while(bucketEntry->pointIndex == -1){

                    bucketEntry = bucketEntry->nextEntry;

                    //if next point is null, then the current bucket has been cleaned and we need to process the next bucket
                    if(bucketEntry == NULL){
                        break;
                    }
                }
                if(bucketEntry == NULL){
                     break;
                }
                compEntry = bucketEntry->nextEntry;
                float hasPair = -1;
                int findIndex;
                
				if(compEntry != NULL){

                    while(compEntry->pointIndex == -1){
                        compEntry = compEntry->nextEntry;
                        if(compEntry == NULL){
                            //Can't find a compare entry that has index != -1
                            break;
                        }
                    }

                    if(compEntry != NULL){
                        matchedEntry = compEntry;
                        findIndex = compEntry->pointIndex;
                        hasPair = 1;
                    }

                }

                //if there is only one point left in the bucket, wait for the next round
                if(hasPair < 0){
                    // put the bucketEntry into head of the unusedEntry
                    if(bucketEntry != &(bucket->firstEntry)){

                        PBucketEntryT tempEntry = bucketEntry;
                        bucketEntry = bucketEntry->nextEntry;
                        tempEntry->nextEntry = uhash->unusedPBucketEntrys;
                        uhash->unusedPBucketEntrys = tempEntry;
                    }
                    break;
                }else{
                    PPairPointsT mp;
                    FAILIF(NULL == (mp = (PPairPointsT)MALLOC(sizeof(PairPointsT))));
                    FAILIF(NULL == (mp->index = (int*)MALLOC(sizeof(int)*2)));
                    mp->index[0] = dataSet[bucketEntry->pointIndex]->index;
                    mp->index[1] = dataSet[findIndex]->index;
                    if(pmatch->matchedPoint == NULL){
                        pmatch->matchedPoint = mp;
                        mp->nextPair = NULL;
                    }else{
                          //link right after the first pair
                          PPairPointsT temp = pmatch->matchedPoint->nextPair;
                          pmatch->matchedPoint->nextPair = mp;
                          mp->nextPair = temp;
                    }
                    //then mark the datasets
                    dataSet[bucketEntry->pointIndex]->tag = -1;
                    dataSet[findIndex]->tag = -1;
                    bucketEntry->pointIndex = -1;
                    matchedEntry->pointIndex = -1;
                    pmatch->nMPoints++;
                    //the current entry has been checked, so we could realease it.
                    if(bucketEntry != &(bucket->firstEntry)){
                        PBucketEntryT tempEntry = bucketEntry;
                        bucketEntry = bucketEntry->nextEntry;
                        tempEntry->nextEntry = uhash->unusedPBucketEntrys;
                        uhash->unusedPBucketEntrys = tempEntry;
                    }
                }
            }
            //the current buckets has been checked, so we could realease it.
            //put it clear

            uhash->llHashTable[i] = bucket->nextGBucketInChain;

            PGBucketT tempBucket = bucket;
            tempBucket->nextGBucketInChain = uhash->unusedPGBuckets;
            uhash->unusedPGBuckets = tempBucket;

            bucket = uhash->llHashTable[i];
            //check for the next bucket in the chain
        }

    }
}

//write out the sample point from each pair to a PPointT structure
PPointT * getSamplePoint(PPointMatchStructT pmatch, PPointT *dataSet, int pDimension){
	
	PPointT *newdataset;
	PPairPointsT matchedPoint = pmatch->matchedPoint;
	
	FILE *fp;
	FAILIF(NULL == (pmatch->sampleIndex = (int*)MALLOC(sizeof(int)*pmatch->nMPoints)));
	FAILIF(NULL == (newdataset = (PPointT*)MALLOC(pmatch->nMPoints * sizeof(PPointT))));
	for(int i = 0; i<pmatch->nMPoints; i++){
		int randindex = genRandomInt(0,1);
		matchedPoint->chPointIndex = matchedPoint->index[randindex];
		pmatch->sampleIndex[i] = matchedPoint->chPointIndex;
		newdataset[i] = dataSet[pmatch->sampleIndex[i]];
		newdataset[i]->tag = 0;
	
		matchedPoint = matchedPoint->nextPair;
	}
	return newdataset;
}

void freePMatchStructure(PPointMatchStructT pmatch){
	if (pmatch == NULL){
   		 return;
 	}
	
	PPairPointsT mpoints = pmatch->matchedPoint;
	int count = 0;
	while (mpoints != NULL){
          PPairPointsT tempMP = mpoints;
          mpoints = mpoints->nextPair;
          free(tempMP->index);
	  free(tempMP);
	  count++;
        }
	pmatch->nMPoints = 0;
	freeUHashStructure(pmatch->pmHashStruct);	
	pmatch->pmHashStruct = NULL;
	free(pmatch->sampleIndex);
	pmatch->sampleIndex = NULL;
	free(pmatch);
	pmatch = NULL;
}
	
