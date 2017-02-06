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

#ifdef USE_L1_DISTANCE
// Returns the L1 distance from point <p1> to <p2>.
float distance(int dimension, PPointT p1, PPointT p2){
  float result = 0;

  for (int i = 0; i < dimension; i++){
    result += ABS(p1->coordinates[i] - p2->coordinates[i]);
  }

  return result;
}
#else
// Returns the Euclidean distance from point <p1> to <p2>.
float distance(int dimension, PPointT p1, PPointT p2){
  float result = 0;

  for (int i = 0; i < dimension; i++){
    result += (p1->coordinates[i] - p2->coordinates[i])*(p1->coordinates[i] - p2->coordinates[i]);
  }

  return sqrt(result);
}
#endif
