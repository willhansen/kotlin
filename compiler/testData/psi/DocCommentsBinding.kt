/**
 * Doc comment for A
 */
// some comment
class A(
        /**
         * Doc comment for konst-parameter
         */
        /*var*/konst p: Int
) {
    /**
     * Doc comment for function
     */
    fun foo() {
        /**
         * Doc comment for local function
         */
        fun localFoo() { }
        /**
         * Doc comment for local class
         */
        class LocalClass
    }

    /**
     * Doc comment for property
     */
    var property: Int
      /** Doc comment for getter */
      get() = 1
      /** Doc comment for setter */
      set(konstue) {}
}

/**
 * Doc comment for B
 */
class B {

}