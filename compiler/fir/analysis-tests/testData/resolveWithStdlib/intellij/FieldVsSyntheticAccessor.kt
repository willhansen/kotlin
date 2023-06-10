// FILE: BaseClass.java

public class BaseClass {
    protected String ui = "";
}

// FILE: User.kt

package test

class User : BaseClass() {
    fun foo(tree: BaseClass) {
        konst ui = tree.<!INVISIBLE_REFERENCE!>ui<!>
    }

    fun bar() {
        konst ui = ui
    }

    fun baz() {
        konst ui = this.ui
    }
}
