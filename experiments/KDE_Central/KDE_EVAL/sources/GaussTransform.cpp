//-------------------------------------------------------------------
// The code was written by Vikas C. Raykar 
// and is copyrighted under the Lesser GPL: 
//
// Copyright (C) 2006 Vikas C. Raykar
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as
// published by the Free Software Foundation; version 2.1 or later.
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
// See the GNU Lesser General Public License for more details. 
// You should have received a copy of the GNU Lesser General Public
// License along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, 
// MA 02111-1307, USA.  
//
// The author may be contacted via email at: vikas(at)cs(.)umd(.)edu 
//-------------------------------------------------------------------

//-------------------------------------------------------------------
// File    : GaussTransform.cpp
// Purpose : Implementation for Gauss Transform.
// Author  : Vikas C. Raykar (vikas@cs.umd.edu)
// Date    : July 08 2005
//-------------------------------------------------------------------
#include "stdio.h"
#include "GaussTransform.h"
#include <math.h>

#define PI 3.1415926535
//-------------------------------------------------------------------
// Constructor 
//
// PURPOSE                                                    
// -------   
// Initialize the class. 
// Read the parameters.
//
// PARAMETERS                                                      
// ----------
// Dim			   --> dimension of the points.
// NSources		   --> number of sources.
// MTargets		   --> number of targets.
// pSources		   --> pointer to sources, (d*N).
// Bandwidth	   --> source bandwidth.
// pWeights        --> pointer to the weights, (N).
// pTargets        --> pointer to the targets, (d*M).
// pGaussTransform --> pointer the the evaluated Gauss Transform,(M).
//-------------------------------------------------------------------

GaussTransform::GaussTransform(int Dim,
			int NSources,
			int MTargets,
			double *pSources,
			double Bandwidth,
			double *pWeights,
			double *pTargets,
			double *pGaussTransform)
{	

	d=Dim;
	N=NSources;
	M=MTargets;
	px=pSources;
	h=Bandwidth;
	pq=pWeights;
	py=pTargets;
	pG=pGaussTransform;

}

//-------------------------------------------------------------------
// Destructor
//-------------------------------------------------------------------

GaussTransform::~GaussTransform()
{
}

//-------------------------------------------------------------------
// Actual function to evaluate the Gauss Transform.
//-------------------------------------------------------------------

void
GaussTransform::Evaluate()
{
	double h_square=h*h;

	for(int j=0; j<M; j++)
	{
		pG[j]=0.0;

		for(int i=0; i<N; i++)
		{
			double norm=0.0;
			//double coeff = 1.0/(h_square*PI);
			double coeff = 1;
			for (int k=0; k<d; k++)
			{
				double temp=px[(d*i)+k]-py[(d*j)+k];
				
				//if(temp < 1 && temp > -1 ) printf("px[%d] = (%lf, %lf), py[%d] = (%lf,%lf), temp = %lf\n",k, px[(d*i)+0],px[(d*i)+1], k, py[(d*j)+0],py[(d*j)+1], temp);
				double temp_square = temp*temp;
				norm += temp_square;
				//if(temp < 1 && temp > -1 ) printf("norm[%d] = %lf,  temp*temp= %lf\n",i, norm,temp_square);
			}
			//if(norm < 1) printf("attention norm[%d] = %lf\n",i, norm);	
			pG[j] = pG[j]+(pq[i]*coeff * exp(-norm/h_square));		
			//if(pG[j]>0) printf("pG[%d] = %lf\n",j, pG[j]);

		}
	}
}

