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

#ifndef BUCKETHASHING_INCLUDED
#define BUCKETHASHING_INCLUDED

#define UH_PRIME_DEFAULT 4294967291U
typedef struct _BucketEntryT {
  //PPointT point;
  int pointIndex;
  _BucketEntryT *nextEntry;
} BucketEntryT, *PBucketEntryT;

typedef struct _GBucketT {
  // These controlValues are used instead of the full k-vector (value
  // of the hash function g) describing the bucket. With a high
  // probability all buckets will have different pairs of
  // controlValues.
  unsigned controlValue1;

  // The bucket entries (stored in a linked list).
  BucketEntryT firstEntry;
  _GBucketT *nextGBucketInChain;
} GBucketT, *PGBucketT;

typedef struct _UHashStructureT {

	 // The size of hashTable.
	int hashTableSize;


	// Number of elements(buckets) stored in the hash table in total (that
  	// is the number of non-empty buckets).
  	int nHashedBuckets;
	
	int nHashedPoints;

	PGBucketT *llHashTable;
	// Unused (but allocated) instances of the corresponding
	// structs. May be reused if needed (instead of allocated new
  	// memory).
  	PGBucketT unusedPGBuckets;
  	PBucketEntryT unusedPBucketEntrys;

	// The main hash function (that defines the index
	unsigned *mainHashA;

	// Control hash functions: used to compute/check the <controlValue>s
  	// of <GBucket>s.
  	// The type of the hash function is: h_{a}(k) = (a\cdot k)mod p
  	unsigned *controlHash1;
	float t;
} UHashStructureT, *PUHashStructureT;

PUHashStructureT newUHashStructure(int hashTableSize,float t);

void clearUHashStructure(PUHashStructureT uhash);
void freeUHashStructure(PUHashStructureT uhash);
void addBucketEntry(int pIndex, int i, PUHashStructureT uhash, PPointT point,float *min, float *max, int ptsDimension, int nPoints);
void printHashBucket(PUHashStructureT uhash, int hashtablesize);
#endif

