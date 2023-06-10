package test

class LocalClass {
    private fun foo() = run {
        class Local

        Local()
    }

    private konst bar = object {}

    private konst sam = Runnable {}

    private konst sub = object : Runnable {
        override fun run() {
        }
    }
}
