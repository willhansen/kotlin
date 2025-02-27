# Diagnostic tests format specification

Each diagnostic test consists of a single .kt file containing the code of one or several Kotlin or Java source files.
Each diagnostic, be it a warning or an error, is marked in the following way:

    <!DIAGNOSTIC_FACTORY_NAME!>element<!>

where `DIAGNOSTIC_FACTORY_NAME` is the name of the diagnostic which is usually one of the constants in one of `Errors*` classes.

To test not only the presence of the diagnostic but also the arguments which will be rendered to the user, provide string representations of all of them in the parentheses delimited with `;` after the diagnostic name:

    return <!TYPE_MISMATCH(String; Nothing)!>"OK"<!>

Note: if you're unsure what text should be added for the parameters, just leave the parentheses empty and the failed test will present the actual konstues in the assertion message.

## Directives

Several directives can be added to the beginning of a test file with the following syntax:

    // !DIRECTIVE

### 1. DIAGNOSTICS

This directive allows to exclude some irrelevant diagnostics (e.g. unused parameter) from a certain test or to include others.

The syntax is

    '([ + - ] DIAGNOSTIC_FACTORY_NAME | ERROR | WARNING | INFO ) +'

  where

* `+` means 'include';
* `-` means 'exclude'.

  Directives are applied in the order of appearance, i.e. `+FOO -BAR` means include `FOO` but not `BAR`.

#### Usage:

    // !DIAGNOSTICS: -WARNING +CAST_NEVER_SUCCEEDS

    // !DIAGNOSTICS: -UNUSED_EXPRESSION -UNUSED_PARAMETER -UNUSED_VARIABLE


### 2. CHECK_TYPE

The directive adds the following declarations to the file:

    fun <T> checkSubtype(t: T) = t

    class Inv<T>
    fun <E> Inv<E>._() {}
    infix fun <T> T.checkType(f: Inv<T>.() -> Unit) {}

With that, an exact type of an expression can be checked in the following way:

    fun test(expr: A) {
       expr checkType { _<A>() }
    }

`CHECK_TYPE` directive also disables `UNDERSCORE_USAGE_WITHOUT_BACKTICKS` diagnostics output.

#### Usage:

    // !CHECK_TYPE

### 3. FILE

The directive lets you compose a test consisting of several files in one actual file.

#### Usage:

    // FILE: A.java
    /* Java code */

    // FILE: B.kt
    /* Kotlin code */

### 4. LANGUAGE

This directive lets you enable or disable certain language features. Language features are named as enum entries of the class `LanguageFeature`.
Each feature can be enabled with `+`, disabled with `-`, or enabled with warning with `warn:`.

#### Usage:

    // !LANGUAGE: -TopLevelSealedInheritance

    // !LANGUAGE: +TypeAliases -LocalDelegatedProperties

    // !LANGUAGE: warn:Coroutines

### 5. API_VERSION

This directive emulates the behavior of the `-api-version` command line option, disallowing to use declarations annotated with `@SinceKotlin(X)` where X is greater than the specified API version.
Note that if this directive is present, the NEWER_VERSION_IN_SINCE_KOTLIN diagnostic is automatically disabled, _unless_ the "!DIAGNOSTICS" directive is present.

#### Usage:

    // !API_VERSION: 1.0
    
### 6. RENDER_DIAGNOSTICS_MESSAGES

This directive forces *Diagnostic printer* prints parametrized diagnostics with all parameters.

#### Usage:

    // !RENDER_DIAGNOSTICS_MESSAGES
