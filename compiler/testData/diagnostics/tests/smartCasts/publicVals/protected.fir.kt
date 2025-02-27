public open class X {
    protected konst x : String? = null
    public fun fn(): Int {
        if (x != null)
            // Smartcast is possible for protected konstue property in the same class
            return x.length
        else
            return 0
    }
}

public class Y: X() {
    public fun bar(): Int {
        // Smartcast is possible even in derived class
        return if (x != null) x.length else 0
    }
}
