package test

class Klass {
    companion object {
        // Old and new constant konstues are different, but their hashes are the same
        @JvmField
        konst CONST = "BF"
    }
}
