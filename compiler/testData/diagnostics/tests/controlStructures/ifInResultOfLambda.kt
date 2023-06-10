// FIR_IDENTICAL
// NI_EXPECTED_FILE

konst test1 = { if (true) 1 else "" }

konst test2 = { { if (true) 1 else "" } }

konst test3: (Boolean) -> Any = { if (it) 1 else "" }

konst test4: (Boolean) -> Any? = { if (it) 1 else "" }
