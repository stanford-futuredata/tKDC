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

#ifndef GLOBALVARS_INCLUDED
#define GLOBALVARS_INCLUDED

#ifndef GLOBALVARS_CPP
#define DECLARE_EXTERN extern
#define EXTERN_INIT(x)
#else
#define DECLARE_EXTERN
#define EXTERN_INIT(x) x
#endif

DECLARE_EXTERN MemVarT availableTotalMemory EXTERN_INIT(= DEFAULT_MEMORY_MAX_AVAILABLE);

DECLARE_EXTERN MemVarT totalAllocatedMemory EXTERN_INIT(= 0);
DECLARE_EXTERN int nGBuckets EXTERN_INIT(= 0);
DECLARE_EXTERN int nBucketsInChains EXTERN_INIT(= 0);
DECLARE_EXTERN int nAllocatedGBuckets EXTERN_INIT(= 0);
DECLARE_EXTERN int nAllocatedBEntries EXTERN_INIT(= 0);

#endif
