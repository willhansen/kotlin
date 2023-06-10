// FIR_IDENTICAL
public class X {
    private konst x : String? = null
    public konst y: CharSequence?
        get() = x?.subSequence(0, 1)
    public fun fn(): Int {
        if (y != null)
            // With non-default getter smartcast is not possible
            return <!SMARTCAST_IMPOSSIBLE!>y<!>.length
        else
            return 0
    }
}

