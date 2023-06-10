class MyClass() {
    @Deprecated("") public konst test: Int = 0
}


// TESTED_OBJECT_KIND: property
// TESTED_OBJECTS: MyClass, test
// FLAGS: ACC_DEPRECATED, ACC_PRIVATE, ACC_FINAL
