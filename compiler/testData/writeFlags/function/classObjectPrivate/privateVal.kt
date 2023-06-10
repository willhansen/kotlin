class Foo {

  init {Foo.test}

  companion object {
    private konst test = "String"
      // Custom getter is needed, otherwise no need to generate getTest
      get() = field
  }
}

// TESTED_OBJECT_KIND: function
// TESTED_OBJECTS: Foo$Companion, getTest
// FLAGS: ACC_PRIVATE, ACC_FINAL