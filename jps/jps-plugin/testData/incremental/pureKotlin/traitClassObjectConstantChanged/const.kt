package test

interface Trait {
    companion object {
        // Old and new constant konstues are different, but their hashes are the same
        const konst CONST = "BF"
    }
}
