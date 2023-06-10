konst foo = bar.foo.bar

konst foo
konst @[a] foo
konst foo.bar

konst foo : T
konst @[a] foo = bar
konst foo.bar
   get() {}
   set(sad) = foo


konst foo get
konst foo set

var foo
  get
  private set

konst foo.bar
   get() {}
   set

konst foo.bar
   get
   set(sad) = foo

konst foo = 5; get
konst foo =1; get set

var foo = 5
  get
  private set

konst foo.bar = 5
   get() {}
   set

konst foo.bar = 5
   get
   set(sad) = foo

fun foo() {
  konst foo = 5
  get() = 5
}

konst IList<T>.lastIndex : Int
  get() = this.size - 1

konst Int?.opt : Int
konst Int? .opt : Int