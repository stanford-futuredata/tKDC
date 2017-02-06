#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <time.h>
#include <string.h>

#define ERROR_OUTPUT stderr
#define ASSERT(b) {if (!(b)) {fprintf(ERROR_OUTPUT, "ASSERT failed on line %d, file %s.\n", __LINE__, __FILE__); exit(1);}}

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

// Generate a random 32-bits unsigned (int) in the range
// [rangeStart, rangeEnd]. Inputs must satisfy: rangeStart <=
// rangeEnd.
int genRandomUns32(int rangeStart, int rangeEnd){
  ASSERT(rangeStart <= rangeEnd);
  int r;
  if (RAND_MAX >= rangeEnd - rangeStart) {
    r = rangeStart + (int)((rangeEnd - rangeStart + 1.0) * random() / (RAND_MAX + 1.0));
  } else {
    r = rangeStart + (int)((rangeEnd - rangeStart + 1.0) * ((long int)random() * ((long int)RAND_MAX + 1) + (long int)random()) / ((long int)RAND_MAX * ((long int)RAND_MAX + 1) + (long int)RAND_MAX + 1.0));
  }
  ASSERT(r >= rangeStart && r <= rangeEnd);
  return r;
}

void usage(char *programName){
  printf("Usage: %s kde_from_allpts kde_from_sample #pts_in_test_file \n", programName);
}

int main(int nargs, char **args){


	if(nargs < 4){
        usage(args[0]);
		exit(1);
    }

	srand(time(0));
	FILE* fpin1 = fopen(args[1],"r");
	FILE* fpin2 = fopen(args[2],"r");

	int nPoints = atoi(args[3]);
	float data1 = 0;
	float data2 = 0;
	char str [80];
	float max = 0;
	float diff = 0;
	for(int i = 0; i<nPoints; i++){
		int dummy = fscanf(fpin1, "%f", &(data1));
		int dummy2 = fscanf(fpin2, "%f", &(data2));
		diff = fabs(data1-data2);
		//printf("data1 = %f, data2 = %f, diff = %f, max = %f\n",data1, data2, diff, max);
		if(diff > max)	max = diff;
	}
	printf("%f\n",max);
	fclose(fpin1);
	fclose(fpin2);

}
