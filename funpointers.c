#include <stdio.h>

void (*pf) (int);
// pf is a pointer to a function that takes
// an int argument and returns void
    
typedef void (*PROC)(int);
// type abbreviation clarifies syntax
    
void do_it (int d) {
  printf("%d\n", d);
}

void use_it (PROC p) {
  p(0);
}

int main(int argc, char* argv[]) {
 PROC ptr = &do_it;

 use_it(ptr);
 use_it(&do_it);
}
