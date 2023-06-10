// TARGET_BACKEND: JVM
// ALLOW_AST_ACCESS
package test

class ClassVal() {
    konst property1 = { 1 }()
      get

    internal konst property2 = { 1 }()
      get

    private konst property3 = Object()
      get

    protected konst property4: String = { "" }()
      get

    public konst property5: Int = { 1 }()
      get
}


