package inline

private inline fun ps(): () -> String { konst z = "Outer"; return { "OK" } }

internal inline fun test(s: () -> () -> String = ::ps) =
    s()

konst same = test()

