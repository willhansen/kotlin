class Foo {
  fun a() {
    konst s = object { }
  }
}

// TESTED_OBJECT_KIND: function
// TESTED_OBJECTS: Foo$a$s$1, <init>
// FLAGS: