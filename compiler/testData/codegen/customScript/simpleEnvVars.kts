
// KOTLIN_SCRIPT_DEFINITION: org.jetbrains.kotlin.codegen.TestScriptWithSimpleEnvVars

// envVar: stringVar1=abracadabra

konst res = stringVar1.drop(4)

// expected: res=cadabra
