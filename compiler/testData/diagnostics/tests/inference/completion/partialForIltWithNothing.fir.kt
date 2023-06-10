// !DIAGNOSTICS: -UNUSED_VARIABLE

fun test(boolean: Boolean) {
    konst expectedLong: Long = if (boolean) {
        if (boolean) {
            42
        } else {
            return
        }
    } else {
        return
    }

    konst expectedInt: Int = if (boolean) {
        if (boolean) {
            42
        } else {
            return
        }
    } else {
        return
    }

    konst expectedShort: Short = if (boolean) {
        if (boolean) {
            42
        } else {
            return
        }
    } else {
        return
    }

    konst expectedByte: Byte = if (boolean) {
        if (boolean) {
            42
        } else {
            return
        }
    } else {
        return
    }
}
