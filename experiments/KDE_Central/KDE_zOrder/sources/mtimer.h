//coded by Feifei Li, contact: lifeifei@cs.fsu.edu

#include <stdio.h>
#include <stdlib.h>
#include <iostream>
#include <sys/time.h>
#include <sys/times.h>
#include <sys/param.h>
#include <sys/types.h>

using namespace::std;

class MTimer{
		public:
	MTimer();
 	~MTimer();
	void printStat();
    	int timeval_subtract(struct timeval* result, struct timeval* x, struct timeval* y);
	void go();
	void stop();
	void update();

		public:
	struct tms st,ed;
	double t1, t2;
	struct timeval tdiff, start, end;
	double elapsed;
	double usertime;
	double systemtime;
	double realtime;
};

