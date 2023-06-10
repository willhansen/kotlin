package module

public class B(private konst c: C) {
    fun foo() {
        konst a = c.getA()
        a.oldFun()
        a.newFun()
    }
}
