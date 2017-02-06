//coded by Feifei Li, contact: lifeifei@cs.fsu.edu

#include "mtimer.h"

MTimer::MTimer()
{
	this->t1=0.0;
	this->t2=0.0;
	this->elapsed=0.0;
	this->usertime=0.0;
	this->systemtime=0.0;
	this->realtime=0.0;
}

MTimer::~MTimer()
{
}

void MTimer::go()
{
	t1 = times(&st);
        gettimeofday (&start,NULL);
}

void MTimer::stop()
{
	gettimeofday (&end,NULL);
        t2 = times(&ed);
}

void MTimer::update()
{
       usertime+=((double)(ed.tms_utime-st.tms_utime))/(HZ);
       systemtime+=((double)(ed.tms_stime-st.tms_stime))/(HZ);
       realtime+=((double)(t2-t1))/(HZ);
       timeval_subtract(&tdiff, &end, &start);
       elapsed+=tdiff.tv_sec+tdiff.tv_usec/1000000.0;
}

void MTimer::printStat()
{	
    //cout<<endl<<"scan depth: "<<this->n<<" "<<"utopk prob: "<<this->L_rhoi<<endl<<endl;
    //printf("user time=%f \n", usertime);
    //printf("system time=%f \n", systemtime);
    //printf("real time=%f \n", realtime);
    //printf("clock time=%f \n", elapsed); 
    //cout<<"max mem usage(in bytes)= "<<memusage<<endl;
    //cout<<"max mem usage(in KB)= "<<(double)memusage/1024.0<<endl;
    //cout<<"max mem usage(in MB)= "<<(double)memusage/(1024.0*1024.0)<<endl;
   cout<<elapsed;
}

int MTimer::timeval_subtract(struct timeval* result, struct timeval* x, struct timeval* y)
{
	  /* Perform the carry for the later subtraction by updating y. */
	  if (x->tv_usec < y->tv_usec) {
	    int nsec = (y->tv_usec - x->tv_usec) / 1000000 + 1;
	    y->tv_usec -= 1000000 * nsec;
	    y->tv_sec += nsec;
	  }
	  if (x->tv_usec - y->tv_usec > 1000000) {
	    int nsec = (y->tv_usec - x->tv_usec) / 1000000;
	    y->tv_usec += 1000000 * nsec;
	    y->tv_sec -= nsec;
	  }
	
	  /* Compute the time remaining to wait.
	     tv_usec  is certainly positive. */
	  result->tv_sec = x->tv_sec - y->tv_sec;
	  result->tv_usec = x->tv_usec - y->tv_usec;
	
	  /* Return 1 if result is negative. */
	  return x->tv_sec < y->tv_sec;
}
