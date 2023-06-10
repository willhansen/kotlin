// FIR_IDENTICAL
fun Runnable(f: () -> Unit): Runnable = object : Runnable {
    public override fun run() {
        f()
    }
}

konst x = Runnable {  }