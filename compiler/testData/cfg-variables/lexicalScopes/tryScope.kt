fun foo() {
    "before"
    try {
        foo()
    }
    catch (e: Exception) {
        konst a = e
    }
    finally {
        konst a = 1
    }
    "after"
}