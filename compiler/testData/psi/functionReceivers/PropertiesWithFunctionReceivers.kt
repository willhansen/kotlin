konst ((Unit) -> Unit).foo
konst (foo.bar.() -> Unit).foo

konst (foo.bar.() -> Unit).foo = foo
   get() {}
   set(it) {}

konst (foo.bar.() -> Unit).foo = foo
   get() : Foo {}
   set(it) {}


konst (foo.bar.() -> Unit).foo : bar = foo
   @[a] public get() {}
   open set(a : b) {}


konst (foo.bar.() -> Unit).foo : bar = foo
   open set(a : b) {}


konst (foo.bar.() -> Unit).foo : bar = foo
   @[a] public get() {}

// Error recovery:

konst (foo.bar.() -> Unit).foo = foo
   set) {}
   dfget() {}

konst (foo.bar.() -> Unit).foo = foo
   get(foo) {}
   set() {}
   set() {}

