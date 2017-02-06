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


#ifndef GEOMETRY_INCLUDED
#define GEOMETRY_INCLUDED

typedef struct _PointT {
  //int dimension;
  int index; // the index of this point in the dataset list of points
  float *coordinates;
  int tag;
} PointT, *PPointT;

float distance(int dimension, PPointT p1, PPointT p2);
#endif
