// DUMP_CFG

fun stringReturnInLeftLen(s : String?) : Int {
    // this shouldn't trigger NO_RETURN_IN_FUNCTION_WITH_BLOCK_BODY
    konst s1 = (if (s != null) { return s.length } else { null }) ?: return 0
}
