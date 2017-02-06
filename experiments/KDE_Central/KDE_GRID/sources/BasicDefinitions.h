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

#ifndef BASICDEFINITIONS_INCLUDED
#define BASICDEFINITIONS_INCLUDED

// Log of the maximum number of points in the database
#define N_BITS_PER_POINT_INDEX 20
// Maximum number of points = (2^N_BITS_PER_POINT_INDEX-2)
#define MAX_N_POINTS ((1U << N_BITS_PER_POINT_INDEX) - 1)
// Maxumum number of points to be reported (i.e., the "k" if interested in "k-NN")
#define MAX_REPORTED_POINTS 10


// TODO: this is the max available memory. Set for a particular
// machine at the moment. It is used in the function util.h/getAvailableMemory
#define DEFAULT_MEMORY_MAX_AVAILABLE 100000000000U

#define DEBUG

// Attention: if this macro is defined, the program might be less portable.
#define DEBUG_MEM


#define ERROR_OUTPUT stderr

#define FAILIF(b) {if (b) {fprintf(ERROR_OUTPUT, "FAILIF triggered on line %d, file %s. Memory allocated: %lld\n", __LINE__, __FILE__, totalAllocatedMemory); exit(1);}}

#define ASSERT(b) {if (!(b)) {fprintf(ERROR_OUTPUT, "ASSERT failed on line %d, file %s.\n", __LINE__, __FILE__); exit(1);}}

#ifdef DEBUG
#define CR_ASSERT(b) {if (!(b)) {fprintf(ERROR_OUTPUT, "ASSERT failed on line %d, file %s.\n", __LINE__, __FILE__); exit(1);}}
#else
#define CR_ASSERT(b)
#endif

#define MemVarT long long int

#define MALLOC(amount) ((amount > 0) ? totalAllocatedMemory += amount, malloc(amount) : NULL)

#define FREE(pointer) {if (pointer != NULL) {free(pointer);} pointer = NULL; }

#endif
