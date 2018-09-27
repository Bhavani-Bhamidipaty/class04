#include <stdio.h>

void incr(int& x) {
  x = x + 1;
}

int main(int argc, char* argv[]) {

  int counter = 0;

  incr(counter);

  printf("%d\n", counter);
  
  return 0;
}
