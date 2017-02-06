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
#include <math.h>
#include <time.h>
#include <string.h>


#include "BasicDefinitions.h"
#include "Random.h"
#include "Geometry.h"
#include "BucketHashing.h"
#include "PointMatch.h"

/** On OS X malloc definitions reside in stdlib.h */
#ifdef DEBUG_MEM
 #ifndef __APPLE__
    #include <malloc.h>
 #endif
#endif

#ifdef DEBUG_TIMINGS
#include <sys/time.h>
#endif

#include "GlobalVars.h"
