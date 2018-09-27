// some code that generates a debug message
def complexAnalysis(): String = {
  println("complexAnalysis")
  "banana"
}

// call by value (default)
{
  def debug(enabled: Boolean, s: String): Unit = {
    println("debug")
    if (enabled) {
      println(s) 
      println(s)
    }
  }
  
  // prints "complexAnalysis" and "debug"
  debug(false, complexAnalysis())

  // prints "complexAnalysis", "debug", and two times "banana"
  debug(true, complexAnalysis())
}

// call by name (indicated by preceding parameter type by '=>')
{
  def debug(enabled: Boolean, s: => String): Unit = {
    println("debug")
    if (enabled) println(s)
  }

  // prints only "debug"
  debug(false, complexAnalysis())

  // prints "debug", and two times "complexAnalysis" and "banana"
  debug(true, complexAnalysis())
}

// passing nested functions as closures

def foo(x: Int, p: Int => Unit): Unit = {
  def bar(y: Int): Unit = {
    println(x)
    println(y)
  }
  if (x > 1) p(x) else foo(2, bar)
}

def f(x: Int): Unit = {
  println("hello")
}

// prints 1 and 2
foo(1, f)
