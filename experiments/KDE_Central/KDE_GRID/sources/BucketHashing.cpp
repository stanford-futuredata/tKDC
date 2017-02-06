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
 * Part of the code from E2LSH project
 */

#include <stdio.h>
#include <stdlib.h>
#include "headers.h"


// Creates a new bucket with specified fields. The new bucket contains
// only a single entry -- bucketEntry. bucketEntry->nextEntry is
// expected to be NULL.
inline PGBucketT newGBucket(PUHashStructureT uhash, unsigned control1, int pointIndex, PGBucketT nextGBucket){
  PGBucketT bucket;
  if (uhash != NULL && uhash->unusedPGBuckets != NULL){
    bucket = uhash->unusedPGBuckets;
    uhash->unusedPGBuckets = uhash->unusedPGBuckets->nextGBucketInChain;
  } else {
    FAILIF(NULL == (bucket = (PGBucketT)MALLOC(sizeof(GBucketT))));
    nAllocatedGBuckets++;
  }
  ASSERT(bucket != NULL);
  bucket->controlValue1 = control1;
  bucket->firstEntry.pointIndex = pointIndex;
  bucket->firstEntry.nextEntry = NULL;
  bucket->nextGBucketInChain = nextGBucket;

  nGBuckets++;
  return bucket;
}

// Adds the entry <bucketEntry> to the bucket <bucket>.
inline void addPointToGBucket(PUHashStructureT uhash, PGBucketT bucket/*, PPointT point*/ , int pointIndex){
  ASSERT(bucket != NULL);
  ASSERT(uhash != NULL);

  // create a new bucket entry for the point
  PBucketEntryT bucketEntry;
  if (uhash->unusedPBucketEntrys != NULL){
    bucketEntry = uhash->unusedPBucketEntrys;
    uhash->unusedPBucketEntrys = uhash->unusedPBucketEntrys->nextEntry;
  }else{
    FAILIF(NULL == (bucketEntry = (PBucketEntryT)MALLOC(sizeof(BucketEntryT))));
    nAllocatedBEntries++;
  }
  ASSERT(bucketEntry != NULL);
  bucketEntry->pointIndex = pointIndex;

 //insert into the position right after the firstEntry
  bucketEntry->nextEntry = bucket->firstEntry.nextEntry;
  bucket->firstEntry.nextEntry = bucketEntry;
}


// Creates a new UH structure (initializes the hash table and the hash
// functions used). hashTableSize is number of hashed points	 
PUHashStructureT newUHashStructure(int hashTableSize,float t){
  PUHashStructureT uhash;
  FAILIF(NULL == (uhash = (PUHashStructureT)MALLOC(sizeof(UHashStructureT))));
  uhash->hashTableSize = hashTableSize;
  uhash->nHashedBuckets = 0;
  uhash->nHashedPoints = 0;
  uhash->unusedPGBuckets = NULL;
  uhash->unusedPBucketEntrys = NULL;
  uhash->t = t;
  //uhash->prime = UH_PRIME_DEFAULT;

  FAILIF(NULL == (uhash->llHashTable = (PGBucketT*)MALLOC(hashTableSize * sizeof(PGBucketT))));
  for(int i = 0; i < hashTableSize; i++){
      uhash->llHashTable[i] = NULL;
  }

  return uhash;
}

// Removes all the buckets/points from the hash table.
void clearUHashStructure(PUHashStructureT uhash){
  ASSERT(uhash != NULL);
  for(int i = 0; i < uhash->hashTableSize; i++){
      PGBucketT bucket = uhash->llHashTable[i];
      while(bucket != NULL){
        PBucketEntryT bucketEntry = bucket->firstEntry.nextEntry;
        while(bucketEntry != NULL){
          PBucketEntryT tempEntry = bucketEntry;
          bucketEntry = bucketEntry->nextEntry;
          tempEntry->nextEntry = uhash->unusedPBucketEntrys;
          uhash->unusedPBucketEntrys = tempEntry;
        }
      
        PGBucketT tempBucket = bucket;
        bucket = bucket->nextGBucketInChain;
        tempBucket->nextGBucketInChain = uhash->unusedPGBuckets;
        uhash->unusedPGBuckets = tempBucket;
      }

      uhash->llHashTable[i] = NULL;
    }
  uhash->nHashedPoints = 0;
  uhash->nHashedBuckets = 0;
}

// Frees the <uhash> structure. If <freeHashFunctions>==FALSE, then
// the hash functions are not freed (because they might be reused, and
// therefore shared by several PUHashStructureT structures).
void freeUHashStructure(PUHashStructureT uhash){
  if (uhash == NULL){
    return;
  }

  for(int i = 0; i < uhash->hashTableSize; i++){
      PGBucketT bucket = uhash->llHashTable[i];
      while (bucket != NULL){
        PGBucketT tempBucket = bucket;
        bucket = bucket->nextGBucketInChain;
        PBucketEntryT bucketEntry = tempBucket->firstEntry.nextEntry;
        while (bucketEntry != NULL){
          PBucketEntryT tempEntry = bucketEntry;
          bucketEntry = bucketEntry->nextEntry;
          free(tempEntry);
        }
        free(tempBucket);
      }
    }
  free(uhash);
}

inline unsigned getHashValue(int index, float t, float *coordinates, float *min, float *max, int ptsDimension, int nPoints){
	float s = t * pow(2.0,index);
	int h[2] ;
	char str[80] = {0};
	
	for(int i = 0; i < ptsDimension; i++){
		h[i] = int(floor((coordinates[i] - min[i])/s));
	}
	int temp = h[0];
	while(temp>0){
		temp = temp /10;
		h[1]*=10;
	}
	unsigned hvalue = h[1]+h[0];
	hvalue = hvalue%UH_PRIME_DEFAULT;
	return hvalue;
}

// Adds the bucket entry (a point <point>) to the bucket defined by
// bucketVector in the uh structure with number uhsNumber. If no such
// bucket exists, then it is first created.

//pIndex is the piont index from the original dataset
//hIndex is the bucket number the point has been hashed into
void addBucketEntry(int pIndex, int i, PUHashStructureT uhash, PPointT point,float *min, float *max, int ptsDimension, int nPoints){
  CR_ASSERT(uhash != NULL);
  // CR_ASSERT(bucketVector != NULL);
  unsigned control1;

  control1  = getHashValue(i, uhash->t, point->coordinates,min,max,ptsDimension, nPoints);
  //hIndex = control1 mod #points
  unsigned hIndex = control1;
  hIndex = hIndex % nPoints;
  PGBucketT p;
  int found;
  int j;
  int temp;

  p = uhash->llHashTable[hIndex];
  while(p != NULL &&
          (p->controlValue1 != control1)) {
  	   p = p->nextGBucketInChain;
  }
  if (p == NULL) {
      // new bucket to add to the hash table
      uhash->nHashedBuckets++;
      uhash->llHashTable[hIndex] = newGBucket(uhash,
					          		control1,
                                    pIndex,
                                    uhash->llHashTable[hIndex]);
  } else {
      // add this bucket entry to the existing bucket
     addPointToGBucket(uhash, p, pIndex);
  }
  uhash->nHashedPoints++;
}

void printHashBucket(PUHashStructureT uhash, int hashtablesize){

	PBucketEntryT bucketEntry;
	CR_ASSERT(uhash != NULL);
	PGBucketT bucket;
	int j = 0;
	int k = 0;
	for(int i = 0; i<hashtablesize; i++){
		printf("hashgroup %d\n",i);
		PGBucketT bucket =uhash->llHashTable[i];
		j = 0;
		while(bucket != NULL){
			bucketEntry = &bucket->firstEntry;
			k = 0;
			printf("bucketgroup = %d\n",j);
			while(bucketEntry != NULL){
				printf("index %d = %d, ", k, bucketEntry->pointIndex);
				k++;
				bucketEntry = bucketEntry->nextEntry;
			}
			printf("\n\n");
			bucket = bucket->nextGBucketInChain;
			j++;	
		}
	}

}
