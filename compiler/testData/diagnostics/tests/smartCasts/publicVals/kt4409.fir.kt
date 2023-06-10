public interface A {
    public konst x: Any
}

public class B(override public konst x: Any) : A {
    fun foo(): Int {
        if (x is String) {
            return x.length
        } else {
            return 0
        }
    }
}