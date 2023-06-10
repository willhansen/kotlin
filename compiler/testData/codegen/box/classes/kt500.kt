
var GUEST_USER_ID = 3
konst USER_ID =
    try {
        getUserIdFromEnvironment()
    }
    catch (e : UnsupportedOperationException) {
        ++GUEST_USER_ID
    }

konst USER_ID_2 =
    try {
        getUserIdFromEnvironment()
    }
    catch (e : UnsupportedOperationException) {
        GUEST_USER_ID
    }
    finally {
        GUEST_USER_ID++
    }

fun getUserIdFromEnvironment() : Int = throw UnsupportedOperationException()

fun box() : String {
    if(USER_ID   != 4) return "test0 failed"
    if(USER_ID_2 != 4) return "test2 failed"
    if(GUEST_USER_ID != 5) return "test3 failed"

    return "OK"
}
