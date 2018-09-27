# Class 4

## Subroutines and Function/Procedural Abstraction

Subroutines provide the basic abstraction mechanism in programs. They
can be used to abstract over the specific values bound to variables
when evaluating an expression. One often distinguishes between
*functions* and *procedures* as two different kinds of subroutines:

* *Functions* correspond to the mathematical notion of computation,
  i.e. they are viewed as mapping from input to output values. They
  can be viewed as abstractions of side-effect free expressions.

* *Procedures* can be viewed as abstractions over statements. That is,
  tey affect the environment (mutable variables, hard disk, network,
  ...), and are called for their side-effects.

* A pure functional model is possible but rare (e.g. Haskell)

* Hybrid model is most common: functions can have (limited) side effects

### Activation Records

Recall that subroutine calls rely heavily on use of the
*stack*.

Each time a subroutine is called, space on the stack is allocated for
the objects needed by the subroutine. This space is called a *stack
frame* or *activation record*.

The *stack pointer* contains the address of either the last used
location or the next unused location on the stack.

The *frame pointer* points into the activation record of a subroutine
so that any objects allocated on the stack can be referenced with a
static offset from the frame pointer.

Question: Why not use an offset from the stack pointer to reference
subroutine objects?

Answer: There may be objects that are allocated on the stack whose
size is unknown at compile time.

These objects get allocated last so that objects whose size is known
at compile time can still be accessed quickly via a known offset from
the frame pointer. Example:

```c
 void foo (int size) {
   char arr[size]; // allocates char array of length size on the stack
   ...
 }
```

### Managing Activation Records

When a subroutine is called, a new activation record is created and
populated with data.

The management of this task involves both the *caller* and the
*callee* and is referred to as the *calling sequence*.

* The *prologue* refers to activation record management code executed
  at the beginning of a subroutine call.

* The *epilogue* refers to activation record management code executed
  at the end of a subroutine call.

#### Calling Sequence

Upon calling a subroutine, the prologue has to perform the
following tasks:

* Pass parameters
* Save return address
* Update static chain (only needed for certain forms of nested subroutines)
* Change program counter
* Move stack pointer
* Save register values, including frame pointer
* Move frame pointer
* Initialize objects

Upon returning from the subroutine, the epilogue has to perform the
following tasks:

* Finalize (destroy) objects
* Pass return value(s) back to caller
* Restore register values, including frame pointer
* Restore stack pointer
* Restore program counter

Question: Are there advantages to having the caller or callee perform various tasks?

Answer: If possible, have the callee perform tasks: task code needs to
occur only once, rather than at every call site.

Some tasks (e.g. parameter passing) must be performed by the caller.


Typical calling sequence:

Stack (before call to subroutine):

```
|                          |
├──────────────────────────┤<- stack pointer
| Caller activation record |
├──────────────────────────┤<- frame pointer
| ...                      |
```

Step 1: Save caller-save registers:

```
|                          |
├──────────────────────────┤<- stack pointer
| Caller-saved registers   |
├──────────────────────────┤
| Caller activation record |
├==========================|<- frame pointer
| ...                      |
```

Step 2: Push arguments on stack:

```
|                          |
├==========================|<- stack pointer
| Arguments                |
├──────────────────────────┤
| Caller-saved registers   |
├──────────────────────────┤
| Caller activation record |
├──────────────────────────┤<- frame pointer
| ...                      |
```

Step 3: Jump to subroutine, saving return address on stack

```
|                          |
├==========================|<- stack pointer
| Return address           |
├──────────────────────────┤
| Arguments                |
├──────────────────────────┤
| Caller-saved registers   |
├──────────────────────────┤
| Caller activation record |
├──────────────────────────┤<- frame pointer
| ...                      |
```

Step 4: Save caller's fp, set new fp

```
|                          |
├──────────────────────────┤<- stack pointer
| Saved fp (dynamic link)  |
├==========================|<- frame pointer
| Return address           |
├──────────────────────────┤
| Arguments                |
├──────────────────────────┤
| Caller-saved registers   |
├──────────────────────────┤
| Caller activation record |
├==========================|
| ...                      |
```

Step 5: Save callee-saved registers

```
|                          |
├──────────────────────────┤<- stack pointer
| Callee-saved registers   |
├──────────────────────────┤
| Saved fp (dynamic link)  |
|==========================|<- frame pointer
| Return address           |
├──────────────────────────┤
| Arguments                |
├──────────────────────────┤
| Caller-saved registers   |
├──────────────────────────┤
| Caller activation record |
|==========================|
| ...                      |
```

Step 7: Allocate and initialize locals

```
|                          |
├──────────────────────────┤<- stack pointer
| Local variables          |
├──────────────────────────┤
| Callee-saved registers   |
├──────────────────────────┤
| Saved fp (dynamic link)  |
|==========================|<- frame pointer
| Return address           |
├──────────────────────────┤
| Arguments                |
├──────────────────────────┤
| Caller-saved registers   |
├──────────────────────────┤
| Caller activation record |
|==========================|
| ...                      |
```

Step 7: Push and pop temporaries

```
| Temporaries              |
├──────────────────────────┤<- stack pointer
| Local variables          |
├──────────────────────────┤
| Callee-saved registers   |
├──────────────────────────┤
| Saved fp (dynamic link)  |
|==========================|<- frame pointer
| Return address           |
├──────────────────────────┤
| Arguments                |
├──────────────────────────┤
| Caller-saved registers   |
├──────────────────────────┤
| Caller activation record |
|==========================|
| ...                      |
```

Step 8: Pop locals

```
|                          |
├──────────────────────────┤<- stack pointer
| Callee-saved registers   |
├──────────────────────────┤
| Saved fp (dynamic link)  |
|==========================|<- frame pointer
| Return address           |
├──────────────────────────┤
| Arguments                |
├──────────────────────────┤
| Caller-saved registers   |
├──────────────────────────┤
| Caller activation record |
|==========================|
| ...                      |
```


Step 9: Restore callee-saved registers

```
|                          |
├──────────────────────────┤<- stack pointer
| Saved fp (dynamic link)  |
|==========================|<- frame pointer
| Return address           |
├──────────────────────────┤
| Arguments                |
├──────────────────────────┤
| Caller-saved registers   |
├──────────────────────────┤
| Caller activation record |
|==========================|
| ...                      |
```


Step 10: Restore caller's fp

```
|                          |
|==========================|<- stack pointer
| Return address           |
├──────────────────────────┤
| Arguments                |
├──────────────────────────┤
| Caller-saved registers   |
├──────────────────────────┤
| Caller activation record |
|==========================|<- frame pointer
| ...                      |
```

Step 11: Jump to return address

```
|                          |
├──────────────────────────┤<- stack pointer
| Arguments                |
├──────────────────────────┤
| Caller-saved registers   |
├──────────────────────────┤
| Caller activation record |
|==========================|<- frame pointer
| ...                      |
```

Step 12: Pop arguments

```
|                          |
├──────────────────────────┤<- stack pointer
| Caller-saved registers   |
├──────────────────────────┤
| Caller activation record |
|==========================|<- frame pointer
| ...                      |
```

Step 13: Restore caller-saved registers

```
|                          |
├──────────────────────────┤<- stack pointer
| Caller activation record |
|==========================|<- frame pointer
| ...                      |
```


Summary of calling sequence:

Prologue (caller)
1. Save caller-save registers      
2. Push arguments on stack         
3. Jump to subroutine, saving
   return address on stack
Prologue (callee)
4. Save old fp, set new fp
5. Save callee-save registers
6. Allocate and initialize locals
Execute callee
7: Push and pop temporaries
Epilogue (callee)
8: Pop locals
9. Restore callee-save registers
10. Restore frame pointer
11. Jump to return address
Epilogue (caller)
12. Pop arguments
13. Restore caller-save registers


##### Saving Registers

One difficult question is whether the caller or callee should be in
charge of saving registers.

Question: What would the caller have to do to ensure proper saving of
registers?

Answer: Save all registers currently being used by caller.

Question: What would the callee have to do to ensure proper saving of
registers?

Answer: Save all registers that will be used by callee.

Question: Which is better?

Answer: Could be either one -- no clear answer in general. In
practice, many processors (including MIPS and x86) compromise: half
the registers are caller-save and half are callee-save.

*Register windows* offer an alternative: each routine has access only
to a small *window* of a large number of registers; when a subroutine
is called, the window moves, overlapping a bit to allow parameter
passing.

#### Optimizations

##### Leaf routines

A *leaf routine* is one which does not call any subroutines.

Leaf routines can avoid pushing the return address on the stack: it
can just be left in a register.

If a leaf routine is sufficiently simple (no local variables), it may
not even need a stack frame at all.

##### Inlining

Another optimization is to *inline* a function: inserting the code for
the function at every call site.

Question: What are advantages and disadvantages of inlining?

* Advantages: avoid overhead, enable more compiler optimizations

* Disadvantages: increases code size, can't always do inlining
  (e.g. recursive procedures)

#### Evaluation Strategy and Parameter Passing Modes

We distinguish between two types of parameters:

* *Formal parameters*: these are the names that appear in the
declaration of the subroutine. 

* *Actual parameters* or *arguments*: these refer to the expressions
  passed to a subroutine at a particular call site.

```scala
  // formal parameters: a, b, c
  def f (a: Int, b: Int, c: Int): Unit = ...

  // actual parameters: i, 2/i, g(i,j)
  f(i, 2/i, g(i,j))
```

An important consideration for the execution of a subroutine call is:
what does a reference to a formal parameter in the subroutine mean in
terms of the actual parameters?

The answer to this question depends on the *evaluation strategy*. 
We distinguish between two basic strategies:

* *Strict evaluation*: the actual is evaluated before the call to the
function
* *Lazy evaluation*: the actual is evaluated only if and when its
value is needed during execution of the call.

Each evaluation strategy additionally supports different *parameter
passing modes*.

Parameter passing modes for strict evaluation:

* *by value*: 

  * formal is bound to copy of value of actual
  * assignment to formal, if allowed, changes value at location of
    local copy, not at location of the actual that stores the
    original value.
  
* *by reference*: 

  * formal is bound to location of actual, forming an alias
  * assignment to formal, if allowed, also affects actual
  * only works if actual evaluates to an l-value
  
* *by copy-return*: 

  * formal is bound to copy of value of actual
  * upon return from routine, actual gets copy of formal
  
Parameter passing modes for lazy evaluation:
  
* *by name*: 

  * formal is bound to expression for actual
  * expression is (re)evaluated each time formal is read when
    executing callee
  * can be viewed as textually substituting every occurrence of the
    formal parameter in the body of the subroutine by the expression
    of the actual
  * cannot assign to formal
    
* *by need*: 

  * formal is bound to expression for actual
  * expression is evaluated the first time its value is needed
  * subsequent reads from the formal will use the value computed earlier
  * cannot assign to formal

Question: What are the advantages and disadvantages of passing by need?

* Advantage: The argument is only evaluated if it is actually used while
  executing the call.

* Disadvantage: implementation of parameter passing is more complex;
  behavior can be confusing if evaluation of actual has side
  effects. Hence, it is usually only used in purely functional languages.
  
Programming languages differ in their evaluation strategies and the
specific parameter passing modes that they support. Some languages
support multiple strategies and modes.

##### C

* Evaluation strategy is strict. The order in which actuals are
  evaluated is unspecified.

* Parameter passing is always by value: assignment to formal is
  assignment to local copy.

  ```c
    void incr (int x) {
      x = x + 1;
    }
    
    int counter = 0;
    
    incr(counter); /* passes copy of value stored in counter */
    printf("%d", counter); // prints 0
  ```

* Passing by reference can be simulated by using pointers

  ```c
    void incr (int *x) {
      *x = *x + 1;
    }
    
    int counter = 0;
    
    incr(&counter); /* pass pointer to counter */
    printf("%d", counter); // prints 1
  ```

* No need to distinguish between functions and procedures: return type
  `void` indicates side-effects only.

##### C++

* Evaluation strategy is strict. The order in which actuals are
  evaluated is unspecified.

* Default mode is by-value (same semantics as C)
    
* Explicit by-reference parameters are also supported:
    
  ```c++
  void incr (int& y) {
    y++;
  }

  int counter = 0;

  // compiler knows declaration of incr,
  // builds reference
  incr(counter);
  std::cout << counter; // prints 1
  ```
  
  The `&` after the parameter type, indicates that the parameter is
  passed by reference.
  
  Advantage over C's pointers: avoids the potential of passing invalid
  pointers
  
* Semantic intent can be indicated by qualifier:
    
  ```c++
  // 'val' is passed by reference, but 'f' cannot modify it
  void f (const double& val);
  ```

##### Java

* Evaluation strategy is strict. The order in which actuals are
  evaluated is left to right.

* Parameters are passed by value.

* However, object types have *reference semantics*. 

* That is, if the type of an argument is a class or interface, it will
  evaluate to a reference pointing to a heap-allocated object.
  
* This reference is passed by value (similar to a C pointer).
  
* Consequence: 

  * Methods can modify objects through the passed reference.
  * Assignment to the formal only affects the local copy of the
    reference, not the actual (like a C pointer).
  * Confusingly, this is sometimes referred to as call-by-reference
    even though it is not (because the reference stored in the actual
    cannot be modified by assignment to the formal).
  * Sometimes, this semantics is referred to as call-by-sharing.

* For parameters with object types: `final` means that formal is
  read-only.


##### Scala

* Default is same as in Java (strict evaluation with
  call-by-value/call-by-sharing, evaluation order left to right).

* However, formals cannot be assigned (their declarations are `val`
  rather than `var`).

* By-name parameters are also supported:

  ```scala
  var debugEnabled = false;
  
  
  def debug(msg: => String): Unit =
    if (debugEnabled) println(msg)
  
  // some expensive operation that generates a debug message
  def complexAnalysis(): String = ...
  
  // call to complexAnalysis is only executed
  // if debugEnabled is set to true
  debug(complexAnalysis())

  ```
  
  The `=>` in front of the argument type indicates that parameter is
  passed by name.

* By-need parameters can be simulated using by-name parameters and
  *lazy values*.

##### OCaml

* Evaluation strategy is strict.  The order in which actuals are
  evaluated is unspecified.

* Parameters are passed by value/sharing (as in Java/Scala).

* Lazy evaluation can be simulated using higher-order functions.

##### Ada

* Goal: separate semantic intent from implementation

* Parameter modes:

  * `in`: read-only in subroutine
  * `out`: write-only in subroutine
  * `in out`: read-write in subprogram


* Mode is independent of whether binding by value, by reference, or by
  copy-return
 
  * `in`: bind by value or reference
  * `out`: bind by reference or copy-return
  * `in out`: bind by reference or by value/copy-return

* Functions can only have `in` parameters. Otherwise, they must be
  declared as procedures.

##### Haskell

* Evaluation strategy is lazy.

* Default parameter passing mode is by-need.

#### Variable Number of Parameters

Some languages allow functions with a variable number of parameters. 

Example (in C):

```c
printf("this is %d a format %d string", x, y);
```

* Within body of `printf`, need to locate as many actuals as
  placeholders in the format string

* Solution: place parameters on stack in *reverse* order

  ```
  | ...                      |
  ├──────────────────────────┤
  | return address           |
  ├──────────────────────────┤
  | actual 1 (format string) |
  ├──────────────────────────┤
  | ...                      |
  ├──────────────────────────┤
  | actual n-1               |
  ├──────────────────────────┤
  | actual n                 |
  ├──────────────────────────┤
  | ...                      |
  ```
  
#### Passing Subroutines as Parameters

C and C++ allow parameters which are pointers to subroutines:

```c
 void (*pf) (int);
 // pf is a pointer to a function that takes
 // an int argument and returns void
    
 typedef void (*PROC)(int);
 // type abbreviation clarifies syntax
    
 void do_it (int d) { ... }

 void use_it (PROC p) { ... }

 PROC ptr = &do_it;

 use_it(ptr);
 use_it(&do_it);
```

Question: Are there any implementation challenges for this kind of
subroutine call?

Not really. This feature can be implemented in the same way as a usual
subroutine call: in particular the *referencing environment* (current
bindings of variables that are in scope) can stay the same.
  
However, what if a *nested* subroutine is passed as a parameter?
Consider the following Scala code:

```scala
def a(i: Int, p: () => Unit): Unit = {
  def b(): Unit = println(i)
    
  if (i > 1) p()
  else a(2, b)
}

def c(): Unit = ()

a(1, c)
```

What does this program print? There are two possible semantics:

* *Deep Binding*:

  * A *closure* must be created and passed in place of the subroutine.

  * A closure is a reference to a subroutine together with its
    referencing environment.

  * When a subroutine is called through a closure, the referencing
    environment from when the closure was created is restored as part
    of the calling sequence.

  * With deep binding, the program prints `1` because in the reference
    environment where the closure for `b` was created when it was
    passed to the recursive call of `a`, the name `i` was bound to
    `1`.

* *Shallow Binding*:

  * When a subroutine is called, it uses the current referencing
    environment at the call site.

  * With shallow binding, the program prints `2` because at the point
    where `b` is called via `p`, the name `i` is now bound to `2`.

Static scoping demands the use of deep binding for nested
subroutines. Hence, Scala uses deep binding semantics. Shallow binding
is typically the default in languages with dynamic scoping.
    

##### First-class functions: implementation issues

Functional programming languages treat functions as first-class values
(i.e. they can be passed to and returned by other functions).  When
using deep binding, this entails that the current activation record
must be allocated on the heap when a closure is created.

* Environment of function definition must be preserved until the point
  of call: activation record cannot be reclaimed if it creates
  functions.

* Functional languages therefore require more complex run-time
  management.

* Higher-order functions: functions that take (other) functions
  as arguments and/or return functions
  
  * very powerful 
  
  * but complex to implement efficiently
  
  * imperative languages (traditionally) restrict their use.
  
