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

#include "headers.h"

// The state vector for generation of random numbers.
char rngState[256];

// Initialize the random number generator.
void initRandom(){
	srandom(time(0));
}

// Generate a random integer in the range [rangeStart,
// rangeEnd]. Inputs must satisfy: rangeStart <= rangeEnd.
int genRandomInt(int rangeStart, int rangeEnd){
  ASSERT(rangeStart <= rangeEnd);
  int r;
  r = rangeStart + (int)((rangeEnd - rangeStart + 1.0) * random() / (RAND_MAX + 1.0));
  ASSERT(r >= rangeStart && r <= rangeEnd);
  return r;
}

// Generate a random 32-bits unsigned (unsigned) in the range
// [rangeStart, rangeEnd]. Inputs must satisfy: rangeStart <=
// rangeEnd.
unsigned genRandomUns32(unsigned rangeStart, unsigned rangeEnd){
  ASSERT(rangeStart <= rangeEnd);
  unsigned r;
  if (RAND_MAX >= rangeEnd - rangeStart) {
    r = rangeStart + (unsigned)((rangeEnd - rangeStart + 1.0) * random() / (RAND_MAX + 1.0));
  } else {
    r = rangeStart + (unsigned)((rangeEnd - rangeStart + 1.0) * ((long long unsigned)random() * ((long long unsigned)RAND_MAX + 1) + (long long unsigned)random()) / ((long long unsigned)RAND_MAX * ((long long unsigned)RAND_MAX + 1) + (long long unsigned)RAND_MAX + 1.0));
  }
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
float genGaussianRandom(){
  // Use Box-Muller transform to generate a point from normal
  // distribution.
  float x1, x2;
  do{
    x1 = genUniformRandom(0.0, 1.0);
  } while (x1 == 0); // cannot take log of 0.
  x2 = genUniformRandom(0.0, 1.0);
  float z;
  z = sqrt(-2.0 * log(x1)) * cos(2.0 * M_PI * x2);
  return z;
}

// Generate a random real from Cauchy distribution N(0,1).
float genCauchyRandom(){
  float x, y;
  x = genGaussianRandom();
  y = genGaussianRandom();
  if (abs(y) < 0.0000001) {
    y = 0.0000001;
  }
  return x / y;
}
