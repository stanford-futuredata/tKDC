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
 * Author: Jeffrey Jestes (jestes@cs.utah.edu)
 */

#include <iostream>
#include <fstream>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <climits>
#include <vector>
#include <math.h>
#include <algorithm>
#include "mtimer.h"

using namespace std;

typedef struct zRec {
   zRec( long inZ, float inx, float iny ) {
      z = inZ;
      x = inx;
      y = iny;
   }

   unsigned long z;
   float x, y;
} zRec;

bool zComp (zRec z1, zRec z2) { return (z1.z < z2.z); }

int toScaledInt( float f ) {
	int scale = 1000;

    int intpart = (int) f;
    float floatpart = f;
    floatpart = floatpart - intpart;
    intpart *= scale;
    intpart += (int) ( floatpart * scale );

    return intpart;
}

unsigned int shift( int coord, int shift ) {
	unsigned int result = (unsigned int) ( coord + shift );
    return result;
}


unsigned long zValue( unsigned int x, unsigned int y ) {
   unsigned long z = 0;
   for (int i = 0; i < sizeof(x) * CHAR_BIT; i++)
   {
     z |= (x & 1U << i) << (i+1) | (y & 1U << i) << i;
   }
}

int main(int argc, char** argv) {

	if( argc != 6) {
    	cout << "usage: " << argv[0] << " data_set_file outfile epsilon delta #pts_in_data_set" << endl;
   		return 0;
 	}

    MTimer mt;
    mt.go();

    srand(time(0));
	ifstream fin( argv[1] );
	ofstream fout( argv[2] );
    double eps = atof( argv[3] );
    double delta = atof( argv[4] );
    int N = atoi( argv[5] );
    double rsk = ceil( ( 1.0 / (eps*eps) ) * log( 1.0 / delta ) );
    double prob = (double) rsk / (double) N;
    double k = ceil( (4.0/eps) * sqrt( log( 4.0 / eps ) )*log( 4.0 / eps ) );

    vector<zRec> zVec;
    int s1 = rand() % ( UINT_MAX - 200000 );
    int s2 = rand() % ( UINT_MAX - 200000 );

	char c[4096];
	float f1, f2;
    int i1, i2;
    unsigned int u1, u2;
    double rNumber;
	while( fin >> f1 >> f2) {
    	fin.getline( c, 4096 );
          
        rNumber = ( ( (double) rand() ) / ( (double) RAND_MAX ) ); 
        if( rNumber < prob ) {
        	i1 = toScaledInt( f1 );
            i2 = toScaledInt( f2 );
            u1 = shift( i1, s1 );
            u2 = shift( i2, s2 );

            zRec rec( zValue( u1, u2 ), f1, f2 );
            zVec.push_back( rec );
         }    
	}

    sort( zVec.begin(), zVec.end(), zComp );

    int skipLength = (int) floor( ( (double) zVec.size() - k ) / ( k - 1.0 ) );
    if( skipLength == 0 ) skipLength = 1;

    for( int j = 0; j < zVec.size(); j += skipLength ) {
    	fout << zVec[j].x << ' ' << zVec[j].y << '\n';
    }

	fin.close();
	fout.close();
    mt.stop();
    mt.update();

  	cout << "time spend " << mt.elapsed << '\n';
}
