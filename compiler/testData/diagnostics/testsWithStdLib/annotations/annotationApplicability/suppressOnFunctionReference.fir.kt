// See KT-15839

konst x = "1".let(@Suppress("DEPRECATION") Integer::parseInt)
