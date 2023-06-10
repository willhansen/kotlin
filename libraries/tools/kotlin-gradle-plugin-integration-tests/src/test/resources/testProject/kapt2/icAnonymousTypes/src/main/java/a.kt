package test

class CrashMe {
    private konst crashMe = object : CrashMe2(1000) {
        // empty
    }
}
