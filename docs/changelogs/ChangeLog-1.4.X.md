
## 1.4.32

### IDE

- [`KT-43824`](https://youtrack.jetbrains.com/issue/KT-43824) KtLightClassForSourceDeclaration#isInheritor works in a different way than java implementation
- [`KT-45287`](https://youtrack.jetbrains.com/issue/KT-45287) LightClasses: `KtLightSimpleModifierList` is no more a parent of `KtLightAnnotationForSourceEntry`
- [`KT-45291`](https://youtrack.jetbrains.com/issue/KT-45291) LightClasses: can't get annotations for constructor konst-parameter
- [`KT-45417`](https://youtrack.jetbrains.com/issue/KT-45417) ULC leakage of primitive type annotations

### Tools. CLI

- [`KT-44758`](https://youtrack.jetbrains.com/issue/KT-44758) kotlin-compiler-embeddable dependency includes unshaded `fastutil` package
- [`KT-45007`](https://youtrack.jetbrains.com/issue/KT-45007) Concurrent Kotlin script compilation/execution results in NullPointerException in KeyedExtensionCollector.getPoint()

## 1.4.31

### Compiler

- [`KT-39776`](https://youtrack.jetbrains.com/issue/KT-39776) 2020.3+: Unresolved reference to Kotlin stdlib function

### IDE. Gradle Integration

- [`KT-44845`](https://youtrack.jetbrains.com/issue/KT-44845) After update to Kotlin 1.4.30 all external dependencies is unresolved in IDE with kotlin.mpp.enableGranularSourceSetsMetadata=true

### IDE. Gradle. Script

- [`KTIJ-11137`](https://youtrack.jetbrains.com/issue/KTIJ-1137) build.gradle.kts: Fatal error during save/load standalone scripts settings
- [`KTIJ-898`](https://youtrack.jetbrains.com/issue/KTIJ-898) Unable to import with Kotlin DSL buildscript - NullPointerException in KotlinDslScriptModelProcessorKt.toListOfScriptModels

### IDE. Multiplatform

- [`KTIJ-1200`](https://youtrack.jetbrains.com/issue/KTIJ-1200) KotlinIconProviderKt.addExpectActualMarker takes up to 180+ seconds

### IDE

#### Fixes

- [`KT-44697`](https://youtrack.jetbrains.com/issue/KT-44697) New JVM IR backend notification - narrow its triggering to Kotlin projects
- [`KT-44523`](https://youtrack.jetbrains.com/issue/KT-44523) IDE notification for trying new JVM backend
- [`KTIJ-696`](https://youtrack.jetbrains.com/issue/KTIJ-696) Freeze during startup of IDEA with intellij project with Kotlin (211-1.4.10-release-IJ1440)

## 1.4.30

### Android

- [`KT-42383`](https://youtrack.jetbrains.com/issue/KT-42383) HMPP: Bad IDEA dependencies: Missing dependency from p1:jvmAndAndroid to p2:jvmAndAndroid

### Backend. Native

- [`KT-38772`](https://youtrack.jetbrains.com/issue/KT-38772) Native: support non-reified type parameters in typeOf
- [`KT-42234`](https://youtrack.jetbrains.com/issue/KT-42234) Move LLVM optimization parameters into konan.properties
- [`KT-42649`](https://youtrack.jetbrains.com/issue/KT-42649) IndexOutOfBoundsException during InlineClassTransformer lowering
- [`KT-42942`](https://youtrack.jetbrains.com/issue/KT-42942) Native: optimize peak backend memory by clearing BindingContext after psi2ir
- [`KT-43198`](https://youtrack.jetbrains.com/issue/KT-43198) Native: support `init` blocks inside inline classes

### Compiler

#### New Features

- [`KT-28055`](https://youtrack.jetbrains.com/issue/KT-28055) Support `init` blocks inside inline classes
- [`KT-28056`](https://youtrack.jetbrains.com/issue/KT-28056) Consider supporting non-public primary constructors for inline classes
- [`KT-41265`](https://youtrack.jetbrains.com/issue/KT-41265) Support noarg compiler plugin for JVM IR
- [`KT-42094`](https://youtrack.jetbrains.com/issue/KT-42094) Allow open callable members in expect interfaces
- [`KT-43129`](https://youtrack.jetbrains.com/issue/KT-43129) FIR: Support OverloadResolutionByLambdaReturnType
- [`KT-43592`](https://youtrack.jetbrains.com/issue/KT-43592) Promote JVM IR compiler backend to Beta
- [`KT-43919`](https://youtrack.jetbrains.com/issue/KT-43919) Support loading Java annotations on base classes and implementing interfaces'  type arguments
- [`KT-44021`](https://youtrack.jetbrains.com/issue/KT-44021) Enable JVM IR backend by default in 1.5

#### Performance Improvements

- [`KT-41352`](https://youtrack.jetbrains.com/issue/KT-41352) JVM IR: reduce bytecode size in for loops and range checks with 'until' by not using inclusive end
- [`KT-41644`](https://youtrack.jetbrains.com/issue/KT-41644) NI: Infinite compilation
- [`KT-42791`](https://youtrack.jetbrains.com/issue/KT-42791) OutOfMemoryError on compilation using kotlin 1.4 on a class with a lot of type inference
- [`KT-42920`](https://youtrack.jetbrains.com/issue/KT-42920) NI: Improve performance around adding constraints

#### Fixes

- [`KT-11454`](https://youtrack.jetbrains.com/issue/KT-11454) Load annotations on TYPE_USE/TYPE_PARAMETER positions from Java class-files
- [`KT-11732`](https://youtrack.jetbrains.com/issue/KT-11732) Verify error for generic interface method invocation with default parameters
- [`KT-14612`](https://youtrack.jetbrains.com/issue/KT-14612) "ISE: Recursive call in a lazy konstue" during processing of a (weakly) recursive type alias
- [`KT-18344`](https://youtrack.jetbrains.com/issue/KT-18344) Upper bound of a typealias type parameter is not reported correctly if it contains the typealias itself
- [`KT-18768`](https://youtrack.jetbrains.com/issue/KT-18768) @Notnull annotation from Java does not work with varargs
- [`KT-20548`](https://youtrack.jetbrains.com/issue/KT-20548) java.lang.IllegalStateException: Illegal class container on simple Java code parsing
- [`KT-22465`](https://youtrack.jetbrains.com/issue/KT-22465) Excessive synthetic method for private setter from superclass
- [`KT-23816`](https://youtrack.jetbrains.com/issue/KT-23816) Inline classes: constants and annotations
- [`KT-24158`](https://youtrack.jetbrains.com/issue/KT-24158) AE: No receiver found on incomplete code with $-signs
- [`KT-24392`](https://youtrack.jetbrains.com/issue/KT-24392) Nullability of Java arrays is read incorrectly if @Nullable annotation has both targets TYPE_USE and VALUE_PARAMETER
- [`KT-26229`](https://youtrack.jetbrains.com/issue/KT-26229) Lambda/anonymous function argument in parentheses is not supported for callsInPlace effect
- [`KT-29735`](https://youtrack.jetbrains.com/issue/KT-29735) KNPE at `KtEnumEntrySuperclassReferenceExpression.getReferencedElement` with explicit type argument inside enum member constructor
- [`KT-31389`](https://youtrack.jetbrains.com/issue/KT-31389) ClassFormatError with companion object in annotation with @JvmStatic
- [`KT-31907`](https://youtrack.jetbrains.com/issue/KT-31907) ISE: UNIT_EXPECTED_TYPE on parsing array literal inside lambda with Unit return type
- [`KT-32228`](https://youtrack.jetbrains.com/issue/KT-32228) Inconsistent boxing/unboxing for inline classes when interface is specialized by object expression
- [`KT-32450`](https://youtrack.jetbrains.com/issue/KT-32450) Inline class incorrectly gets re-wrapped when provided to a function
- [`KT-35849`](https://youtrack.jetbrains.com/issue/KT-35849) Missing nullability assertion on lambda return konstue if expected type has generic return konstue type
- [`KT-35902`](https://youtrack.jetbrains.com/issue/KT-35902) Kotlin generates a private parameterless constructor for constructors taking inline class arguments with default konstues
- [`KT-36399`](https://youtrack.jetbrains.com/issue/KT-36399) Gradually support TYPE_USE nullability annotations read from class-files
- [`KT-36769`](https://youtrack.jetbrains.com/issue/KT-36769) JVM IR: Missing LVT entries for inline function (default) parameters at call site
- [`KT-36982`](https://youtrack.jetbrains.com/issue/KT-36982) JVM IR: SAM adapter classes are generated as synthetic
- [`KT-37007`](https://youtrack.jetbrains.com/issue/KT-37007) JVM IR: extraneous property accessors are generated in multifile facade for InlineOnly property
- [`KT-37317`](https://youtrack.jetbrains.com/issue/KT-37317) [FIR] Add support of extension functions in postponed lambda completion
- [`KT-38400`](https://youtrack.jetbrains.com/issue/KT-38400) FIR: interface abstract is preferred to Any method in super resolve
- [`KT-38536`](https://youtrack.jetbrains.com/issue/KT-38536) JVM IR: bound adapted function references are not inlined
- [`KT-38656`](https://youtrack.jetbrains.com/issue/KT-38656) FIR: determine overridden member visibility properly
- [`KT-38901`](https://youtrack.jetbrains.com/issue/KT-38901) FIR: Make behavior of integer literals overflow consistent with FE 1.0
- [`KT-39709`](https://youtrack.jetbrains.com/issue/KT-39709) [FIR] False positive UNINITIALIZED_VARIABLE in presence of complex graph with jumps
- [`KT-39923`](https://youtrack.jetbrains.com/issue/KT-39923) Result.Failure will get wrapped with Success when using with RxJava
- [`KT-40198`](https://youtrack.jetbrains.com/issue/KT-40198) '$default' methods in 'kotlin/test/AssertionsKt' generated as non-synthetic by JVM_IR
- [`KT-40200`](https://youtrack.jetbrains.com/issue/KT-40200) IDE: Multiple top-level main functions in different files: broken highlighting, "No descriptor resolved for FUN"
- [`KT-40262`](https://youtrack.jetbrains.com/issue/KT-40262) ACC_DEPRECATED flag not generated for property getter delegate in multifile class facade in JVM_IR
- [`KT-40282`](https://youtrack.jetbrains.com/issue/KT-40282) Inline class wrapping Any gets double boxed
- [`KT-40464`](https://youtrack.jetbrains.com/issue/KT-40464) JVM_IR does not generate LINENUMBER at closing brace of (suspend) lambda
- [`KT-40500`](https://youtrack.jetbrains.com/issue/KT-40500) Warnings reporting by Java nullability annotations doesn't work for not top-level types
- [`KT-40926`](https://youtrack.jetbrains.com/issue/KT-40926) IDE import actions do not add required import for convention `invoke()` extension call
- [`KT-40948`](https://youtrack.jetbrains.com/issue/KT-40948) IllegalAccessError while initializing konst property in EXACTLY_ONCE lambda that is passed to another function
- [`KT-40991`](https://youtrack.jetbrains.com/issue/KT-40991) NI: UNRESOLVED_REFERENCE_WRONG_RECEIVER instead of FUNCTION_EXPECTED with convention `invoke` call
- [`KT-41163`](https://youtrack.jetbrains.com/issue/KT-41163) Double wrapped konstue in Result class after map operation
- [`KT-41284`](https://youtrack.jetbrains.com/issue/KT-41284) Spring CGLIB proxies break auto-generated data class componentN and copy methods when using JVM IR
- [`KT-41468`](https://youtrack.jetbrains.com/issue/KT-41468) JVM IR: IllegalAccessError on access to abstract base member from another package, from anonymous object inside abstract class
- [`KT-41491`](https://youtrack.jetbrains.com/issue/KT-41491) UNRESOLVED_REFERENCE_WRONG_RECEIVER instead of FUNCTION_EXPECTED when invoking non-functional konstue as a function
- [`KT-41493`](https://youtrack.jetbrains.com/issue/KT-41493) JVM IR: names of classes for local delegated variables contain the variable name twice
- [`KT-41792`](https://youtrack.jetbrains.com/issue/KT-41792) [FIR] Introduce & use ConeAttribute.UnsafeVariance
- [`KT-41793`](https://youtrack.jetbrains.com/issue/KT-41793) [FIR] Make captured types accessible at the end of resolve
- [`KT-41809`](https://youtrack.jetbrains.com/issue/KT-41809) JVM IR: name for internal $default method doesn't include module name
- [`KT-41810`](https://youtrack.jetbrains.com/issue/KT-41810) JVM IR: Deprecated(HIDDEN) class is incorrectly generated as synthetic
- [`KT-41841`](https://youtrack.jetbrains.com/issue/KT-41841) JVM IR: delegates for private functions with default arguments are generated in multifile classes
- [`KT-41857`](https://youtrack.jetbrains.com/issue/KT-41857) Flaky 'ConcurrentModificationException' through `kotlin.serialization.DescriptorSerializer`
- [`KT-41903`](https://youtrack.jetbrains.com/issue/KT-41903) JVM IR: do not generate LineNumberTable in auto-generated members of data classes
- [`KT-41911`](https://youtrack.jetbrains.com/issue/KT-41911) JVM IR: nested big-arity function calls are not lowered
- [`KT-41957`](https://youtrack.jetbrains.com/issue/KT-41957) JVM IR: step into suspend function goes to the first line of the file
- [`KT-41960`](https://youtrack.jetbrains.com/issue/KT-41960) JVM IR: smart step into members implemented with delegation to interface doesn't work
- [`KT-41961`](https://youtrack.jetbrains.com/issue/KT-41961) JVM IR: line numbers are not generated in JvmMultifileClass facade declarations
- [`KT-41962`](https://youtrack.jetbrains.com/issue/KT-41962) JVM IR: intermittent -1 line numbers in the state machine cause double stepping in the debugger
- [`KT-42001`](https://youtrack.jetbrains.com/issue/KT-42001) Cannot resolve symbol: AssertionError: Module <sdk 1.8> is not contained in his own dependencies
- [`KT-42002`](https://youtrack.jetbrains.com/issue/KT-42002) JVM / IR: IllegalStateException: "No mapping for symbol: VAR IR_TEMPORARY_VARIABLE" caused by named arguments
- [`KT-42021`](https://youtrack.jetbrains.com/issue/KT-42021) JVM / IR: "IndexOutOfBoundsException: Index 0 out of bounds for length 0" during IR lowering with suspend conversion
- [`KT-42033`](https://youtrack.jetbrains.com/issue/KT-42033) JVM IR: accidental override in Map subclass with custom implementations of some members
- [`KT-42036`](https://youtrack.jetbrains.com/issue/KT-42036) IR: "AssertionError: TypeAliasDescriptor expected: deserialized  class Nothing" when referencing typealias with @UnsafeVariance
- [`KT-42043`](https://youtrack.jetbrains.com/issue/KT-42043) JVM IR: Don't generate collection stubs when implementing methods with more specific return types
- [`KT-42044`](https://youtrack.jetbrains.com/issue/KT-42044) Compiler error when lambda with contract surrounded with parentheses
- [`KT-42114`](https://youtrack.jetbrains.com/issue/KT-42114) JVM_IR generates stub for 'removeIf' in abstract classes implementing 'List' and 'Set'
- [`KT-42115`](https://youtrack.jetbrains.com/issue/KT-42115) JVM_IR doesn't generate 'next' and 'hasNext' method in an abstract class implementing 'ListIterator'
- [`KT-42116`](https://youtrack.jetbrains.com/issue/KT-42116) FIR: Java accessor function should not exist in scope together with relevant property
- [`KT-42117`](https://youtrack.jetbrains.com/issue/KT-42117) IR-based ekonstuator cannot handle Java static final fields
- [`KT-42118`](https://youtrack.jetbrains.com/issue/KT-42118) FIR2IR: field-targeted annotation is placed on a property, not on a field
- [`KT-42130`](https://youtrack.jetbrains.com/issue/KT-42130) FIR: type variable is observed after when condition analysis
- [`KT-42132`](https://youtrack.jetbrains.com/issue/KT-42132) FIR2IR: companion function reference has no dispatch receiver
- [`KT-42137`](https://youtrack.jetbrains.com/issue/KT-42137) JVM IR: AbstractMethodError on complex hierarchy where implementation comes from another supertype and has a more specific type
- [`KT-42186`](https://youtrack.jetbrains.com/issue/KT-42186) JVM / IR: Infinite cycle in for expression when unsigned bytes are used in decreasing loop range
- [`KT-42251`](https://youtrack.jetbrains.com/issue/KT-42251) JVM / IR: "IllegalStateException: Descriptor can be left only if it is last" when comparing the i-th element of the container of Int? and `i` with change
- [`KT-42253`](https://youtrack.jetbrains.com/issue/KT-42253) JVM IR: NoSuchFieldError on local delegated property in inline function whose call site happens before declaration in the source
- [`KT-42281`](https://youtrack.jetbrains.com/issue/KT-42281) JVM / IR: AnalyzerException when comparing Int and array that cast to Any in if condition
- [`KT-42340`](https://youtrack.jetbrains.com/issue/KT-42340) FIR2IR: duplicating fake overrides
- [`KT-42344`](https://youtrack.jetbrains.com/issue/KT-42344) IR-based ekonstuator doesn't support "annotation in annotation"
- [`KT-42346`](https://youtrack.jetbrains.com/issue/KT-42346) FIR: double-vararg in IR while resolving collection literal as Java annotation argument
- [`KT-42348`](https://youtrack.jetbrains.com/issue/KT-42348) FIR: false UNINITIALIZED_VARIABLE in local class
- [`KT-42350`](https://youtrack.jetbrains.com/issue/KT-42350) FIR: false UNINITIALIZED_VARIABLE after initialization in try block
- [`KT-42351`](https://youtrack.jetbrains.com/issue/KT-42351) FIR: false HIDDEN in enum entry member call
- [`KT-42354`](https://youtrack.jetbrains.com/issue/KT-42354) JVM / IR: "AssertionError: Unexpected IR element found during code generation" with KProperty `get` invocation
- [`KT-42359`](https://youtrack.jetbrains.com/issue/KT-42359) FIR2IR: cannot mangle type parameter
- [`KT-42373`](https://youtrack.jetbrains.com/issue/KT-42373) FIR2IR: local object nested class has no parent if forward-referenced by nested class supertype
- [`KT-42384`](https://youtrack.jetbrains.com/issue/KT-42384) FIR (BE): top-level field has no parent class in BE
- [`KT-42496`](https://youtrack.jetbrains.com/issue/KT-42496) FIR resolve: synthetic property is written but has no setter
- [`KT-42517`](https://youtrack.jetbrains.com/issue/KT-42517) FIR: exception in BE for recursive inline call
- [`KT-42530`](https://youtrack.jetbrains.com/issue/KT-42530) "AssertionError: No type for resolved lambda argument" on attempting to assign a Pair to a couple of konstues in a scratch file
- [`KT-42601`](https://youtrack.jetbrains.com/issue/KT-42601) [FIR] Inherited declaration clash for stdlib inheritors
- [`KT-42622`](https://youtrack.jetbrains.com/issue/KT-42622) NI: IllegalStateException for if expression with method reference inside flow
- [`KT-42642`](https://youtrack.jetbrains.com/issue/KT-42642) ISE: No `getProgressionLastElement` for progression type IntProgressionType
- [`KT-42650`](https://youtrack.jetbrains.com/issue/KT-42650) JVM IR: extraneous nullability annotation on a generic function of a flexible type
- [`KT-42656`](https://youtrack.jetbrains.com/issue/KT-42656) FIR2IR: unsupported callable reference for Java field
- [`KT-42725`](https://youtrack.jetbrains.com/issue/KT-42725) Debugger steps into core library inline functions in chained calls
- [`KT-42758`](https://youtrack.jetbrains.com/issue/KT-42758) JVM / IR: Deserialized object that overrides readResolve() is not reference equal to the singleton instance
- [`KT-42770`](https://youtrack.jetbrains.com/issue/KT-42770) FIR: duplicating signatures in mangler (typealias for functional type)
- [`KT-42771`](https://youtrack.jetbrains.com/issue/KT-42771) FIR: duplicating signature in mangler (data class with delegate)
- [`KT-42814`](https://youtrack.jetbrains.com/issue/KT-42814) FIR: false UNINITIALIZED_VARIABLE in local function after if...else
- [`KT-42844`](https://youtrack.jetbrains.com/issue/KT-42844) FIR: Property write in init block resolved to parameter write
- [`KT-42846`](https://youtrack.jetbrains.com/issue/KT-42846) JVM_IR: NPE on function reference to @JvmStatic method in a different file
- [`KT-42933`](https://youtrack.jetbrains.com/issue/KT-42933) JVM / IR: "AnalyzerException: Expected an object reference, but found I" with local delegate in inline class
- [`KT-43006`](https://youtrack.jetbrains.com/issue/KT-43006) JVM/JVM_IR: do not generate no-arg constructor for constructor with default arguments if there are inline class types in the signature
- [`KT-43017`](https://youtrack.jetbrains.com/issue/KT-43017) JVM / IR: AssertionError when callable reference passed into a function requiring a suspendable function
- [`KT-43051`](https://youtrack.jetbrains.com/issue/KT-43051) JVM IR: extraneous methods overridding default (Java 8) collection methods in inline class that extends MutableList
- [`KT-43067`](https://youtrack.jetbrains.com/issue/KT-43067) Inner class declaration inside inline class should be prohibited
- [`KT-43068`](https://youtrack.jetbrains.com/issue/KT-43068) JVM IR: no generic signatures for explicitly written methods in a List subclass, whose signature coincides with MutableList methods
- [`KT-43132`](https://youtrack.jetbrains.com/issue/KT-43132) JVM / IR: Method name '<get-...>' in class '...$screenTexts$1$1' cannot be represented in dex format.
- [`KT-43145`](https://youtrack.jetbrains.com/issue/KT-43145) JVM IR: $default methods in multi-file facades are generated as non-synthetic final
- [`KT-43156`](https://youtrack.jetbrains.com/issue/KT-43156) FIR: false UNINITIALIZED_VARIABLE after initialization in `synchronized` block
- [`KT-43196`](https://youtrack.jetbrains.com/issue/KT-43196) JVM: extra non-static member is generated for extension property in inline class
- [`KT-43199`](https://youtrack.jetbrains.com/issue/KT-43199) JVM IR: synthetic flag for deprecated-hidden is not generated for DeprecatedSinceKotlin and deprecation from override
- [`KT-43207`](https://youtrack.jetbrains.com/issue/KT-43207) JVM IR: no collection stub for `iterator` is generated on extending AbstractCollection
- [`KT-43217`](https://youtrack.jetbrains.com/issue/KT-43217) JVM_IR: Multiple FAKE_OVERRIDES for java methods using @NonNull Double and java double
- [`KT-43225`](https://youtrack.jetbrains.com/issue/KT-43225) Confusing message of warning NULLABLE_TYPE_PARAMETER_AGAINST_NOT_NULL_TYPE_PARAMETER
- [`KT-43226`](https://youtrack.jetbrains.com/issue/KT-43226) "Incompatible stack heights" with non-local return to outer lambda inside suspend lambda
- [`KT-43242`](https://youtrack.jetbrains.com/issue/KT-43242) JVM / IR: "AnalyzerException: Expected I, but found R" caused by `when` inside object with @Nullable Integer subject
- [`KT-43249`](https://youtrack.jetbrains.com/issue/KT-43249) Wrong code generated for suspend lambdas with inline class parameters
- [`KT-43286`](https://youtrack.jetbrains.com/issue/KT-43286) JVM IR: IAE "Inline class types should have the same representation: Lkotlin/UInt; != I" on smart cast of unsigned type konstue with JVM target 1.8
- [`KT-43326`](https://youtrack.jetbrains.com/issue/KT-43326) JVM_IR: No deprecated flag for getter of deprecated interface property copied to DefaultImpls
- [`KT-43327`](https://youtrack.jetbrains.com/issue/KT-43327) JVM_IR: No deprecated or synthetic flag for accessors of deprecated-hidden property of unsigned type
- [`KT-43332`](https://youtrack.jetbrains.com/issue/KT-43332) FIR: Smart casts lead to false-positive ambiguity
- [`KT-43370`](https://youtrack.jetbrains.com/issue/KT-43370) JVM IR: No deprecated flag for getter of deprecated property copied via delegation by interface
- [`KT-43459`](https://youtrack.jetbrains.com/issue/KT-43459) JVM_IR. Wrong signature for synthetic $annotations method for extension property on nullable primitive
- [`KT-43478`](https://youtrack.jetbrains.com/issue/KT-43478) NI: "IndexOutOfBoundsException: Index: 3, Size: 3" caused by `is` check with raw type inside `if` condition with `when` inside
- [`KT-43519`](https://youtrack.jetbrains.com/issue/KT-43519) JVM_IR. External functions generated differently in multi file facades
- [`KT-43524`](https://youtrack.jetbrains.com/issue/KT-43524) JVM_IR. Missed deprecation flag on companion @JvmStatic property accessor
- [`KT-43525`](https://youtrack.jetbrains.com/issue/KT-43525) Prohibit JvmOverloads on declarations with inline class types in parameters
- [`KT-43536`](https://youtrack.jetbrains.com/issue/KT-43536) JVM IR: IllegalStateException is not caught by runCatching under Deferred.await() with kotlinx.coroutines
- [`KT-43562`](https://youtrack.jetbrains.com/issue/KT-43562) JVM IR: incorrect mangling for Collection.size in unsigned arrays
- [`KT-43584`](https://youtrack.jetbrains.com/issue/KT-43584) [FIR] Java annotations with named arguments aren't loaded correctly
- [`KT-43587`](https://youtrack.jetbrains.com/issue/KT-43587) Inkonstid default parameter konstue in expect actual declaration on jvm
- [`KT-43630`](https://youtrack.jetbrains.com/issue/KT-43630) "AssertionError: Number of arguments should not be less than number of parameters" during capturing intersection raw type with star projection
- [`KT-43698`](https://youtrack.jetbrains.com/issue/KT-43698) NoSuchMethodError for inline class implementing interface with @JvmDefault methods, -Xjvm-default=enable
- [`KT-43741`](https://youtrack.jetbrains.com/issue/KT-43741) Report error on inline class implementing 'kotlin.Cloneable'
- [`KT-43845`](https://youtrack.jetbrains.com/issue/KT-43845) org.jetbrains.kotlin.codegen.CompilationException: Back-end (JVM) Internal error: Failed to generate expression: KtBlockExpression
- [`KT-43956`](https://youtrack.jetbrains.com/issue/KT-43956) NI: "Error type encountered – UninferredParameterTypeConstructor" on "try" and other constructs with code block as a konstue
- [`KT-44055`](https://youtrack.jetbrains.com/issue/KT-44055) Left uninferred type parameter for callable references inside special calls
- [`KT-44113`](https://youtrack.jetbrains.com/issue/KT-44113) Compiler frontend exception: Number of arguments should not be less than number of parameters, but: parameters=2, args=1
- [`KT-44145`](https://youtrack.jetbrains.com/issue/KT-44145) No highlighting for not initialized base constructor and NoSuchMethodError in Android plugin

### IDE

#### New Features

- [`KT-44075`](https://youtrack.jetbrains.com/issue/KT-44075) Sealed interfaces: New Kotlin Class/File menu update

#### Fixes

- [`KT-29454`](https://youtrack.jetbrains.com/issue/KT-29454) Light class with unexpected name when using obfuscated library
- [`KT-31553`](https://youtrack.jetbrains.com/issue/KT-31553) Complete Statement: Wrong auto-insertion of closing curly brace for a code block
- [`KT-33466`](https://youtrack.jetbrains.com/issue/KT-33466) IDE generates incorrect `external override` with body for overriding `open external` method
- [`KT-39458`](https://youtrack.jetbrains.com/issue/KT-39458) Add CLI support for UL classes
- [`KT-40403`](https://youtrack.jetbrains.com/issue/KT-40403) UAST: PsiMethod for invoked extension function/property misses `@receiver:` annotations
- [`KT-41406`](https://youtrack.jetbrains.com/issue/KT-41406) Kotlin doesn't report annotations for type arguments (no way to add `@Nls`, `@NonNls` annotations to String collections in Kotlin)
- [`KT-41420`](https://youtrack.jetbrains.com/issue/KT-41420) UAST does not return information about type annotations
- [`KT-42194`](https://youtrack.jetbrains.com/issue/KT-42194) OOME: Java heap space from incremental compilation
- [`KT-42754`](https://youtrack.jetbrains.com/issue/KT-42754) MPP: no smart cast for Common nullable property used in platform module
- [`KT-42821`](https://youtrack.jetbrains.com/issue/KT-42821) MPP, IDE: Platform-specific errors are reported even when build doesn't target that platform
- [`KT-44116`](https://youtrack.jetbrains.com/issue/KT-44116) Add language version 1.5 to the compiler configuration preferences
- [`KT-44523`](https://youtrack.jetbrains.com/issue/KT-44523) IDE notification for trying new JVM backend
- [`KT-44543`](https://youtrack.jetbrains.com/issue/KT-44543) Kotlin's LowMemoryWatcher leaks on Kotlin plugin unload

### IDE. Android

- [`KT-42381`](https://youtrack.jetbrains.com/issue/KT-42381) MPP: Bad IDEA dependencies: JVM module depending on built artifact instead of sources of module with Android Plugin applied

### IDE. Completion

- [`KT-44016`](https://youtrack.jetbrains.com/issue/KT-44016) Code completion: support for "sealed interface"
- [`KT-44250`](https://youtrack.jetbrains.com/issue/KT-44250) Code completion does not work in when expression with sealed type argument

### IDE. Gradle. Script

- [`KT-39105`](https://youtrack.jetbrains.com/issue/KT-39105) AE “JvmBuiltins has not been initialized properly” after creating new Gradle/Kotlin-based project via old Project Wizard

### IDE. Inspections and Intentions

#### New Features

- [`KT-22666`](https://youtrack.jetbrains.com/issue/KT-22666) "Create enum constant" quick fix could be provided
- [`KT-24556`](https://youtrack.jetbrains.com/issue/KT-24556) Add Remove quick fix for "Expression under 'when' is never equal to null"
- [`KT-34121`](https://youtrack.jetbrains.com/issue/KT-34121) Report unused result of data class `copy` method
- [`KT-34533`](https://youtrack.jetbrains.com/issue/KT-34533) INLINE_CLASS_CONSTRUCTOR_NOT_FINAL_READ_ONLY_PARAMETER: Add quickfix "Add konst to parameter"
- [`KT-35215`](https://youtrack.jetbrains.com/issue/KT-35215) Quickfix for CONST_VAL_NOT_TOP_LEVEL_OR_OBJECT to remove `const` modifier
- [`KT-40251`](https://youtrack.jetbrains.com/issue/KT-40251) Intention action to ekonstuate compile time expression
- [`KT-44017`](https://youtrack.jetbrains.com/issue/KT-44017) Sealed interfaces: Java side Inspection "implementation of Kotlin sealed interface is forbidden"
- [`KT-43941`](https://youtrack.jetbrains.com/issue/KT-43941) Sealed interfaces: intention to extend class/interface
- [`KT-44043`](https://youtrack.jetbrains.com/issue/KT-44043) Sealed interfaces: quickfix to move class/interface to proper location

#### Fixes

- [`KT-20420`](https://youtrack.jetbrains.com/issue/KT-20420) Intention "Put arguments/parameters on separate lines" doesn't respect the "Place ')' on new line" Kotlin code style setting
- [`KT-21799`](https://youtrack.jetbrains.com/issue/KT-21799) Quickfix "Change function signature" for receiver type doesn't change it
- [`KT-22665`](https://youtrack.jetbrains.com/issue/KT-22665) "Create object" quick fix produce wrong code for enum
- [`KT-23934`](https://youtrack.jetbrains.com/issue/KT-23934) IntelliJ suggest "merge map to joinToString" even when such action is impossible due to suspending actions in map
- [`KT-30894`](https://youtrack.jetbrains.com/issue/KT-30894) Wrong results of intention "Add names to call arguments" when backticked argument starts from digit
- [`KT-31523`](https://youtrack.jetbrains.com/issue/KT-31523) ReplaceWith introduces additional argument name for lambda when named argument is used on call-site
- [`KT-31833`](https://youtrack.jetbrains.com/issue/KT-31833) JavaMapForEachInspection should report for expression with implicit receiver
- [`KT-33096`](https://youtrack.jetbrains.com/issue/KT-33096) Turn 'MapGetWithNotNullAssertionOperator' into an intention
- [`KT-33212`](https://youtrack.jetbrains.com/issue/KT-33212) False positive "map.put() should be converted to assignment" inspection when class inherited from MutableMap has "set" method
- [`KT-34270`](https://youtrack.jetbrains.com/issue/KT-34270) False negative "Join declaration and assignment" with constructor call
- [`KT-34859`](https://youtrack.jetbrains.com/issue/KT-34859) False positive "Should be replaced with Kotlin function" inspection for Character.toString(int) function
- [`KT-34959`](https://youtrack.jetbrains.com/issue/KT-34959) False positive "Redundant overriding method" with different implemented/overridden signatures
- [`KT-35051`](https://youtrack.jetbrains.com/issue/KT-35051) False positive "Remove redundant backticks" if variable inside the string and isn't followed by space
- [`KT-35097`](https://youtrack.jetbrains.com/issue/KT-35097) False positive "Call replaceable with binary operator" on explicit 'equals' call on a platform type konstue
- [`KT-35165`](https://youtrack.jetbrains.com/issue/KT-35165) "Replace 'if' with elvis operator": don't suggest if konst initializer is a complex expression
- [`KT-35346`](https://youtrack.jetbrains.com/issue/KT-35346) False positive 'Make internal' suggestion for function inside interface
- [`KT-35357`](https://youtrack.jetbrains.com/issue/KT-35357) "Move lambda argument out of parentheses" does not preserve block comments
- [`KT-38349`](https://youtrack.jetbrains.com/issue/KT-38349) Inkonstid suggestion to fold to elvis when having a var-variable
- [`KT-40704`](https://youtrack.jetbrains.com/issue/KT-40704) False negative "Redundant semicolon" at start of line
- [`KT-40861`](https://youtrack.jetbrains.com/issue/KT-40861) "Convert to secondary constructor" intention expected on class name
- [`KT-40879`](https://youtrack.jetbrains.com/issue/KT-40879) False positive "Redundant 'inner' modifier" when calling another inner class with empty constructor
- [`KT-40985`](https://youtrack.jetbrains.com/issue/KT-40985) "Remove explicit type arguments" is suggested when type has an annotation
- [`KT-41223`](https://youtrack.jetbrains.com/issue/KT-41223) False positive "Redundant inner modifier" inspection ignores constructor arguments of object expressions
- [`KT-41246`](https://youtrack.jetbrains.com/issue/KT-41246) False positive "Receiver parameter is never used" with anonymous function expression
- [`KT-41298`](https://youtrack.jetbrains.com/issue/KT-41298) "Remove redundant 'with' call" intention works incorrectly with non-local returns and single-expression functions
- [`KT-41311`](https://youtrack.jetbrains.com/issue/KT-41311) False positive "Redundant inner modifier" when deriving from a nested Java class
- [`KT-41499`](https://youtrack.jetbrains.com/issue/KT-41499) "Convert receiver to parameter" produces code with incorrect order of generic type and function invocation  in case of generic function with lambda as a parameter
- [`KT-41680`](https://youtrack.jetbrains.com/issue/KT-41680) False positive "Redundant inner modifier" when deriving from class with non-empty constructor and konstue passed to it from enclosing class
- [`KT-42201`](https://youtrack.jetbrains.com/issue/KT-42201) Add Opt-In action doesn't work if there is already OptIn annotation
- [`KT-42255`](https://youtrack.jetbrains.com/issue/KT-42255) "Replace elvis expression with 'if' expression" intention shouldn't introduce unnecessary variable if 'error' expression is used

### IDE. JS

- [`KT-43760`](https://youtrack.jetbrains.com/issue/KT-43760) KJS: Debugging Kotlin code for Node.js runtime doesn't work

### IDE. Misc

- [`KT-44018`](https://youtrack.jetbrains.com/issue/KT-44018) Sealed interfaces: IDE side implementation for hierarchy provider

### IDE. Multiplatform

- [`KT-40814`](https://youtrack.jetbrains.com/issue/KT-40814) MISSING_DEPENDENCY_CLASS when consuming native-shared library in a source-set with fewer targets than library has

### IDE. Run Configurations

- [`KT-34535`](https://youtrack.jetbrains.com/issue/KT-34535) Unable to run common tests on Android via gutter icon in a multiplatform project

### IDE. Scratch

- [`KT-25038`](https://youtrack.jetbrains.com/issue/KT-25038) Scratch: Destructuring declaration produces an unresolved reference
- [`KT-43415`](https://youtrack.jetbrains.com/issue/KT-43415) Kotlin scratch file could not be run and could lead to dead lock

### IDE. Script

- [`KT-44117`](https://youtrack.jetbrains.com/issue/KT-44117) IDE / Scripts: custom kotlin script definitions aren't loaded

### JavaScript

#### Fixes

- [`KT-31072`](https://youtrack.jetbrains.com/issue/KT-31072) Don't use non-reified arguments to specialize type operations in IR inliner
- [`KT-39964`](https://youtrack.jetbrains.com/issue/KT-39964) Throwable incorrectly implements constructor for (null, cause) args in K/JS-IR
- [`KT-40090`](https://youtrack.jetbrains.com/issue/KT-40090) KJS: IR. Inkonstid behaviour for optional parameters (redundant tail undefined parameters)
- [`KT-40686`](https://youtrack.jetbrains.com/issue/KT-40686) KJS: Uncaught ReferenceError caused by external class as type inside eventListener in init block
- [`KT-40771`](https://youtrack.jetbrains.com/issue/KT-40771) KJS / IR: "ReferenceError: Metadata is not defined" caused by default parameter konstue in inner class constructor
- [`KT-41032`](https://youtrack.jetbrains.com/issue/KT-41032) KJS / IR: "AssertionError: Assertion failed" caused by class that is delegated to inherited interface
- [`KT-41076`](https://youtrack.jetbrains.com/issue/KT-41076) KJS / IR: "AssertionError: Assertion failed" caused by overridden extensiion function in child class
- [`KT-41771`](https://youtrack.jetbrains.com/issue/KT-41771) KJS / IR: IndexOutOfBoundsException "Index 0 out of bounds for length 0" caused by inline class with List in primary constructor and vararg in secondary
- [`KT-42025`](https://youtrack.jetbrains.com/issue/KT-42025) KJS / IR:  IrConstructorCallImpl: No such type argument slot: 0
- [`KT-42112`](https://youtrack.jetbrains.com/issue/KT-42112) KJS: StackOverflowError on @JsExport in case of name clash with function with Enum parameter with star-projection
- [`KT-42262`](https://youtrack.jetbrains.com/issue/KT-42262) KJS: `break`-statements without label are ignored in a `when`
- [`KT-42357`](https://youtrack.jetbrains.com/issue/KT-42357) KotlinJS - external class constructor with vararg does not correctly handle spread operator.
- [`KT-42364`](https://youtrack.jetbrains.com/issue/KT-42364) KJS: Properties of interface delegate are non-configurable
- [`KT-43212`](https://youtrack.jetbrains.com/issue/KT-43212) JS IR: support `init` blocks inside inline classes
- [`KT-43222`](https://youtrack.jetbrains.com/issue/KT-43222) KJS IR: prototype lazy initialization for top-level properties like in JVM
- [`KT-43313`](https://youtrack.jetbrains.com/issue/KT-43313) KJS / IR: "Can't find name for declaration FUN" for secondary constructor
- [`KT-43901`](https://youtrack.jetbrains.com/issue/KT-43901) Call to enum konstues() method from enum companion object leads to non-initialized enum instances

### KMM Plugin

- [`KT-41677`](https://youtrack.jetbrains.com/issue/KT-41677) Could not launch iOS project with custom display name
- [`KT-42463`](https://youtrack.jetbrains.com/issue/KT-42463) Launch common tests for Android on local JVM via run gutter
- [`KT-43188`](https://youtrack.jetbrains.com/issue/KT-43188) NoSuchMethodError in New Module Wizard of KMM Project

### Libraries

- [`KT-41112`](https://youtrack.jetbrains.com/issue/KT-41112) Docs: add more details about bit shift operations
- [`KT-41278`](https://youtrack.jetbrains.com/issue/KT-41278) map.entries.contains can return false if the argument is not MutableEntry
- [`KT-41356`](https://youtrack.jetbrains.com/issue/KT-41356) Incorrect documentation for `rangeTo` function
- [`KT-44456`](https://youtrack.jetbrains.com/issue/KT-44456) Introduce locale-agnostic API for case conversions
- [`KT-44458`](https://youtrack.jetbrains.com/issue/KT-44458) Introduce new Char-to-code and Char-to-digit conversions

### Middle-end. IR

- [`KT-41765`](https://youtrack.jetbrains.com/issue/KT-41765) [Native/IR]  Could not resolveFakeOverride()
- [`KT-42054`](https://youtrack.jetbrains.com/issue/KT-42054) Psi2ir: "RuntimeException: IrSimpleFunctionSymbolImpl is already bound" when using result of function with overload resolution by lambda return type

### Native. C and ObjC Import

- [`KT-42412`](https://youtrack.jetbrains.com/issue/KT-42412) [C-interop] Modality of generated property accessors is always FINAL

### Native. ObjC Export

- [`KT-38530`](https://youtrack.jetbrains.com/issue/KT-38530) Native: konstues() method of enum classes is not exposed to Objective-C/Swift
- [`KT-43599`](https://youtrack.jetbrains.com/issue/KT-43599) K/N: Unbound symbols not allowed

### Native. Platform libraries

- [`KT-43597`](https://youtrack.jetbrains.com/issue/KT-43597) Support for Xcode 12.2 SDK

### Native. Platforms

- [`KT-43276`](https://youtrack.jetbrains.com/issue/KT-43276) Support watchos_x64 target

### Native. Runtime

- [`KT-42822`](https://youtrack.jetbrains.com/issue/KT-42822) Kotlin/Native Worker leaks ObjC/Swift autorelease references (and indirectly bridged K/N references) on Darwin targets

### Native. Stdlib

- [`KT-42172`](https://youtrack.jetbrains.com/issue/KT-42172) Kotlin/Native: StableRef.dispose race condition on Kotlin deinitRuntime
- [`KT-42428`](https://youtrack.jetbrains.com/issue/KT-42428) Inconsistent behavior of map.entries on Kotlin.Native

### Reflection

- [`KT-34024`](https://youtrack.jetbrains.com/issue/KT-34024) "KotlinReflectionInternalError: Inconsistent number of parameters" with `javaMethod` on suspending functions with inline class in function signature or inside the function

### Tools. CLI

- [`KT-43294`](https://youtrack.jetbrains.com/issue/KT-43294) Support `-no-stdlib` option for the `kotlin` runner
- [`KT-43406`](https://youtrack.jetbrains.com/issue/KT-43406) JVM: produce deterministic jar files if -d option konstue is a .jar file

### Tools. CLI. Native

- [`KT-40670`](https://youtrack.jetbrains.com/issue/KT-40670) Allow to override konan.properties via CLI

### Tools. Compiler Plugins

- [`KT-41764`](https://youtrack.jetbrains.com/issue/KT-41764) KJS /IR IllegalStateException: "Symbol for public kotlin/arrayOf is unbound" with serialization plugin
- [`KT-42976`](https://youtrack.jetbrains.com/issue/KT-42976) kotlinx.serialization + JVM IR: NPE on annotation with @SerialInfo
- [`KT-43725`](https://youtrack.jetbrains.com/issue/KT-43725) Prohibit inner and local classes in kotlin-noarg

### Tools. Gradle

- [`KT-38692`](https://youtrack.jetbrains.com/issue/KT-38692) KaptGenerateStubs Gradle task will not clean up outputs when sources are empty and not an incremental build
- [`KT-40140`](https://youtrack.jetbrains.com/issue/KT-40140) kotlin-android plugin eagerly creates several Gradle tasks
- [`KT-41295`](https://youtrack.jetbrains.com/issue/KT-41295) Kotlin Gradle Plugin 1.4.20 Configuration Caching bug due to friendPath provider
- [`KT-42058`](https://youtrack.jetbrains.com/issue/KT-42058) Support moduleName option in Kotlin Gradle plugin for JVM
- [`KT-43054`](https://youtrack.jetbrains.com/issue/KT-43054) Implementation of `AbstractKotlinTarget#buildAdhocComponentsFromKotlinVariants` breaks configuration caching
- [`KT-43489`](https://youtrack.jetbrains.com/issue/KT-43489) Incremental compilation - unable to find history files causing full recompilation
- [`KT-43740`](https://youtrack.jetbrains.com/issue/KT-43740) Gradle out-of-process runner fails with unclear diagnostics if build directory does not exist
- [`KT-43895`](https://youtrack.jetbrains.com/issue/KT-43895) Fix cacheability warnings for the Kotlin plugins

### Tools. Gradle. JS

- [`KT-42400`](https://youtrack.jetbrains.com/issue/KT-42400) Kotlin/JS: Gradle DSL: customField() is rejected in Groovy build.gradle
- [`KT-42462`](https://youtrack.jetbrains.com/issue/KT-42462) NPM dependency declaration with Groovy interpolated string
- [`KT-42954`](https://youtrack.jetbrains.com/issue/KT-42954) Kotlin/JS: IDE import after changing `kotlin.js.externals.output.format` does not re-generate externals
- [`KT-43535`](https://youtrack.jetbrains.com/issue/KT-43535) Common webpack configuration breaks on lambda serialization in some cases
- [`KT-43668`](https://youtrack.jetbrains.com/issue/KT-43668) PackageJson task use file dependencies as is (files and directories), but only files necessary
- [`KT-43793`](https://youtrack.jetbrains.com/issue/KT-43793) nodeArgs in NodeJsExec task
- [`KT-43842`](https://youtrack.jetbrains.com/issue/KT-43842) KJS: Inkonstid `output.library` support for `null` konstue
- [`KT-44104`](https://youtrack.jetbrains.com/issue/KT-44104) KJS / Gradle: An ability to pass jvm args to K2JSDce process

### Tools. Gradle. Multiplatform

- [`KT-42269`](https://youtrack.jetbrains.com/issue/KT-42269) Setup default dependsOn edges for Android source sets
- [`KT-42413`](https://youtrack.jetbrains.com/issue/KT-42413) [MPP/gradle] `withJava` breaks build on 1.4.20-M1
- [`KT-43141`](https://youtrack.jetbrains.com/issue/KT-43141) Gradle / Configuration cache: NPE from org.jetbrains.kotlin.gradle.tasks.KotlinCompileCommon.getKotlinOptions() on reusing configuration cache for task compileCommonMainKotlinMetadata
- [`KT-43329`](https://youtrack.jetbrains.com/issue/KT-43329) Gradle / Configuration cache: IAE “Parameter specified as non-null is null: method KotlinMetadataTargetConfiguratorKt.isCompatibilityMetadataVariantEnabled, parameter $this$isCompatibilityMetadataVariantEnabled” on reusing configuration cache for task compileKotlinMetadata
- [`KT-44298`](https://youtrack.jetbrains.com/issue/KT-44298) Kotlin 1.4.20+ MPP "root" module publication does not include the source JAR that used to be published in the -metadata modules

### Tools. Gradle. Native

- [`KT-39564`](https://youtrack.jetbrains.com/issue/KT-39564) Make kotlin-native Gradle tasks Cacheable
- [`KT-42485`](https://youtrack.jetbrains.com/issue/KT-42485) Fail on cinterop: clang_indexTranslationUnit returned 1
- [`KT-42550`](https://youtrack.jetbrains.com/issue/KT-42550) Adding subspec dependency with git location failed
- [`KT-42849`](https://youtrack.jetbrains.com/issue/KT-42849) Gradle / Configuration cache: tasks nativeMetadataJar, runReleaseExecutableNative, runDebugExecutableNative are unsupported and fails on reusing configuration cache
- [`KT-42938`](https://youtrack.jetbrains.com/issue/KT-42938) CocoaPods Gradle plugin: podBuildDependencies doesn't properly report xcodebuild failures
- [`KT-43151`](https://youtrack.jetbrains.com/issue/KT-43151) Gradle / Configuration cache: UPAE “lateinit property binary has not been initialized” on reusing configuration cache for linkDebugExecutableNative, linkDebugTestNative, linkReleaseExecutableNative tasks
- [`KT-43516`](https://youtrack.jetbrains.com/issue/KT-43516) Failed to resolve Kotin library [Multiple Multiplatform modules]

### Tools. Incremental Compile

- [`KT-42937`](https://youtrack.jetbrains.com/issue/KT-42937) another compilation fail (problem with compilation caches?)

### Tools. JPS

- [`KT-39536`](https://youtrack.jetbrains.com/issue/KT-39536) JPS compilation fails with IOException "storage is already closed"

### Tools. Parcelize

- [`KT-41553`](https://youtrack.jetbrains.com/issue/KT-41553) JVM IR, Parcelize: IrStarProjectionImpl cannot be cast to class IrTypeProjection

### Tools. Scripts

- [`KT-43534`](https://youtrack.jetbrains.com/issue/KT-43534) Allow running "main.kts" script that does not end in a "main.kts" filename (would allow kotlin scripting on GitHub Actions)

### Tools. kapt

- [`KT-34340`](https://youtrack.jetbrains.com/issue/KT-34340) Incremental annotation processor recompile all files (only if KAPT enabled).
- [`KT-36667`](https://youtrack.jetbrains.com/issue/KT-36667) Kapt: Add a flag to strip kotlin.Metadata() annotations from stubs
- [`KT-40493`](https://youtrack.jetbrains.com/issue/KT-40493) KAPT does not support aggregating annotations processors in incremental mode
- [`KT-40882`](https://youtrack.jetbrains.com/issue/KT-40882) Kapt stub generation is non-deterministic for incremental compilation
- [`KT-41788`](https://youtrack.jetbrains.com/issue/KT-41788) NullPointerException: Random crashes of build using gradle and kapt because of not calling Processor.init()
- [`KT-42182`](https://youtrack.jetbrains.com/issue/KT-42182) KAPT: Does not consider generated sources for incremental compilation.


## 1.4.20

### Android

- [`KT-42121`](https://youtrack.jetbrains.com/issue/KT-42121) Deprecate Kotlin Android Extensions compiler plugin
- [`KT-42267`](https://youtrack.jetbrains.com/issue/KT-42267) `Platform declaration clash` error in IDE when using `kotlinx.android.parcel.Parcelize`
- [`KT-42406`](https://youtrack.jetbrains.com/issue/KT-42406) Long or infinite code analysis on simple files modification

### Backend. Native

- [`KT-27534`](https://youtrack.jetbrains.com/issue/KT-27534) Bridges to Nothing-returning methods have incorrect signature
- [`KT-30284`](https://youtrack.jetbrains.com/issue/KT-30284) Native: Nothing? type for expression override and crash
- [`KT-36430`](https://youtrack.jetbrains.com/issue/KT-36430) Optimize when with in range cases
- [`KT-38787`](https://youtrack.jetbrains.com/issue/KT-38787) Missing optimization for "in range" check
- [`KT-39100`](https://youtrack.jetbrains.com/issue/KT-39100) Make Native behaviour of property initialization consistent with JVM
- [`KT-39798`](https://youtrack.jetbrains.com/issue/KT-39798) Override equals/hashCode in functional interface wrappers on Native
- [`KT-39800`](https://youtrack.jetbrains.com/issue/KT-39800) equals/hashCode on adapted function references on Native
- [`KT-41394`](https://youtrack.jetbrains.com/issue/KT-41394) Compilation failed: Backend Internal error: Exception during IR lowering
- [`KT-41907`](https://youtrack.jetbrains.com/issue/KT-41907) Framework test segfaults on GC on watchos_x86 compiled with -opt

### Compiler

#### New Features

- [`KT-21147`](https://youtrack.jetbrains.com/issue/KT-21147) JEP 280: Indify String Concatenation (StringConcatFactory)
- [`KT-34178`](https://youtrack.jetbrains.com/issue/KT-34178) Scripts should be able to access imports objects
- [`KT-35549`](https://youtrack.jetbrains.com/issue/KT-35549) Support kotlin-android-extensions in JVM IR backend (for use with Jetpack Compose projects)
- [`KT-31567`](https://youtrack.jetbrains.com/issue/KT-31567) Support special semantics for underscore-named catch block parameters

#### Performance Improvements

- [`KT-20571`](https://youtrack.jetbrains.com/issue/KT-20571) Coroutines: Reduce number of local variables stored at suspension point
- [`KT-28016`](https://youtrack.jetbrains.com/issue/KT-28016) Coroutine state-machines spill/unspill shall be optimized using data-flow analysis
- [`KT-33394`](https://youtrack.jetbrains.com/issue/KT-33394) UI freezes triggered by QualifiedExpressionResolver.resolveToPackageOrClassPrefix
- [`KT-36814`](https://youtrack.jetbrains.com/issue/KT-36814) Support optimized delegated properties in JVM_IR
- [`KT-36829`](https://youtrack.jetbrains.com/issue/KT-36829) Optimize 'in' expressions (operator fun contains) in JVM_IR
- [`KT-41741`](https://youtrack.jetbrains.com/issue/KT-41741) NI: "AssertionError: Empty intersection for types" with generic Java collection
- [`KT-42195`](https://youtrack.jetbrains.com/issue/KT-42195) NI: prohibitively long compilation time for konstues of nested data structures with type inference
- [`KT-42221`](https://youtrack.jetbrains.com/issue/KT-42221) Native compiler never finishes frontend phase after migrating to Kotlin 1.4.10

#### Fixes

- [`KT-11713`](https://youtrack.jetbrains.com/issue/KT-11713) Refine visibility check for synthetic property with protected setter
- [`KT-16222`](https://youtrack.jetbrains.com/issue/KT-16222) Coroutine should be clearing any internal state as soon as possible to avoid memory leaks
- [`KT-25519`](https://youtrack.jetbrains.com/issue/KT-25519) Extra inline marks inside suspending function callable reference bytecode
- [`KT-33226`](https://youtrack.jetbrains.com/issue/KT-33226) Object INSTANCE field not annotated with NotNull in generated bytecode
- [`KT-35495`](https://youtrack.jetbrains.com/issue/KT-35495) FIR: forbid non-Java synthetic properties
- [`KT-35651`](https://youtrack.jetbrains.com/issue/KT-35651) Kotlin stdlib has greater resolution priority than jars added via @file:DependsOn annotation
- [`KT-35716`](https://youtrack.jetbrains.com/issue/KT-35716) Using @JvmOverloads in @JvmStatic functions in interface companion objects causes a ClassFormatError
- [`KT-35730`](https://youtrack.jetbrains.com/issue/KT-35730) FIR: consider creating fake overrides for objects
- [`KT-36951`](https://youtrack.jetbrains.com/issue/KT-36951) IllegalStateException: Expected some types: Throwing exception when there is a type parameter upper bound for itself
- [`KT-37321`](https://youtrack.jetbrains.com/issue/KT-37321) [FIR] Support java array in type argument
- [`KT-37431`](https://youtrack.jetbrains.com/issue/KT-37431) [FIR] Support Builder Inference
- [`KT-38272`](https://youtrack.jetbrains.com/issue/KT-38272) FIR2IR: use lazy IR symbols for externals & fake overrides
- [`KT-38333`](https://youtrack.jetbrains.com/issue/KT-38333) FIR: CCE on red code with assignment used as expression
- [`KT-38334`](https://youtrack.jetbrains.com/issue/KT-38334) FIR: CCE when resolving try-as-expression
- [`KT-38336`](https://youtrack.jetbrains.com/issue/KT-38336) FIR: NPE with corrupted numeric constant literal
- [`KT-38397`](https://youtrack.jetbrains.com/issue/KT-38397) FIR: Exception while resolving contract definition
- [`KT-38444`](https://youtrack.jetbrains.com/issue/KT-38444) [FIR] Invoke extension lambda with safe call
- [`KT-38470`](https://youtrack.jetbrains.com/issue/KT-38470) FIR: ConeDefinitelyNotNullType in signature
- [`KT-38471`](https://youtrack.jetbrains.com/issue/KT-38471) FIR: ConeIntersectionType in signature
- [`KT-38925`](https://youtrack.jetbrains.com/issue/KT-38925) Internal error: wrong bytecode generated. (AssertionError: int type expected, but null was found in basic frames)
- [`KT-38989`](https://youtrack.jetbrains.com/issue/KT-38989) FIR: Refine sealed classes exhaustiveness in case of sealed subclass
- [`KT-38992`](https://youtrack.jetbrains.com/issue/KT-38992) FIR: Refine type resolution for inner classes
- [`KT-39000`](https://youtrack.jetbrains.com/issue/KT-39000) FIR: Support smartcast after reference equality check
- [`KT-39005`](https://youtrack.jetbrains.com/issue/KT-39005) FIR: Resolve plusAssign in a dependent context
- [`KT-39008`](https://youtrack.jetbrains.com/issue/KT-39008) FIR: Investigate strange effect of type alias and not-nullable bound on inference
- [`KT-39012`](https://youtrack.jetbrains.com/issue/KT-39012) FIR: Inference doesn't get through elvis to lambda parameter
- [`KT-39028`](https://youtrack.jetbrains.com/issue/KT-39028) FIR: Strange resolution to synthetic property with implicit extension receiver while there's an explicit receiver
- [`KT-39032`](https://youtrack.jetbrains.com/issue/KT-39032) FIR: Ambiguity in member scope of a type parameter with multiple bounds
- [`KT-39033`](https://youtrack.jetbrains.com/issue/KT-39033) FIR: Ambiguity when calling generic overridden property
- [`KT-39034`](https://youtrack.jetbrains.com/issue/KT-39034) FIR: Support nested extension function types
- [`KT-39040`](https://youtrack.jetbrains.com/issue/KT-39040) FIR: Deserialize annotations from compiled Kotlin binaries
- [`KT-39043`](https://youtrack.jetbrains.com/issue/KT-39043) FIR: Bare types incorrectly work with type aliases
- [`KT-39044`](https://youtrack.jetbrains.com/issue/KT-39044) FIR: Add fillInStackTrace to member scope of kotlin.Throwable
- [`KT-39046`](https://youtrack.jetbrains.com/issue/KT-39046) FIR: Implicit types in lambdas left when call argument is a type cast
- [`KT-39048`](https://youtrack.jetbrains.com/issue/KT-39048) FIR: Inference fails with integer literal used as Comparable
- [`KT-39050`](https://youtrack.jetbrains.com/issue/KT-39050) FIR: Type resolver doesn't see nested classes from super class of a local class
- [`KT-39070`](https://youtrack.jetbrains.com/issue/KT-39070) FIR: Ambiguity on super calls to hashCode/equals when having super interface and class
- [`KT-39072`](https://youtrack.jetbrains.com/issue/KT-39072) FIR: Subtyping check doesn't affect nullability of safe-call receiver
- [`KT-39076`](https://youtrack.jetbrains.com/issue/KT-39076) FIR: Synthetic property is not a var because of @Nullable annotation on parameter
- [`KT-39080`](https://youtrack.jetbrains.com/issue/KT-39080) FIR: Smart casts remain incorrect if lambda is present in when branch
- [`KT-39374`](https://youtrack.jetbrains.com/issue/KT-39374) Wrong bytecode generated for suspend function call with EXACTLY_ONCE lambda capturing a variable initialized in when-subject
- [`KT-39621`](https://youtrack.jetbrains.com/issue/KT-39621) [FIR] Support when exhaustiveness checker for java enums
- [`KT-40135`](https://youtrack.jetbrains.com/issue/KT-40135) JVM IR does not generate restricted suspend lambdas
- [`KT-40382`](https://youtrack.jetbrains.com/issue/KT-40382) Missing proper jvmSignature for synthesized equals/hashCode/toString in inline classes
- [`KT-40605`](https://youtrack.jetbrains.com/issue/KT-40605) JVM IR: IndexOutOfBoundsException caused by inner class passed to generic outer class method with Nothing type argument
- [`KT-40664`](https://youtrack.jetbrains.com/issue/KT-40664) JVM: No bounds check in optimization of `ULong in UInt..UInt`
- [`KT-40665`](https://youtrack.jetbrains.com/issue/KT-40665) JVM: No resolved function check when optimizing `in/contains` with mismatched bound types
- [`KT-41014`](https://youtrack.jetbrains.com/issue/KT-41014) FIR2IR: when/where/how to determine the presence of a backing field for a property
- [`KT-41018`](https://youtrack.jetbrains.com/issue/KT-41018) FIR2IR: sort members during de/serialization
- [`KT-41144`](https://youtrack.jetbrains.com/issue/KT-41144) False positive "Redundant spread operator" in when statement and generic vararg argument
- [`KT-41218`](https://youtrack.jetbrains.com/issue/KT-41218) HMPP: arrayList declarations are visible both from stdlib-common and stdlib-jvm and lead to false-positive resolution ambiguity in IDE
- [`KT-41374`](https://youtrack.jetbrains.com/issue/KT-41374) JVM / IR: NoSuchMethodError in Android project compiler caused by combination of inline classes and coroutines
- [`KT-41388`](https://youtrack.jetbrains.com/issue/KT-41388) NI: Backend Internal error: Exception during IR lowering
- [`KT-41429`](https://youtrack.jetbrains.com/issue/KT-41429) Inline class returned from suspend function should be boxed on resume path
- [`KT-41465`](https://youtrack.jetbrains.com/issue/KT-41465) JVM / IR: "AssertionError: inconsistent parent function for CLASS LAMBDA_IMPL CLASS name" caused by inline method call into multiple constructors
- [`KT-41484`](https://youtrack.jetbrains.com/issue/KT-41484) JVM IR: support -Xemit-jvm-type-annotations
- [`KT-41668`](https://youtrack.jetbrains.com/issue/KT-41668) JVM IR: incorrect enclosing constructor for lambdas in initializers of inner classes
- [`KT-41669`](https://youtrack.jetbrains.com/issue/KT-41669) JVM IR: incorrect hashCode intrinsic is used in JVM target 1.8 for generic type substituted with primitive
- [`KT-41693`](https://youtrack.jetbrains.com/issue/KT-41693) NI: Type inference in nested expression incorrectly assumes non-nullable return type of Java function, causing NullPointerException
- [`KT-41729`](https://youtrack.jetbrains.com/issue/KT-41729) NI: UnsupportedOperationException with inkonstid callable reference
- [`KT-41761`](https://youtrack.jetbrains.com/issue/KT-41761) JVM IR: CCE from backend on generating typeOf for non-reified type parameter with star projection in upper bound
- [`KT-41789`](https://youtrack.jetbrains.com/issue/KT-41789) Missing DebugMetadata in inlined suspend lambda
- [`KT-41913`](https://youtrack.jetbrains.com/issue/KT-41913) NI: Kotlin 1.4 type inference breaks konstid code
- [`KT-41934`](https://youtrack.jetbrains.com/issue/KT-41934) NI: a type variable for lambda parameter has been inferred to nullable type instead of not null one
- [`KT-42005`](https://youtrack.jetbrains.com/issue/KT-42005) JVM / IR: "NullPointerException: Parameter specified as non-null is null" when toString is called on inline class with not primitive property
- [`KT-42450`](https://youtrack.jetbrains.com/issue/KT-42450) NI: "IllegalStateException: Error type encountered: NonFixed:" with coroutines
- [`KT-42523`](https://youtrack.jetbrains.com/issue/KT-42523) Missed DefaultImpls for interface in `-jvm-default=all` mode on inheriting it from interface compiled in old scheme
- [`KT-42524`](https://youtrack.jetbrains.com/issue/KT-42524) Wrong specialization diagnostic is reported on inheriting from java interface with default with -Xjvm-default=all-compatibility
- [`KT-42546`](https://youtrack.jetbrains.com/issue/KT-42546) HMPP: incorrect subtyping of nullable types & overload resolution ambiguity on using and expect-function declaration with nullable expect in a signature
- [`KT-17691`](https://youtrack.jetbrains.com/issue/KT-17691) Wrong argument order in resolved call with varargs
- [`KT-25114`](https://youtrack.jetbrains.com/issue/KT-25114) Prohibit @JvmStatic on functions in private companions
- [`KT-33917`](https://youtrack.jetbrains.com/issue/KT-33917) Prohibit to expose anonymous types from private inline functions
- [`KT-35870`](https://youtrack.jetbrains.com/issue/KT-35870) Forbid secondary enum class constructors which do not delegate to the primary constructor
- [`KT-39098`](https://youtrack.jetbrains.com/issue/KT-39098) NI: parameter of anonymous function can be inferred to Any? if another parameter's type is specified
- [`KT-41176`](https://youtrack.jetbrains.com/issue/KT-41176) NI with Gson: "ClassCastException: java.util.ArrayList cannot be cast to java.lang.Void"
- [`KT-41194`](https://youtrack.jetbrains.com/issue/KT-41194) ClassCastException on returning Result.failure from lambda within suspend function
- [`KT-42438`](https://youtrack.jetbrains.com/issue/KT-42438) NI: ClassCastException: cannot be cast to java.lang.Void caused by when statement in `run` function
- [`KT-42699`](https://youtrack.jetbrains.com/issue/KT-42699) False positive NON_JVM_DEFAULT_OVERRIDES_JAVA_DEFAULT diagnostic in new jvm-default modes
- [`KT-42706`](https://youtrack.jetbrains.com/issue/KT-42706) Kotlin 1.4 infers generic is Nothing instead of actual Foo class (Android project)


### Docs & Examples

- [`KT-42318`](https://youtrack.jetbrains.com/issue/KT-42318) No documentation for `kotlin.js.js`

### IDE

#### New Features

- [`KT-20775`](https://youtrack.jetbrains.com/issue/KT-20775) More kotlin kinds in new kotlin file/class menu
- [`KT-31331`](https://youtrack.jetbrains.com/issue/KT-31331) Improve: Optimize Import should remove unused unresolved imports
- [`KT-31500`](https://youtrack.jetbrains.com/issue/KT-31500) Smart enter: support get() clause
- [`KT-39231`](https://youtrack.jetbrains.com/issue/KT-39231) Injection: Add receiver of kotlin.text.toPattern to standard Kotlin injections
- [`KT-39844`](https://youtrack.jetbrains.com/issue/KT-39844) Add specific highlight for Enum class

#### Performance Improvements

- [`KT-39353`](https://youtrack.jetbrains.com/issue/KT-39353) Implement EnterBetweenBracesNoCommitDelegate
- [`KT-39720`](https://youtrack.jetbrains.com/issue/KT-39720) A lot of freezes in Kotlin project
- [`KT-41634`](https://youtrack.jetbrains.com/issue/KT-41634) Deadlock in org.jetbrains.kotlin.idea.framework.LibraryEffectiveKindProviderImpl.getEffectiveKind
- [`KT-41936`](https://youtrack.jetbrains.com/issue/KT-41936) Impossible to work with Kotlin 1.4 in 202 idea, idea eats all cpu

#### Fixes

- [`KT-10790`](https://youtrack.jetbrains.com/issue/KT-10790) "Move statement up" for @file-targeted annotation moves package declaration to bottom of file
- [`KT-15262`](https://youtrack.jetbrains.com/issue/KT-15262) "Generate toString()" is ignoring property if it has a getter
- [`KT-24352`](https://youtrack.jetbrains.com/issue/KT-24352) Method separators: displayed between properties, not displayed between companion object and function
- [`KT-29364`](https://youtrack.jetbrains.com/issue/KT-29364) "Extend selection" can't select lambda body with parameters
- [`KT-32403`](https://youtrack.jetbrains.com/issue/KT-32403) Clickable links in annotation parameters (like in TODOs)
- [`KT-32409`](https://youtrack.jetbrains.com/issue/KT-32409) Organizing imports should not remove imports while there are unresolved symbols
- [`KT-34566`](https://youtrack.jetbrains.com/issue/KT-34566) Too small indent after line break for multi line strings
- [`KT-34587`](https://youtrack.jetbrains.com/issue/KT-34587) "Move statement down" doesn't work for statement in constructor with end-of-line comment
- [`KT-34705`](https://youtrack.jetbrains.com/issue/KT-34705) "Move statement down" for penultimate statement with end-of-line comment in constructor leads to moving comma to the end of comment
- [`KT-34707`](https://youtrack.jetbrains.com/issue/KT-34707) "Move statement up" for last statement with end-of-line comment in constructor leads to moving comma to the end of comment
- [`KT-35424`](https://youtrack.jetbrains.com/issue/KT-35424) FIR IDE: Kotlin project does not see stdlib
- [`KT-35732`](https://youtrack.jetbrains.com/issue/KT-35732) URLs in String literals are not clickable
- [`KT-35859`](https://youtrack.jetbrains.com/issue/KT-35859) Language injection doesn't work with named arguments in different position
- [`KT-37210`](https://youtrack.jetbrains.com/issue/KT-37210) UAST: KtLightClassForSourceDeclaration.isInheritor sometimes returns the wrong result
- [`KT-37219`](https://youtrack.jetbrains.com/issue/KT-37219) File level OptIn annotation is not recognized by the IDE
- [`KT-38959`](https://youtrack.jetbrains.com/issue/KT-38959) IDE: False negative EXPLICIT_DELEGATION_CALL_REQUIRED, "IllegalArgumentException: Range must be inside element being annotated"
- [`KT-39398`](https://youtrack.jetbrains.com/issue/KT-39398) Wrong import of unrelated object member is suggested for receiver
- [`KT-39457`](https://youtrack.jetbrains.com/issue/KT-39457) Separate decompiled declarations Light implementation from LightClasses infrastructure
- [`KT-39899`](https://youtrack.jetbrains.com/issue/KT-39899) KotlinOptimizeImportsRefactoringHelper: ISE: Attempt to modify PSI for non-committed Document
- [`KT-40578`](https://youtrack.jetbrains.com/issue/KT-40578) UAST: write accesses to Kotlin properties should resolve to setter
- [`KT-41290`](https://youtrack.jetbrains.com/issue/KT-41290) KotlinClassViaConstructorUSimpleReferenceExpression resolves to PsiMethod instead of PsiClass
- [`KT-42029`](https://youtrack.jetbrains.com/issue/KT-42029) HMPP, IDE: NPE from `FacetSerializationKt.getFacetPlatformByConfigurationElement` on project opening
- [`KT-43202`](https://youtrack.jetbrains.com/issue/KT-43202) On 1.4.20-RC version AS ask for xml compatibility update for EAP version of plugin
- [`KT-42883`](https://youtrack.jetbrains.com/issue/KT-42883) No highlighting for elements marked as @Deprecated in stdlib

### IDE. Android

- [`KT-42406`](https://youtrack.jetbrains.com/issue/KT-42406) Long or infinite code analysis on simple files modification
- [`KT-42061`](https://youtrack.jetbrains.com/issue/KT-42061) Highlighting is broken in Android activity
- [`KT-41930`](https://youtrack.jetbrains.com/issue/KT-41930) Android Studio 4.2 cannot start after updating to 1.4.20 plugin with error: Missing essential plugin: org.jetbrains.android

### IDE. Completion

- [`KT-26235`](https://youtrack.jetbrains.com/issue/KT-26235) Kotlin methods/fields don't have icons in Java completion

### IDE. Debugger

- [`KT-37486`](https://youtrack.jetbrains.com/issue/KT-37486) Kotlin plugin keeps reference to stream debugger support classes after stream debugger plugin is disabled
- [`KT-38659`](https://youtrack.jetbrains.com/issue/KT-38659) Ekonstuate Expression: `toString()` on variable returns error when breakpoint is in `commonTest` sourceset
- [`KT-39309`](https://youtrack.jetbrains.com/issue/KT-39309) Debugger: Prolonged "Collecting data" for variables when breakpoint is inside `respondHtml`
- [`KT-39435`](https://youtrack.jetbrains.com/issue/KT-39435) "Collecting data..." in debugger variables view never finishes
- [`KT-39717`](https://youtrack.jetbrains.com/issue/KT-39717) Debugger shows "Collecting data..." forever for instances of some class
- [`KT-40386`](https://youtrack.jetbrains.com/issue/KT-40386) Memory leak detected: 'org.jetbrains.kotlin.idea.debugger.coroutine.view.XCoroutineView'
- [`KT-40635`](https://youtrack.jetbrains.com/issue/KT-40635) Coroutines Debugger: make IDE plugin accept coroutines 1.3.8-rc* versions as well
- [`KT-41505`](https://youtrack.jetbrains.com/issue/KT-41505) Coroutines Debugger: “Access is allowed from event dispatch thread with IW lock only.”

### IDE. Decompiler, Indexing, Stubs

- [`KT-28732`](https://youtrack.jetbrains.com/issue/KT-28732) Stub file element types should be registered early enough
- [`KT-41346`](https://youtrack.jetbrains.com/issue/KT-41346) IDE: "AssertionError: Stub type mismatch: USER_TYPE!=REFERENCE_EXPRESSION" with `CollapsedDumpParser` class from IDEA SDK
- [`KT-41859`](https://youtrack.jetbrains.com/issue/KT-41859) File analysis never ending with kotlinx.cli (AssertionError: Stub type mismatch: TYPEALIAS!=CLASS)
- [`KT-41640`](https://youtrack.jetbrains.com/issue/KT-41640) "Project roots have changed" happened during indexing because of org.jetbrains.kotlin.idea.core.script.ucache.ScriptClassRootsUpdater$notifyRootsChanged increases overall indexing time.
- [`KT-41646`](https://youtrack.jetbrains.com/issue/KT-41646) "AssertionError: ContentElementType: FILE"; Code analysis never finishes on some files from my project

### IDE. Gradle Integration

- [`KT-34271`](https://youtrack.jetbrains.com/issue/KT-34271) Support `pureKotlinSourceFolders` for MPP projects
- [`KT-37106`](https://youtrack.jetbrains.com/issue/KT-37106) Gradle + IDE integration: on creating source roots from Project tree IDEA creates incorrect settings
- [`KT-38830`](https://youtrack.jetbrains.com/issue/KT-38830) addTransitiveDependenciesOnImplementedModules performance is slowing down Android Studio Gradle Sync
- [`KT-41703`](https://youtrack.jetbrains.com/issue/KT-41703) Kotlin plugin not functional: PluginException: While loading class org.jetbrains.kotlin.idea.core.script.KotlinScriptDependenciesClassFinder

### IDE. Gradle. Script

- [`KT-35092`](https://youtrack.jetbrains.com/issue/KT-35092) “Unable to get Gradle home directory” popup and no build.gradle.kts highlighting right after creating a new project
- [`KT-37590`](https://youtrack.jetbrains.com/issue/KT-37590) Wrong notification for precompiled build script
- [`KT-39523`](https://youtrack.jetbrains.com/issue/KT-39523) Go to Declaration navigates to decompiled classfile instead of sources in case of jumping to Gradle plugin sources in buildSrc
- [`KT-39542`](https://youtrack.jetbrains.com/issue/KT-39542) EA-218043: java.util.NoSuchElementException: No element of given type found (GradleBuildRootsManager)
- [`KT-39790`](https://youtrack.jetbrains.com/issue/KT-39790) List of standalone script should be saved between IDE restarts
- [`KT-39910`](https://youtrack.jetbrains.com/issue/KT-39910) build.gradle.kts isn't highlighted after import
- [`KT-39916`](https://youtrack.jetbrains.com/issue/KT-39916) init.gradle.kts isn't highlighted
- [`KT-40243`](https://youtrack.jetbrains.com/issue/KT-40243) gradle.kts: standalone script under build root isn't highlighted as standalone
- [`KT-41141`](https://youtrack.jetbrains.com/issue/KT-41141) Gradle Kotlin DSL: "cannot access 'java.lang.Comparable'. Check your module classpath" with empty JDK in Project structure
- [`KT-41281`](https://youtrack.jetbrains.com/issue/KT-41281) Multiple Script Definitions warning shown in git project having multiple Gradle projects

### IDE. Hints

- [`KT-32368`](https://youtrack.jetbrains.com/issue/KT-32368) Rework Inline hints settings so that they look appropriate with the new UI in 2019.3
- [`KT-38027`](https://youtrack.jetbrains.com/issue/KT-38027) Support Code Vision feature in Kotlin
- [`KT-42014`](https://youtrack.jetbrains.com/issue/KT-42014) ClassNotFoundException in Android Studio 4.2 after installing 1.4.20 plugin

### IDE. Hints. Parameter Info

- [`KT-24172`](https://youtrack.jetbrains.com/issue/KT-24172) Parameter info marks signature as inapplicable when using argument labels
- [`KT-41617`](https://youtrack.jetbrains.com/issue/KT-41617) Parameter Info shows nothing inside already present type arguments of supertype
- [`KT-41645`](https://youtrack.jetbrains.com/issue/KT-41645) Add support for mixed named arguments to parameter info popup

### IDE. Inspections and Intentions

#### New Features

- [`KT-14578`](https://youtrack.jetbrains.com/issue/KT-14578) Suggest "Convert lambda to reference" intention for generic extension function
- [`KT-19321`](https://youtrack.jetbrains.com/issue/KT-19321) Warn if method with nullable return type always returns non-null
- [`KT-20718`](https://youtrack.jetbrains.com/issue/KT-20718) Add a quick fix for "this class has no constructor" error on 'expect' annotation classes
- [`KT-21223`](https://youtrack.jetbrains.com/issue/KT-21223) Add inspection for incomplete destructuring
- [`KT-22420`](https://youtrack.jetbrains.com/issue/KT-22420) Intention to replace a = b with b.also { a = it }
- [`KT-39930`](https://youtrack.jetbrains.com/issue/KT-39930) Add inspection for nullable Boolean in `if` condition
- [`KT-40016`](https://youtrack.jetbrains.com/issue/KT-40016) Replace 'a.toLowerCase() == b.toLowerCase()' with 'a.equals(b, ignoreCase = true)' inspection
- [`KT-40283`](https://youtrack.jetbrains.com/issue/KT-40283) Inspection which replaces `also`/`apply` with nested `forEach` to `onEach`
- [`KT-40769`](https://youtrack.jetbrains.com/issue/KT-40769) Add intention to replace isBlank/isNotBlank method negation

#### Fixes

- [`KT-12222`](https://youtrack.jetbrains.com/issue/KT-12222) Intention 'Convert to block body' should take nullability of overriden method into account when expression is of platform type
- [`KT-14395`](https://youtrack.jetbrains.com/issue/KT-14395) Lambda to Reference doesn't work for generic types
- [`KT-15846`](https://youtrack.jetbrains.com/issue/KT-15846) 'Change lambda expression return type' quick fix does nothing
- [`KT-15944`](https://youtrack.jetbrains.com/issue/KT-15944) IDEA doesn't suggest to replace deprecated get set operator functions when it used as operator
- [`KT-17222`](https://youtrack.jetbrains.com/issue/KT-17222) "Convert reference to lambda" creates red code for method with default argument konstues
- [`KT-18125`](https://youtrack.jetbrains.com/issue/KT-18125) "Wrap with let {...}" intention not available in all cases
- [`KT-20438`](https://youtrack.jetbrains.com/issue/KT-20438) Naming convention inspections: factory functions that are named like classes are flagged
- [`KT-24138`](https://youtrack.jetbrains.com/issue/KT-24138) Incorrect behavior in "convert reference to lambda" with new inference enabled, on function reference with default arguments
- [`KT-29844`](https://youtrack.jetbrains.com/issue/KT-29844) "Create class from usage" should mark constructor of created class as internal if its parameters have internal visibility
- [`KT-30928`](https://youtrack.jetbrains.com/issue/KT-30928) "Show hints for suspend calls" is too easy to enable and hard to disable
- [`KT-31749`](https://youtrack.jetbrains.com/issue/KT-31749) "Surround with null check" produces incorrect check for 'in' expression
- [`KT-32963`](https://youtrack.jetbrains.com/issue/KT-32963) Don't suggest `java` directory of a JVM source set in multiplatform project as a target for `Create actual` quick fix
- [`KT-33211`](https://youtrack.jetbrains.com/issue/KT-33211) Quickfix "add parameter" for method references should infer functional type instead of KFunction
- [`KT-33258`](https://youtrack.jetbrains.com/issue/KT-33258) "Merge 'if's" intention drops comment before nested if
- [`KT-34572`](https://youtrack.jetbrains.com/issue/KT-34572) Convert to block body action improperly works with suppress annotations
- [`KT-35128`](https://youtrack.jetbrains.com/issue/KT-35128) Intention `Convert member to extension` hides property delegate text with single line comment
- [`KT-35214`](https://youtrack.jetbrains.com/issue/KT-35214) Intention `Put parameters on one line`: don't suggest if parameters has end-of-line comments
- [`KT-35320`](https://youtrack.jetbrains.com/issue/KT-35320) False positive "Replace explicit parameter 'x' with 'it'" in 'when' expression which returns lambda
- [`KT-35525`](https://youtrack.jetbrains.com/issue/KT-35525) False positive intention for 'run': "Convert to 'let'" when invoked without receiver
- [`KT-35526`](https://youtrack.jetbrains.com/issue/KT-35526) Intention "Eliminate argument of 'when'" is broken for 'when' expression without 'else' branch
- [`KT-35805`](https://youtrack.jetbrains.com/issue/KT-35805) FoldInitializerAndIfToElvis: should not add new line for multiline initializer
- [`KT-36051`](https://youtrack.jetbrains.com/issue/KT-36051) IfThenToSafeAccessInspection: do not report if condition is SENSELESS_COMPARISON
- [`KT-37748`](https://youtrack.jetbrains.com/issue/KT-37748) "Convert anonymous function to lambda expression" intention does not add necessary lambda type parameter
- [`KT-37841`](https://youtrack.jetbrains.com/issue/KT-37841) IllegalStateException after "add non-null asserted call" on nullable function reference
- [`KT-38139`](https://youtrack.jetbrains.com/issue/KT-38139) False negative "Add suspend modifier" quickfix when suspend function is called in inline lambda
- [`KT-38267`](https://youtrack.jetbrains.com/issue/KT-38267) False positive "Call on collection type may be reduced" with Java platform types: suggested to reduce 'mapNotNull' call to 'map'
- [`KT-38282`](https://youtrack.jetbrains.com/issue/KT-38282) False positive "Remove redundant spread operator" inspection with array as class property or fun argument
- [`KT-38915`](https://youtrack.jetbrains.com/issue/KT-38915) "Remove explicit type specification" intention should be disabled in explicit API mode
- [`KT-38981`](https://youtrack.jetbrains.com/issue/KT-38981) "Specify return type explicitly" inspection is not reported for declaration annotated with @PublishedApi in Explicit Api mode
- [`KT-39026`](https://youtrack.jetbrains.com/issue/KT-39026) 'Specify return type explicitly' intention duplicates compiler warning in Explicit api mode
- [`KT-39200`](https://youtrack.jetbrains.com/issue/KT-39200) False positive "Redundant qualifier name" with same-named member object and companion property
- [`KT-39263`](https://youtrack.jetbrains.com/issue/KT-39263) False positive "Variable should be inlined" for override konstue in  initialized object
- [`KT-39311`](https://youtrack.jetbrains.com/issue/KT-39311) Batch quick fix name for "Change file's package" is truncated
- [`KT-39393`](https://youtrack.jetbrains.com/issue/KT-39393) "Convert anonymous function to lambda expression" intention does not add necessary lambda type parameter for outer function
- [`KT-39454`](https://youtrack.jetbrains.com/issue/KT-39454) False positive "Unused symbol" with private anonymous object property
- [`KT-39467`](https://youtrack.jetbrains.com/issue/KT-39467) False negative "Move variable declaration into when" if a variable declaration is placed on a new line
- [`KT-39490`](https://youtrack.jetbrains.com/issue/KT-39490) 'Wrap with ?.let' quickfix put the receiver in `let` lambda
- [`KT-39552`](https://youtrack.jetbrains.com/issue/KT-39552) Merge 'if's intention drops comment after nested if
- [`KT-39604`](https://youtrack.jetbrains.com/issue/KT-39604) "Package directive doesn't match file location" quick fix does not insert a space between keyword `package` and the package name
- [`KT-39772`](https://youtrack.jetbrains.com/issue/KT-39772) "Redundant  'Unit'" should support lambdas
- [`KT-40215`](https://youtrack.jetbrains.com/issue/KT-40215) "Create abstract function" quick fix suggested even though surrounding class is non-abstract
- [`KT-40448`](https://youtrack.jetbrains.com/issue/KT-40448) "Convert call chain into sequence": support functions added in Kotlin 1.4
- [`KT-40558`](https://youtrack.jetbrains.com/issue/KT-40558) False positive "Move to class body" intention on data class constructor property
- [`KT-41338`](https://youtrack.jetbrains.com/issue/KT-41338) False positive "Redundant 'asSequence' call" when Map.Entry properties are used.
- [`KT-41615`](https://youtrack.jetbrains.com/issue/KT-41615) "Unused equals expression" inspection: highlight whole expression with yellow background
- [`KT-43037`](https://youtrack.jetbrains.com/issue/KT-43037) Disable "Incomplete destructuring declaration" in 1.4.20

### IDE. J2K

- [`KT-20421`](https://youtrack.jetbrains.com/issue/KT-20421) J2K: SUPERTYPE_NOT_INITIALIZED for object extending base class
- [`KT-37298`](https://youtrack.jetbrains.com/issue/KT-37298) J2K: implicit widening conversion for whole argument expression is transformed to cast on subexpression
- [`KT-38879`](https://youtrack.jetbrains.com/issue/KT-38879) J2K loses class annotations when converting class to object
- [`KT-39149`](https://youtrack.jetbrains.com/issue/KT-39149) J2K fails with augmented assignment operators when multiplying int by a non-int
- [`KT-40359`](https://youtrack.jetbrains.com/issue/KT-40359) J2K: Conversion of inkonstid octal numbers throws NumberFormatException
- [`KT-40363`](https://youtrack.jetbrains.com/issue/KT-40363) J2K: Converting HEX integer literal in for-loop throws NumberFormatException

### IDE. JS

- [`KT-39319`](https://youtrack.jetbrains.com/issue/KT-39319) KJS: Support debugging through new Intellij 202 API
- [`KT-41328`](https://youtrack.jetbrains.com/issue/KT-41328) KJS / Gradle: explicitApi mode doesn't work

### IDE. KDoc

- [`KT-17926`](https://youtrack.jetbrains.com/issue/KT-17926) IDE should show documentation for a class if the constructor has no docs.
- [`KT-19069`](https://youtrack.jetbrains.com/issue/KT-19069) KDoc: show default argument konstues
- [`KT-37132`](https://youtrack.jetbrains.com/issue/KT-37132) Redundant @NotNull annotation in a Quick Documentation pop-up

### IDE. Misc

- [`KT-39327`](https://youtrack.jetbrains.com/issue/KT-39327) Get rid of usages of internal classes ModuleOrderEntryImpl and LibraryImpl
- [`KT-40455`](https://youtrack.jetbrains.com/issue/KT-40455) Improve support for EditorConfig

### IDE. Navigation

- [`KT-24616`](https://youtrack.jetbrains.com/issue/KT-24616) Find usages fails to find setter usage
- [`KT-38762`](https://youtrack.jetbrains.com/issue/KT-38762) "Cannot access 'java.io.Serializable' which is a supertype of 'kotlin.Int'" brokes navigation to constructor parameter declaration
- [`KT-39558`](https://youtrack.jetbrains.com/issue/KT-39558) Call Hierarchy shows references from KDoc
- [`KT-40788`](https://youtrack.jetbrains.com/issue/KT-40788) "Find usages" on Java getter overridden in Kotlin doesn't find synthetic property usage when it is called without base declaration
- [`KT-40960`](https://youtrack.jetbrains.com/issue/KT-40960) Unable to find usages in java of Kotlin constructor in library when sources is attached.

### IDE. Project View

- [`KT-37528`](https://youtrack.jetbrains.com/issue/KT-37528) 'Add Kotlin File/Class' suggests file before class

### IDE. Refactorings

- [`KT-19744`](https://youtrack.jetbrains.com/issue/KT-19744) "Change Signature" is not available on `constructor` keyword in primary constructor
- [`KT-22170`](https://youtrack.jetbrains.com/issue/KT-22170) "Change Signature" ignores Java usages of methods marked with @JvmStatic
- [`KT-37517`](https://youtrack.jetbrains.com/issue/KT-37517) Inkonstid qualified name on copy-paste in build.gradle.kts

### IDE. Run Configurations

- [`KT-24463`](https://youtrack.jetbrains.com/issue/KT-24463) MPP, Intellij runner: Run does not add resource directory to classpath
- [`KT-36370`](https://youtrack.jetbrains.com/issue/KT-36370) Hide run gutter icons for not yet implemented targets in multiplatform projects
- [`KT-39788`](https://youtrack.jetbrains.com/issue/KT-39788) MPP, Gradle runner: Run does not add resource directory to classpath on project reopen without Gradle sync

### IDE. Scratch

- [`KT-40557`](https://youtrack.jetbrains.com/issue/KT-40557) IDE / Scratch: .kt files are treated as Kotlin scratches, opening fails

### IDE. Script

- [`KT-35825`](https://youtrack.jetbrains.com/issue/KT-35825) Custom kotlin scripts have no project import suggestions in sub modules.
- [`KT-39796`](https://youtrack.jetbrains.com/issue/KT-39796) Performance of KotlinScriptDependenciesClassFinder
- [`KT-41622`](https://youtrack.jetbrains.com/issue/KT-41622) IDE: Kotlin scripting support can't find context class from same project
- [`KT-41905`](https://youtrack.jetbrains.com/issue/KT-41905) IDE / Script: FilePathPattern parameter in @KotlinScript annotation is not reflected correctly in Pattern / Extension
- [`KT-42206`](https://youtrack.jetbrains.com/issue/KT-42206) Cannot load script definitions using org.jetbrains.kotlin.jsr223.ScriptDefinitionForExtensionAndIdeConsoleRootsSource

### IDE. Tests Support

- [`KT-28854`](https://youtrack.jetbrains.com/issue/KT-28854) Run/Debug configurations: "Redirect input from" option is not available for Kotlin apps
- [`KT-36909`](https://youtrack.jetbrains.com/issue/KT-36909) IDE attempts to run non-JVM tests launched from context menu as JVM ones
- [`KT-37799`](https://youtrack.jetbrains.com/issue/KT-37799) Don't show a target choice in context menu for a test launched on specific platform

### IDE. Wizards

- [`KT-37965`](https://youtrack.jetbrains.com/issue/KT-37965) New Project wizard 1.4+: adding JUnit5 test library add dependency to runner
- [`KT-40527`](https://youtrack.jetbrains.com/issue/KT-40527) Node.JS application template in New Project Wizard
- [`KT-40874`](https://youtrack.jetbrains.com/issue/KT-40874) Open new project wizard help page in browser when clicking help button in new project wizard
- [`KT-41417`](https://youtrack.jetbrains.com/issue/KT-41417) Add react template to new project wizard
- [`KT-41418`](https://youtrack.jetbrains.com/issue/KT-41418) Wizard: Support KJS compiler choice
- [`KT-41958`](https://youtrack.jetbrains.com/issue/KT-41958) New project wizard: Backend/Console applications template with Groovy DSL missing compileTestKotlin block
- [`KT-42372`](https://youtrack.jetbrains.com/issue/KT-42372) Rrename test classes in wizard template to avoid name clashing

### JavaScript

- [`KT-38136`](https://youtrack.jetbrains.com/issue/KT-38136) JS IR BE: add an ability to generate separate js files for each module and maybe each library
- [`KT-38868`](https://youtrack.jetbrains.com/issue/KT-38868) [MPP / JS / IR] IllegalStateException: "Serializable class must have single primary constructor" for expect class without primary constructor with @Serializable annotation
- [`KT-39088`](https://youtrack.jetbrains.com/issue/KT-39088) [ KJS / IR ] IllegalStateException: Concrete fake override IrBasedFunctionHandle
- [`KT-39367`](https://youtrack.jetbrains.com/issue/KT-39367) KJS: .d.ts generation not working for objects
- [`KT-39378`](https://youtrack.jetbrains.com/issue/KT-39378) KJS / IR: "IllegalStateException: Operation is unsupported" with binaries.executable() and external function inside `for` loop  with Iterator as return type
- [`KT-41275`](https://youtrack.jetbrains.com/issue/KT-41275) KJS / IR: "IllegalStateException: Can't find name for declaration FUN" caused by default konstue in constructor parameter
- [`KT-41627`](https://youtrack.jetbrains.com/issue/KT-41627) KJS / IR / Serialization: IllegalStateException: Serializable class must have single primary constructor
- [`KT-37829`](https://youtrack.jetbrains.com/issue/KT-37829) Kotlin JS IR: "Properties without fields are not supported" for companion objects
- [`KT-39740`](https://youtrack.jetbrains.com/issue/KT-39740) KJS / IR: Can't use Serializable and JsExport annotations at the same time

### KMM Plugin

- [`KT-41522`](https://youtrack.jetbrains.com/issue/KT-41522) KMM: exceptions for Mobile Multiplatform plugin are suggested to report to Google, not JetBrains
- [`KT-42065`](https://youtrack.jetbrains.com/issue/KT-42065) [KMM plugin] iOS apps fail to launch on iOS simulator with Xcode 12

### Libraries

- [`KT-41799`](https://youtrack.jetbrains.com/issue/KT-41799) String.replace performance improvements
- [`KT-43306`](https://youtrack.jetbrains.com/issue/KT-43306) Deprecate createTempFile and createTempDir functions in kotlin.io
- [`KT-19192`](https://youtrack.jetbrains.com/issue/KT-19192) Provide file system extensions/APIs based on java.nio.file.Path
- [`KT-41837`](https://youtrack.jetbrains.com/issue/KT-41837) Remove @ExperimentalStdlibApi from CancellationException

### Middle-end. IR

- [`KT-40193`](https://youtrack.jetbrains.com/issue/KT-40193) IR: pluginContext.referenceClass() is not resolving typealias
- [`KT-41181`](https://youtrack.jetbrains.com/issue/KT-41181) Kotlin/Native 1.4.0 compiler fails on data class with >120 fields

### Native. C and ObjC Import

- [`KT-41250`](https://youtrack.jetbrains.com/issue/KT-41250) [C-interop] Stubs for C functions without parameter names should have non-stable names
- [`KT-41639`](https://youtrack.jetbrains.com/issue/KT-41639) Use LazyIR for enums and structs from cached libraries
- [`KT-41655`](https://youtrack.jetbrains.com/issue/KT-41655) Native: "type cnames.structs.S  of return konstue is not supported here: doesn't correspond to any C type" when accessing forward-declared-struct-typed C global variable

### Native. ObjC Export

- [`KT-38641`](https://youtrack.jetbrains.com/issue/KT-38641) Kotlin-Multiplatform: Objective-C `description` method name collision in Swift
- [`KT-39206`](https://youtrack.jetbrains.com/issue/KT-39206) New line characters in @Deprecated annotation cause syntax error in Kotlin/native exported header

### Native. Platform libraries

- [`KT-42191`](https://youtrack.jetbrains.com/issue/KT-42191) Support for Xcode 12

### Native. Runtime. Memory

- [`KT-42275`](https://youtrack.jetbrains.com/issue/KT-42275) "Memory.cpp:1605: runtime assert: Recursive GC is disallowed" sometimes when using Kotlin from Swift deinit

### Native. Stdlib

- [`KT-39145`](https://youtrack.jetbrains.com/issue/KT-39145) MutableData append method

### Tools. Android Extensions

- [`KT-42342`](https://youtrack.jetbrains.com/issue/KT-42342) Build fails with `java.lang.RuntimeException: Duplicate class found in modules` on `checkDebug(Release)DuplicateClasses` task when both `kotlin-parcelize` and `kotlin-android-extensions` plugins are applied

### Tools. CLI

- [`KT-35111`](https://youtrack.jetbrains.com/issue/KT-35111) Extend CLI compilers help with link to online docs
- [`KT-41916`](https://youtrack.jetbrains.com/issue/KT-41916) Add JVM target bytecode version 15

### Tools. Commonizer

- [`KT-41220`](https://youtrack.jetbrains.com/issue/KT-41220) [Commonizer] Short-circuit type aliases
- [`KT-41247`](https://youtrack.jetbrains.com/issue/KT-41247) [Commonizer] Missed supertypes in commonized class
- [`KT-41643`](https://youtrack.jetbrains.com/issue/KT-41643) Commonizer exception for targets [ios_x64], [macos_x64]
- [`KT-42574`](https://youtrack.jetbrains.com/issue/KT-42574) HMPP: unresolved platform.* imports in nativeMain source set

### Tools. Compiler Plugins

- [`KT-36329`](https://youtrack.jetbrains.com/issue/KT-36329) Provide diagnostic in kotlinx.serialization when custom serializer mismatches property type
- [`KT-40030`](https://youtrack.jetbrains.com/issue/KT-40030) Move the Parcelize functionality out of the Android Extensions plugin

### Tools. Gradle

- [`KT-33908`](https://youtrack.jetbrains.com/issue/KT-33908) Make Kotlin Gradle plugin compatible with the Gradle configuration cache
- [`KT-35341`](https://youtrack.jetbrains.com/issue/KT-35341) KotlinCompile: Symlinked friend paths are no longer supported

### Tools. Gradle. JS

#### New Features

- [`KT-35330`](https://youtrack.jetbrains.com/issue/KT-35330) Allow to customise generated package.json
- [`KT-39825`](https://youtrack.jetbrains.com/issue/KT-39825) Provide single point of Webpack configuration in Gradle script
- [`KT-41054`](https://youtrack.jetbrains.com/issue/KT-41054) Support Yarn resolutions
- [`KT-41340`](https://youtrack.jetbrains.com/issue/KT-41340) Add flag to suppress kotlin2js deprecation message
- [`KT-41566`](https://youtrack.jetbrains.com/issue/KT-41566) Kotlin/JS: Support JavaScript Library distribution
- [`KT-42222`](https://youtrack.jetbrains.com/issue/KT-42222) KJS / Gradle: "Cannot find package@version in yarn.lock" when npm dependencies of one package but with different version are used in project
- [`KT-42339`](https://youtrack.jetbrains.com/issue/KT-42339) Support dukat binaries generation

#### Fixes

- [`KT-39515`](https://youtrack.jetbrains.com/issue/KT-39515) package.json is regenerated without a visible reason
- [`KT-39838`](https://youtrack.jetbrains.com/issue/KT-39838) Kotlin/JS Gradle tooling: NPM dependencies of different kinds with different versions of the same package fail with "Cannot find package@version in yarn.lock"
- [`KT-39995`](https://youtrack.jetbrains.com/issue/KT-39995) Collect statistic about generateExternals feature
- [`KT-40087`](https://youtrack.jetbrains.com/issue/KT-40087) Kotlin/JS, IR backend: browserRun: update in continuous mode fails: "ENOENT: no such file or directory" referring output .js
- [`KT-40159`](https://youtrack.jetbrains.com/issue/KT-40159) Implement workaround / fix for webpack's "window is not defined"
- [`KT-40178`](https://youtrack.jetbrains.com/issue/KT-40178) Browser run task prints output in TeamCity format
- [`KT-40201`](https://youtrack.jetbrains.com/issue/KT-40201) Kotlin/JS: Gradle: public package.json has empty `devDependencies {}`
- [`KT-40202`](https://youtrack.jetbrains.com/issue/KT-40202) Kotlin/JS: Gradle: NPM version range operators are written into package.json as escape sequences
- [`KT-40342`](https://youtrack.jetbrains.com/issue/KT-40342) [Gradle, JS, Maven] "Cannot find module" generating fake NPM module from Maven dependendency
- [`KT-40462`](https://youtrack.jetbrains.com/issue/KT-40462) Collect statistic about usages of kotlin.js.generate.executable.default option
- [`KT-40753`](https://youtrack.jetbrains.com/issue/KT-40753) Type script definition file is not referenced as types in the package.json
- [`KT-40812`](https://youtrack.jetbrains.com/issue/KT-40812) Node.JS run working directory
- [`KT-40865`](https://youtrack.jetbrains.com/issue/KT-40865) KJS / Gradle: Registering a task with a type that directly extends AbstractTask has been deprecated
- [`KT-40986`](https://youtrack.jetbrains.com/issue/KT-40986) KJS / Gradle: BuildOperationQueueFailure when two different versions of js library are used as dependencies
- [`KT-41125`](https://youtrack.jetbrains.com/issue/KT-41125) Bump NPM versions in 1.4.20
- [`KT-41286`](https://youtrack.jetbrains.com/issue/KT-41286) KJS / Gradle: args order in runTask is changed in 1.4.0
- [`KT-41475`](https://youtrack.jetbrains.com/issue/KT-41475) KJS / Gradle: debug mode doesn't support custom launchers in karma config
- [`KT-41662`](https://youtrack.jetbrains.com/issue/KT-41662) Kotlin/JS: with CSS support mode == "extract" browser test fails even without CSS usage: "Error in config file!"
- [`KT-42494`](https://youtrack.jetbrains.com/issue/KT-42494) KJS / Gradle: "Configuration cache state could not be cached" caused by Gradle configuration cache

### Tools. Gradle. Native

- [`KT-39764`](https://youtrack.jetbrains.com/issue/KT-39764) Assertions are disabled when running K/N compiler in Gradle process
- [`KT-39999`](https://youtrack.jetbrains.com/issue/KT-39999) Cocoapods plugin's dummy header cannot be compiled
- [`KT-40999`](https://youtrack.jetbrains.com/issue/KT-40999) CocoaPods Gradle plugin: Support custom cinterop options when declaring a pod dependency.
- [`KT-41367`](https://youtrack.jetbrains.com/issue/KT-41367) CocoaPods Gradle plugin: support git repository dependency
- [`KT-41844`](https://youtrack.jetbrains.com/issue/KT-41844) Kotlin 1.4.10 gradle configuration error with cocoapods using multiple multiplatform modules
- [`KT-42531`](https://youtrack.jetbrains.com/issue/KT-42531) Gradle task "podGenIos" fails if a Pod with a static library is added.

### Tools. Incremental Compile

- [`KT-37446`](https://youtrack.jetbrains.com/issue/KT-37446) Incremental analysis for Java sources fails when run on JDK 11

### Tools. Parcelize

- [`KT-39981`](https://youtrack.jetbrains.com/issue/KT-39981) Android parcel 'java.lang.VerifyError: Bad return type'
- [`KT-42267`](https://youtrack.jetbrains.com/issue/KT-42267) `Platform declaration clash` error in IDE when using `kotlinx.android.parcel.Parcelize`
- [`KT-42958`](https://youtrack.jetbrains.com/issue/KT-42958) False positive IDE error on classes with kotlinx.parcelize.Parcelize on project initial import
- [`KT-43290`](https://youtrack.jetbrains.com/issue/KT-43290) Typo in error message for `ErrorsParcelize.DEPRECATED_ANNOTATION` - kotlin.parcelize instead of kotlinx.parcelize
- [`KT-43291`](https://youtrack.jetbrains.com/issue/KT-43291) Diagnostic deprecation messages should not be shown in case `kotlin-android-extensions` plugin is applied

### Tools. Scripts

- [`KT-37987`](https://youtrack.jetbrains.com/issue/KT-37987) Kotlin script: hyphen arguments not forwarded to script
- [`KT-38404`](https://youtrack.jetbrains.com/issue/KT-38404) Scripting API: Provide Location of Annotation Usage
- [`KT-39502`](https://youtrack.jetbrains.com/issue/KT-39502) Scripting: reverse order of Severity enum so that ERROR > INFO
- [`KT-42335`](https://youtrack.jetbrains.com/issue/KT-42335) No "caused by" info about an exception that thrown in Kotlin Script

### Tools. kapt

- [`KT-25960`](https://youtrack.jetbrains.com/issue/KT-25960) Interfaces annotated with JvmDefault has wrong modifiers during annotation processing
- [`KT-37732`](https://youtrack.jetbrains.com/issue/KT-37732) Kapt task is broken after update to 1.3.70/1.3.71
- [`KT-42915`](https://youtrack.jetbrains.com/issue/KT-42915) Kapt generates inkonstid stubs for static methods in interfaces in Kotlin 1.4.20-M2


## 1.4.10

### Compiler

#### Performance Improvements

- [`KT-41149`](https://youtrack.jetbrains.com/issue/KT-41149) NI: Upgraded project from 1.3.72 to 1.4.0 hangs during build
- [`KT-41335`](https://youtrack.jetbrains.com/issue/KT-41335) Kotlin Out of Memory When ekonstuating expression
- [`KT-41400`](https://youtrack.jetbrains.com/issue/KT-41400) NI: Huge performance regression for kotlin compiler from 1.3.72 to 1.4.0 due to using list of dozen lambdas in a call

#### Fixes

- [`KT-41005`](https://youtrack.jetbrains.com/issue/KT-41005) Coercion to Unit doesn't take into account nullability of a return type for lambda
- [`KT-41043`](https://youtrack.jetbrains.com/issue/KT-41043) NI: StackOverflowError through `PostponedArgumentInputTypesResolver.getAllDeeplyRelatedTypeVariables`
- [`KT-41135`](https://youtrack.jetbrains.com/issue/KT-41135) Type Inference Regression For Property Delegate With Receiver Type
- [`KT-41140`](https://youtrack.jetbrains.com/issue/KT-41140) Unresolved reference to parameter of a catch block from lambda expression on 1.4.0
- [`KT-41150`](https://youtrack.jetbrains.com/issue/KT-41150) IllegalStateException: Couldn't obtain compiled function body for public final inline fun
- [`KT-41164`](https://youtrack.jetbrains.com/issue/KT-41164) NI: "IllegalStateException: Error type encountered" with callbackFlow builder inside condition
- [`KT-41202`](https://youtrack.jetbrains.com/issue/KT-41202) Type inference fails in 1.4.0
- [`KT-41218`](https://youtrack.jetbrains.com/issue/KT-41218) HMPP: arrayList declarations are visible both from stdlib-common and stdlib-jvm and lead to false-positive resolution ambiguity in IDE
- [`KT-41308`](https://youtrack.jetbrains.com/issue/KT-41308) 1.4.0 Type inference regression in suspend blocks with elvis operator expressions
- [`KT-41357`](https://youtrack.jetbrains.com/issue/KT-41357) ClassCastException for unstable smart cast on a property call receiver
- [`KT-41386`](https://youtrack.jetbrains.com/issue/KT-41386) NI: Type mismatch with generic type parameters
- [`KT-41426`](https://youtrack.jetbrains.com/issue/KT-41426) Operator compareTo is not called with nullable type
- [`KT-41430`](https://youtrack.jetbrains.com/issue/KT-41430) Broken choosing overload by lambda return type inside builder inference
- [`KT-41470`](https://youtrack.jetbrains.com/issue/KT-41470) Wrong nullability assertion is generated when using the BuilderInference annotation
- [`KT-41482`](https://youtrack.jetbrains.com/issue/KT-41482) Add a fallback compiler flag to disable the unified null checks behavior

### IDE

- [`KT-41325`](https://youtrack.jetbrains.com/issue/KT-41325) SOE in IDEKotlinAsJavaSupport
- [`KT-41390`](https://youtrack.jetbrains.com/issue/KT-41390) Typo: equals & hashCode are written with the first uppercase letter

### IDE. Code Style, Formatting

- [`KT-41314`](https://youtrack.jetbrains.com/issue/KT-41314) Formatter: Frequent freezes when reformatting code (TrailingCommaPostFormatProcessor)

### IDE. Debugger

- [`KT-40417`](https://youtrack.jetbrains.com/issue/KT-40417) Coroutines Debugger: “No coroutine information found” in case of main() entry-point without explicit debug-artifact

### IDE. Gradle. Script

- [`KT-39542`](https://youtrack.jetbrains.com/issue/KT-39542) EA-218043: java.util.NoSuchElementException: No element of given type found (GradleBuildRootsManager)
- [`KT-41283`](https://youtrack.jetbrains.com/issue/KT-41283) Gradle scripts unable to support 1.4 language level features yet

### IDE. Inspections and Intentions

- [`KT-41264`](https://youtrack.jetbrains.com/issue/KT-41264) Disable “Redundant inner modifier” in 1.4.10
- [`KT-41395`](https://youtrack.jetbrains.com/issue/KT-41395) Inspection description misses a space (needs to be lowercase)

### IDE. REPL

- [`KT-40898`](https://youtrack.jetbrains.com/issue/KT-40898) REPL: "IllegalAccessError: tried to access field" caused by log4j

### IDE. Wizards

- [`KT-38921`](https://youtrack.jetbrains.com/issue/KT-38921) New project wizard: Backend/Console applications template with Groovy DSL generates build.gradle with KotlinCompile instead of compileKotlin

### JavaScript

- [`KT-38059`](https://youtrack.jetbrains.com/issue/KT-38059) Support arrays passed as named arguments to varargs
- [`KT-40964`](https://youtrack.jetbrains.com/issue/KT-40964) KJS / IR: don't generate "import" (short names) for external interfaces
- [`KT-41081`](https://youtrack.jetbrains.com/issue/KT-41081) KJS IR: nativeGetter, nativeSetter, nativeInvoke are not supported

### Libraries

- [`KT-41320`](https://youtrack.jetbrains.com/issue/KT-41320) Actual  kotlin.test annotation typealiases are inaccessible in modular environment

### Middle-end. IR

- [`KT-41324`](https://youtrack.jetbrains.com/issue/KT-41324) IR: "Compilation failed: null" caused by StackOverflowError in compiler in multi-module project

### Native. C and ObjC Import

- [`KT-39762`](https://youtrack.jetbrains.com/issue/KT-39762) cinterop on 1.4-M2 doesn't include @Deprecated Kotlin declaration for C declaration it fails to import

### Native. ObjC Export

- [`KT-39206`](https://youtrack.jetbrains.com/issue/KT-39206) New line characters in @Deprecated annotation cause syntax error in Kotlin/native exported header
- [`KT-40976`](https://youtrack.jetbrains.com/issue/KT-40976) "Unrecognized selector sent to instance" exception invoking suspending lambda from Swift in Native iOS project

### Reflection

- [`KT-40842`](https://youtrack.jetbrains.com/issue/KT-40842) "AssertionError: Built-in class kotlin.Any is not found" on java modular run

### Tools. Compiler Plugins

- [`KT-41321`](https://youtrack.jetbrains.com/issue/KT-41321) Upgrading to 1.4.0 fails compiling native with <x> is not bound error

### Tools. Gradle. Multiplatform

- [`KT-41083`](https://youtrack.jetbrains.com/issue/KT-41083) Transitive dependency on an MPP with host-specific source sets fails to resolve: "Couldn't resolve metadata artifact..."

### Tools. Gradle. Native

- [`KT-40834`](https://youtrack.jetbrains.com/issue/KT-40834) Cannot build Kotlin Multiplatform project on Windows 10 64-bit when the Cocoapods plugin is applied

### Tools. Scripts

- [`KT-35925`](https://youtrack.jetbrains.com/issue/KT-35925) REPL: Springboot autoconfiguration problem (META-INF/spring.factories not found ?)

### Tools. kapt

- [`KT-41313`](https://youtrack.jetbrains.com/issue/KT-41313) kapt 1.4.0 throws "ZipException: zip END header not found", when Graal SVM jar in classpath


## 1.4.0

### Compiler

#### New Features

- [`KT-23729`](https://youtrack.jetbrains.com/issue/KT-23729) Provide a way to generate JVM default method bodies in interfaces delegating to DefaultImpls
- [`KT-30330`](https://youtrack.jetbrains.com/issue/KT-30330) Introduce KotlinNothingValueException and throw it instead of NPE on expressions of type Nothing
- [`KT-38435`](https://youtrack.jetbrains.com/issue/KT-38435) Support suspend conversion on callable references in JVM backend

#### Fixes

- [`KT-35483`](https://youtrack.jetbrains.com/issue/KT-35483) NI: compatibility mode
- [`KT-39728`](https://youtrack.jetbrains.com/issue/KT-39728) Declarations from `kotlin.reflect` resolved to expect-classes even in JVM modules in IDE
- [`KT-40153`](https://youtrack.jetbrains.com/issue/KT-40153) REPL IDE services completion fails on imports completion
- [`KT-40404`](https://youtrack.jetbrains.com/issue/KT-40404) Mixed named/positional arguments: argument can be passed twice
- [`KT-40544`](https://youtrack.jetbrains.com/issue/KT-40544) NI: "TYPE_MISMATCH: Required: MutableList<out T!> Found: List<T>" caused by Java interface function
- [`KT-40555`](https://youtrack.jetbrains.com/issue/KT-40555) NI: Spread operator allows inferred nullable types
- [`KT-40646`](https://youtrack.jetbrains.com/issue/KT-40646) NI: TYPE_MISMATCH: "inferred type is Unit but Observer<T> was expected" caused by LieData.observe inference
- [`KT-40691`](https://youtrack.jetbrains.com/issue/KT-40691) False positive CAPTURED_VAL_INITIALIZATION with EXACTLY_ONCE contract
- [`KT-40693`](https://youtrack.jetbrains.com/issue/KT-40693) UnsupportedOperationException: no descriptor for type constructor of (CapturedType(out TResult)..CapturedType(out TResult)?)
- [`KT-40824`](https://youtrack.jetbrains.com/issue/KT-40824) Usages of a typealias lose nullability and annotations in deserialization
- [`KT-40843`](https://youtrack.jetbrains.com/issue/KT-40843) Unhandled exception for suspending methods that return Result type
- [`KT-40869`](https://youtrack.jetbrains.com/issue/KT-40869) Recursion computation inside compiler resolve on a correct code
- [`KT-40893`](https://youtrack.jetbrains.com/issue/KT-40893) Error "Captured konstues initialization is forbidden due to possible reassignment" when attempting to use the plusAssign operator with a java list inside an inline function
- [`KT-40919`](https://youtrack.jetbrains.com/issue/KT-40919) kotlin.collections.ArrayDeque requires an explicit import when compiling with kotlinc 1.4-RC and -language-version 1.3
- [`KT-40920`](https://youtrack.jetbrains.com/issue/KT-40920) Regression in JvmDefault: incorrect access to missing DefaultImpls on default methods in Java interface overriding Kotlin interface
- [`KT-40978`](https://youtrack.jetbrains.com/issue/KT-40978) Prohibit using suspend functions as SAM in fun interfaces

### Docs & Examples

- [`KT-35218`](https://youtrack.jetbrains.com/issue/KT-35218) Fix misleading in JavaDoc for `createTempDir`/`createTempFile`
- [`KT-36981`](https://youtrack.jetbrains.com/issue/KT-36981) Provide a visual illustration of source sets structure generated by `ios()`, `watchos()`, `tvos()` presets
- [`KT-38050`](https://youtrack.jetbrains.com/issue/KT-38050) Language Guide: MPP reference: add sample of language settings for all roots instead of common root only

### IDE

- [`KT-30265`](https://youtrack.jetbrains.com/issue/KT-30265) IDE, MPP: False negative TYPE_PARAMETER_AS_REIFIED in common code
- [`KT-40494`](https://youtrack.jetbrains.com/issue/KT-40494) UAST: "NoSuchElementException: No element of given type found" with use-site target annotation
- [`KT-40639`](https://youtrack.jetbrains.com/issue/KT-40639) Shift IDE plugins updater numbers for Kotlin plugin

### IDE. Code Style, Formatting

- [`KT-40636`](https://youtrack.jetbrains.com/issue/KT-40636) Hard freeze on formatter: infinite recursion

### IDE. Gradle Integration

- [`KT-30116`](https://youtrack.jetbrains.com/issue/KT-30116) IDE: Unresolved reference in MPP module (androidMain source set) for annotation from common module (defined in commonMain and with JVM target)

### IDE. Gradle. Script

- [`KT-34552`](https://youtrack.jetbrains.com/issue/KT-34552) Deadlock in ScriptDefinitionsManager
- [`KT-40675`](https://youtrack.jetbrains.com/issue/KT-40675) Gradle build file is not highlighted until navigating to symbol

### IDE. JS

- [`KT-40461`](https://youtrack.jetbrains.com/issue/KT-40461) Create new projects with kotlin.js.generate.executable.default=false

### IDE. Script

- [`KT-39547`](https://youtrack.jetbrains.com/issue/KT-39547) Kotlin script support freezes IDEA
- [`KT-40242`](https://youtrack.jetbrains.com/issue/KT-40242) gradle.kts: Deadlock in ScriptClassRootsUpdater

### IDE. Wizards

- [`KT-36153`](https://youtrack.jetbrains.com/issue/KT-36153) New Project Wizard: provide more referential info on project structure editor screen
- [`KT-39904`](https://youtrack.jetbrains.com/issue/KT-39904) New Project wizard 1.4: update Frontend Application to make it run with JS IR
- [`KT-40149`](https://youtrack.jetbrains.com/issue/KT-40149) Gradle project wizard: templates for Kotlin/JS are not properly formatted

### JavaScript

- [`KT-25859`](https://youtrack.jetbrains.com/issue/KT-25859) JS: support function references to functions with vararg if expected type ends with repeated vararg element type
- [`KT-40083`](https://youtrack.jetbrains.com/issue/KT-40083) K/JS-IR: java.lang.IllegalStateException: has not acquired a symbol yet
- [`KT-40892`](https://youtrack.jetbrains.com/issue/KT-40892) KJS, IR: Unresolved references on importing classes from kotlinx-nodejs

### Libraries

- [`KT-39051`](https://youtrack.jetbrains.com/issue/KT-39051) Libraries native artifacts are published without sources

### Tools. Gradle

- [`KT-27816`](https://youtrack.jetbrains.com/issue/KT-27816) Provide a possibility to specify intermediate source sets between platform-agnostic and platform-specific test source sets
- [`KT-37720`](https://youtrack.jetbrains.com/issue/KT-37720) Replace ArtifactTransform with TransformAction
- [`KT-40559`](https://youtrack.jetbrains.com/issue/KT-40559) Adding the stdlib by default triggers warning in the Android Gradle Plugin

### Tools. Gradle. JS

- [`KT-40093`](https://youtrack.jetbrains.com/issue/KT-40093) Incorrect updating version of Kotlin/JS dependencies in package.json

### Tools. Gradle. Multiplatform

- [`KT-27320`](https://youtrack.jetbrains.com/issue/KT-27320) Provide a way to reuse same sources for similar Native target
- [`KT-40058`](https://youtrack.jetbrains.com/issue/KT-40058) NPE from mpp gradle plugin on kotlinx.benchmarks

### Tools. Gradle. Native

- [`KT-40801`](https://youtrack.jetbrains.com/issue/KT-40801) Gradle CocoaPods integration: Cannot change a framework name


## 1.4-RC

### Backend. Native

- [`KT-40209`](https://youtrack.jetbrains.com/issue/KT-40209) java.lang.UnsupportedOperationException: org.jetbrains.kotlin.ir.declarations.impl.IrValueParameterImpl@76484173
- [`KT-40367`](https://youtrack.jetbrains.com/issue/KT-40367) Kotlin/Native-Swift interop (iOS): Array member initialization failing in release builds

### Compiler

#### Fixes

- [`KT-31025`](https://youtrack.jetbrains.com/issue/KT-31025) Type mismatch when callable reference is resolved with a functional expected type and SAM conversion
- [`KT-37388`](https://youtrack.jetbrains.com/issue/KT-37388) Consider relaxing rules about inferring Nothing inside special constructions (if, try, when)
- [`KT-37717`](https://youtrack.jetbrains.com/issue/KT-37717) NI: "IllegalStateException: Error type encountered" with @BuilderInference
- [`KT-38427`](https://youtrack.jetbrains.com/issue/KT-38427) New inference in branched conditions (if, when) results in odd behavior with inconsistent compiler warnings and runtime errors
- [`KT-38899`](https://youtrack.jetbrains.com/issue/KT-38899) NI: False positive IMPLICIT_NOTHING_TYPE_ARGUMENT_IN_RETURN_POSITION leads to NPE
- [`KT-39468`](https://youtrack.jetbrains.com/issue/KT-39468) NI: overload resolution ambiguity between functions passing `T` and `Foo<T>` with a contravariant receiver
- [`KT-39618`](https://youtrack.jetbrains.com/issue/KT-39618) NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER: unable to infer generic type on @BuilderInference annotated lambda parameter with receiver type
- [`KT-39633`](https://youtrack.jetbrains.com/issue/KT-39633) NI: Incorrect type parameter instantiation based on contravariant type argument
- [`KT-39691`](https://youtrack.jetbrains.com/issue/KT-39691) NI: Missing SAM conversion for nullable lambda
- [`KT-39860`](https://youtrack.jetbrains.com/issue/KT-39860) Make Kotlin binaries publicly available (set KotlinCompilerVersion.IS_PRE_RELEASE = false)
- [`KT-39900`](https://youtrack.jetbrains.com/issue/KT-39900) NI: Incorrect type inference in a lambda returning Unit
- [`KT-39925`](https://youtrack.jetbrains.com/issue/KT-39925) New JVM default compilation mode doesn't check that JVM target is 1.8
- [`KT-39943`](https://youtrack.jetbrains.com/issue/KT-39943) Write information about all-compatibility mode in metadata
- [`KT-39953`](https://youtrack.jetbrains.com/issue/KT-39953) NI: ClassCastException "cannot be cast to java.lang.Void" with if-else in return statement in ANdroid project
- [`KT-40045`](https://youtrack.jetbrains.com/issue/KT-40045) NI: lambda’s receiver type isn't inferred properly
- [`KT-40057`](https://youtrack.jetbrains.com/issue/KT-40057) NI: provideDelegate org.jetbrains.kotlin.codegen.CompilationException: Back-end (JVM) Internal error: wrong bytecode generated for static initializer
- [`KT-40060`](https://youtrack.jetbrains.com/issue/KT-40060) NI: postponed variable from the builder inference flows to back-end and leads to throw an exception
- [`KT-40112`](https://youtrack.jetbrains.com/issue/KT-40112) Kotlin Gradle DSL: COMPATIBILITY_WARNING on `kotlin.sourceSets` block
- [`KT-40113`](https://youtrack.jetbrains.com/issue/KT-40113) Kotlin Gradle DSL: "Expression 'main' cannot be invoked as a function" for `distributions.main` DSL block
- [`KT-40128`](https://youtrack.jetbrains.com/issue/KT-40128) Introduce compiler key to disable compatibility resolution mechanism for new inference features
- [`KT-40151`](https://youtrack.jetbrains.com/issue/KT-40151) NI: postponed variable isn't substituted for top-level CR inside builder inference
- [`KT-40214`](https://youtrack.jetbrains.com/issue/KT-40214) AbstractMethodError in gradle subplugin which is used in Android app
- [`KT-40234`](https://youtrack.jetbrains.com/issue/KT-40234) Deprecation level "hidden" has no effect on callable reference argument
- [`KT-40247`](https://youtrack.jetbrains.com/issue/KT-40247) NI: false positive "function should be called from coroutine or another suspend function" for suspend invoke operator in try-catch
- [`KT-40254`](https://youtrack.jetbrains.com/issue/KT-40254) Rewrite at slice with two callable reference arguments
- [`KT-40269`](https://youtrack.jetbrains.com/issue/KT-40269) NI: "disabled Unit conversions" error has appeared on green code
- [`KT-40337`](https://youtrack.jetbrains.com/issue/KT-40337) NI: false positive "function should be called from coroutine or another suspend function" for suspend invoke operator in `when` block
- [`KT-40406`](https://youtrack.jetbrains.com/issue/KT-40406) Prohibit reflection on adapted callable references

### IDE

- [`KT-39968`](https://youtrack.jetbrains.com/issue/KT-39968) Paths in KotlinJavaRuntime library aren't updated after you run IDE from a different directory
- [`KT-39989`](https://youtrack.jetbrains.com/issue/KT-39989) NullPointerException when opening Kotlin facets in Project Structure dialog in IDEA 192
- [`KT-40311`](https://youtrack.jetbrains.com/issue/KT-40311) Create change_notes for 1.4 in IDE plugin description

### IDE. Debugger

- [`KT-39808`](https://youtrack.jetbrains.com/issue/KT-39808) (CoroutineDebugger) Doesn't start with kotlinx-coroutines-core >= 1.3.6
- [`KT-40073`](https://youtrack.jetbrains.com/issue/KT-40073) (CoroutineDebugger) Change minimum supported kotlinx.coroutines version to 1.3.8*
- [`KT-40172`](https://youtrack.jetbrains.com/issue/KT-40172) Restored frame variables isn't shown for suspended coroutines
- [`KT-40635`](https://youtrack.jetbrains.com/issue/KT-40635) Coroutines Debugger: make IDE plugin accept coroutines 1.3.8-rc* versions as well

### IDE. Gradle Integration

- [`KT-38744`](https://youtrack.jetbrains.com/issue/KT-38744) No dependency between Android `test` and commonTest source sets with kotlin.mpp.enableGranularSourceSetsMetadata=true
- [`KT-39037`](https://youtrack.jetbrains.com/issue/KT-39037) 'None of the consumable configurations have attributes' in MPP IDE import with transitive project dependency on self

### IDE. Gradle. Script

- [`KT-31137`](https://youtrack.jetbrains.com/issue/KT-31137) IntelliJ would get very slow when editing gradle buildSrc(using the kotlin dsl)
- [`KT-36078`](https://youtrack.jetbrains.com/issue/KT-36078) Gradle Kotlin script context is not reloaded when gradle/wrapper/gradle-wrapper.properties file is changed
- [`KT-39317`](https://youtrack.jetbrains.com/issue/KT-39317) ISE “Calling invokeAndWait from read-action leads to possible deadlock.” on importing simple Gradle-based project in nightly IJ

### IDE. Inspections and Intentions

- [`KT-28662`](https://youtrack.jetbrains.com/issue/KT-28662) Inspection to flag usage of the wrong Transient annotation on Kotlin Serializable class
- [`KT-34209`](https://youtrack.jetbrains.com/issue/KT-34209) Switch default behaviour in 1.4 for insertion (to build script) via quick fix of the compiler option enabling inline classes
- [`KT-36131`](https://youtrack.jetbrains.com/issue/KT-36131) Suggest to add a missing module dependency on an unresolved reference in Kotlin code
- [`KT-37462`](https://youtrack.jetbrains.com/issue/KT-37462) Add "Add dependency to module" quickfix in multimodule Maven project
- [`KT-39869`](https://youtrack.jetbrains.com/issue/KT-39869) Add whole project migration usages of kotlin.browser.* & kotlin.dom.* to kotlinx.browser.* & kotlinx.dom.* respectively

### IDE. Wizards

- [`KT-40004`](https://youtrack.jetbrains.com/issue/KT-40004) New Project wizard 1.4+: no `https://dl.bintray.com/kotlin/kotlinx` repository is added for kotlinx-html
- [`KT-40037`](https://youtrack.jetbrains.com/issue/KT-40037) New Project wizard: update Ktor version
- [`KT-40092`](https://youtrack.jetbrains.com/issue/KT-40092) Wizard: the templates panel on mac OS is too wide
- [`KT-40232`](https://youtrack.jetbrains.com/issue/KT-40232) New Wizard: Android Sdk path doesn't have backslash escaping on Windows
- [`KT-40371`](https://youtrack.jetbrains.com/issue/KT-40371) New Project Wizard: Frontend Application / Library results in broken run configuration
- [`KT-40377`](https://youtrack.jetbrains.com/issue/KT-40377) New Project Wizard: Frontend Application defines NPM dependencies that are unnecessary with Kotlin 1.4-RC+
- [`KT-40378`](https://youtrack.jetbrains.com/issue/KT-40378) New Project Wizard: Frontend Application, Disabling JavaScript test framework has no effect
- [`KT-40407`](https://youtrack.jetbrains.com/issue/KT-40407) Wizard: do not add stdlib by default for Gradle projects in wizard

### JS. Tools

- [`KT-39984`](https://youtrack.jetbrains.com/issue/KT-39984) Update dukat version in toolchain near to release of 1.4-RC

### JavaScript

- [`KT-32186`](https://youtrack.jetbrains.com/issue/KT-32186) Make sure K/JS Reflection API documentation is correct and fix it.
- [`KT-37563`](https://youtrack.jetbrains.com/issue/KT-37563) K/JS: stacktrace is not captured for exceptions without primary constructor inherited from Exception/Error
- [`KT-37752`](https://youtrack.jetbrains.com/issue/KT-37752) Generated typescript incorrect for constructors of derived classes
- [`KT-37883`](https://youtrack.jetbrains.com/issue/KT-37883) KJS: Generated TypeScript uses 'declare' rather than 'export'
- [`KT-38771`](https://youtrack.jetbrains.com/issue/KT-38771) JS: support non-reified type parameters in typeOf
- [`KT-39873`](https://youtrack.jetbrains.com/issue/KT-39873) Update Kotlin JavaScript wrappers due to NON_EXPORTABLE_TYPE diagnostic introduction
- [`KT-40126`](https://youtrack.jetbrains.com/issue/KT-40126) [JS / IR] NPE while compiling interfaces with invoke which is passed as a delegate
- [`KT-40216`](https://youtrack.jetbrains.com/issue/KT-40216) KJS / IR:  AssertionError caused by an anonymous object in the dependency project

### Libraries

- [`KT-33069`](https://youtrack.jetbrains.com/issue/KT-33069) StringBuilder common functions
- [`KT-35972`](https://youtrack.jetbrains.com/issue/KT-35972) Add contract to builder functions
- [`KT-37101`](https://youtrack.jetbrains.com/issue/KT-37101) Mark following api with DeprecatedSinceKotlin("1.4")
- [`KT-38360`](https://youtrack.jetbrains.com/issue/KT-38360) Make sure that JB libraries correctly define their npm deps and republish them (after KT-30619)
- [`KT-38817`](https://youtrack.jetbrains.com/issue/KT-38817) 'capitalize' should convert digraphs to title case
- [`KT-40168`](https://youtrack.jetbrains.com/issue/KT-40168) Remove StringBuilder.capacity from common and JS parts

### Middle-end. IR

- [`KT-40520`](https://youtrack.jetbrains.com/issue/KT-40520) Assert during fake-override generation

### Tools. Commonizer

- [`KT-40199`](https://youtrack.jetbrains.com/issue/KT-40199) Commonizer loses nullability of abbreviated types

### Tools. Compiler Plugins

- [`KT-40036`](https://youtrack.jetbrains.com/issue/KT-40036) Add diagnostic that shows is serialization plugin compatible with serialization-runtime

### Tools. Gradle

- [`KT-39755`](https://youtrack.jetbrains.com/issue/KT-39755) [KJS / Gradle / Legacy mode] Directory with whitespace is not processed
- [`KT-39809`](https://youtrack.jetbrains.com/issue/KT-39809) Kotlin Gradle plugin: ServiceConfigurationError: org.jetbrains.kotlin.gradle.plugin.KotlinGradleSubplugin: Provider org.jetbrains.kotlin.gradle.internal.AndroidSubplugin not a subtype
- [`KT-39977`](https://youtrack.jetbrains.com/issue/KT-39977) Collect statistics of used -Xjvm-default options
- [`KT-40300`](https://youtrack.jetbrains.com/issue/KT-40300) Fail the build if in MPP plugin no targets configured

### Tools. Gradle. JS

- [`KT-38170`](https://youtrack.jetbrains.com/issue/KT-38170) Investigate how to improve migration experience from pre 1.4.0 DSL to the new one
- [`KT-39654`](https://youtrack.jetbrains.com/issue/KT-39654) Default CSS settings in webpack has priority over user's settings
- [`KT-39842`](https://youtrack.jetbrains.com/issue/KT-39842) Kotlin/JS Gradle DSL: peerNpm dependency fails
- [`KT-40048`](https://youtrack.jetbrains.com/issue/KT-40048) [Gradle, JS] Deprecate kotlin-frontend plugin
- [`KT-40067`](https://youtrack.jetbrains.com/issue/KT-40067) [Gradle, JS] Public package.json for mixed JS/TS project
- [`KT-40210`](https://youtrack.jetbrains.com/issue/KT-40210) Fail the build when Kotlin/JS target is not (properly) configured
- [`KT-40320`](https://youtrack.jetbrains.com/issue/KT-40320) Gradle JS: make migration to the new Gradle DSL smoother

### Tools. Gradle. Multiplatform

- [`KT-32239`](https://youtrack.jetbrains.com/issue/KT-32239) Custom configurations inside Kotlin JVM Gradle projects can't properly resolve multiplatform dependencies
- [`KT-39897`](https://youtrack.jetbrains.com/issue/KT-39897) [Commonizer] Fast-pass for library fragments absent for some targets

### Tools. J2K

- [`KT-39739`](https://youtrack.jetbrains.com/issue/KT-39739) J2K: Access is allowed from event dispatch thread with IW lock only

### Tools. kapt

- [`KT-34604`](https://youtrack.jetbrains.com/issue/KT-34604) KAPT: Flaky NPE through `org.jetbrains.kotlin.kapt3.base.ProcessorLoader.doLoadProcessors`
- [`KT-36302`](https://youtrack.jetbrains.com/issue/KT-36302) TypeTreeVisitor.visitMemberSelect IllegalStateException: node.sym must not be null on JDK 11
- [`KT-39876`](https://youtrack.jetbrains.com/issue/KT-39876) KAPT: Serialization of classpath structure is incorrect if there are dependencies between types in jar/dir


## 1.4-M3

### Compiler

#### New Features

- [`KT-23575`](https://youtrack.jetbrains.com/issue/KT-23575) Deprecate with replacement and SinceKotlin
- [`KT-38652`](https://youtrack.jetbrains.com/issue/KT-38652) Do not generate optional annotations to class files on JVM
- [`KT-38777`](https://youtrack.jetbrains.com/issue/KT-38777) Hide Throwable.addSuppressed member and prefer extension instead

#### Performance Improvements

- [`KT-38489`](https://youtrack.jetbrains.com/issue/KT-38489) Compilation of kotlin html DSL increasingly slow
- [`KT-28650`](https://youtrack.jetbrains.com/issue/KT-28650) Type inference for argument type is very slow if several interfaces with a type parameter is used as an upper bound of a type parameter

#### Fixes

- [`KT-15971`](https://youtrack.jetbrains.com/issue/KT-15971) Incorrect bytecode generated when inheriting default arguments not from the first supertype
- [`KT-25290`](https://youtrack.jetbrains.com/issue/KT-25290) NI: "AssertionError: If original type is SAM type, then candidate should have same type constructor" on out projection of Java class
- [`KT-28672`](https://youtrack.jetbrains.com/issue/KT-28672) Contracts on calls with implicit receivers
- [`KT-30279`](https://youtrack.jetbrains.com/issue/KT-30279) Support non-reified type parameters in typeOf
- [`KT-31908`](https://youtrack.jetbrains.com/issue/KT-31908) NI: CCE on passing lambda to function which accepts vararg SAM interface
- [`KT-32156`](https://youtrack.jetbrains.com/issue/KT-32156) New inference issue with generics
- [`KT-32229`](https://youtrack.jetbrains.com/issue/KT-32229) New inference algorithm not taking into account the upper bound class
- [`KT-33455`](https://youtrack.jetbrains.com/issue/KT-33455) Override equals/hashCode in functional interface wrappers
- [`KT-34902`](https://youtrack.jetbrains.com/issue/KT-34902) AnalyzerException: Argument 1: expected I, but found R for unsigned types in generic data class
- [`KT-35075`](https://youtrack.jetbrains.com/issue/KT-35075) AssertionError: "No resolved call for ..." with conditional function references
- [`KT-35468`](https://youtrack.jetbrains.com/issue/KT-35468) Overcome ambiguity between typealias kotlin.Throws and the aliased type kotlin.jvm.Throws
- [`KT-35494`](https://youtrack.jetbrains.com/issue/KT-35494) NI: Multiple duplicate error diagnostics (in IDE popup) with NULL_FOR_NONNULL_TYPE
- [`KT-35681`](https://youtrack.jetbrains.com/issue/KT-35681) Wrong common supertype between raw and integer literal type leads to unsound code
- [`KT-35937`](https://youtrack.jetbrains.com/issue/KT-35937) Error "Declaration has several compatible actuals" on incremental build
- [`KT-36013`](https://youtrack.jetbrains.com/issue/KT-36013) Functional interface conversion not happens on a konstue of functional type with smart cast to a relevant functional type
- [`KT-36045`](https://youtrack.jetbrains.com/issue/KT-36045) Do not depend on the order of lambda arguments to coerce result to `Unit`
- [`KT-36448`](https://youtrack.jetbrains.com/issue/KT-36448) NI: fix tests after enabling NI in the compiler
- [`KT-36706`](https://youtrack.jetbrains.com/issue/KT-36706) Prohibit functional interface constructor references
- [`KT-36969`](https://youtrack.jetbrains.com/issue/KT-36969) Generate @NotNull on instance parameters of Interface$DefaultImpls methods
- [`KT-37058`](https://youtrack.jetbrains.com/issue/KT-37058) Incorrect overload resolution ambiguity on callable reference in a conditional expression with new inference
- [`KT-37120`](https://youtrack.jetbrains.com/issue/KT-37120) [FIR] False UNRESOLVED_REFERENCE for public and protected member functions and properties which are declared in object inner class
- [`KT-37149`](https://youtrack.jetbrains.com/issue/KT-37149) Conversion when generic specified by type argument of SAM type
- [`KT-37249`](https://youtrack.jetbrains.com/issue/KT-37249) false TYPE_MISMATCH when When-expression branches have try-catch blocks
- [`KT-37341`](https://youtrack.jetbrains.com/issue/KT-37341) NI: Type mismatch with combination of lambda and function reference
- [`KT-37436`](https://youtrack.jetbrains.com/issue/KT-37436) AME: "Receiver class does not define or inherit an implementation of the resolved method" in runtime on usage of non-abstract method of fun interface
- [`KT-37510`](https://youtrack.jetbrains.com/issue/KT-37510) NI infers `java.lang.Void` from the expression in a lazy property delegate and throws ClassCastException at runtime
- [`KT-37541`](https://youtrack.jetbrains.com/issue/KT-37541) SAM conversion with fun interface without a function fails on compiling and IDE analysis in SamAdapterFunctionsScope.getSamConstructor()
- [`KT-37574`](https://youtrack.jetbrains.com/issue/KT-37574) NI: Type mismatch with Kotlin object extending functional type passed as @FunctionalInterface to Java
- [`KT-37630`](https://youtrack.jetbrains.com/issue/KT-37630) NI: ILT suitability in a call is broken if there are CST calculation and calling function's type parameters
- [`KT-37665`](https://youtrack.jetbrains.com/issue/KT-37665) NI: applicability error due to implicitly inferred Nothing for returning T with expected type
- [`KT-37712`](https://youtrack.jetbrains.com/issue/KT-37712) No extension receiver in functional interface created with lambda
- [`KT-37715`](https://youtrack.jetbrains.com/issue/KT-37715) NI: VerifyError: Bad type on operand stack with varargs generic konstue when type is inferred
- [`KT-37721`](https://youtrack.jetbrains.com/issue/KT-37721) NI: Function reference with vararg parameter treated as array and missing default parameter is rejected
- [`KT-37887`](https://youtrack.jetbrains.com/issue/KT-37887) NI: Smart casting for Map doesn't work if the variable is already  "smart casted"
- [`KT-37914`](https://youtrack.jetbrains.com/issue/KT-37914) NI: broken inference for a casting to subtype function within the common constraint system with this subtype
- [`KT-37952`](https://youtrack.jetbrains.com/issue/KT-37952) NI: improve lambdas completion through separation the lambdas analysis into several steps
- [`KT-38069`](https://youtrack.jetbrains.com/issue/KT-38069) Callable reference adaptation should have dependency on API version 1.4
- [`KT-38143`](https://youtrack.jetbrains.com/issue/KT-38143) New type inference fails when calling extension function defined on generic type with type arguments nested too deep
- [`KT-38156`](https://youtrack.jetbrains.com/issue/KT-38156) FIR Metadata generation
- [`KT-38197`](https://youtrack.jetbrains.com/issue/KT-38197) java.lang.OutOfMemoryError: Java heap space: failed reallocation of scalar replaced objects
- [`KT-38259`](https://youtrack.jetbrains.com/issue/KT-38259) NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER for provideDelegate
- [`KT-38337`](https://youtrack.jetbrains.com/issue/KT-38337) Map delegation fails for inline classes
- [`KT-38401`](https://youtrack.jetbrains.com/issue/KT-38401) FIR: protected effective visibility is handled unprecisely
- [`KT-38416`](https://youtrack.jetbrains.com/issue/KT-38416) FIR: infinite loop in BB coroutine test 'overrideDefaultArgument.kt'
- [`KT-38432`](https://youtrack.jetbrains.com/issue/KT-38432) FIR: incorrect effective visibility in anonymous object
- [`KT-38434`](https://youtrack.jetbrains.com/issue/KT-38434) Implement resolution of suspend-conversion on FE only, but give error if suspend conversion is called
- [`KT-38437`](https://youtrack.jetbrains.com/issue/KT-38437) [FIR] String(CharArray) is resolved to java.lang.String constructor instead of kotlin.text.String pseudo-constructor
- [`KT-38439`](https://youtrack.jetbrains.com/issue/KT-38439) NI: anonymous functions without receiver is allowed if there is an expected type with receiver
- [`KT-38473`](https://youtrack.jetbrains.com/issue/KT-38473) FIR: ConeIntegerLiteralType in signature
- [`KT-38537`](https://youtrack.jetbrains.com/issue/KT-38537) IllegalArgumentException: "marginPrefix must be non-blank string" with raw strings and space as margin prefix in trimMargin() call
- [`KT-38604`](https://youtrack.jetbrains.com/issue/KT-38604) Implicit suspend conversion on call arguments doesn't work on vararg elements
- [`KT-38680`](https://youtrack.jetbrains.com/issue/KT-38680) NSME when calling generic interface method with default parameters overriden with inline class type argument
- [`KT-38681`](https://youtrack.jetbrains.com/issue/KT-38681) Wrong bytecode generated when calling generic interface method with default parameters overriden with primitive type argument
- [`KT-38691`](https://youtrack.jetbrains.com/issue/KT-38691) NI: overload resolution ambiguity if take `R` and `() -> R`, and pass literal lambda, which returns `R`
- [`KT-38799`](https://youtrack.jetbrains.com/issue/KT-38799) False positive USELESS_CAST for lambda parameter
- [`KT-38802`](https://youtrack.jetbrains.com/issue/KT-38802) Generated code crashes by ClassCastException when delegating with inline class
- [`KT-38853`](https://youtrack.jetbrains.com/issue/KT-38853) Backend Internal error: Error type encountered: Unresolved type for nested class used in an annotation argument on an interface method
- [`KT-38890`](https://youtrack.jetbrains.com/issue/KT-38890) NI: false negative Type mismatch for konstues with fun keyword
- [`KT-39010`](https://youtrack.jetbrains.com/issue/KT-39010) NI: Regression with false-positive smartcast on var of generic type
- [`KT-39013`](https://youtrack.jetbrains.com/issue/KT-39013) 202, ASM 8: "AnalyzerException: Execution can fall off the end of the code"
- [`KT-39260`](https://youtrack.jetbrains.com/issue/KT-39260) "AssertionError: Unsigned type expected: Int" in range
- [`KT-39305`](https://youtrack.jetbrains.com/issue/KT-39305) NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER: unable to infer deeply nested type bound when class implements generic interface
- [`KT-39408`](https://youtrack.jetbrains.com/issue/KT-39408) Using unsigned arrays as generics fails in 1.4-M2 with class cast exception
- [`KT-39533`](https://youtrack.jetbrains.com/issue/KT-39533) NI: Wrong overload resolution for methods with SAM converted function reference arguments
- [`KT-39535`](https://youtrack.jetbrains.com/issue/KT-39535) NI: Inference fails for the parameters of SAM converted lambdas with type parameters
- [`KT-39603`](https://youtrack.jetbrains.com/issue/KT-39603) Require explicit override in JVM default compatibility mode on implicit generic specialization of inherited methods in classes
- [`KT-39671`](https://youtrack.jetbrains.com/issue/KT-39671) Couldn't inline method call 'expectBody'
- [`KT-39816`](https://youtrack.jetbrains.com/issue/KT-39816) NI:ClassCastException and no IDE error with provideDelegate when DELEGATE_SPECIAL_FUNCTION_MISSING in OI
- [`KT-32779`](https://youtrack.jetbrains.com/issue/KT-32779) `Rewrite at slice` in array access resolution in coroutine inference
- [`KT-39387`](https://youtrack.jetbrains.com/issue/KT-39387) Can't build Kotlin project due to overload resolution ambiguity on flatMap calls
- [`KT-39229`](https://youtrack.jetbrains.com/issue/KT-39229) NI: resolution to wrong candidate (SAM-type against similar functional type)

### Docs & Examples

- [`KT-36245`](https://youtrack.jetbrains.com/issue/KT-36245) Document that @kotlin.native.ThreadLocal annotation doesn't work anywhere except in Kotlin/Native
- [`KT-37943`](https://youtrack.jetbrains.com/issue/KT-37943) Conflicting overloads in the factory functions sample code in Coding Conventions Page

### IDE

#### New Features

- [`KT-10974`](https://youtrack.jetbrains.com/issue/KT-10974) Add Code Style: Import Layout Configuration Table
- [`KT-39065`](https://youtrack.jetbrains.com/issue/KT-39065) "Join lines" should remove trailing comma on call site

#### Fixes

- [`KT-9065`](https://youtrack.jetbrains.com/issue/KT-9065) Wrong result when move statement through if block with call with lambda
- [`KT-14757`](https://youtrack.jetbrains.com/issue/KT-14757) Move statement up breaks code in function parameter list
- [`KT-14946`](https://youtrack.jetbrains.com/issue/KT-14946) Move statement up/down (with Ctrl+Shift+Up/Down) messes with empty lines
- [`KT-15143`](https://youtrack.jetbrains.com/issue/KT-15143) Kotlin: Colors&Fonts -> "Enum entry" should use Language Default -> Classes - Static field
- [`KT-17887`](https://youtrack.jetbrains.com/issue/KT-17887) Moving statement (Ctrl/Cmd+Shift+Down) messes with use block
- [`KT-34187`](https://youtrack.jetbrains.com/issue/KT-34187) UAST cannot get type of array access
- [`KT-34524`](https://youtrack.jetbrains.com/issue/KT-34524) "PSI and index do not match" and IDE freeze with library import from `square/workflow`
- [`KT-35574`](https://youtrack.jetbrains.com/issue/KT-35574) UAST: UBreakExpression in when expression should be UYieldExpression
- [`KT-36801`](https://youtrack.jetbrains.com/issue/KT-36801) IDE: Unsupported language version konstue is represented with "latest stable" in GUI
- [`KT-37378`](https://youtrack.jetbrains.com/issue/KT-37378) Remove IDE option "Enable new type inference algorithm..." in 1.4
- [`KT-38003`](https://youtrack.jetbrains.com/issue/KT-38003) "Analyze Data Flow from Here" should work on parameter of abstract method
- [`KT-38173`](https://youtrack.jetbrains.com/issue/KT-38173) Reified types do no have extends information
- [`KT-38217`](https://youtrack.jetbrains.com/issue/KT-38217) Make Kotlin plugin settings searchable
- [`KT-38247`](https://youtrack.jetbrains.com/issue/KT-38247) "IncorrectOperationException: Incorrect expression" through UltraLightUtils.kt: inlined string is not escaped before parsing
- [`KT-38293`](https://youtrack.jetbrains.com/issue/KT-38293) Throwable: "'codestyle.name.kotlin' is not found in java.util.PropertyResourceBundle" at KotlinLanguageCodeStyleSettingsProvider.getConfigurableDisplayName()
- [`KT-38407`](https://youtrack.jetbrains.com/issue/KT-38407) Drop components from plugin.xml
- [`KT-38443`](https://youtrack.jetbrains.com/issue/KT-38443) No error on change in property initializer
- [`KT-38521`](https://youtrack.jetbrains.com/issue/KT-38521) ISE: Loop in parent structure when converting a DOT_QUALIFIED_EXPRESSION with parent ANNOTATED_EXPRESSION
- [`KT-38571`](https://youtrack.jetbrains.com/issue/KT-38571) Rework deprecated EPs
- [`KT-38632`](https://youtrack.jetbrains.com/issue/KT-38632) Change the code style to official in tests

### IDE. Code Style, Formatting

#### Fixes

- [`KT-24750`](https://youtrack.jetbrains.com/issue/KT-24750) Formatter: Minimum blank lines after class header does nothing
- [`KT-31169`](https://youtrack.jetbrains.com/issue/KT-31169) IDEA settings search fails to find "Tabs and Indents" tab in Kotlin code style settings
- [`KT-35359`](https://youtrack.jetbrains.com/issue/KT-35359) Incorrect indent for multiline expression in string template
- [`KT-37420`](https://youtrack.jetbrains.com/issue/KT-37420) Add setting to disable inserting empty line between declaration and declaration with comment
- [`KT-37891`](https://youtrack.jetbrains.com/issue/KT-37891) Formatter inserts empty lines between annotated properties
- [`KT-38036`](https://youtrack.jetbrains.com/issue/KT-38036) Use trailing comma setting does not apply to code example in Settings dialog
- [`KT-38568`](https://youtrack.jetbrains.com/issue/KT-38568) False positive: weak warning "Missing line break" on -> in when expression
- [`KT-39024`](https://youtrack.jetbrains.com/issue/KT-39024) Add option for blank lines before declaration with comment or annotation on separate line
- [`KT-39079`](https://youtrack.jetbrains.com/issue/KT-39079) Trailing comma: add base support for call site
- [`KT-39123`](https://youtrack.jetbrains.com/issue/KT-39123) Option `Align 'when' branches in columns` does nothing
- [`KT-39180`](https://youtrack.jetbrains.com/issue/KT-39180) Move trailing comma settings in Other tab

### IDE. Completion

- [`KT-18538`](https://youtrack.jetbrains.com/issue/KT-18538) Completion of static members of grand-super java class inserts unnecessary qualifier
- [`KT-38445`](https://youtrack.jetbrains.com/issue/KT-38445) Fully qualified class name is used instead after insertion of `delay` method

### IDE. Debugger

#### Fixes

- [`KT-14057`](https://youtrack.jetbrains.com/issue/KT-14057) Debugger couldn't step into Reader.read
- [`KT-14828`](https://youtrack.jetbrains.com/issue/KT-14828) Bad step into/over behavior for functions with default parameters
- [`KT-36403`](https://youtrack.jetbrains.com/issue/KT-36403) Method breakpoints don't work for libraries
- [`KT-36404`](https://youtrack.jetbrains.com/issue/KT-36404) Ekonstuate: "AssertionError: Argument expression is not saved for a SAM constructor"
- [`KT-37486`](https://youtrack.jetbrains.com/issue/KT-37486) Kotlin plugin keeps reference to stream debugger support classes after stream debugger plugin is disabled
- [`KT-38484`](https://youtrack.jetbrains.com/issue/KT-38484) Coroutines Debugger: IAE “Requested element count -1 is less than zero.” is thrown by calling dumpCoroutines
- [`KT-38606`](https://youtrack.jetbrains.com/issue/KT-38606) Coroutine Debugger: OCE from org.jetbrains.kotlin.idea.debugger.coroutine.proxy.mirror.BaseMirror.isCompatible
- [`KT-39143`](https://youtrack.jetbrains.com/issue/KT-39143) NPE on setCurrentStackFrame to Kotlin inner compiled class content
- [`KT-39412`](https://youtrack.jetbrains.com/issue/KT-39412) Failed to find Premain-Class manifest attribute when debugging main method with ktor
- [`KT-39634`](https://youtrack.jetbrains.com/issue/KT-39634) (CoroutineDebugger) Agent doesn't start if using kotlinx-coroutines-core only dependency
- [`KT-39648`](https://youtrack.jetbrains.com/issue/KT-39648) Coroutines debugger doesn't see stacktraces in case of the project has kotlinx-coroutines-debug dependency

### IDE. Gradle Integration

#### Performance Improvements

- [`KT-39059`](https://youtrack.jetbrains.com/issue/KT-39059) Poor performance of `modifyDependenciesOnMppModules`

#### Fixes

- [`KT-35921`](https://youtrack.jetbrains.com/issue/KT-35921) Gradle Import fails with "Unsupported major.minor version 52.0" on pure Java project in case "Gradle JDK" is lower 1.8 and Kotlin plugin is enabled
- [`KT-36673`](https://youtrack.jetbrains.com/issue/KT-36673) Gradle Project importing: move ModelBuilders and ModelProviders to kotlin-gradle-tooling jar
- [`KT-36792`](https://youtrack.jetbrains.com/issue/KT-36792) IDEA 2020.1: Some module->module dependencies in HMPP project are missed after import from Gradle
- [`KT-37125`](https://youtrack.jetbrains.com/issue/KT-37125) Imported modules structure for MPP project is displayed messy in UI in IDEA 2020.1
- [`KT-37428`](https://youtrack.jetbrains.com/issue/KT-37428) NPE at KotlinFacetSettings.setLanguageLevel() on the first project import
- [`KT-38706`](https://youtrack.jetbrains.com/issue/KT-38706) IDE Gradle import creates 4 JavaScript modules for MPP source sets with BOTH compiler type
- [`KT-38767`](https://youtrack.jetbrains.com/issue/KT-38767) Published hierarchical multiplatform library symbols are unresolved in IDE (master)
- [`KT-38842`](https://youtrack.jetbrains.com/issue/KT-38842) False positive [INVISIBLE_MEMBER] for `internal` declaration of commonMain called from commonTest
- [`KT-39213`](https://youtrack.jetbrains.com/issue/KT-39213) IDE: references from MPP project to JavaScript library are unresolved, when project and library are compiled with "both" mode
- [`KT-39657`](https://youtrack.jetbrains.com/issue/KT-39657) Language settings for intermediate source-sets are lost during import

### IDE. Gradle. Script

#### New Features

- [`KT-34481`](https://youtrack.jetbrains.com/issue/KT-34481) `*.gradle.kts`: use Intellij IDEA Gradle project sync mechanics for updating script configuration

#### Performance Improvements

- [`KT-34138`](https://youtrack.jetbrains.com/issue/KT-34138) Deadlock in `ScriptTemplatesFromDependenciesProvider`
- [`KT-38875`](https://youtrack.jetbrains.com/issue/KT-38875) Deadlock in ScriptClassRootsUpdater.checkInkonstidSdks

#### Fixes

- [`KT-34265`](https://youtrack.jetbrains.com/issue/KT-34265) Bogus "build configuration failed, run 'gradle tasks' for more information" message and other issues related to "script dependencies"
- [`KT-34444`](https://youtrack.jetbrains.com/issue/KT-34444) *.gradle.kts: special storage of all scripts configuration on one file
- [`KT-35153`](https://youtrack.jetbrains.com/issue/KT-35153) build.gradle.kts: scripts in removed subproject remain imported, but shouldn't
- [`KT-35573`](https://youtrack.jetbrains.com/issue/KT-35573) Request for gradle build script configuration only after explicit click on notification
- [`KT-36675`](https://youtrack.jetbrains.com/issue/KT-36675) move .gradle.kts ModelBuilders and ModelProviders to kotlin-gradle-tooling jar
- [`KT-37178`](https://youtrack.jetbrains.com/issue/KT-37178) build.gradle.kts: Rework the notification for scripts out of project
- [`KT-37631`](https://youtrack.jetbrains.com/issue/KT-37631) Unnecessary loading dependencies after opening build.gradle.kts after project import with Gradle 6
- [`KT-37863`](https://youtrack.jetbrains.com/issue/KT-37863) Scanning dependencies for script definitions takes too long or indefinitely during Gradle import
- [`KT-38296`](https://youtrack.jetbrains.com/issue/KT-38296) MISSING_DEPENDENCY_SUPERCLASS in the build.gradle.kts editor while Gradle runs Ok
- [`KT-38541`](https://youtrack.jetbrains.com/issue/KT-38541) "Inkonstid file" exception in ScriptChangeListener.getAnalyzableKtFileForScript()
- [`KT-39104`](https://youtrack.jetbrains.com/issue/KT-39104) “Gradle Kotlin DSL script configuration is missing” after importing project in IJ201, Gradle 6.3
- [`KT-39469`](https://youtrack.jetbrains.com/issue/KT-39469) Gradle version is not updated in script dependencies if the version of gradle was changed in gradle-wrapper.properties
- [`KT-39771`](https://youtrack.jetbrains.com/issue/KT-39771) Freeze 30s from org.jetbrains.kotlin.scripting.resolve.ApiChangeDependencyResolverWrapper.resolve on loading script configuration with Gradle 5.6.4

### IDE. Inspections and Intentions

#### New Features

- [`KT-14884`](https://youtrack.jetbrains.com/issue/KT-14884) Intention to add missing "class" keyword for enum and annotation top-level declarations
- [`KT-17209`](https://youtrack.jetbrains.com/issue/KT-17209) Provide intention to fix platform declaration clash (CONFLICTING_JVM_DECLARATIONS)
- [`KT-24522`](https://youtrack.jetbrains.com/issue/KT-24522) Suggest to move typealias outside the class
- [`KT-30263`](https://youtrack.jetbrains.com/issue/KT-30263) Detect redundant conversions of unsigned types
- [`KT-35893`](https://youtrack.jetbrains.com/issue/KT-35893) Support Inspection for unnecessary asSequence() call
- [`KT-38559`](https://youtrack.jetbrains.com/issue/KT-38559) "Change JVM name" (@JvmName) quickfix: improve name suggester for generic functions
- [`KT-38597`](https://youtrack.jetbrains.com/issue/KT-38597) Expand Boolean intention
- [`KT-38982`](https://youtrack.jetbrains.com/issue/KT-38982) Add "Logger initialized with foreign class" inspection
- [`KT-39131`](https://youtrack.jetbrains.com/issue/KT-39131) TrailingCommaInspection: should suggest fixes for call-site without warnings

#### Fixes

- [`KT-5271`](https://youtrack.jetbrains.com/issue/KT-5271) Missing QuickFix for Multiple supertypes available
- [`KT-11865`](https://youtrack.jetbrains.com/issue/KT-11865) "Create secondary constructor" quick fix always inserts parameter-less call to `this()`
- [`KT-14021`](https://youtrack.jetbrains.com/issue/KT-14021) Quickfix to add parameter to function gives strange name to parameter
- [`KT-17121`](https://youtrack.jetbrains.com/issue/KT-17121) "Implement members" quick fix is not suggested
- [`KT-17368`](https://youtrack.jetbrains.com/issue/KT-17368) Don't highlight members annotated with @JsName as unused
- [`KT-20795`](https://youtrack.jetbrains.com/issue/KT-20795) "replace explicit parameter with it" creates inkonstid code in case of overload ambiguities
- [`KT-22014`](https://youtrack.jetbrains.com/issue/KT-22014) Intention "convert lambda to reference" should be available for implicit 'this'
- [`KT-22015`](https://youtrack.jetbrains.com/issue/KT-22015) Intention "Convert lambda to reference" should be available in spite of the lambda in or out of parentheses
- [`KT-22142`](https://youtrack.jetbrains.com/issue/KT-22142) Intentions: "Convert to primary constructor" changes semantics for property with custom setter
- [`KT-22878`](https://youtrack.jetbrains.com/issue/KT-22878) Empty argument list at the call site of custom function named "suspend" shouldn't be reported as unnecessary
- [`KT-24281`](https://youtrack.jetbrains.com/issue/KT-24281) Importing of invoke() from the same file is reported as unused even if it isn't
- [`KT-25050`](https://youtrack.jetbrains.com/issue/KT-25050) False-positive inspection "Call replaceable with binary operator" for 'equals'
- [`KT-26361`](https://youtrack.jetbrains.com/issue/KT-26361) @Deprecated "ReplaceWith" quickfix inserts 'this' incorrectly when using function imports
- [`KT-27651`](https://youtrack.jetbrains.com/issue/KT-27651) 'Condition is always true' inspection should not be triggered when the condition has references to a named constant
- [`KT-29934`](https://youtrack.jetbrains.com/issue/KT-29934) False negative `Change type` quickfix on primary constructor override konst parameter when it has wrong type
- [`KT-31682`](https://youtrack.jetbrains.com/issue/KT-31682) 'Convert lambda to reference' intention inside class with function which return object produces uncompilable code
- [`KT-31760`](https://youtrack.jetbrains.com/issue/KT-31760) Implement Abstract Function/Property intentions position generated member improperly
- [`KT-32511`](https://youtrack.jetbrains.com/issue/KT-32511) Create class quick fix is not suggested in super type list in case of missing primary constructor
- [`KT-32565`](https://youtrack.jetbrains.com/issue/KT-32565) False positive "Variable is the same as 'credentials' and should be inlined" with object declared and returned from lambda
- [`KT-32801`](https://youtrack.jetbrains.com/issue/KT-32801) False positive "Call on collection type may be reduced" with mapNotNull, generic lambda block and new inference
- [`KT-33951`](https://youtrack.jetbrains.com/issue/KT-33951) ReplaceWith quickfix with unqualified object member call doesn't substitute argument for parameter
- [`KT-34378`](https://youtrack.jetbrains.com/issue/KT-34378) "Convert lambda to reference" refactoring does not work for suspend functions
- [`KT-34677`](https://youtrack.jetbrains.com/issue/KT-34677) False positive "Collection count can be converted to size" with `Iterable`
- [`KT-34696`](https://youtrack.jetbrains.com/issue/KT-34696) Wrong 'Redundant qualifier name' for 'MyEnum.konstues' usage
- [`KT-34713`](https://youtrack.jetbrains.com/issue/KT-34713) "Condition is always 'false'": quickfix "Delete expression" doesn't remove `else` keyword (may break control flow)
- [`KT-35015`](https://youtrack.jetbrains.com/issue/KT-35015) ReplaceWith doesn't substitute parameters with argument expressions
- [`KT-35329`](https://youtrack.jetbrains.com/issue/KT-35329) Replace 'when' with 'if' intention: do not suggest if 'when' is used as expression and it has no 'else' branch
- [`KT-36194`](https://youtrack.jetbrains.com/issue/KT-36194) "Add braces to 'for' statement" inserts extra line break and moves the following single-line comment
- [`KT-36406`](https://youtrack.jetbrains.com/issue/KT-36406) "To ordinary string literal" intention adds unnecessary escapes to characters in template expression
- [`KT-36461`](https://youtrack.jetbrains.com/issue/KT-36461) "Create enum constant" quick fix adds after semicolon, if the last entry has a comma
- [`KT-36462`](https://youtrack.jetbrains.com/issue/KT-36462) "Create enum constant" quick fix doesn't add trailing comma
- [`KT-36508`](https://youtrack.jetbrains.com/issue/KT-36508) False positive "Replace 'to' with infix form" when 'to' lambda generic type argument is specified explicitly
- [`KT-36930`](https://youtrack.jetbrains.com/issue/KT-36930) Intention "Specify type explicitly" adds NotNull annotation when calling java method with the annotation
- [`KT-37148`](https://youtrack.jetbrains.com/issue/KT-37148) "Remove redundant `.let` call doesn't remove extra calls
- [`KT-37156`](https://youtrack.jetbrains.com/issue/KT-37156) "Unused unary operator" inspection highlighting is hard to see
- [`KT-37173`](https://youtrack.jetbrains.com/issue/KT-37173) "Replace with string templates" intention for String.format produces uncompilable string template
- [`KT-37181`](https://youtrack.jetbrains.com/issue/KT-37181) Don't show "Remove redundant qualifier name" inspection on qualified Companion imported with star import
- [`KT-37214`](https://youtrack.jetbrains.com/issue/KT-37214) "Convert lambda to reference" with a labeled "this" receiver fails
- [`KT-37256`](https://youtrack.jetbrains.com/issue/KT-37256) False positive `PlatformExtensionReceiverOfInline` inspection if a platform type konstue is passed to a nullable receiver
- [`KT-37744`](https://youtrack.jetbrains.com/issue/KT-37744) "Convert lambda to reference" inspection quick fix create incompilable code when type is inferred from lambda parameter
- [`KT-37746`](https://youtrack.jetbrains.com/issue/KT-37746) "Redundant suspend modifier" should not be reported for functions with actual keyword
- [`KT-37842`](https://youtrack.jetbrains.com/issue/KT-37842) "Convert to anonymous function" creates broken code with suspend functions
- [`KT-37908`](https://youtrack.jetbrains.com/issue/KT-37908) "Convert to anonymous object" quickfix: false negative when interface has concrete functions
- [`KT-37967`](https://youtrack.jetbrains.com/issue/KT-37967) Replace 'invoke' with direct call intention adds unnecessary parenthesis
- [`KT-37977`](https://youtrack.jetbrains.com/issue/KT-37977) "Replace 'invoke' with direct call" intention: false positive when function is not operator
- [`KT-38062`](https://youtrack.jetbrains.com/issue/KT-38062) Reactor Quickfix throws `NotImplementedError` for Kotlin
- [`KT-38240`](https://youtrack.jetbrains.com/issue/KT-38240) False positive redundant semicolon with `as` cast and `not` unary operator on next line
- [`KT-38261`](https://youtrack.jetbrains.com/issue/KT-38261) Redundant 'let' call remokonst leaves ?. operator and makes code uncompilable
- [`KT-38310`](https://youtrack.jetbrains.com/issue/KT-38310) Remove explicit type annotation intention drops 'suspend'
- [`KT-38492`](https://youtrack.jetbrains.com/issue/KT-38492) False positive "Add import" intention for already imported class
- [`KT-38520`](https://youtrack.jetbrains.com/issue/KT-38520) SetterBackingFieldAssignmentInspection throws exception
- [`KT-38649`](https://youtrack.jetbrains.com/issue/KT-38649) False positive quickfix "Assignment should be lifted out of when" in presence of smartcasts
- [`KT-38677`](https://youtrack.jetbrains.com/issue/KT-38677) Inkonstid psi tree after `Lift assigment out of...`
- [`KT-38790`](https://youtrack.jetbrains.com/issue/KT-38790) "Convert sealed subclass to object" for data classes doesn't remove 'data' keyword
- [`KT-38829`](https://youtrack.jetbrains.com/issue/KT-38829) 'Remove redundant backticks' can be broken with @ in name
- [`KT-38831`](https://youtrack.jetbrains.com/issue/KT-38831) 'Replace with assignment' can be broken with fast code change
- [`KT-38832`](https://youtrack.jetbrains.com/issue/KT-38832) "Remove curly braces" intention may produce CCE
- [`KT-38948`](https://youtrack.jetbrains.com/issue/KT-38948) False positive quickfix "Make containing function suspend" for anonymous function
- [`KT-38961`](https://youtrack.jetbrains.com/issue/KT-38961) "Useless call on collection type" for filterNotNull on non-null array where list return type is expected
- [`KT-39069`](https://youtrack.jetbrains.com/issue/KT-39069) Improve TrailingCommaInspection
- [`KT-39151`](https://youtrack.jetbrains.com/issue/KT-39151) False positive inspection to replace Java forEach with Kotlin forEach when using ConcurrentHashMap

### IDE. JS

- [`KT-39275`](https://youtrack.jetbrains.com/issue/KT-39275) Kotlin JS Browser template for kotlin dsl doesn't include index.html

### IDE. KDoc

- [`KT-32163`](https://youtrack.jetbrains.com/issue/KT-32163) Open Quick Documentation when cursor inside function / constructor brackets

### IDE. Navigation

- [`KT-32245`](https://youtrack.jetbrains.com/issue/KT-32245) Method in Kotlin class is not listed among implementing methods
- [`KT-33510`](https://youtrack.jetbrains.com/issue/KT-33510) There is no gutter icon to navigate from `actual` to `expect` if `expect` and the corresponding `actual` declarations are in the same file
- [`KT-38260`](https://youtrack.jetbrains.com/issue/KT-38260) Navigation bar doesn't show directories of files with a single top level Kotlin class
- [`KT-38466`](https://youtrack.jetbrains.com/issue/KT-38466) Top level functions/properties aren't shown in navigation panel

### IDE. Project View

- [`KT-36444`](https://youtrack.jetbrains.com/issue/KT-36444) Structure view: add ability to sort by visibility
- [`KT-38276`](https://youtrack.jetbrains.com/issue/KT-38276) Structure view: support visibility filter for class properties

### IDE. REPL

- [`KT-38454`](https://youtrack.jetbrains.com/issue/KT-38454) Kotlin REPL in IntelliJ doesn't take module's JVM target setting into account

### IDE. Refactorings

- [`KT-12878`](https://youtrack.jetbrains.com/issue/KT-12878) "Change signature" forces line breaks after every parameter declaration
- [`KT-30128`](https://youtrack.jetbrains.com/issue/KT-30128) Change Signature should move lambda outside of parentheses if the arguments are reordered so that the lambda goes last
- [`KT-35338`](https://youtrack.jetbrains.com/issue/KT-35338) Move/rename refactorings mess up code formatting by wrapping lines
- [`KT-38449`](https://youtrack.jetbrains.com/issue/KT-38449) Extract variable refactoring is broken by NPE
- [`KT-38543`](https://youtrack.jetbrains.com/issue/KT-38543) Copy can't work to package with escaped package
- [`KT-38627`](https://youtrack.jetbrains.com/issue/KT-38627) Rename package refactorings mess up code formatting by wrapping lines

### IDE. Run Configurations

- [`KT-34516`](https://youtrack.jetbrains.com/issue/KT-34516) Don't suggest incompatible targets in a drop-down list for run test gutter icon in multiplatform projects
- [`KT-38102`](https://youtrack.jetbrains.com/issue/KT-38102) DeprecatedMethodException ConfigurationFactory.getId

### IDE. Scratch

- [`KT-38455`](https://youtrack.jetbrains.com/issue/KT-38455) Kotlin scratch files don't take module's JVM target setting into account

### IDE. Script

- [`KT-39791`](https://youtrack.jetbrains.com/issue/KT-39791) Kotlin plugin loads VFS in the output directories

### IDE. Structural Search

- [`KT-39721`](https://youtrack.jetbrains.com/issue/KT-39721) Optimize Kotlin SSR by using the index
- [`KT-39733`](https://youtrack.jetbrains.com/issue/KT-39733) Augmented assignment matching
- [`KT-39769`](https://youtrack.jetbrains.com/issue/KT-39769) "When expressions" predefined template doesn't match all when expressions

### IDE. Wizards

- [`KT-38673`](https://youtrack.jetbrains.com/issue/KT-38673) New Project Wizard: multiplatform templates are generated having unsupported Gradle version in a wrapper
- [`KT-38810`](https://youtrack.jetbrains.com/issue/KT-38810) Incorrect order of build phases in Xcode project from new wizard
- [`KT-38952`](https://youtrack.jetbrains.com/issue/KT-38952) Remove old new_project_wizards
- [`KT-39503`](https://youtrack.jetbrains.com/issue/KT-39503) New Project wizard 1.4+: release kotlinx.html version is added to dependencies with milestone IDE plugin
- [`KT-39700`](https://youtrack.jetbrains.com/issue/KT-39700) Wizard: group project templates on the first step by the project type
- [`KT-39770`](https://youtrack.jetbrains.com/issue/KT-39770) CSS Support in Kotlin wizards
- [`KT-39826`](https://youtrack.jetbrains.com/issue/KT-39826) Fix Android app in New Template Wizard
- [`KT-39843`](https://youtrack.jetbrains.com/issue/KT-39843) Change imports in JS/browser wizard

### JS. Tools

- [`KT-32273`](https://youtrack.jetbrains.com/issue/KT-32273) Kotlin/JS console error on hot reload
- [`KT-39498`](https://youtrack.jetbrains.com/issue/KT-39498) Update dukat version in toolchain near to release of 1.4-M3

### JavaScript

- [`KT-29916`](https://youtrack.jetbrains.com/issue/KT-29916) Implement `typeOf` on JS
- [`KT-35857`](https://youtrack.jetbrains.com/issue/KT-35857) Kotlin/JS CLI bundled to IDEA plugin can't compile using IR back-end out of the box
- [`KT-36798`](https://youtrack.jetbrains.com/issue/KT-36798) KJS: prohibit using @JsExport on a non-top-level declaration
- [`KT-37771`](https://youtrack.jetbrains.com/issue/KT-37771) KJS: Generated TypeScript does not recursively export base classes (can fail with generics)
- [`KT-38113`](https://youtrack.jetbrains.com/issue/KT-38113) Review public API of JS stdlib for IR BE
- [`KT-38765`](https://youtrack.jetbrains.com/issue/KT-38765) [JS / IR] AssertionError: class EventEmitter: Super class should be any: with nested class extending parent class
- [`KT-38768`](https://youtrack.jetbrains.com/issue/KT-38768) KJS IR: generate ES2015 (aka ES6) classes

### Libraries

#### New Features

- [`KT-11253`](https://youtrack.jetbrains.com/issue/KT-11253) Function to sum long or other numeric property of items in a collection
- [`KT-28933`](https://youtrack.jetbrains.com/issue/KT-28933) capitalize() with Locale argument in the JDK stdlib
- [`KT-34142`](https://youtrack.jetbrains.com/issue/KT-34142) Create SortedMap with Comparator and items
- [`KT-34506`](https://youtrack.jetbrains.com/issue/KT-34506) Add Sequence.flatMap overload that works on Iterable
- [`KT-36894`](https://youtrack.jetbrains.com/issue/KT-36894) Support flatMapIndexed in the Collections API
- [`KT-38480`](https://youtrack.jetbrains.com/issue/KT-38480) Introduce experimental annotation for enabling overload resolution by lambda result
- [`KT-38708`](https://youtrack.jetbrains.com/issue/KT-38708) minOf/maxOf functions to return min/max konstue provided by selector
- [`KT-39707`](https://youtrack.jetbrains.com/issue/KT-39707) Make some interfaces in stdlib functional

#### Performance Improvements

- [`KT-23142`](https://youtrack.jetbrains.com/issue/KT-23142) toHashSet is suboptimal for inputs with a lot of duplicates

#### Fixes

- [`KT-21266`](https://youtrack.jetbrains.com/issue/KT-21266) Add module-info for standard library artifacts
- [`KT-23322`](https://youtrack.jetbrains.com/issue/KT-23322) Document 'reduce' operation behavior on empty collections
- [`KT-28753`](https://youtrack.jetbrains.com/issue/KT-28753) Comparing floating point konstues in array/list operations 'contains', 'indexOf', 'lastIndexOf': IEEE 754 or total order
- [`KT-30083`](https://youtrack.jetbrains.com/issue/KT-30083) Annotate KTypeProjection.STAR with JvmField in a compatible way
- [`KT-30084`](https://youtrack.jetbrains.com/issue/KT-30084) Annotate functions in KTypeProjection.Companion with JvmStatic
- [`KT-31343`](https://youtrack.jetbrains.com/issue/KT-31343) Deprecate old String <-> CharArray, ByteArray conversion api
- [`KT-34596`](https://youtrack.jetbrains.com/issue/KT-34596) Add some konstidation to KTypeProjection constructor
- [`KT-35978`](https://youtrack.jetbrains.com/issue/KT-35978) Review and remove experimental stdlib API status for 1.4
- [`KT-38388`](https://youtrack.jetbrains.com/issue/KT-38388) Document `fromIndex` and `toIndex` parameters
- [`KT-38854`](https://youtrack.jetbrains.com/issue/KT-38854) Gradually change the return type of collection min/max functions to non-nullable
- [`KT-39023`](https://youtrack.jetbrains.com/issue/KT-39023) Document split(Pattern) extension differences from Pattern.split
- [`KT-39064`](https://youtrack.jetbrains.com/issue/KT-39064) Introduce minOrNull and maxOrNull extension functions on collections
- [`KT-39235`](https://youtrack.jetbrains.com/issue/KT-39235) Lift experimental annotation from bit operations
- [`KT-39237`](https://youtrack.jetbrains.com/issue/KT-39237) Lift experimental annotation from common StringBuilder
- [`KT-39238`](https://youtrack.jetbrains.com/issue/KT-39238) Appendable.appendRange - remove nullability
- [`KT-39239`](https://youtrack.jetbrains.com/issue/KT-39239) Lift experimental annotation from String <-> utf8 conversion api
- [`KT-39244`](https://youtrack.jetbrains.com/issue/KT-39244) KJS: update polyfills, all or most of them must not be enumerable
- [`KT-39330`](https://youtrack.jetbrains.com/issue/KT-39330) Migrate declarations from kotlin.dom and kotlin.browser packages to kotlinx.*

### Middle-end. IR

- [`KT-31088`](https://youtrack.jetbrains.com/issue/KT-31088) need a way to compute fake overrides for pure IR
- [`KT-33207`](https://youtrack.jetbrains.com/issue/KT-33207) Kotlin/Native: KNPE during deserialization of an inner class
- [`KT-33267`](https://youtrack.jetbrains.com/issue/KT-33267) Kotlin/Native: Deserialization error for an "inner" extension property imported from a class
- [`KT-37255`](https://youtrack.jetbrains.com/issue/KT-37255) Make psi2ir aware of declarations provided by compiler plugins

### Reflection

- [`KT-22936`](https://youtrack.jetbrains.com/issue/KT-22936) Not all things can be changed to `createType` yet, and now `defaultType` methods are starting to fail
- [`KT-32241`](https://youtrack.jetbrains.com/issue/KT-32241) Move KType.javaType into stdlib from reflect
- [`KT-34344`](https://youtrack.jetbrains.com/issue/KT-34344) KType.javaType implementation throws when invoked with a typeOf<T>()
- [`KT-38491`](https://youtrack.jetbrains.com/issue/KT-38491) IllegalArgumentException when using callBy on function with inline class parameters and default arguments
- [`KT-38881`](https://youtrack.jetbrains.com/issue/KT-38881) Add KClass.isFun modifier of functional interfaces to reflection

### Tools. Android Extensions

- [`KT-25807`](https://youtrack.jetbrains.com/issue/KT-25807) Kotlin extension annotation @Parcelize in AIDL returns Object instead of original T

### Tools. CLI

- [`KT-30211`](https://youtrack.jetbrains.com/issue/KT-30211) Support a way to pass arguments to the underlying JVM in kotlinc batch scripts on Windows
- [`KT-30778`](https://youtrack.jetbrains.com/issue/KT-30778) kotlin-compiler.jar contains shaded but not relocated kotlinx.coroutines
- [`KT-38070`](https://youtrack.jetbrains.com/issue/KT-38070) Compiler option to bypass prerelease metadata incompatibility error
- [`KT-38413`](https://youtrack.jetbrains.com/issue/KT-38413) Add JVM target bytecode version 14

### Tools. Compiler Plugins

- [`KT-39274`](https://youtrack.jetbrains.com/issue/KT-39274) [KJS / IR] Custom serializer for class without zero argument constructor doesn't compile

### Tools. Gradle

- [`KT-25428`](https://youtrack.jetbrains.com/issue/KT-25428) Kotlin Gradle Plugin: Use new Gradle API for Lazy tasks
- [`KT-34487`](https://youtrack.jetbrains.com/issue/KT-34487) Gradle build fails with "Cannot run program "java": error=7, Argument list too long
- [`KT-35957`](https://youtrack.jetbrains.com/issue/KT-35957) MPP IC fails with "X has several compatible actual declarations" error
- [`KT-38250`](https://youtrack.jetbrains.com/issue/KT-38250) Drop support for Gradle versions older than 5.3 in the Kotlin Gradle plugin

### Tools. Gradle. JS

#### New Features

- [`KT-30619`](https://youtrack.jetbrains.com/issue/KT-30619) Support NPM transitive dependencies in multi-platform JS target
- [`KT-38286`](https://youtrack.jetbrains.com/issue/KT-38286) [Gradle, JS] Error handling on Webpack problems

#### Fixes

- [`KT-31669`](https://youtrack.jetbrains.com/issue/KT-31669) Gradle/JS: rise error when plugin loaded more than once
- [`KT-32531`](https://youtrack.jetbrains.com/issue/KT-32531) [Gradle/JS] Add scoped NPM dependencies
- [`KT-34832`](https://youtrack.jetbrains.com/issue/KT-34832) [Kotlin/JS] Failed build after webpack run (Karma not found)
- [`KT-35194`](https://youtrack.jetbrains.com/issue/KT-35194) Kotlin/JS: browserRun fails with "address already in use" when trying to connect to local server
- [`KT-35611`](https://youtrack.jetbrains.com/issue/KT-35611) Kotlin Gradle plugin should report `kotlin2js` plugin ID as deprecated
- [`KT-35641`](https://youtrack.jetbrains.com/issue/KT-35641) Kotlin Gradle plugin should report `kotlin-dce-js` plugin ID as deprecated
- [`KT-36410`](https://youtrack.jetbrains.com/issue/KT-36410) JS: Collect stats about IR backend usage
- [`KT-36451`](https://youtrack.jetbrains.com/issue/KT-36451) KJS Adding npm dependency breaks Webpack devserver reloading
- [`KT-37258`](https://youtrack.jetbrains.com/issue/KT-37258) Kotlin/JS + Gradle: in continuous mode kotlinNpmInstall time to time outputs "ENOENT: no such file or directory" error
- [`KT-38109`](https://youtrack.jetbrains.com/issue/KT-38109) [Gradle, JS] Error handling on Karma launcher problems
- [`KT-38331`](https://youtrack.jetbrains.com/issue/KT-38331) Add an ability to control generating externals for npm deps individually
- [`KT-38485`](https://youtrack.jetbrains.com/issue/KT-38485) [Gradle, JS] Unable to configure JS compiler with string
- [`KT-38683`](https://youtrack.jetbrains.com/issue/KT-38683) Remove possibility to set NPM dependency without version
- [`KT-38990`](https://youtrack.jetbrains.com/issue/KT-38990) Support multiple range versions for NPM dependencies
- [`KT-38994`](https://youtrack.jetbrains.com/issue/KT-38994) Remove possibility to set NPM dependency with npm(org, name, version)
- [`KT-39109`](https://youtrack.jetbrains.com/issue/KT-39109) ArithmeticException: "/ by zero" caused by kotlinNodeJsSetup task with enabled gradle caching on Windows
- [`KT-39210`](https://youtrack.jetbrains.com/issue/KT-39210) Kotlin/JS: with both JS and MPP modules in the same project Gradle configuration fails on `nodejs {}` and `browser {}`
- [`KT-39377`](https://youtrack.jetbrains.com/issue/KT-39377) Use standard source-map-loader instead of custom one

### Tools. Gradle. Multiplatform

- [`KT-39184`](https://youtrack.jetbrains.com/issue/KT-39184) Support publication of Kotlin-distributed libraries with Gradle Metadata
- [`KT-39304`](https://youtrack.jetbrains.com/issue/KT-39304) Gradle import error `java.util.NoSuchElementException: Key source set foo is missing in the map` on unused source set

### Tools. Gradle. Native

- [`KT-37514`](https://youtrack.jetbrains.com/issue/KT-37514) CocoaPods Gradle plugin: Support building from terminal projects for several platforms
- [`KT-38440`](https://youtrack.jetbrains.com/issue/KT-38440) Make error message about missing Podfile path for cocoapods integration actionable for a user
- [`KT-38991`](https://youtrack.jetbrains.com/issue/KT-38991) Gradle MPP plugin: Enable parallel in-process execution for K/N compiler
- [`KT-39935`](https://youtrack.jetbrains.com/issue/KT-39935) Support overriding the `KotlinNativeCompile` task sources
- [`KT-37512`](https://youtrack.jetbrains.com/issue/KT-37512) Cocoapods Gradle plugin: Improve error logging for external tools

### Tools. J2K

- [`KT-35169`](https://youtrack.jetbrains.com/issue/KT-35169) Do not show "Inline local variable" popup during "Cleaning up code" phase of J2K
- [`KT-38004`](https://youtrack.jetbrains.com/issue/KT-38004) J2K breaks java getter call in java code
- [`KT-38450`](https://youtrack.jetbrains.com/issue/KT-38450) J2K should convert Java SAM interfaces to Kotlin fun interfaces

### Tools. JPS

- [`KT-27458`](https://youtrack.jetbrains.com/issue/KT-27458) The Kotlin standard library is not found in the module graph ... in a non-Kotlin project.
- [`KT-29552`](https://youtrack.jetbrains.com/issue/KT-29552) Project is completely rebuilt after each gradle sync.

### Tools. Scripts

- [`KT-37766`](https://youtrack.jetbrains.com/issue/KT-37766) Impossible to apply compiler plugins onto scripts with the new scripting API

### Tools. kapt

- [`KT-29355`](https://youtrack.jetbrains.com/issue/KT-29355) Provide access to default konstues for primary constructor properties


## 1.4-M2

### Compiler

#### New Features

- [`KT-37432`](https://youtrack.jetbrains.com/issue/KT-37432) Do not include annotations fields into 'visibility must be explicitly specified' check in api mode

#### Performance Improvements

- [`KT-27362`](https://youtrack.jetbrains.com/issue/KT-27362) Anonymous classes representing function/property references contain rarely used methods
- [`KT-35626`](https://youtrack.jetbrains.com/issue/KT-35626) NI: Performance problem with many type parameters
- [`KT-36047`](https://youtrack.jetbrains.com/issue/KT-36047) Compiler produces if-chain instead of switch when when subject captured as variable
- [`KT-36638`](https://youtrack.jetbrains.com/issue/KT-36638) Use 'java/lang/StringBuilder.append (C)Ljava/lang/StringBuilder;' when appending single character in JVM_IR
- [`KT-37389`](https://youtrack.jetbrains.com/issue/KT-37389) Avoid type approximation during generation constraints with EQUALITY kind
- [`KT-37392`](https://youtrack.jetbrains.com/issue/KT-37392) Avoid substitution and type approximation for simple calls
- [`KT-37546`](https://youtrack.jetbrains.com/issue/KT-37546) NI: high memory and CPU consumption due to creating useless captured types (storing in approximated types cache, unneeded computations)

#### Fixes

- [`KT-11265`](https://youtrack.jetbrains.com/issue/KT-11265) Factory pattern and overload resolution ambiguity
- [`KT-27524`](https://youtrack.jetbrains.com/issue/KT-27524) Inline class is boxed when used with suspend modifier
- [`KT-27586`](https://youtrack.jetbrains.com/issue/KT-27586) ClassCastException occurs if the Result (or any other inline class) is returned from a lambda
- [`KT-30419`](https://youtrack.jetbrains.com/issue/KT-30419) Use boxed version of an inline class in return type position for covariant and generic-specialized overrides
- [`KT-31163`](https://youtrack.jetbrains.com/issue/KT-31163) FIR: consider replacing comparisons with compareTo calls and some additional intrinsics
- [`KT-31585`](https://youtrack.jetbrains.com/issue/KT-31585) ClassCastException with derived class delegated to generic class with inline class type argument
- [`KT-31823`](https://youtrack.jetbrains.com/issue/KT-31823) NI: Type mismatch with a star projection and `UnsafeVariance`
- [`KT-33119`](https://youtrack.jetbrains.com/issue/KT-33119) Pre-increment for inline class wrapping Int compiles to direct increment instead of inc-impl
- [`KT-33715`](https://youtrack.jetbrains.com/issue/KT-33715) Kotlin/Native: metadata compiler
- [`KT-34048`](https://youtrack.jetbrains.com/issue/KT-34048) IllegalAccessError when initializing konst property in EXACTLY_ONCE lambda
- [`KT-34433`](https://youtrack.jetbrains.com/issue/KT-34433) NI: Type mismatch with a star projection and `UnsafeVariance`
- [`KT-35133`](https://youtrack.jetbrains.com/issue/KT-35133) FIR Java: don't set 'isOperator' for methods with non-operator names
- [`KT-35234`](https://youtrack.jetbrains.com/issue/KT-35234) ClassCastException with creating an inline class from a function reference of covariant or generic-specialized override
- [`KT-35406`](https://youtrack.jetbrains.com/issue/KT-35406) Generic type implicitly inferred as Nothing with no warning
- [`KT-35587`](https://youtrack.jetbrains.com/issue/KT-35587) Plain namespace strings in JvmNameResolver.PREDEFINED_STRINGS are prone to namespace changes during jar relocation.
- [`KT-36044`](https://youtrack.jetbrains.com/issue/KT-36044) NI: premature fixation a type variable if there were nested lambdas (constraint source was the deepest lambda)
- [`KT-36057`](https://youtrack.jetbrains.com/issue/KT-36057) [FIR] Incorrect smartcast
- [`KT-36069`](https://youtrack.jetbrains.com/issue/KT-36069) NI: TYPE_MISMATCH caused by incorrect inference to Nothing
- [`KT-36125`](https://youtrack.jetbrains.com/issue/KT-36125) Callable reference resolution ambiguity error is not displayed properly in the IDE
- [`KT-36191`](https://youtrack.jetbrains.com/issue/KT-36191) IDE locks loading packages and editing file containing `try` keyword inside string template
- [`KT-36222`](https://youtrack.jetbrains.com/issue/KT-36222) NI: Improve error message about nullability mismatch for a generic call
- [`KT-36249`](https://youtrack.jetbrains.com/issue/KT-36249) NI doesn't use upper bound for T of called function during infer return type and as a result infer it to `Any?` if the resulting type was intersection type
- [`KT-36345`](https://youtrack.jetbrains.com/issue/KT-36345) FIR: record argument mapping for use in back-end
- [`KT-36446`](https://youtrack.jetbrains.com/issue/KT-36446) NI: "UnsupportedOperationException no descriptor for type constructor of IntegerLiteralType[Int,Long,Byte,Short]" with BuilderInference and delegate
- [`KT-36758`](https://youtrack.jetbrains.com/issue/KT-36758) [FIR] Unresolved callable reference to member of local class
- [`KT-36759`](https://youtrack.jetbrains.com/issue/KT-36759) [FIR] Unsupported callable reference resolution for methods with default parameters
- [`KT-36762`](https://youtrack.jetbrains.com/issue/KT-36762) [FIR] Unresolved `array.clone()`
- [`KT-36764`](https://youtrack.jetbrains.com/issue/KT-36764) [FIR] Bug in inference with DefinitelyNotNull types
- [`KT-36816`](https://youtrack.jetbrains.com/issue/KT-36816) NI: definitely not-null (T!!) types in invariant positions don't approximate to T inside inference process
- [`KT-36819`](https://youtrack.jetbrains.com/issue/KT-36819) NI: premature completion of lambdas, which are passed somewhere
- [`KT-36850`](https://youtrack.jetbrains.com/issue/KT-36850) Incorrect private visibility of sealed class constructors
- [`KT-36856`](https://youtrack.jetbrains.com/issue/KT-36856) Throwing exception when there is inheritance in Kotlin from Java class, which contains methods with the same JVM descriptors
- [`KT-36879`](https://youtrack.jetbrains.com/issue/KT-36879) Introduce FIR_IDENTICAL in diagnostic tests
- [`KT-36881`](https://youtrack.jetbrains.com/issue/KT-36881) FIR: completion don't runs for return expressions
- [`KT-36887`](https://youtrack.jetbrains.com/issue/KT-36887) [FIR] Unresolved member in nested lambda in initializer
- [`KT-36905`](https://youtrack.jetbrains.com/issue/KT-36905) [FIR] Unresolved in lambda in default argument position
- [`KT-36953`](https://youtrack.jetbrains.com/issue/KT-36953) AssertionError: "Unsigned type expected: null" when there is a range with an unsigned type
- [`KT-37009`](https://youtrack.jetbrains.com/issue/KT-37009) FIR: Bound smart-cast lost
- [`KT-37027`](https://youtrack.jetbrains.com/issue/KT-37027) FIR: Wrong projection on spread + varargs on non-final types
- [`KT-37038`](https://youtrack.jetbrains.com/issue/KT-37038) NI: redundant lambda's arrow breaks CST calculation for extension lambdas
- [`KT-37043`](https://youtrack.jetbrains.com/issue/KT-37043) NI: inference T to Any? if there was elvis between Java out-type and reified `materialize` for this type without out projection
- [`KT-37066`](https://youtrack.jetbrains.com/issue/KT-37066) [FIR] Wrong type inference for lambdas
- [`KT-37070`](https://youtrack.jetbrains.com/issue/KT-37070) [FIR] Unresolved parameters of outer lambda in scope of inner lambda
- [`KT-37087`](https://youtrack.jetbrains.com/issue/KT-37087) "IllegalStateException: Can't find method 'invoke()'" for mutable property reference in default konstue of an inline function parameter
- [`KT-37091`](https://youtrack.jetbrains.com/issue/KT-37091) [FIR] Wrong inferred type of when-expression if when-argument  is not-null-asserted and type is not specifies explicitly
- [`KT-37176`](https://youtrack.jetbrains.com/issue/KT-37176) [FIR] Incorrect resolution mode for statements of block
- [`KT-37302`](https://youtrack.jetbrains.com/issue/KT-37302) Unexpected conversion:`Int` constant inferred to `Long` in when expression
- [`KT-37327`](https://youtrack.jetbrains.com/issue/KT-37327) FIR: Smartcast problem
- [`KT-37343`](https://youtrack.jetbrains.com/issue/KT-37343) NI: definitely not null types pre-approximation is inconsistent with OI
- [`KT-37380`](https://youtrack.jetbrains.com/issue/KT-37380) NI: broken some code with def not null types due to skip needed constraints
- [`KT-37419`](https://youtrack.jetbrains.com/issue/KT-37419) NI: UNRESOLVED_REFERENCE_WRONG_RECEIVER is reported in case lambda with receiver is returned from `when` expression
- [`KT-37434`](https://youtrack.jetbrains.com/issue/KT-37434) Kotlin/JS, Kotlin/Native: fun interfaces: SAM conversion to Kotlin interface is not compiled with RESOLUTION_TO_CLASSIFIER
- [`KT-37447`](https://youtrack.jetbrains.com/issue/KT-37447) Expression from annotation entry in konstue parameter inside konstue parameter should be marked as USED_AS_EXPRESSION
- [`KT-37453`](https://youtrack.jetbrains.com/issue/KT-37453) Type arguments not checked to be empty for candidates with no declared parameters
- [`KT-37488`](https://youtrack.jetbrains.com/issue/KT-37488) [FIR] Incorrect exhaustiveness checking for branches with equals to object that implements sealed class
- [`KT-37497`](https://youtrack.jetbrains.com/issue/KT-37497) NI:  'super' is not an expression, it can not be used as a receiver for extension functions
- [`KT-37530`](https://youtrack.jetbrains.com/issue/KT-37530) NI: instantiation of abstract class via callable reference argument causes run time InstantiationError
- [`KT-37531`](https://youtrack.jetbrains.com/issue/KT-37531) NI: callable reference argument with left hand side type parameter causes frontend exception
- [`KT-37554`](https://youtrack.jetbrains.com/issue/KT-37554) NI: Nothing is inferred incorrectly with elvis return
- [`KT-37579`](https://youtrack.jetbrains.com/issue/KT-37579) NI: inconsistent behaviour with OI around implicit invoke convention after safe call with additional implicit receiver
- [`KT-37604`](https://youtrack.jetbrains.com/issue/KT-37604) "VerifyError: Call to wrong <init> method" in 'invoke' for adapted callable reference to constructor with coercion to Unit
- [`KT-37621`](https://youtrack.jetbrains.com/issue/KT-37621) NI: type variable is inferred to Nothing if the second branch was Nothing and there was upper bound for a type parameter
- [`KT-37626`](https://youtrack.jetbrains.com/issue/KT-37626) NI: builder inference with expected type breaks class references resolution for a class with parameters
- [`KT-37627`](https://youtrack.jetbrains.com/issue/KT-37627) NI: wrong order of the type variable fixation (Nothing? against a call with lambda)
- [`KT-37628`](https://youtrack.jetbrains.com/issue/KT-37628) NI: wrong approximation of type argument to star projection during common super type calculation
- [`KT-37644`](https://youtrack.jetbrains.com/issue/KT-37644) NI: appeared exception during incorporation of a captured type into a type variable for elvis resolve
- [`KT-37650`](https://youtrack.jetbrains.com/issue/KT-37650) NI: it's impossible to infer a type variable with the participation of a wrapped covariant type
- [`KT-37718`](https://youtrack.jetbrains.com/issue/KT-37718) False positive unused parameter for @JvmStatic main function in object
- [`KT-37779`](https://youtrack.jetbrains.com/issue/KT-37779) ClassCastException: Named argument without spread operator for vararg parameter causes code to crash on runtime
- [`KT-37832`](https://youtrack.jetbrains.com/issue/KT-37832) In MPP, subtypes of types defined in legacy libraries, like stdlib, cannot properly resolve on the consumer side receviing both
- [`KT-37861`](https://youtrack.jetbrains.com/issue/KT-37861) Capturing an outer class instance in a default parameter of inner class constructor causes VerifyError
- [`KT-37986`](https://youtrack.jetbrains.com/issue/KT-37986) Return konstue of function reference returning inline class mapped to 'java.lang.Object' is not boxed properly
- [`KT-37998`](https://youtrack.jetbrains.com/issue/KT-37998) '!!' operator on safe call of function returning inline class konstue causes CCE at runtime
- [`KT-38042`](https://youtrack.jetbrains.com/issue/KT-38042) Allow kotlin.Result as a return type only if one enabled inline classes explicitly
- [`KT-38134`](https://youtrack.jetbrains.com/issue/KT-38134) NI: Type mismatch with a star projection and `UnsafeVariance`
- [`KT-38298`](https://youtrack.jetbrains.com/issue/KT-38298) Inconsistent choice of candidate when both expect/actual are available (affects only `enableGranularSourceSetMetadata`)
- [`KT-38661`](https://youtrack.jetbrains.com/issue/KT-38661) NI: "Cannot infer type variable TypeVariable" with lambda with receiver
- [`KT-38668`](https://youtrack.jetbrains.com/issue/KT-38668) Project with module dependency in KN, build fails with Kotlin 1.3.71 and associated libs but passes with 1.3.61.
- [`KT-38857`](https://youtrack.jetbrains.com/issue/KT-38857) Class versions V1_5 or less must use F_NEW frames.
- [`KT-39113`](https://youtrack.jetbrains.com/issue/KT-39113) "AssertionError: Uninitialized konstue on stack" with EXACTLY_ONCE contract in non-inline function and lambda destructuring
- [`KT-28483`](https://youtrack.jetbrains.com/issue/KT-28483) Override of generic-return-typed function with inline class should lead to a boxing
- [`KT-37963`](https://youtrack.jetbrains.com/issue/KT-37963) ClassCastException: Value of inline class represented as 'java.lang.Object' is not boxed properly on return from lambda

### Docs & Examples

- [`KT-35231`](https://youtrack.jetbrains.com/issue/KT-35231) toMutableList documentation is vague

### IDE

#### Performance Improvements

- [`KT-30541`](https://youtrack.jetbrains.com/issue/KT-30541) EDT Freeze after new Kotlin Script creation
- [`KT-35050`](https://youtrack.jetbrains.com/issue/KT-35050) Significant freezes due to findSdkAcrossDependencies()
- [`KT-37301`](https://youtrack.jetbrains.com/issue/KT-37301) Freeze when "Optimize Imports" in KotlinImportOptimizer
- [`KT-37466`](https://youtrack.jetbrains.com/issue/KT-37466) Inkonstidate partialBodyResolveCache on OCB
- [`KT-37467`](https://youtrack.jetbrains.com/issue/KT-37467) PerFileAnalysisCache.fetchAnalysisResults
- [`KT-37993`](https://youtrack.jetbrains.com/issue/KT-37993) Do not resolve references if paste code is located in the same origin
- [`KT-38318`](https://youtrack.jetbrains.com/issue/KT-38318) Freezes in IDEA

#### Fixes

- [`KT-27935`](https://youtrack.jetbrains.com/issue/KT-27935) Functional typealias with typealias in type parameters causes UnsupportedOperationException in TypeSignatureMappingKt (IDEA analysis)
- [`KT-31668`](https://youtrack.jetbrains.com/issue/KT-31668) Complete statement for class declaration: add '()' to supertype
- [`KT-33473`](https://youtrack.jetbrains.com/issue/KT-33473) UAST: References to local variable are resolved to UastKotlinPsiVariable
- [`KT-34564`](https://youtrack.jetbrains.com/issue/KT-34564) Kotlin USimpleNameReferenceExpression for annotation parameter resolves to null for compiled Kotlin classes
- [`KT-34973`](https://youtrack.jetbrains.com/issue/KT-34973) Light class incorrectly claiming ambiguous method call from Java when one overload is synthetic
- [`KT-35801`](https://youtrack.jetbrains.com/issue/KT-35801) UAST: UnknownKotlinExpression for konstid Kotlin annotated expression
- [`KT-35804`](https://youtrack.jetbrains.com/issue/KT-35804) UAST: Annotations missing from catch clause parameters
- [`KT-35848`](https://youtrack.jetbrains.com/issue/KT-35848) UAST:  ClassCastException when trying to invoke UElement for some wrapped PsiElements
- [`KT-36156`](https://youtrack.jetbrains.com/issue/KT-36156) Kotlin annotation attributes have blue color whereas white in Java
- [`KT-36275`](https://youtrack.jetbrains.com/issue/KT-36275) UAST: UCallExpression::resolve returns null for local function calls
- [`KT-36717`](https://youtrack.jetbrains.com/issue/KT-36717) Fix failing light class tests after switching plugin to language version 1.4
- [`KT-36877`](https://youtrack.jetbrains.com/issue/KT-36877) Message bundles for copy paste are missed in 201
- [`KT-36907`](https://youtrack.jetbrains.com/issue/KT-36907) IDE: `-Xuse-ir` setting on facet level does not affect highlighting
- [`KT-37133`](https://youtrack.jetbrains.com/issue/KT-37133) UAST: Annotating assignment expression sometimes leads to UnknownKotlinExpression
- [`KT-37312`](https://youtrack.jetbrains.com/issue/KT-37312) "Implement members" intention put function in the primary constructor if there are unused brackets in class
- [`KT-37613`](https://youtrack.jetbrains.com/issue/KT-37613) Uast: no parameters in reified method
- [`KT-37933`](https://youtrack.jetbrains.com/issue/KT-37933) Rare NPE in ProjectRootsUtilKt.isKotlinBinary [easy fix]
- [`KT-38081`](https://youtrack.jetbrains.com/issue/KT-38081) Configure kotlin in project produces IDE error "heavy operation and should not be call on AWT thread"
- [`KT-38354`](https://youtrack.jetbrains.com/issue/KT-38354) HMPP. IDE. Dependency leakage from leaf native to shared native module
- [`KT-38634`](https://youtrack.jetbrains.com/issue/KT-38634) IDE: Error on opening MPP project in 1.3.72 after opening it in 1.4-M2

### IDE. Code Style, Formatting

- [`KT-37870`](https://youtrack.jetbrains.com/issue/KT-37870) "Remove trailing comma" action stops working after applying and cancelling it

### IDE. Completion

- [`KT-36808`](https://youtrack.jetbrains.com/issue/KT-36808) Delete Flow.collect from autocompletion list or make it least prioritized
- [`KT-36860`](https://youtrack.jetbrains.com/issue/KT-36860) Provide convenient completion of extension functions from objects
- [`KT-37395`](https://youtrack.jetbrains.com/issue/KT-37395) Inkonstid callable reference completion of member extension

### IDE. Debugger

- [`KT-34906`](https://youtrack.jetbrains.com/issue/KT-34906) Implement Coroutine Debugger
- [`KT-35392`](https://youtrack.jetbrains.com/issue/KT-35392) Debugger omits meaningful part of the stacktrace even with disabled filter
- [`KT-36215`](https://youtrack.jetbrains.com/issue/KT-36215) Coroutines debugger tab is empty in Android Studio
- [`KT-37238`](https://youtrack.jetbrains.com/issue/KT-37238) Coroutines Debugger: dump creation fails every time
- [`KT-38047`](https://youtrack.jetbrains.com/issue/KT-38047) Coroutines Debugger: Assertion failed: “Should be invoked in manager thread, use DebuggerManagerThreadImpl” on moving to source code from suspended coroutine in project without debugger jar in classpath
- [`KT-38049`](https://youtrack.jetbrains.com/issue/KT-38049) Coroutines Debugger: NPE “null cannot be cast to non-null type com.sun.jdi.ObjectReference” is thrown by calling dumpCoroutines
- [`KT-38487`](https://youtrack.jetbrains.com/issue/KT-38487) Any Field Watch interaction causes a MissingResourceException

### IDE. Decompiler, Indexing, Stubs

- [`KT-37896`](https://youtrack.jetbrains.com/issue/KT-37896) IAE: "Argument for @NotNull parameter 'file' of IndexTodoCacheManagerImpl.getTodoCount must not be null" through KotlinTodoSearcher.processQuery()

### IDE. Gradle Integration

- [`KT-33809`](https://youtrack.jetbrains.com/issue/KT-33809) With `kotlin.mpp.enableGranularSourceSetsMetadata=true`, IDE misses dependsOn-relation between kotlin and android sourceSets, leading to issues with expect/actual matching
- [`KT-36354`](https://youtrack.jetbrains.com/issue/KT-36354) IDE: Gradle import from non-JVM projects: dependency to output artifact is created instead of module dependency
- [`KT-38037`](https://youtrack.jetbrains.com/issue/KT-38037) UnsupportedOperationException on sync gradle Kotlin project with at least two multiplatform modules

### IDE. Gradle. Script

- [`KT-36763`](https://youtrack.jetbrains.com/issue/KT-36763) Drop modification stamp for scripts after project import
- [`KT-37237`](https://youtrack.jetbrains.com/issue/KT-37237) Script configurations should be loaded during project import in case of errors
- [`KT-38041`](https://youtrack.jetbrains.com/issue/KT-38041) Do not request for script configuration after VCS update

### IDE. Inspections and Intentions

#### New Features

- [`KT-3262`](https://youtrack.jetbrains.com/issue/KT-3262) Inspection "Inner class could be nested"
- [`KT-15723`](https://youtrack.jetbrains.com/issue/KT-15723) Add 'Convert to konstue' quickfix for property containing only getter
- [`KT-34026`](https://youtrack.jetbrains.com/issue/KT-34026) Add "Remove argument" quick fix for redundant argument in constructor call
- [`KT-34332`](https://youtrack.jetbrains.com/issue/KT-34332) Add "Remove argument" quick fix for redundant argument in function call
- [`KT-34450`](https://youtrack.jetbrains.com/issue/KT-34450) `Convert function to property` intention should be also displayed on `fun` keyword
- [`KT-34593`](https://youtrack.jetbrains.com/issue/KT-34593) Invert 'if' condition: Invert `String.isNotEmpty` should be `String.isEmpty`
- [`KT-34819`](https://youtrack.jetbrains.com/issue/KT-34819) Inspection: report useless elvis "?: return null"
- [`KT-37849`](https://youtrack.jetbrains.com/issue/KT-37849) Support `ReplaceWith` for supertypes call

#### Performance Improvements

- [`KT-37515`](https://youtrack.jetbrains.com/issue/KT-37515) Deadlock

#### Fixes

- [`KT-12329`](https://youtrack.jetbrains.com/issue/KT-12329) "invert if" inserts unnecessary 'continue' for statement inside a loop with 'continue'
- [`KT-17615`](https://youtrack.jetbrains.com/issue/KT-17615) "Convert parameter to receiver" changes `this` to `this@ < no name provided >`
- [`KT-20868`](https://youtrack.jetbrains.com/issue/KT-20868) IntelliJ says method from anonymous inner class with inferred interface type is not used even though it is
- [`KT-20907`](https://youtrack.jetbrains.com/issue/KT-20907) Secondary constructor is marked as unused by IDE when called by typealias
- [`KT-22368`](https://youtrack.jetbrains.com/issue/KT-22368) "Convert to block body" intention incorrectly formats closing brace
- [`KT-23510`](https://youtrack.jetbrains.com/issue/KT-23510) "Remove parameter" quick fix keeps lambda argument when it's out of parentheses
- [`KT-27601`](https://youtrack.jetbrains.com/issue/KT-27601) False positive "Unused import directive" for extension function used in KDoc
- [`KT-28085`](https://youtrack.jetbrains.com/issue/KT-28085) "Convert receiver to parameter" introduces incorrect this@class in lambda
- [`KT-30028`](https://youtrack.jetbrains.com/issue/KT-30028) "Convert parameter to receiver" introduces wrong 'this' qualifier for extension lambda receiver
- [`KT-31601`](https://youtrack.jetbrains.com/issue/KT-31601) "Remove redundant let call" changes semantics by introducing multiple safe calls
- [`KT-31800`](https://youtrack.jetbrains.com/issue/KT-31800) False positive "never used" with function in private konst object expression
- [`KT-31912`](https://youtrack.jetbrains.com/issue/KT-31912) QF “Convert to anonymous object” do nothing on SAM-interfaces
- [`KT-32561`](https://youtrack.jetbrains.com/issue/KT-32561) "Property can be declared in constructor" causes another warning
- [`KT-32809`](https://youtrack.jetbrains.com/issue/KT-32809) Convert parameter to receiver inserts wrong qualifiers for this (when nothing needs to be changed)
- [`KT-34371`](https://youtrack.jetbrains.com/issue/KT-34371) "Surround with lambda" quickfix is not available for suspend lambda parameters.
- [`KT-34640`](https://youtrack.jetbrains.com/issue/KT-34640) Replace 'if' with 'when' leads to copy comment line above when from another if
- [`KT-36225`](https://youtrack.jetbrains.com/issue/KT-36225) KNPE: CodeInliner.processTypeParameterUsages with `ReplaceWith` for inline reified generic function
- [`KT-36266`](https://youtrack.jetbrains.com/issue/KT-36266) NPE when invoking Lift return out of if/when after intention becomes inapplicable but still beeing shown
- [`KT-36296`](https://youtrack.jetbrains.com/issue/KT-36296) False negative "Redundant SAM-constructor" with multiple SAM arguments
- [`KT-36367`](https://youtrack.jetbrains.com/issue/KT-36367) False negative "Redundant SAM-constructor" for kotlin functions
- [`KT-36368`](https://youtrack.jetbrains.com/issue/KT-36368) False negative "Redundant SAM-constructor" for fun interfaces in kotlin
- [`KT-36395`](https://youtrack.jetbrains.com/issue/KT-36395) False positive "Redundant SAM-constructor" with two java interfaces extending one another
- [`KT-36411`](https://youtrack.jetbrains.com/issue/KT-36411) "Put parameters on separate lines" and "Put parameters on one line" actions do not respect trailing comma
- [`KT-36482`](https://youtrack.jetbrains.com/issue/KT-36482) "Add JvmOverloads annotation" intention is still suggested for annotation's parameters
- [`KT-36686`](https://youtrack.jetbrains.com/issue/KT-36686) Implement members quickfix puts the implementation *before* the data class if it already has a body
- [`KT-36685`](https://youtrack.jetbrains.com/issue/KT-36685) "Convert to a range check" transform hex range to int if it is compared with "Less" or "Greater"
- [`KT-36707`](https://youtrack.jetbrains.com/issue/KT-36707) False positive redundant companion object on calling companion object members
- [`KT-36735`](https://youtrack.jetbrains.com/issue/KT-36735) Inspection 'Replace 'toString' with string template' miss curly braces and generates wrong code for constructor calls
- [`KT-36834`](https://youtrack.jetbrains.com/issue/KT-36834) Convert use-site targets and usages with convert property to fun intention
- [`KT-37213`](https://youtrack.jetbrains.com/issue/KT-37213) "Move to top level" intention does not update imports for extension functions
- [`KT-37496`](https://youtrack.jetbrains.com/issue/KT-37496) False positive "Remove redundant backticks" for multiple underscores variable name
- [`KT-37502`](https://youtrack.jetbrains.com/issue/KT-37502) False positive "redundant lambda arrow" with inline generic function with reified type in object and anonymous parameter name
- [`KT-37508`](https://youtrack.jetbrains.com/issue/KT-37508) "Convert receiver to parameter" breaks code in anonymous objects (this@ < no name provided >)
- [`KT-37576`](https://youtrack.jetbrains.com/issue/KT-37576) Kotlin InspectionSuppressor not being called for the kotlin's inspections
- [`KT-37749`](https://youtrack.jetbrains.com/issue/KT-37749) "Convert to anonymous object" intention is suggested for Java SAM conversion, but not for Kotlin
- [`KT-37781`](https://youtrack.jetbrains.com/issue/KT-37781) "Add modifier" intention/quickfix works incorrectly with functional interfaces
- [`KT-37893`](https://youtrack.jetbrains.com/issue/KT-37893) i18n: Incorrect quickfix name "Lift return out of '"

### IDE. KDoc

- [`KT-37361`](https://youtrack.jetbrains.com/issue/KT-37361) Support for showing rendered doc comments in editor

### IDE. Libraries

- [`KT-36276`](https://youtrack.jetbrains.com/issue/KT-36276) IDE: references to declarations in JavaScript KLib dependency are unresolved
- [`KT-37562`](https://youtrack.jetbrains.com/issue/KT-37562) IDE: references to JavaScript KLib dependency are unresolved, when project and library are compiled with "both" mode

### IDE. Navigation

- [`KT-18472`](https://youtrack.jetbrains.com/issue/KT-18472) UI lockup on find usages
- [`KT-18619`](https://youtrack.jetbrains.com/issue/KT-18619) Find Usages of element used via import alias does not show actual usage location
- [`KT-34088`](https://youtrack.jetbrains.com/issue/KT-34088) Navigate | Implementations action doesn't show implementations of Java methods in Kotlin files if method has parameters referring to generic type
- [`KT-35006`](https://youtrack.jetbrains.com/issue/KT-35006) IDE: "Navigate to inline function call site" from stack trace for nested inline call navigates to outer inline call
- [`KT-36138`](https://youtrack.jetbrains.com/issue/KT-36138) 628 second freeze when doing Find Usages on data class property
- [`KT-36218`](https://youtrack.jetbrains.com/issue/KT-36218) Show Kotlin file members in navigation bar
- [`KT-37494`](https://youtrack.jetbrains.com/issue/KT-37494) AnnotatedElementsSearch unable to find annotated property accessor

### IDE. Project View

- [`KT-32886`](https://youtrack.jetbrains.com/issue/KT-32886) Project tool window: Show Visibility Icons does nothing for Kotlin classes
- [`KT-37632`](https://youtrack.jetbrains.com/issue/KT-37632) IDE error on project structure opening

### IDE. Refactorings

#### Performance Improvements

- [`KT-37801`](https://youtrack.jetbrains.com/issue/KT-37801) Renaming private property with common name is very slow

#### Fixes

- [`KT-22733`](https://youtrack.jetbrains.com/issue/KT-22733) Refactor / Inline Function: fun with type parameter: KNPE at CodeInliner.processTypeParameterUsages()
- [`KT-27389`](https://youtrack.jetbrains.com/issue/KT-27389) MPP: Refactoring "Move Class" does not change the package declaration
- [`KT-29870`](https://youtrack.jetbrains.com/issue/KT-29870) Inline variable doesn't handle 'when' subject konst correctly
- [`KT-33045`](https://youtrack.jetbrains.com/issue/KT-33045) Cover Move Refactoring by statistics (FUS)
- [`KT-36071`](https://youtrack.jetbrains.com/issue/KT-36071) Refactoring: Move top declaration implementation refactoring
- [`KT-36072`](https://youtrack.jetbrains.com/issue/KT-36072) Empty files are removed on Refactor/Move action with turned off "Delete empty source files" option
- [`KT-36114`](https://youtrack.jetbrains.com/issue/KT-36114) java.lang.NoClassDefFoundError exception on Refactor/Move of kotlin function if it is referenced in java
- [`KT-36129`](https://youtrack.jetbrains.com/issue/KT-36129) java.lang.Throwable: Inkonstid file exception occurs on Refactor/Move of class from kotlin script
- [`KT-36382`](https://youtrack.jetbrains.com/issue/KT-36382) Move file refactoring breaks ktor application config
- [`KT-36504`](https://youtrack.jetbrains.com/issue/KT-36504) "Extract property" suggests potentially inkonstid name for new property
- [`KT-37637`](https://youtrack.jetbrains.com/issue/KT-37637) KotlinChangeSignatureUsageProcessor broke Change Signature in Python plugin
- [`KT-37797`](https://youtrack.jetbrains.com/issue/KT-37797) Useless "Value for new paramater" step in 'Update usages to reflect signature changes' for method with default parameter konstue
- [`KT-37822`](https://youtrack.jetbrains.com/issue/KT-37822) Improve message "Inline all references and remove the kind"
- [`KT-38348`](https://youtrack.jetbrains.com/issue/KT-38348) UL methods return signature without generic type parameters
- [`KT-38527`](https://youtrack.jetbrains.com/issue/KT-38527) Move nested class to upper level fails silently: MissingResourceException

### IDE. Script

- [`KT-37765`](https://youtrack.jetbrains.com/issue/KT-37765) NCDFE KJvmCompiledModuleInMemory on running `*.main.kts` script

### IDE. Tests Support

- [`KT-36716`](https://youtrack.jetbrains.com/issue/KT-36716) With `kotlin.gradle.testing.enabled=true`, gradle console output gets extra ijLog messages
- [`KT-36910`](https://youtrack.jetbrains.com/issue/KT-36910) There are no Run/Debug actions in context menu for non-JVM platform-specific test results
- [`KT-37037`](https://youtrack.jetbrains.com/issue/KT-37037) [JS, Debug] Node.JS test debug doesn't stop on breakpoints

### IDE. Wizards

#### New Features

- [`KT-36150`](https://youtrack.jetbrains.com/issue/KT-36150) New Project Wizard: provide a way to connect with "dependsOn" relation the added project modules
- [`KT-36179`](https://youtrack.jetbrains.com/issue/KT-36179) New Project Wizard: it's impossible to make a JVM target friendly to Java code in a multiplatform project

#### Fixes

- [`KT-35583`](https://youtrack.jetbrains.com/issue/KT-35583) New Project wizard: don't suggest build systems which cannot be used
- [`KT-35585`](https://youtrack.jetbrains.com/issue/KT-35585) New Project wizard: remember choices which have sense for many projects
- [`KT-35691`](https://youtrack.jetbrains.com/issue/KT-35691) New Project wizard: artifact and group konstues are effectively ignored
- [`KT-35693`](https://youtrack.jetbrains.com/issue/KT-35693) New Project wizard creates pom.xml / build.gradle referring to release Kotlin version only
- [`KT-36136`](https://youtrack.jetbrains.com/issue/KT-36136) New Project Wizard: generated projects are missing m2 Gradle repository
- [`KT-36137`](https://youtrack.jetbrains.com/issue/KT-36137) New Project Wizard: "multiplatform" shall be written as a single word, without the capital P in the middle
- [`KT-36155`](https://youtrack.jetbrains.com/issue/KT-36155) New Project Wizard: show warning "Multiplatform project cannot be generated" only for MPP projects
- [`KT-36162`](https://youtrack.jetbrains.com/issue/KT-36162) New Project Wizard: make the error messages in modules editor actionable
- [`KT-36163`](https://youtrack.jetbrains.com/issue/KT-36163) New Project Wizard: remove trailing spaces in Android SDK Path automatically
- [`KT-36166`](https://youtrack.jetbrains.com/issue/KT-36166) New Project Wizard: addition of Android target into a multiplatform project doesn't add a necessary minimal Android configuration
- [`KT-36169`](https://youtrack.jetbrains.com/issue/KT-36169) New Project Wizard: Android-related projects failed to build
- [`KT-36176`](https://youtrack.jetbrains.com/issue/KT-36176) New Project Wizard: module templates doesn't work for multiplatform projects
- [`KT-36177`](https://youtrack.jetbrains.com/issue/KT-36177) New Project Wizard: it's impossible to add more than one target of JVM kind to a multiplatform project
- [`KT-36180`](https://youtrack.jetbrains.com/issue/KT-36180) New Project Wizard: it's impossible to set target JVM version for a JVM module
- [`KT-36226`](https://youtrack.jetbrains.com/issue/KT-36226) New Project Wizard: add Mobile Android/iOS project template
- [`KT-36267`](https://youtrack.jetbrains.com/issue/KT-36267) New Project Wizard: flatten JVM targets list for multiplatform projects
- [`KT-36328`](https://youtrack.jetbrains.com/issue/KT-36328) New Project wizard fails for certain templates with AE: "Wrong line separators" at KotlinFormattingModelBuilder.createModel()
- [`KT-37599`](https://youtrack.jetbrains.com/issue/KT-37599) New Project Wizard: Open Kotlin Wizard via hyperlink
- [`KT-37667`](https://youtrack.jetbrains.com/issue/KT-37667) New project wizard: implement new UI design
- [`KT-37674`](https://youtrack.jetbrains.com/issue/KT-37674) Kotlin version in build files includes the IDEA version
- [`KT-38061`](https://youtrack.jetbrains.com/issue/KT-38061) New Project wizard 1.4: do not allow choosing build system if corresponding IJ plugin is disabled
- [`KT-38567`](https://youtrack.jetbrains.com/issue/KT-38567) New Project wizard 1.4+: Improve processing case when project with required path already exists
- [`KT-38579`](https://youtrack.jetbrains.com/issue/KT-38579) New Project wizard 1.4+: multiplatform mobile application: build fails on lint task: Configuration with name 'compileClasspath' not found
- [`KT-38929`](https://youtrack.jetbrains.com/issue/KT-38929) New project wizard: update libraries in project template according to kotlin IDE plugin version
- [`KT-38417`](https://youtrack.jetbrains.com/issue/KT-38417) Enable new project wizard by-default
- [`KT-38158`](https://youtrack.jetbrains.com/issue/KT-38158) java.lang.NullPointerException when try to create new project via standard wizard on Mac os

### JS. Tools

- [`KT-36484`](https://youtrack.jetbrains.com/issue/KT-36484) KotlinJS, MPP: Compilation throws "TypeError: b is not a function" only in production mode

### JavaScript

- [`KT-31126`](https://youtrack.jetbrains.com/issue/KT-31126) Inkonstid JS constructor call (primary ordinary -> secondary external)
- [`KT-35966`](https://youtrack.jetbrains.com/issue/KT-35966) Make @JsExport annotation usable in common code
- [`KT-37128`](https://youtrack.jetbrains.com/issue/KT-37128) KJS: StackOverflowException when using reified recursive bound for type parameter
- [`KT-37163`](https://youtrack.jetbrains.com/issue/KT-37163) KJS: NullPointerException on using intersection type as a reified one
- [`KT-37418`](https://youtrack.jetbrains.com/issue/KT-37418) Support `AssociatedObjectKey` and `findAssociatedObject` in JS IR BE

### Libraries

#### New Features

- [`KT-8658`](https://youtrack.jetbrains.com/issue/KT-8658) Add property delegates which call get/set on the given KProperty instance, e.g. a property reference
- [`KT-12448`](https://youtrack.jetbrains.com/issue/KT-12448) Make `@Suppress` applicable for type parameters
- [`KT-22932`](https://youtrack.jetbrains.com/issue/KT-22932) String.format should support null locale
- [`KT-23514`](https://youtrack.jetbrains.com/issue/KT-23514) assertFailsWith should link unexpected exception as cause
- [`KT-23737`](https://youtrack.jetbrains.com/issue/KT-23737) JS & MPP: Support exception cause and addSuppressed
- [`KT-25651`](https://youtrack.jetbrains.com/issue/KT-25651) Add shuffle() to Array<T>, ByteArray, IntArray, etc to match MutableList
- [`KT-26494`](https://youtrack.jetbrains.com/issue/KT-26494) Create an interface with provideDelegate()
- [`KT-27729`](https://youtrack.jetbrains.com/issue/KT-27729) Inherit `ReadWriteProperty` from `ReadOnlyProperty`
- [`KT-28290`](https://youtrack.jetbrains.com/issue/KT-28290) Add the onEach extension function to the Array
- [`KT-29182`](https://youtrack.jetbrains.com/issue/KT-29182) SIZE_BYTES/BITS for Float and Double
- [`KT-30372`](https://youtrack.jetbrains.com/issue/KT-30372) Add associateWith to Array<T>
- [`KT-33906`](https://youtrack.jetbrains.com/issue/KT-33906) Add vararg overloads for maxOf/minOf functions
- [`KT-34161`](https://youtrack.jetbrains.com/issue/KT-34161) Array.contentEquals/contentHashCode/contentToString should allow null array receiver and argument
- [`KT-35851`](https://youtrack.jetbrains.com/issue/KT-35851) Add setOfNotNull function
- [`KT-36866`](https://youtrack.jetbrains.com/issue/KT-36866) reduceIndexedOrNull
- [`KT-36955`](https://youtrack.jetbrains.com/issue/KT-36955) stdlib: Reverse range and sortDescending range
- [`KT-37161`](https://youtrack.jetbrains.com/issue/KT-37161) Add #onEachIndexed similar to #forEachIndexed
- [`KT-37603`](https://youtrack.jetbrains.com/issue/KT-37603) Throwable.stackTraceToString: string with detailed information about exception
- [`KT-37751`](https://youtrack.jetbrains.com/issue/KT-37751) Implement shuffled() method Sequences
- [`KT-37804`](https://youtrack.jetbrains.com/issue/KT-37804) Add 'fail' in kotlin-test that allows to specify cause
- [`KT-37839`](https://youtrack.jetbrains.com/issue/KT-37839) StringBuilder.appendLine in stdlib-common
- [`KT-37910`](https://youtrack.jetbrains.com/issue/KT-37910) Support Media Source Extension (MSE) and Encrypted Media Extensions (EME) in Kotlin/Js
- [`KT-38044`](https://youtrack.jetbrains.com/issue/KT-38044) Common Throwable.printStackTrace

#### Performance Improvements

- [`KT-37416`](https://youtrack.jetbrains.com/issue/KT-37416) readLine() is very slow

#### Fixes

- [`KT-13887`](https://youtrack.jetbrains.com/issue/KT-13887) Double/Float companion konstues such as NaN should be constants
- [`KT-14119`](https://youtrack.jetbrains.com/issue/KT-14119)  `String.toBoolean()` should be `String?.toBoolean()`
- [`KT-16529`](https://youtrack.jetbrains.com/issue/KT-16529) Names of KProperty's type parameters are inconsistent with ReadOnlyProperty/ReadWriteProperty
- [`KT-36356`](https://youtrack.jetbrains.com/issue/KT-36356) Specify which element Iterable.distinctBy(selector) retains
- [`KT-38060`](https://youtrack.jetbrains.com/issue/KT-38060) runningFold and runningReduce instead of scanReduce
- [`KT-38566`](https://youtrack.jetbrains.com/issue/KT-38566) Kotlin/JS IR: kx.serialization & ktor+JsonFeature: SerializationException: Can't locate argument-less serializer for class

### Reflection

- [`KT-29969`](https://youtrack.jetbrains.com/issue/KT-29969) Support optional vararg parameter in `KCallable.callBy`
- [`KT-37707`](https://youtrack.jetbrains.com/issue/KT-37707) "IllegalStateException: superInterface.classLoader must not be null" on class, which implements "AutoCloaseable" interface, "isAccessible" property changing

### Tools. CLI

- [`KT-37090`](https://youtrack.jetbrains.com/issue/KT-37090) file does not exist: `C:\Users\NK\DOWNLO~1\kotlin-compiler-1.3.61\kotlinc\bin\..\lib\kotlin-compiler.jar" from standalone compiler on Windows`

### Tools. Gradle

- [`KT-35447`](https://youtrack.jetbrains.com/issue/KT-35447) Warnings should be piped to stderr when using allWarningsAsErrors = true
- [`KT-35942`](https://youtrack.jetbrains.com/issue/KT-35942) User test Gradle source set code cannot reach out internal members from the production code
- [`KT-36019`](https://youtrack.jetbrains.com/issue/KT-36019) Implement Gradle DSL for explicit API mode

### Tools. Gradle. JS

#### New Features

- [`KT-32017`](https://youtrack.jetbrains.com/issue/KT-32017) Kotlin/JS in MPP: support changing the generated JS file name in Gradle DSL
- [`KT-32721`](https://youtrack.jetbrains.com/issue/KT-32721) [Gradle, JS] CSS Support for browser
- [`KT-36843`](https://youtrack.jetbrains.com/issue/KT-36843) [Gradle, JS, IR] Configure JS Compiler Type through DSL
- [`KT-37207`](https://youtrack.jetbrains.com/issue/KT-37207) Allow to use npm dependency from a local directory
- [`KT-38056`](https://youtrack.jetbrains.com/issue/KT-38056) [Gradle, JS] Group tasks by browser and node

#### Fixes

- [`KT-32466`](https://youtrack.jetbrains.com/issue/KT-32466) kotlinNpmResolve fails in the case of composite build
- [`KT-34468`](https://youtrack.jetbrains.com/issue/KT-34468) Consider custom versions while parsing yarn.lock
- [`KT-36489`](https://youtrack.jetbrains.com/issue/KT-36489) [Gradle, JS, IR]: Correct naming for both compilers
- [`KT-36784`](https://youtrack.jetbrains.com/issue/KT-36784) Kotlin. JS. MPP – Cannot find project :js when using Gradle composite builds
- [`KT-36864`](https://youtrack.jetbrains.com/issue/KT-36864) KJS. Composite build require JS plugin in root project
- [`KT-37240`](https://youtrack.jetbrains.com/issue/KT-37240) KJS. Nondeterministic execution order for webpack scripts (from folder 'webpack.config.d')
- [`KT-37582`](https://youtrack.jetbrains.com/issue/KT-37582) Kotlin/JS: KotlinWebpack non-nullable properties are shown as nullable in Gradle configuration
- [`KT-37587`](https://youtrack.jetbrains.com/issue/KT-37587) KJS. Karma ignore dynamically created webpack patches
- [`KT-37635`](https://youtrack.jetbrains.com/issue/KT-37635) [Gradle, JS] Webpack devtool provide enum for only 2 variants
- [`KT-37636`](https://youtrack.jetbrains.com/issue/KT-37636) [Gradle, JS] Extract package.json from klib
- [`KT-37762`](https://youtrack.jetbrains.com/issue/KT-37762) [Gradle, JS] Actualize Node and Yarn versions in 1.4
- [`KT-37988`](https://youtrack.jetbrains.com/issue/KT-37988) [Gradle, JS] Bump NPM versions on 1.4-M2
- [`KT-38051`](https://youtrack.jetbrains.com/issue/KT-38051) [Gradle, JS] browserDistribution doesn't provide outputs
- [`KT-38519`](https://youtrack.jetbrains.com/issue/KT-38519) JS Compiler per project without additional import

### Tools. Gradle. Multiplatform

- [`KT-36674`](https://youtrack.jetbrains.com/issue/KT-36674) `allMetadataJar` task fails if there is an empty intermediate source set in a multiplatform project with native targets
- [`KT-38746`](https://youtrack.jetbrains.com/issue/KT-38746) In HMPP, compilation of a shared-native source set could be mistakenly disabled
- [`KT-39094`](https://youtrack.jetbrains.com/issue/KT-39094) Provide a way to pass custom JVM args to commonizer from Gradle

### Tools. Gradle. Native

- [`KT-25887`](https://youtrack.jetbrains.com/issue/KT-25887) Kotlin Native gradle build fail with `endorsed is not supported. Endorsed standards and standalone APIs` on jdk > 8 & CLion
- [`KT-36721`](https://youtrack.jetbrains.com/issue/KT-36721) Deduce a fully qualified unique_name in klib manifest from something like group name
- [`KT-37730`](https://youtrack.jetbrains.com/issue/KT-37730) Native part of multiplatform build fails with "unresolved reference" errors if there is a local and external module with the same name
- [`KT-38174`](https://youtrack.jetbrains.com/issue/KT-38174) Kotlin/Native: Disable platform libraries generation at the user side by default

### Tools. J2K

#### Fixes

- [`KT-34965`](https://youtrack.jetbrains.com/issue/KT-34965) Convert function reference copied from function call
- [`KT-35593`](https://youtrack.jetbrains.com/issue/KT-35593) New J2K: method's names don't change between functions declared in Number.java and Number.kt
- [`KT-35897`](https://youtrack.jetbrains.com/issue/KT-35897) J2K converts private enum constructors to internal constructors and produces NON_PRIVATE_CONSTRUCTOR_IN_ENUM error
- [`KT-36088`](https://youtrack.jetbrains.com/issue/KT-36088) J2K:  StackOverflowError when trying to convert Java class with recursive type bound
- [`KT-36149`](https://youtrack.jetbrains.com/issue/KT-36149) J2K: PsiInkonstidElementAccessException: Element class com.intellij.psi.impl.source.tree.CompositeElement of type DOT_QUALIFIED_EXPRESSION
- [`KT-36152`](https://youtrack.jetbrains.com/issue/KT-36152) J2K: RuntimeException: Couldn't get containingKtFile for ktElement
- [`KT-36159`](https://youtrack.jetbrains.com/issue/KT-36159) J2K: ClassCastException if constructor contains a super() call and class extends from Kotlin class
- [`KT-36190`](https://youtrack.jetbrains.com/issue/KT-36190) J2K: Wrong property name generation when getter for non-boolean konstue starts with 'is'
- [`KT-36891`](https://youtrack.jetbrains.com/issue/KT-36891) j2k: Fail with java.lang.NoClassDefFoundError when converting array
- [`KT-37052`](https://youtrack.jetbrains.com/issue/KT-37052) new J2K: Java private function is converted to `private open` top-level function
- [`KT-37620`](https://youtrack.jetbrains.com/issue/KT-37620) new J2K: IndexOutOfBoundsException (DefaultArgumentsConversion.applyToElement) with overloaded function with vararg parameter
- [`KT-37919`](https://youtrack.jetbrains.com/issue/KT-37919) new J2K: Redundant line feeds when converting function

### Tools. JPS

- [`KT-37159`](https://youtrack.jetbrains.com/issue/KT-37159) A Typo (forgotten space) in build output in Circular dependencies warning description

### Tools. Scripts

- [`KT-30086`](https://youtrack.jetbrains.com/issue/KT-30086) ThreadDeath when running kotlin scripts using jsr223
- [`KT-37558`](https://youtrack.jetbrains.com/issue/KT-37558) Scripts: implicit receivers don't work correctly when using CompiledScriptJarsCache
- [`KT-37823`](https://youtrack.jetbrains.com/issue/KT-37823) Consecutive invocations of main.kts throw a KotlinReflectionNotSupportedError


## 1.4-M1

### Compiler

#### New Features

- [`KT-4240`](https://youtrack.jetbrains.com/issue/KT-4240) Type inference possible improvements: analyze lambda with expected type from the outer call
- [`KT-7304`](https://youtrack.jetbrains.com/issue/KT-7304) Smart-casts and generic calls with multiple bounds on type parameters
- [`KT-7745`](https://youtrack.jetbrains.com/issue/KT-7745) Support named arguments in their own position even if the result appears as mixed
- [`KT-7770`](https://youtrack.jetbrains.com/issue/KT-7770) SAM for Kotlin classes
- [`KT-8834`](https://youtrack.jetbrains.com/issue/KT-8834) Support function references with default konstues as other function types
- [`KT-10930`](https://youtrack.jetbrains.com/issue/KT-10930) Expected type isn't taken into account for delegated properties
- [`KT-11723`](https://youtrack.jetbrains.com/issue/KT-11723) Support coercion to Unit in callable reference resolution
- [`KT-14416`](https://youtrack.jetbrains.com/issue/KT-14416) Support of @PolymorphicSignature in Kotlin compiler
- [`KT-16873`](https://youtrack.jetbrains.com/issue/KT-16873) Support COERSION_TO_UNIT for suspend lambdas
- [`KT-17643`](https://youtrack.jetbrains.com/issue/KT-17643) Inferring type of Pair based on known Map type
- [`KT-19869`](https://youtrack.jetbrains.com/issue/KT-19869) Support function references to functions with vararg if expected type ends with repeated vararg element type
- [`KT-21178`](https://youtrack.jetbrains.com/issue/KT-21178) Prohibit access of protected members inside public inline members
- [`KT-21368`](https://youtrack.jetbrains.com/issue/KT-21368) Improve type inference
- [`KT-25866`](https://youtrack.jetbrains.com/issue/KT-25866) Iterable.forEach does not accept functions that return non-Unit konstues
- [`KT-26165`](https://youtrack.jetbrains.com/issue/KT-26165) Support VarHandle in JVM codegen
- [`KT-27582`](https://youtrack.jetbrains.com/issue/KT-27582) Allow contracts on final non-override members
- [`KT-28298`](https://youtrack.jetbrains.com/issue/KT-28298) Allow references to generic (reified) type parameters in contracts
- [`KT-31230`](https://youtrack.jetbrains.com/issue/KT-31230) Refine rules for allowed Array-based class literals on different platforms: allow `Array::class` everywhere, disallow `Array<...>::class` on non-JVM
- [`KT-31244`](https://youtrack.jetbrains.com/issue/KT-31244) Choose Java field during overload resolution with a pure Kotlin property
- [`KT-31734`](https://youtrack.jetbrains.com/issue/KT-31734) Empty parameter list required on Annotations of function types
- [`KT-33990`](https://youtrack.jetbrains.com/issue/KT-33990) Type argument isn't checked during resolution part
- [`KT-33413`](https://youtrack.jetbrains.com/issue/KT-33413) Allow 'break' and 'continue' in 'when' statement to point to innermost surrounding loop
- [`KT-34743`](https://youtrack.jetbrains.com/issue/KT-34743) Support trailing comma in the compiler
- [`KT-34847`](https://youtrack.jetbrains.com/issue/KT-34847) Lift restrictions from `kotlin.Result`

#### Fixes

- [`KT-2869`](https://youtrack.jetbrains.com/issue/KT-2869) Incorrect resolve with 'unsafe call error' and generics
- [`KT-3630`](https://youtrack.jetbrains.com/issue/KT-3630) Extension property (generic function type) does not work
- [`KT-3668`](https://youtrack.jetbrains.com/issue/KT-3668) Infer type parameters for extension 'get' in delegated property
- [`KT-3850`](https://youtrack.jetbrains.com/issue/KT-3850) Receiver check fails when type parameter has another parameter as a bound
- [`KT-3884`](https://youtrack.jetbrains.com/issue/KT-3884) Generic candidate with contradiction is preferred over matching global function
- [`KT-4625`](https://youtrack.jetbrains.com/issue/KT-4625) Poor error highlighting when assigning not matched type to index operator
- [`KT-5449`](https://youtrack.jetbrains.com/issue/KT-5449) Wrong resolve when functions differ only in the nullability of generic type
- [`KT-5606`](https://youtrack.jetbrains.com/issue/KT-5606) "Type mismatch" in Java constructor call with SAM lambda and `vararg` parameter
- [`KT-6005`](https://youtrack.jetbrains.com/issue/KT-6005) Type inference problem in sam constructors
- [`KT-6591`](https://youtrack.jetbrains.com/issue/KT-6591) Overloaded generic extension function call with null argument resolved incorrectly
- [`KT-6812`](https://youtrack.jetbrains.com/issue/KT-6812) Type inference fails when passing a null instead of a generic type
- [`KT-7298`](https://youtrack.jetbrains.com/issue/KT-7298) Bogus type inference error in generic method call translated from Java
- [`KT-7301`](https://youtrack.jetbrains.com/issue/KT-7301) Type inference error in Kotlin code translated from Java
- [`KT-7333`](https://youtrack.jetbrains.com/issue/KT-7333) Type inference fails with star-projections in code translated from Java
- [`KT-7363`](https://youtrack.jetbrains.com/issue/KT-7363) Kotlin code with star-projections translated from Java does not typecheck
- [`KT-7378`](https://youtrack.jetbrains.com/issue/KT-7378) 3-dimension array type inference fail
- [`KT-7410`](https://youtrack.jetbrains.com/issue/KT-7410) Call resolution error appears only after adding non-applicable overload
- [`KT-7420`](https://youtrack.jetbrains.com/issue/KT-7420) Type inference sometimes infers less specific type than in Java
- [`KT-7758`](https://youtrack.jetbrains.com/issue/KT-7758) Type of lambda can't be infered
- [`KT-8218`](https://youtrack.jetbrains.com/issue/KT-8218) Wrong 'equals' for generic types with platform type and error type
- [`KT-8265`](https://youtrack.jetbrains.com/issue/KT-8265) Non-typesafe program is compiled without errors
- [`KT-8637`](https://youtrack.jetbrains.com/issue/KT-8637) Useless diagnostics for type parameters with unsafe nullability
- [`KT-8966`](https://youtrack.jetbrains.com/issue/KT-8966) Smart casts don't work with implicit receiver and extension on type parameter with bounds
- [`KT-10265`](https://youtrack.jetbrains.com/issue/KT-10265) Type inference problem when using sealed class and interfaces
- [`KT-10364`](https://youtrack.jetbrains.com/issue/KT-10364) Call completeCall on variable before invoke resolution
- [`KT-10612`](https://youtrack.jetbrains.com/issue/KT-10612) java.util.Comparator.comparing type inference
- [`KT-10628`](https://youtrack.jetbrains.com/issue/KT-10628) Wrong type mismatch with star projection of inner class inside use-site projected type
- [`KT-10662`](https://youtrack.jetbrains.com/issue/KT-10662) Smartcast with not-null assertion
- [`KT-10681`](https://youtrack.jetbrains.com/issue/KT-10681) Explicit type arguments not taken into account when determining applicable overloads of a generic function
- [`KT-10755`](https://youtrack.jetbrains.com/issue/KT-10755) Not "least" common super-type is selected for nested 'if' result in presence of multiple inheritance
- [`KT-10929`](https://youtrack.jetbrains.com/issue/KT-10929) Type inference based on receiver type doesn't work for delegated properties in some cases
- [`KT-10962`](https://youtrack.jetbrains.com/issue/KT-10962) Wrong resolution when argument has unstable DataFlowValue
- [`KT-11108`](https://youtrack.jetbrains.com/issue/KT-11108) RxJava failed platform type inference
- [`KT-11137`](https://youtrack.jetbrains.com/issue/KT-11137) Java synthetic property does not function for a type with projection
- [`KT-11144`](https://youtrack.jetbrains.com/issue/KT-11144) UninferredParameterTypeConstructor exception during build
- [`KT-11184`](https://youtrack.jetbrains.com/issue/KT-11184) Type inference failed for combination of safe-call, elvis, HashSet and emptySet
- [`KT-11218`](https://youtrack.jetbrains.com/issue/KT-11218) Type inference incorrectly infers nullable type for type parameter
- [`KT-11323`](https://youtrack.jetbrains.com/issue/KT-11323) Type inference failed in call with lambda returning emptyList
- [`KT-11331`](https://youtrack.jetbrains.com/issue/KT-11331) Unexpected "Type inference failed" in SAM-conversion to projected type
- [`KT-11444`](https://youtrack.jetbrains.com/issue/KT-11444) Type inference fails
- [`KT-11664`](https://youtrack.jetbrains.com/issue/KT-11664) Disfunctional inference with nullable type parameters
- [`KT-11894`](https://youtrack.jetbrains.com/issue/KT-11894) Type substitution bug related platform types
- [`KT-11897`](https://youtrack.jetbrains.com/issue/KT-11897) No error REIFIED_TYPE_FORBIDDEN_SUBSTITUTION on captured type
- [`KT-11898`](https://youtrack.jetbrains.com/issue/KT-11898) Type inference error related to captured types
- [`KT-12036`](https://youtrack.jetbrains.com/issue/KT-12036) Type inference failed
- [`KT-12038`](https://youtrack.jetbrains.com/issue/KT-12038) non-null checks and inference
- [`KT-12190`](https://youtrack.jetbrains.com/issue/KT-12190) Type inference for TreeMap type parameters from expected type doesn't work when passing comparator.
- [`KT-12684`](https://youtrack.jetbrains.com/issue/KT-12684) A problem with reified type-parameters and smart-casts
- [`KT-12833`](https://youtrack.jetbrains.com/issue/KT-12833) 'it' does not work in typed containers of lambdas
- [`KT-13002`](https://youtrack.jetbrains.com/issue/KT-13002) "Error type encountered: UninferredParameterTypeConstructor" with elvis and when
- [`KT-13028`](https://youtrack.jetbrains.com/issue/KT-13028) cast with star on on type with contravariant generic parameter makes the compiler crash
- [`KT-13339`](https://youtrack.jetbrains.com/issue/KT-13339) Type inference failed for synthetic Java property call on implicit smart cast receiver
- [`KT-13398`](https://youtrack.jetbrains.com/issue/KT-13398) "Type T is not a subtype of Any"
- [`KT-13683`](https://youtrack.jetbrains.com/issue/KT-13683) Type inference incorporation error when passed null into not-null parameter
- [`KT-13721`](https://youtrack.jetbrains.com/issue/KT-13721) Type inference fails when function arguments are involved
- [`KT-13725`](https://youtrack.jetbrains.com/issue/KT-13725) Type inference fails to infer type with array as a generic argument
- [`KT-13800`](https://youtrack.jetbrains.com/issue/KT-13800) Type inference fails when null passed as argument (related to common system)
- [`KT-13934`](https://youtrack.jetbrains.com/issue/KT-13934) Callable reference to companion object member via class name is not resolved
- [`KT-13964`](https://youtrack.jetbrains.com/issue/KT-13964) Unknown descriptor on compiling invoke operator call with generic parameter
- [`KT-13965`](https://youtrack.jetbrains.com/issue/KT-13965) Invoke operator called on extension property of generic type has incorrect parameter type
- [`KT-13992`](https://youtrack.jetbrains.com/issue/KT-13992) Incorrect TYPE_INFERENCE_UPPER_BOUND_VIOLATED
- [`KT-14101`](https://youtrack.jetbrains.com/issue/KT-14101) Type inference failed on null
- [`KT-14174`](https://youtrack.jetbrains.com/issue/KT-14174) Compiler can't infer type when it should be able to
- [`KT-14351`](https://youtrack.jetbrains.com/issue/KT-14351) Internal error with uninferred types for compiling complicated when expression
- [`KT-14460`](https://youtrack.jetbrains.com/issue/KT-14460) Smartcast isn't considered as necessary in the last expression of lambda
- [`KT-14463`](https://youtrack.jetbrains.com/issue/KT-14463) Missing MEMBER_PROJECTED_OUT error after smartcast with invoke
- [`KT-14499`](https://youtrack.jetbrains.com/issue/KT-14499) Type inference fails even if we specify `T` explicitly
- [`KT-14725`](https://youtrack.jetbrains.com/issue/KT-14725) kotlin generics makes agera's compiled repositories unusable
- [`KT-14803`](https://youtrack.jetbrains.com/issue/KT-14803) Weird smart cast absence in lazy delegate
- [`KT-14972`](https://youtrack.jetbrains.com/issue/KT-14972) Type inference failed: wrong common supertype for types with several subtypes
- [`KT-14980`](https://youtrack.jetbrains.com/issue/KT-14980) Type inference for nullable type in if statement
- [`KT-15155`](https://youtrack.jetbrains.com/issue/KT-15155) False "No cast needed" warning for conversion to star-projected type
- [`KT-15185`](https://youtrack.jetbrains.com/issue/KT-15185) Inference for coroutines not work in case where we have suspend function with new coroutine inside
- [`KT-15263`](https://youtrack.jetbrains.com/issue/KT-15263) Kotlin can't infer the type of a multiple bounded method
- [`KT-15389`](https://youtrack.jetbrains.com/issue/KT-15389) Type inference for coroutines in 1.1-M04
- [`KT-15394`](https://youtrack.jetbrains.com/issue/KT-15394) Support coercion to Unit for last statement for suspend lambda
- [`KT-15396`](https://youtrack.jetbrains.com/issue/KT-15396) Type inference for last statement in coroutine
- [`KT-15488`](https://youtrack.jetbrains.com/issue/KT-15488) Type inference not working on generic function returning generic type in lambda result
- [`KT-15648`](https://youtrack.jetbrains.com/issue/KT-15648) Better use of overladed functions with nullables
- [`KT-15922`](https://youtrack.jetbrains.com/issue/KT-15922) TYPE_INFERENCE_CONFLICTING_SUBSTITUTION for function reference
- [`KT-15923`](https://youtrack.jetbrains.com/issue/KT-15923) Internal error: empty intersection for types
- [`KT-16247`](https://youtrack.jetbrains.com/issue/KT-16247) Overload resolution ambiguity with intersection types and method references
- [`KT-16249`](https://youtrack.jetbrains.com/issue/KT-16249) Can't call generic method with overloaded method reference when non-generic overload exists
- [`KT-16421`](https://youtrack.jetbrains.com/issue/KT-16421) Remove @ParameterName annotation from diagnostic messages
- [`KT-16480`](https://youtrack.jetbrains.com/issue/KT-16480) Wrong "Type mismatch" for variable as function call
- [`KT-16591`](https://youtrack.jetbrains.com/issue/KT-16591) Type inferencing doesn't consider common base class of T of multiple Foo<T> function parameters
- [`KT-16678`](https://youtrack.jetbrains.com/issue/KT-16678) Overload resolution ambiguity on println()
- [`KT-16844`](https://youtrack.jetbrains.com/issue/KT-16844) Recursive dependency] (DeferredType) error on code generation.
- [`KT-16869`](https://youtrack.jetbrains.com/issue/KT-16869) Cannot infer type parameter S in fun <S, T : S?> f(x: S, g: (S) -> T): T
- [`KT-17018`](https://youtrack.jetbrains.com/issue/KT-17018) "Rewrite at slice LEXICAL_SCOPE" for 'when' with function reference inside lambda
- [`KT-17048`](https://youtrack.jetbrains.com/issue/KT-17048) Compilation exception: error type encountered in some combination of when/elvis with multiple inheritance
- [`KT-17340`](https://youtrack.jetbrains.com/issue/KT-17340) Type inference failed on overloaded method reference with expected KFunction
- [`KT-17386`](https://youtrack.jetbrains.com/issue/KT-17386) Smart cast on LHS of callable reference doesn't work if expected type is nullable
- [`KT-17487`](https://youtrack.jetbrains.com/issue/KT-17487) ReenteringLazyValueComputationException for interface with a subclass that has a property of interface type
- [`KT-17552`](https://youtrack.jetbrains.com/issue/KT-17552) Call completed not running for elvis as last expression in lambda
- [`KT-17799`](https://youtrack.jetbrains.com/issue/KT-17799) Smart cast doesn't work for callable reference (colon-colon operator)
- [`KT-17968`](https://youtrack.jetbrains.com/issue/KT-17968) More type inference problems with Streams.collect
- [`KT-17995`](https://youtrack.jetbrains.com/issue/KT-17995) No type mismatch with empty `when` expression inside lambda
- [`KT-18002`](https://youtrack.jetbrains.com/issue/KT-18002) Issue with star projection for generic type with recursive upper bound
- [`KT-18014`](https://youtrack.jetbrains.com/issue/KT-18014) Cannot use Comparator.comparing
- [`KT-18080`](https://youtrack.jetbrains.com/issue/KT-18080) Type inference failed for generic method reference argument
- [`KT-18192`](https://youtrack.jetbrains.com/issue/KT-18192) Type inference fails for some nested generic type structures
- [`KT-18207`](https://youtrack.jetbrains.com/issue/KT-18207) Unexpected attempt to smart cast causes compile error
- [`KT-18379`](https://youtrack.jetbrains.com/issue/KT-18379) Type inference failed although explicit specified
- [`KT-18401`](https://youtrack.jetbrains.com/issue/KT-18401) Type mismatch when inferring out type parameter (related to callable reference)
- [`KT-18481`](https://youtrack.jetbrains.com/issue/KT-18481) ReenteringLazyValueComputationException for recursive const declaration without explicit type
- [`KT-18541`](https://youtrack.jetbrains.com/issue/KT-18541) Prohibit "tailrec" modifier on open functions
- [`KT-18790`](https://youtrack.jetbrains.com/issue/KT-18790) function take the parameterized type with multi-bounds can't working
- [`KT-19139`](https://youtrack.jetbrains.com/issue/KT-19139) Kotlin build error in Android Studio on << intent.putExtra(“string”, it.getString(“string”) >> inside "else if" block
- [`KT-19751`](https://youtrack.jetbrains.com/issue/KT-19751) Nested conditionals break type inference when multiple types are possible for the expression
- [`KT-19880`](https://youtrack.jetbrains.com/issue/KT-19880) An overload with nullable parameter is not resolved due to a smart cast attempt after assignment
- [`KT-19884`](https://youtrack.jetbrains.com/issue/KT-19884) type inference with type parameter
- [`KT-20226`](https://youtrack.jetbrains.com/issue/KT-20226) Return type 'Any' inferred for lambda with early return with integer literal in one of possible return konstues
- [`KT-20656`](https://youtrack.jetbrains.com/issue/KT-20656) Callable reference breaks smart cast for next statement
- [`KT-20734`](https://youtrack.jetbrains.com/issue/KT-20734) Inkonstid "Smart-cast impossible" when there are applicable extension and semi-applicable member
- [`KT-20817`](https://youtrack.jetbrains.com/issue/KT-20817) Coroutine builder type inference does not take smart casts into account (confusing)
- [`KT-21060`](https://youtrack.jetbrains.com/issue/KT-21060) ReenteringLazyValueComputationException during type inference
- [`KT-21396`](https://youtrack.jetbrains.com/issue/KT-21396) if-else with a branch of type Nothing fails to infer receiver type
- [`KT-21463`](https://youtrack.jetbrains.com/issue/KT-21463) Compiler doesn't take into accout a type parameter upper bound if a corresponding type argument is in projection
- [`KT-21607`](https://youtrack.jetbrains.com/issue/KT-21607) Type inference fails with intermediate function
- [`KT-21694`](https://youtrack.jetbrains.com/issue/KT-21694) Type inference failed for ObservableList
- [`KT-22012`](https://youtrack.jetbrains.com/issue/KT-22012) Kotlin type deduction does not handle Java's <? super T> constructs
- [`KT-22022`](https://youtrack.jetbrains.com/issue/KT-22022) ReenteringLazyValueComputationException when having cyclic references to static variables
- [`KT-22032`](https://youtrack.jetbrains.com/issue/KT-22032) Multiple type parameter bounds & smart casts
- [`KT-22043`](https://youtrack.jetbrains.com/issue/KT-22043) Report an error when comparing enum (==/!=/when) to any other incompatible type since 1.4
- [`KT-22070`](https://youtrack.jetbrains.com/issue/KT-22070) Type inference issue with platform types and SAM conversion
- [`KT-22474`](https://youtrack.jetbrains.com/issue/KT-22474) Type safety problem because of incorrect subtyping for intersection types
- [`KT-22636`](https://youtrack.jetbrains.com/issue/KT-22636) Anonymous function can be passed as a suspending one, failing at runtime
- [`KT-22723`](https://youtrack.jetbrains.com/issue/KT-22723) Inconsistent behavior of floating-point number comparisons
- [`KT-22775`](https://youtrack.jetbrains.com/issue/KT-22775) Type inference failed when yielding smartcasted not null konstue from `sequence`
- [`KT-22885`](https://youtrack.jetbrains.com/issue/KT-22885) Broken type safety with function variables caused by wrong subtyping check that includes intersection types
- [`KT-23141`](https://youtrack.jetbrains.com/issue/KT-23141) Cannot resolve between two functions when only one of them accepts nullable parameter
- [`KT-23156`](https://youtrack.jetbrains.com/issue/KT-23156) IDEA asking me to remove uninferable explicit type signature
- [`KT-23391`](https://youtrack.jetbrains.com/issue/KT-23391) Bogus type inference error with smart cast, synthetic property and star projection
- [`KT-23475`](https://youtrack.jetbrains.com/issue/KT-23475) Implicit invoke on property with generic functional return type resolves incorrectly
- [`KT-23482`](https://youtrack.jetbrains.com/issue/KT-23482) Incorrect frontend behaviour for not inferred type for generic property with function type as result
- [`KT-23677`](https://youtrack.jetbrains.com/issue/KT-23677) Incorrect error diagnostic for return types that coerced to 'Unit' inside lambda
- [`KT-23748`](https://youtrack.jetbrains.com/issue/KT-23748) Erroneous type mismatch when call with @Exact annotation depends on non-fixed type variable
- [`KT-23755`](https://youtrack.jetbrains.com/issue/KT-23755) Nested lambdas with nested types can cause false USELESS_CAST warning
- [`KT-23791`](https://youtrack.jetbrains.com/issue/KT-23791) Explicit type argument leads to an error, while candidate is picked correctly without it
- [`KT-23992`](https://youtrack.jetbrains.com/issue/KT-23992) Target prefixes for annotations on supertype list elements are not checked
- [`KT-24143`](https://youtrack.jetbrains.com/issue/KT-24143) Type-checking error for generic functions
- [`KT-24217`](https://youtrack.jetbrains.com/issue/KT-24217) Type inference for generic functions reference problem.
- [`KT-24237`](https://youtrack.jetbrains.com/issue/KT-24237) Uninferred type parameter on variable with multiple smart casts in elvis
- [`KT-24317`](https://youtrack.jetbrains.com/issue/KT-24317) Unnecessary non-null assertion inspection shown
- [`KT-24341`](https://youtrack.jetbrains.com/issue/KT-24341) Compiler exception instead of error about "upper bound violated"
- [`KT-24355`](https://youtrack.jetbrains.com/issue/KT-24355) Overload resolution ambiguity due to unstable smartcast
- [`KT-24458`](https://youtrack.jetbrains.com/issue/KT-24458) Cannot solve “Conditional branch result is implicitly cast to Any” with List
- [`KT-24493`](https://youtrack.jetbrains.com/issue/KT-24493) Incorrect error about Array<Nothing> unsupported
- [`KT-24886`](https://youtrack.jetbrains.com/issue/KT-24886) Type inference failure in an if-else_if-else
- [`KT-24918`](https://youtrack.jetbrains.com/issue/KT-24918) type inference not good enough for return type with involved if statement
- [`KT-24920`](https://youtrack.jetbrains.com/issue/KT-24920) Type inference failure when using RxJava compose
- [`KT-24993`](https://youtrack.jetbrains.com/issue/KT-24993) Inference for buildSequence/yield doesn't work for labeled lambdas
- [`KT-25063`](https://youtrack.jetbrains.com/issue/KT-25063) List of Arrays - wrong type inferred if nullable involved
- [`KT-25268`](https://youtrack.jetbrains.com/issue/KT-25268) Incorrect type inference (least upper bound of the types) in exhaustive when for Number
- [`KT-25294`](https://youtrack.jetbrains.com/issue/KT-25294) Reactors Mono map function with Kotlin
- [`KT-25306`](https://youtrack.jetbrains.com/issue/KT-25306) Make f(a = arrayOf(x)) equikonstent to f(a = *arrayOf(x))
- [`KT-25342`](https://youtrack.jetbrains.com/issue/KT-25342) Type inference: lazy with generic fails to infer
- [`KT-25434`](https://youtrack.jetbrains.com/issue/KT-25434) Too eager smartcast cause type mismatch
- [`KT-25585`](https://youtrack.jetbrains.com/issue/KT-25585) "Rewrite at slice LEXICAL_SCOPE" for `if` expression with callable reference inside lambda
- [`KT-25656`](https://youtrack.jetbrains.com/issue/KT-25656) Short-hand array literal notation type inference fails in nested annotations
- [`KT-25675`](https://youtrack.jetbrains.com/issue/KT-25675) `return when` leads to compiler error
- [`KT-25721`](https://youtrack.jetbrains.com/issue/KT-25721) Type inference does not work in nested try catch
- [`KT-25827`](https://youtrack.jetbrains.com/issue/KT-25827) NullPointerException in setResultingSubstitutor with a generic base class
- [`KT-25841`](https://youtrack.jetbrains.com/issue/KT-25841) Collection type is inferred as Collection<Any> if the konstues returned from `map` function have an intersection common type
- [`KT-25942`](https://youtrack.jetbrains.com/issue/KT-25942) Pass a parameter to generic function with several upper bounds
- [`KT-26157`](https://youtrack.jetbrains.com/issue/KT-26157) Failed type inference for `Delegates.observable` parameter with a nullable property
- [`KT-26264`](https://youtrack.jetbrains.com/issue/KT-26264) No smart cast on implicit receiver extension function call
- [`KT-26638`](https://youtrack.jetbrains.com/issue/KT-26638) Check for repeatablilty of annotations doesn't take into account annotations with use-site target
- [`KT-26698`](https://youtrack.jetbrains.com/issue/KT-26698) OnlyInputTypes doesn't work in presence of upper bound constraint
- [`KT-26704`](https://youtrack.jetbrains.com/issue/KT-26704) Correct variable-as-function call fails to compile in case of function type dependent on variable type
- [`KT-27440`](https://youtrack.jetbrains.com/issue/KT-27440) Infer lambda parameter type before lambda analysis in common system
- [`KT-27464`](https://youtrack.jetbrains.com/issue/KT-27464) False negative type mismatch when a non-null assertion is used on a top-level `var` mutable variable
- [`KT-27606`](https://youtrack.jetbrains.com/issue/KT-27606) Incorrect type inference when common supertype of enum classes is involved
- [`KT-27722`](https://youtrack.jetbrains.com/issue/KT-27722) mapNotNull on List<Result<T>> fails if T is not subtype of Any
- [`KT-27781`](https://youtrack.jetbrains.com/issue/KT-27781) ReenteringLazyValueComputationException for anonymous object property referring to itself in overridden method
- [`KT-27799`](https://youtrack.jetbrains.com/issue/KT-27799) Prohibit references to reified type parameters in annotation arguments in local classes / anonymous objects
- [`KT-28083`](https://youtrack.jetbrains.com/issue/KT-28083) Nothing-call smart cast of a variable that a lambda returns does not affect the lambda's return type
- [`KT-28111`](https://youtrack.jetbrains.com/issue/KT-28111) Type inference fails for return type of lambda with constrained generic
- [`KT-28242`](https://youtrack.jetbrains.com/issue/KT-28242) Not-null assertion operator doesn't affect smartcast types
- [`KT-28264`](https://youtrack.jetbrains.com/issue/KT-28264) Resulting type of a safe call should produce type that is intersected with Any
- [`KT-28305`](https://youtrack.jetbrains.com/issue/KT-28305) "Error type encountered" with elvis operator, type argument and explicit `Any` type
- [`KT-28319`](https://youtrack.jetbrains.com/issue/KT-28319) "resultingDescriptor shouldn't be null" for a star projection base class
- [`KT-28334`](https://youtrack.jetbrains.com/issue/KT-28334) Smartcast doesn't work if original type is type with star projection and there was already another smartcast
- [`KT-28370`](https://youtrack.jetbrains.com/issue/KT-28370) Var not-null smartcasts are wrong if reassignments are used inside catch section (in try-catch) or try section (in try-finally)
- [`KT-28424`](https://youtrack.jetbrains.com/issue/KT-28424) Type annotations are not analyzed properly in several cases
- [`KT-28584`](https://youtrack.jetbrains.com/issue/KT-28584) Complex condition fails smart cast
- [`KT-28614`](https://youtrack.jetbrains.com/issue/KT-28614) "Error type encountered: UninferredParameterTypeConstructor"
- [`KT-28726`](https://youtrack.jetbrains.com/issue/KT-28726) Recursion in type inference causes ReenteringLazyValueComputationException
- [`KT-28837`](https://youtrack.jetbrains.com/issue/KT-28837) SAM conversion doesn't work for arguments of members imported from object
- [`KT-28873`](https://youtrack.jetbrains.com/issue/KT-28873) Function reference resolution ambiguity is not reported
- [`KT-28999`](https://youtrack.jetbrains.com/issue/KT-28999) Prohibit type parameters for anonymous objects
- [`KT-28951`](https://youtrack.jetbrains.com/issue/KT-28951) "Error type encountered" with elvis operator, type argument and explicit `Any` type
- [`KT-29014`](https://youtrack.jetbrains.com/issue/KT-29014) Unclear diagnostic for conditional branches coercion with multiple root interfaces and no common superclass
- [`KT-29079`](https://youtrack.jetbrains.com/issue/KT-29079) `ByteArray.let(::String)` can not compile as the callable reference is resolved before the outer call
- [`KT-29258`](https://youtrack.jetbrains.com/issue/KT-29258) Incorrect return type is used for analysis of an extension lambda (if its type is used as a generic parameter)
- [`KT-29330`](https://youtrack.jetbrains.com/issue/KT-29330) NI: Multiple duplicate error messages in IDE popup with lambda argument
- [`KT-29402`](https://youtrack.jetbrains.com/issue/KT-29402) Missed INLINE_FROM_HIGHER_PLATFORM diagnostic if inline function called from Derived class
- [`KT-29515`](https://youtrack.jetbrains.com/issue/KT-29515) type inference fails to infer T of KFunction0 for most types
- [`KT-29712`](https://youtrack.jetbrains.com/issue/KT-29712) Incorrect compiler warning, "No cast needed" for recursive type bound
- [`KT-29876`](https://youtrack.jetbrains.com/issue/KT-29876) ReenteringLazyValueComputationException with usage of property inside lambda during assignment to that property
- [`KT-29911`](https://youtrack.jetbrains.com/issue/KT-29911) Not-null smart cast fails inside inline lambda after safe call on generic variable with nullable upper bound
- [`KT-29943`](https://youtrack.jetbrains.com/issue/KT-29943) Callable reference resolution ambiguity between a property and a function is reported incorrectly in function calls expecting KProperty1
- [`KT-29949`](https://youtrack.jetbrains.com/issue/KT-29949) Cannot chose among overload as soon as nullable type is involved
- [`KT-30151`](https://youtrack.jetbrains.com/issue/KT-30151) Any instead of Number is inferred as common super type of Int and Double
- [`KT-30176`](https://youtrack.jetbrains.com/issue/KT-30176) Compiler can't infer correct type argument which blocks overload resolution
- [`KT-30240`](https://youtrack.jetbrains.com/issue/KT-30240) Can't infer intersection type for a type variable with several bounds
- [`KT-30278`](https://youtrack.jetbrains.com/issue/KT-30278) Retain star projections in typeOf
- [`KT-30394`](https://youtrack.jetbrains.com/issue/KT-30394) Different behaviour in type inferences (bug in the old inference) when cast of variable of nullable type parameter to not-null is used
- [`KT-30496`](https://youtrack.jetbrains.com/issue/KT-30496) Wrong infix generic extension function is chosen for `null` receiver
- [`KT-30550`](https://youtrack.jetbrains.com/issue/KT-30550) Suspend modifier on a functional type changes resolution of return type
- [`KT-30892`](https://youtrack.jetbrains.com/issue/KT-30892) IllegalStateException(UninferredParameterTypeConstructor) with return references to local function from if-else expression
- [`KT-30947`](https://youtrack.jetbrains.com/issue/KT-30947) No smart cast on accessing generic class member with upper-bounded type argument
- [`KT-31102`](https://youtrack.jetbrains.com/issue/KT-31102) Type mismatch in mixing lambda and callable reference
- [`KT-31151`](https://youtrack.jetbrains.com/issue/KT-31151) "IllegalStateException: Error type encountered" with elvis, when and inheritance
- [`KT-31219`](https://youtrack.jetbrains.com/issue/KT-31219) Type mismatch for delegated property depending on anonymous object
- [`KT-31290`](https://youtrack.jetbrains.com/issue/KT-31290) overload resolution ambiguity with Iterable<T>.mapTo()
- [`KT-31352`](https://youtrack.jetbrains.com/issue/KT-31352) JvmName can't eliminate platform declaration clash of annotated properties
- [`KT-31532`](https://youtrack.jetbrains.com/issue/KT-31532) Type inference for complex last expression in lambda
- [`KT-31540`](https://youtrack.jetbrains.com/issue/KT-31540) Change initialization order of default konstues for tail recursive optimized functions
- [`KT-31594`](https://youtrack.jetbrains.com/issue/KT-31594) No SETTER_PROJECTED_OUT diagnostic on synthetic  properties from Java
- [`KT-31630`](https://youtrack.jetbrains.com/issue/KT-31630) TYPE_INFERENCE_PARAMETER_CONSTRAINT_ERROR for generic function with callable reference argument
- [`KT-31654`](https://youtrack.jetbrains.com/issue/KT-31654) False unnecessary non-null assertion (!!) warning inside yield call
- [`KT-31679`](https://youtrack.jetbrains.com/issue/KT-31679) NI: Unresolved reference with delegated property and anonymous object
- [`KT-31739`](https://youtrack.jetbrains.com/issue/KT-31739) Overload resolution ambiguity in generic function call with lambda arguments that take different parameter types
- [`KT-31923`](https://youtrack.jetbrains.com/issue/KT-31923) Outer finally block inserted before return instruction is not excluded from catch interkonst of inner try (without finally) block
- [`KT-31968`](https://youtrack.jetbrains.com/issue/KT-31968) New inference is using a more common system to infer types from nested elvis call than OI
- [`KT-31978`](https://youtrack.jetbrains.com/issue/KT-31978) NI: changed precedence of elvis operator relative to equality operator
- [`KT-32026`](https://youtrack.jetbrains.com/issue/KT-32026) Infer return type of @PolymorphicSignature method to void if no expected type is given
- [`KT-32087`](https://youtrack.jetbrains.com/issue/KT-32087) "Remove explicit type arguments" inspection when function creates a lambda that calls a function with type argument
- [`KT-32097`](https://youtrack.jetbrains.com/issue/KT-32097) NI: NON_APPLICABLE_CALL_FOR_BUILDER_INFERENCE when using awaitClose in channelFlow builder
- [`KT-32098`](https://youtrack.jetbrains.com/issue/KT-32098) Kotlin 1.3.40 type inference mismatch between the IDE and the compiler
- [`KT-32151`](https://youtrack.jetbrains.com/issue/KT-32151) Return arguments of lambda are resolved in common system while in OI aren't
- [`KT-32165`](https://youtrack.jetbrains.com/issue/KT-32165) Cannot use 'Nothing' as reified type parameter,
- [`KT-32196`](https://youtrack.jetbrains.com/issue/KT-32196) Inconsistency between compiler and inspection with mapNotNull
- [`KT-32203`](https://youtrack.jetbrains.com/issue/KT-32203) NON_APPLICABLE_CALL_FOR_BUILDER_INFERENCE warning false-positive
- [`KT-32249`](https://youtrack.jetbrains.com/issue/KT-32249) New inference breaks generic property delegate resolution
- [`KT-32250`](https://youtrack.jetbrains.com/issue/KT-32250) New Type Inference fails for nullable field
- [`KT-32262`](https://youtrack.jetbrains.com/issue/KT-32262) Type inference failed in presence of @JvmSuppressWildcards
- [`KT-32267`](https://youtrack.jetbrains.com/issue/KT-32267) NI: "Overload resolution ambiguity. All these functions match." with KFunction
- [`KT-32284`](https://youtrack.jetbrains.com/issue/KT-32284) False positive "Redundant lambda arrow" with extension function returning generic with lambda type argument
- [`KT-32290`](https://youtrack.jetbrains.com/issue/KT-32290) New Inference: old inference fails on nullable lambda from if-expression
- [`KT-32306`](https://youtrack.jetbrains.com/issue/KT-32306) False positive `Remove explicit type arguments` when using generic return argument from lambda for delegated property
- [`KT-32358`](https://youtrack.jetbrains.com/issue/KT-32358) NI: Smart cast doesn't work with inline function after elvis operator
- [`KT-32383`](https://youtrack.jetbrains.com/issue/KT-32383) NI: listOf(…) infers to inkonstid type if different enums implementing the same interface are involved
- [`KT-32397`](https://youtrack.jetbrains.com/issue/KT-32397) OI can't infer types when there are different lower and upper bounds in common system
- [`KT-32399`](https://youtrack.jetbrains.com/issue/KT-32399) OI can't choose candidate with different lower and upper bounds
- [`KT-32425`](https://youtrack.jetbrains.com/issue/KT-32425) No coercion to Unit by a return type of callable expression argument
- [`KT-32431`](https://youtrack.jetbrains.com/issue/KT-32431) Nothing inferred for upper bound in parametric class
- [`KT-32449`](https://youtrack.jetbrains.com/issue/KT-32449) IDE fails to report error:  Expected type mismatch
- [`KT-32462`](https://youtrack.jetbrains.com/issue/KT-32462) NI: "AssertionError: No resolved call" with callable reference
- [`KT-32497`](https://youtrack.jetbrains.com/issue/KT-32497) Compiler Type inference fails when inferring type parameters ("Generics") - IDE inference works
- [`KT-32507`](https://youtrack.jetbrains.com/issue/KT-32507) IntelliJ Kotlin plugin not recognizing smart cast to non-nullable type
- [`KT-32501`](https://youtrack.jetbrains.com/issue/KT-32501) type inference should infer nullable type if non-nullable doesn't work
- [`KT-32527`](https://youtrack.jetbrains.com/issue/KT-32527) Cast required for Sequence.map when mapping between disjoint types
- [`KT-32548`](https://youtrack.jetbrains.com/issue/KT-32548) OVERLOAD_RESOLUTION_AMBIGUITY with platform generic types
- [`KT-32595`](https://youtrack.jetbrains.com/issue/KT-32595) NI: Overload resolution ambiguity for member function with type parameter with upper bounds and lambda parameter
- [`KT-32598`](https://youtrack.jetbrains.com/issue/KT-32598) Kotlin 1.3.41 type inference problem
- [`KT-32654`](https://youtrack.jetbrains.com/issue/KT-32654) Non-applicable call for builder inference with coroutines
- [`KT-32655`](https://youtrack.jetbrains.com/issue/KT-32655) Kotlin compiler and IDEA plugin disagree on type
- [`KT-32686`](https://youtrack.jetbrains.com/issue/KT-32686) New type inference algorithm infers wrong type when return type of `when` expression has a type parameter, and it's not specified in all cases
- [`KT-32788`](https://youtrack.jetbrains.com/issue/KT-32788) No smartcast for an implicit "this" in old inference
- [`KT-32792`](https://youtrack.jetbrains.com/issue/KT-32792) Bug in new type inference algorithm for Android Studio
- [`KT-32800`](https://youtrack.jetbrains.com/issue/KT-32800) Problem with inlined getValue in Delegates, IDEA does not detect problem, but code doesn't compile
- [`KT-32802`](https://youtrack.jetbrains.com/issue/KT-32802) Odd behaviour with smart-casting in exception block
- [`KT-32850`](https://youtrack.jetbrains.com/issue/KT-32850) Inkonstid suggestion in Kotlin code
- [`KT-32866`](https://youtrack.jetbrains.com/issue/KT-32866) tests in Atrium do not compile with new type inference
- [`KT-33012`](https://youtrack.jetbrains.com/issue/KT-33012) Proper capturing of star projections with recursive types
- [`KT-33102`](https://youtrack.jetbrains.com/issue/KT-33102) Fake overrides aren't created for properties
- [`KT-33152`](https://youtrack.jetbrains.com/issue/KT-33152) Type inference fails when lambda with generic argument type is present
- [`KT-33166`](https://youtrack.jetbrains.com/issue/KT-33166) NI: TYPE_MISMATCH for specific case with `when` expression and one branch throwing exception
- [`KT-33171`](https://youtrack.jetbrains.com/issue/KT-33171) TYPE_INFERENCE_NO_INFORMATION_FOR_PARAMETER with object convention invoke direct call on property delegation
- [`KT-33240`](https://youtrack.jetbrains.com/issue/KT-33240) Generated overloads for @JvmOverloads on open methods should be final
- [`KT-33545`](https://youtrack.jetbrains.com/issue/KT-33545) NI: Error type encountered: NonFixed: TypeVariable(T) (StubType)
- [`KT-33988`](https://youtrack.jetbrains.com/issue/KT-33988) Low priority candidates doesn't match when others fail
- [`KT-34128`](https://youtrack.jetbrains.com/issue/KT-34128) Internal compiler error ReenteringLazyValueComputationException in the absence of right parenthesis
- [`KT-34140`](https://youtrack.jetbrains.com/issue/KT-34140) Function with contract doesn't smartcast return konstue in lambda
- [`KT-34314`](https://youtrack.jetbrains.com/issue/KT-34314) There is no an error diagnostic about type inference failed (impossible to infer type parameters) on callable references
- [`KT-34335`](https://youtrack.jetbrains.com/issue/KT-34335) A lambda argument without a type specifier is inferred to Nothing, if it's passed to a function, declaration of which contains vararg (e.g. Any)
- [`KT-34501`](https://youtrack.jetbrains.com/issue/KT-34501) if-expression infers `Any` when `Number?` is expected
- [`KT-34708`](https://youtrack.jetbrains.com/issue/KT-34708) Couldn't transform method node: emit$$forInline (try-catch in a Flow.map)
- [`KT-34729`](https://youtrack.jetbrains.com/issue/KT-34729) NI: type mismatch error is missed for generic higher-order functions
- [`KT-34830`](https://youtrack.jetbrains.com/issue/KT-34830) Type inference fails when using an extension method with "identical JVM signature" as method reference
- [`KT-34857`](https://youtrack.jetbrains.com/issue/KT-34857) "Illegal resolved call to variable with invoke" with operator resolved to extension property `invoke`
- [`KT-34891`](https://youtrack.jetbrains.com/issue/KT-34891) CompilationException: Failed to generate expression: KtLambdaExpression (Error type encountered)
- [`KT-34925`](https://youtrack.jetbrains.com/issue/KT-34925) MPP, IDE: False positive warning NO_REFLECTION_IN_CLASS_PATH in common code
- [`KT-35020`](https://youtrack.jetbrains.com/issue/KT-35020) "Type checking has run into a recursive problem" for overloaded generic function implemented using expression body syntax
- [`KT-35064`](https://youtrack.jetbrains.com/issue/KT-35064) NI: The new inference has overload resolution ambiguity on passing a callable reference for extension functions (> 1 candidates)
- [`KT-35207`](https://youtrack.jetbrains.com/issue/KT-35207) Incorrect generic signature in annotations when KClass is used as a generic parameter
- [`KT-35210`](https://youtrack.jetbrains.com/issue/KT-35210) NI: OnlyInputTypes check fails for types with captured ones
- [`KT-35213`](https://youtrack.jetbrains.com/issue/KT-35213) NI: overload resolution ambiguity for callable reference with defined LHS
- [`KT-35226`](https://youtrack.jetbrains.com/issue/KT-35226) Forbid spread operator in signature-polymorphic calls
- [`KT-35306`](https://youtrack.jetbrains.com/issue/KT-35306) `Non-applicable call for builder inference` for nested builder functions which return generic types that are wrapped.
- [`KT-35337`](https://youtrack.jetbrains.com/issue/KT-35337) IllegalStateException: Failed to generate expression: KtLambdaExpression
- [`KT-35398`](https://youtrack.jetbrains.com/issue/KT-35398) NI, IDE: Duplicate warning message JAVA_CLASS_ON_COMPANION in argument position
- [`KT-35469`](https://youtrack.jetbrains.com/issue/KT-35469) Change behavior of signature-polymorphic calls to methods with a single vararg parameter, to avoid wrapping the argument into another array
- [`KT-35487`](https://youtrack.jetbrains.com/issue/KT-35487) Type safety problem because of lack of captured conversion against nullable type argument
- [`KT-35494`](https://youtrack.jetbrains.com/issue/KT-35494) NI: Multiple duplicate error diagnostics (in IDE popup) with NULL_FOR_NONNULL_TYPE
- [`KT-35514`](https://youtrack.jetbrains.com/issue/KT-35514) Type inference failure with `out` type and if-else inside lambda
- [`KT-35517`](https://youtrack.jetbrains.com/issue/KT-35517) TYPE_MISMATCH error duplication for not Boolean condition in if-expression
- [`KT-35535`](https://youtrack.jetbrains.com/issue/KT-35535) Illegal callable reference receiver allowed in new inference
- [`KT-35578`](https://youtrack.jetbrains.com/issue/KT-35578) Diagnostics are sometimes duplicated
- [`KT-35602`](https://youtrack.jetbrains.com/issue/KT-35602) NI doesn't approximate star projections properly for self types
- [`KT-35658`](https://youtrack.jetbrains.com/issue/KT-35658) NI: Common super type between Inv<A!>, Inv<A?> and Inv<A> is Inv<out A?>, not Inv<A!> (as old inference)
- [`KT-35668`](https://youtrack.jetbrains.com/issue/KT-35668) NI: the lack of fixing to Nothing problem (one of the bugs: the lack of smartcast through cast of Nothing? to Nothing after elvis)
- [`KT-35679`](https://youtrack.jetbrains.com/issue/KT-35679) Type safety problem because several equal type variables are instantiated with a different types
- [`KT-35684`](https://youtrack.jetbrains.com/issue/KT-35684) NI: "IllegalStateException: Expected some types" from builder-inference about intersecting empty types on trivial code
- [`KT-35814`](https://youtrack.jetbrains.com/issue/KT-35814) Inference fails to infer common upper type for java types
- [`KT-35834`](https://youtrack.jetbrains.com/issue/KT-35834) Do not declare checked exceptions in JVM bytecode when using delegation to Kotlin interfaces
- [`KT-35920`](https://youtrack.jetbrains.com/issue/KT-35920) NI allows callable references prohibited in old inference
- [`KT-35943`](https://youtrack.jetbrains.com/issue/KT-35943) NI: IllegalStateException: No error about uninferred type parameter
- [`KT-35945`](https://youtrack.jetbrains.com/issue/KT-35945) Using Polymorphism and Type Inference the compiler fails having a Type with Type with Type
- [`KT-35992`](https://youtrack.jetbrains.com/issue/KT-35992) Wrong overload resolution with explicit type arguments if KFunction and type parameter once nullable and once non-nullable
- [`KT-36001`](https://youtrack.jetbrains.com/issue/KT-36001) "IllegalStateException: Error type encountered" with elvis in generic function
- [`KT-36002`](https://youtrack.jetbrains.com/issue/KT-36002) KotlinFrontEndException: Exception while analyzing expression
- [`KT-36065`](https://youtrack.jetbrains.com/issue/KT-36065) Type inference failed for generic function invoked on implicitly nullable variable created from Java
- [`KT-36066`](https://youtrack.jetbrains.com/issue/KT-36066) Type inference problem in compiler
- [`KT-36101`](https://youtrack.jetbrains.com/issue/KT-36101) False positive IMPLICIT_NOTHING_AS_TYPE_PARAMETER with suspend lambdas
- [`KT-36146`](https://youtrack.jetbrains.com/issue/KT-36146) Drop support of language version 1.0/1.1, deprecate language version 1.2
- [`KT-36192`](https://youtrack.jetbrains.com/issue/KT-36192) Redundant smart cast for overloaded functions
- [`KT-36202`](https://youtrack.jetbrains.com/issue/KT-36202) NI: false positive "NON_APPLICABLE_CALL_FOR_BUILDER_INFERENCE" with elvis operator in lambda
- [`KT-36220`](https://youtrack.jetbrains.com/issue/KT-36220) NI: false positive NON_APPLICABLE_CALL_FOR_BUILDER_INFERENCE if one use cannot resolve
- [`KT-36221`](https://youtrack.jetbrains.com/issue/KT-36221) NI: UnsupportedOperationException: "no descriptor for type constructor" with vararg parameter when `set` or `get` methods of element passed as function reference
- [`KT-36264`](https://youtrack.jetbrains.com/issue/KT-36264) NI: unstable smart cast changes results of call resolution
- [`KT-36279`](https://youtrack.jetbrains.com/issue/KT-36279) OI: Type inference fails when omitting explicit name of the lambda parameter
- [`KT-36298`](https://youtrack.jetbrains.com/issue/KT-36298) Failure to resolve return type of extension property on function literal
- [`KT-36317`](https://youtrack.jetbrains.com/issue/KT-36317) TYPE_MISMATCH diagnostic error duplication for not Boolean condition in do-while-loop-statement
- [`KT-36338`](https://youtrack.jetbrains.com/issue/KT-36338) Forbid IR backend usage with unsupported language / API version
- [`KT-36371`](https://youtrack.jetbrains.com/issue/KT-36371) Incorrect null check with NI and builder inference in generic context
- [`KT-36644`](https://youtrack.jetbrains.com/issue/KT-36644) Stop discrimination of prerelease compiler version in Kotlin plugin
- [`KT-36745`](https://youtrack.jetbrains.com/issue/KT-36745) NI: "Expected some types" with unresolved class reference
- [`KT-36776`](https://youtrack.jetbrains.com/issue/KT-36776) Treat special constructions (if, when, try) as a usual calls when there is expected type
- [`KT-36818`](https://youtrack.jetbrains.com/issue/KT-36818) NI: the lack of smartcast from Nothing returning "when" branch
- [`KT-37123`](https://youtrack.jetbrains.com/issue/KT-37123) Cycling lambda signature errors in Observable.combineLatest
- [`KT-37146`](https://youtrack.jetbrains.com/issue/KT-37146) Type inference can't resolve plus operator
- [`KT-37189`](https://youtrack.jetbrains.com/issue/KT-37189) Wrong type inferred with class with two types as argument
- [`KT-37295`](https://youtrack.jetbrains.com/issue/KT-37295) NI: Passing function with wrong returning type by reference is not shown as error
- [`KT-37345`](https://youtrack.jetbrains.com/issue/KT-37345) Wrong non-null type assertion when using Sequence and yield
- [`KT-37429`](https://youtrack.jetbrains.com/issue/KT-37429) Type inference failed: Not enough information to infer parameter with java streams
- [`KT-37480`](https://youtrack.jetbrains.com/issue/KT-37480) "None of the following candidates can be called" when compiling but no error in IDEA

### IDE

- [`KT-33573`](https://youtrack.jetbrains.com/issue/KT-33573) IDE runs platform-specific checkers on common code even if the project doesn't target the corresponding platform
- [`KT-35823`](https://youtrack.jetbrains.com/issue/KT-35823) IDE settings: deprecated konstues of language / API version look like regular ones
- [`KT-35871`](https://youtrack.jetbrains.com/issue/KT-35871) NPE in LightMethodBuilder
- [`KT-36034`](https://youtrack.jetbrains.com/issue/KT-36034) Kotlin 1.3.70 creates file `kotlinCodeInsightSettings.xml` with user-level settings under .idea
- [`KT-36084`](https://youtrack.jetbrains.com/issue/KT-36084) "Join lines" should remove trailing comma
- [`KT-36460`](https://youtrack.jetbrains.com/issue/KT-36460) IDE highlighting: Undo on inspection breaks analysis inside top-level property initializer
- [`KT-36712`](https://youtrack.jetbrains.com/issue/KT-36712) Use new annotation highlighting API
- [`KT-36917`](https://youtrack.jetbrains.com/issue/KT-36917) Caret has incorrect position after pressing enter on line with named argument

### IDE. Code Style, Formatting

- [`KT-36387`](https://youtrack.jetbrains.com/issue/KT-36387) Formatter: "Chained Function Calls" formats property chains
- [`KT-36393`](https://youtrack.jetbrains.com/issue/KT-36393) Incorrect trailing comma insertion for boolean operator expression
- [`KT-36466`](https://youtrack.jetbrains.com/issue/KT-36466) Formatter: "Chained Function Calls" with "Wrap first call" wrap single method call

### IDE. Completion

- [`KT-16531`](https://youtrack.jetbrains.com/issue/KT-16531) Error type displayed in completion for the result of buildSequence
- [`KT-32178`](https://youtrack.jetbrains.com/issue/KT-32178) Autocompletion of 'suspend' should not add the 'fun' keyword when writing a function type
- [`KT-34582`](https://youtrack.jetbrains.com/issue/KT-34582) Remove kotlin.coroutines.experimental from autocompletion
- [`KT-35258`](https://youtrack.jetbrains.com/issue/KT-35258) Completion problems with enabled `MixedNamedArgumentsInTheirOwnPosition` feature

### IDE. Debugger

- [`KT-12016`](https://youtrack.jetbrains.com/issue/KT-12016) Step over inside inline function lambda argument steps into inline function body
- [`KT-14296`](https://youtrack.jetbrains.com/issue/KT-14296) Can't step over inlined functions of iterator
- [`KT-14869`](https://youtrack.jetbrains.com/issue/KT-14869) Debugger: always steps into inlined lambda after returning from function
- [`KT-15652`](https://youtrack.jetbrains.com/issue/KT-15652) Step over inline function call stops at Thread.dispatchUncaughtException() in case of exceptions
- [`KT-34905`](https://youtrack.jetbrains.com/issue/KT-34905) Debugger: "Step over" steps into inline function body if lambda call splitted on several lines
- [`KT-35354`](https://youtrack.jetbrains.com/issue/KT-35354) ClassCastException in ekonstuate window

### IDE. Gradle. Script

- [`KT-36703`](https://youtrack.jetbrains.com/issue/KT-36703) .gradle.kts: Change text for out of project scripts

### IDE. Hints

- [`KT-37537`](https://youtrack.jetbrains.com/issue/KT-37537) IDE is missing or swallowing keystrokes when hint popups are displayed

### IDE. Hints. Parameter Info

- [`KT-14523`](https://youtrack.jetbrains.com/issue/KT-14523) Weird parameter info tooltip for map.getValue type arguments

### IDE. Inspections and Intentions

#### New Features

- [`KT-33384`](https://youtrack.jetbrains.com/issue/KT-33384) Intention to switch between single-line/multi-line lambda
- [`KT-34690`](https://youtrack.jetbrains.com/issue/KT-34690) Support intention `Convert lambda to reference` for qualified references
- [`KT-35639`](https://youtrack.jetbrains.com/issue/KT-35639) False negative inspection "redundant internal modifier" inside private class
- [`KT-36256`](https://youtrack.jetbrains.com/issue/KT-36256) Implement migration for WarningOnMainUnusedParameter
- [`KT-36257`](https://youtrack.jetbrains.com/issue/KT-36257) Implement migration for ProhibitRepeatedUseSiteTargetAnnotations
- [`KT-36258`](https://youtrack.jetbrains.com/issue/KT-36258) Implement migration for ProhibitUseSiteTargetAnnotationsOnSuperTypes
- [`KT-36260`](https://youtrack.jetbrains.com/issue/KT-36260) Implement migration for ProhibitJvmOverloadsOnConstructorsOfAnnotationClasses
- [`KT-36261`](https://youtrack.jetbrains.com/issue/KT-36261) Implement migration for ProhibitTypeParametersForLocalVariables
- [`KT-36262`](https://youtrack.jetbrains.com/issue/KT-36262) Implement migration for RestrictReturnStatementTarget

#### Fixes

- [`KT-14001`](https://youtrack.jetbrains.com/issue/KT-14001) "Convert lambda to reference" results in failed type inference
- [`KT-14781`](https://youtrack.jetbrains.com/issue/KT-14781) Import of aliased type is inserted when deprecation replacement contains typealias
- [`KT-16907`](https://youtrack.jetbrains.com/issue/KT-16907) "Convert to lambda reference" intention is erroneously shown for suspending lambda parameters, producing bad code
- [`KT-24869`](https://youtrack.jetbrains.com/issue/KT-24869) False positive inspection "Redundant 'suspend' modifier"
- [`KT-24987`](https://youtrack.jetbrains.com/issue/KT-24987) Implicit (unsafe) cast from dynamic to DONT_CARE
- [`KT-27511`](https://youtrack.jetbrains.com/issue/KT-27511) "Remove explicit type arguments" suggestion creates incorrect code
- [`KT-28415`](https://youtrack.jetbrains.com/issue/KT-28415) False positive inspection "Remove explicit type arguments" with a callable reference
- [`KT-30831`](https://youtrack.jetbrains.com/issue/KT-30831) False positive `Remove explicit type arguments` with generic type constructor with init block
- [`KT-31050`](https://youtrack.jetbrains.com/issue/KT-31050) False positive "Boolean literal argument without parameter name" inspection using expect class
- [`KT-31559`](https://youtrack.jetbrains.com/issue/KT-31559) Type inference failed: Not enough information to infer parameter K
- [`KT-32093`](https://youtrack.jetbrains.com/issue/KT-32093) NI: IDE suggests to use property access syntax instead of getter method
- [`KT-33098`](https://youtrack.jetbrains.com/issue/KT-33098) False positive "Remove explicit type arguments" with "Enable NI for IDE" and Java generic class constructor invocation with nullable argument
- [`KT-33685`](https://youtrack.jetbrains.com/issue/KT-33685) ReplaceWith does not add type parameter for function taking generic lambda with receiver
- [`KT-34511`](https://youtrack.jetbrains.com/issue/KT-34511) kotlin.KotlinNullPointerException after using intention Replace Java Map.forEach with Kotlin's forEach for Map with Pairs as keys
- [`KT-34686`](https://youtrack.jetbrains.com/issue/KT-34686) False positive "Constructor parameter is never used as a property" if property is used as a reference
- [`KT-35451`](https://youtrack.jetbrains.com/issue/KT-35451) Type inference fails after applying false positive inspection "Remove explicit type arguments "
- [`KT-35475`](https://youtrack.jetbrains.com/issue/KT-35475) Applying intention "Redundant curly braces in string template" for label references change semantic
- [`KT-35528`](https://youtrack.jetbrains.com/issue/KT-35528) Intention `Replace 'when' with 'if'` produces wrong code if expression subject is a variable declaration
- [`KT-35588`](https://youtrack.jetbrains.com/issue/KT-35588) Applying "Lift assignment out of 'if'" for if statement that has lambda plus assignment and 'return' leads to type mismatch
- [`KT-35604`](https://youtrack.jetbrains.com/issue/KT-35604) Too long quickfix message for "modifier 'open' is not applicable to 'companion object'"
- [`KT-35648`](https://youtrack.jetbrains.com/issue/KT-35648) False negative intention "Remove argument name" on named positional arguments
- [`KT-36160`](https://youtrack.jetbrains.com/issue/KT-36160) False positive "Constructor has non-null self reference parameter" with vararg parameter of class
- [`KT-36171`](https://youtrack.jetbrains.com/issue/KT-36171) intention "Replace 'get' call with indexing operator" works incorrectly with spread operator
- [`KT-36255`](https://youtrack.jetbrains.com/issue/KT-36255) Implement migration tool for 1.4
- [`KT-36357`](https://youtrack.jetbrains.com/issue/KT-36357) "Lift assignment out of 'if'" breaks code for oneliner function
- [`KT-36360`](https://youtrack.jetbrains.com/issue/KT-36360) OI: False positive "remove explicit type arguments" for SequenceScope leads to compiler failure with "Type inference failed"
- [`KT-36369`](https://youtrack.jetbrains.com/issue/KT-36369) "To raw string literal" intention is not available if string content starts with newline symbol \n

### IDE. Multiplatform

- [`KT-36978`](https://youtrack.jetbrains.com/issue/KT-36978) Infinite "org.jetbrains.kotlin.idea.caches.resolve.KotlinIdeaResolutionException: Kotlin resolution encountered a problem while analyzing KtFile" exceptions in hierarchical multiplatform projects

### IDE. Navigation

- [`KT-30628`](https://youtrack.jetbrains.com/issue/KT-30628) Navigation from Java sources to calls on Kotlin function annotated with @JvmOverloads that have optional arguments omitted leads to decompiled code

### IDE. Refactorings

#### New Features

- [`KT-26999`](https://youtrack.jetbrains.com/issue/KT-26999) Inspection for unused main parameter in Kotlin 1.3
- [`KT-33339`](https://youtrack.jetbrains.com/issue/KT-33339) Refactor / Move is disabled for Kotlin class selected together with Kotlin file

#### Fixes

- [`KT-22131`](https://youtrack.jetbrains.com/issue/KT-22131) "Extract method" refactoring treats smart casted variables as non-local
- [`KT-24615`](https://youtrack.jetbrains.com/issue/KT-24615) "Extract property" generates useless extension property
- [`KT-26047`](https://youtrack.jetbrains.com/issue/KT-26047) Refactor -> Rename: Overridden method renaming in generic class doesn't rename base method
- [`KT-26248`](https://youtrack.jetbrains.com/issue/KT-26248) "Refactor -> Inline variable" breaks callable references
- [`KT-31401`](https://youtrack.jetbrains.com/issue/KT-31401) Refactor / Inlining function reference in Java method with Runnable argument produces compile error
- [`KT-33709`](https://youtrack.jetbrains.com/issue/KT-33709) Inline variable: NONE_APPLICABLE with overloaded generic Java method with SAM conversion
- [`KT-34190`](https://youtrack.jetbrains.com/issue/KT-34190) Inline Function: False positive error "not at the end of the body" with anonymous object
- [`KT-35235`](https://youtrack.jetbrains.com/issue/KT-35235) Unable to move multiple class files to a different package
- [`KT-35463`](https://youtrack.jetbrains.com/issue/KT-35463) ClassCastException during moving .kt file with one single class to the other folder in Android Studio
- [`KT-36312`](https://youtrack.jetbrains.com/issue/KT-36312) Refactor Move refactoring to get it ready for MPP-related fixes

### IDE. Run Configurations

- [`KT-34503`](https://youtrack.jetbrains.com/issue/KT-34503) "Nothing here" is shown as a drop-down list for "Run test" gutter icon for a multiplatform test with expect/actual parts in platform-agnostic code
- [`KT-36093`](https://youtrack.jetbrains.com/issue/KT-36093) Running Gradle java tests are broken for Gradle older than 4.0 (Could not set unknown property 'testClassesDirs' for task ':nonJvmTestIdeSupport')

### JavaScript

- [`KT-23284`](https://youtrack.jetbrains.com/issue/KT-23284) Reified type arguments aren't substituted when invoking inline konst konstue

### Libraries

- [`KT-26654`](https://youtrack.jetbrains.com/issue/KT-26654) Remove deprecated 'mod' operators
- [`KT-27856`](https://youtrack.jetbrains.com/issue/KT-27856) Add contracts to Timing.kt lambdas
- [`KT-28356`](https://youtrack.jetbrains.com/issue/KT-28356) Fail fast in Regex.findAll on an inkonstid startIndex
- [`KT-29748`](https://youtrack.jetbrains.com/issue/KT-29748) Declare kotlin.reflect.KType in kotlin-stdlib-common
- [`KT-30360`](https://youtrack.jetbrains.com/issue/KT-30360) Deprecate conversions of floating point types to integral types lesser than Int
- [`KT-32855`](https://youtrack.jetbrains.com/issue/KT-32855) KTypeParameter is available in common code, but not available in Native
- [`KT-35216`](https://youtrack.jetbrains.com/issue/KT-35216) <T : AutoCloseable?, R> T.use and <T : Closeable?, R> T.use should have contracts
- [`KT-36082`](https://youtrack.jetbrains.com/issue/KT-36082) JS Regex.find does not throw IndexOutOfBoundsException on inkonstid start index
- [`KT-36083`](https://youtrack.jetbrains.com/issue/KT-36083) Extract kotlin.coroutines.experimental.* packages to a separate compatibility artifact

### Reflection

- [`KT-30071`](https://youtrack.jetbrains.com/issue/KT-30071) Implement KTypeProjection.toString
- [`KT-35991`](https://youtrack.jetbrains.com/issue/KT-35991) Embed Proguard/R8 rules in kotlin-reflect artifact jar

### Tools. CLI

- [`KT-28475`](https://youtrack.jetbrains.com/issue/KT-28475) java.sql module not available in kotlinc scripts on Java 9+ due to use of Bootstrap Classloader

### Tools. Gradle. JS

- [`KT-34989`](https://youtrack.jetbrains.com/issue/KT-34989) NodeJs is not re-downloaded if the node binary gets accidentally deleted
- [`KT-35465`](https://youtrack.jetbrains.com/issue/KT-35465) Gradle, JS: Gradle tooling for IR compiler
- [`KT-36472`](https://youtrack.jetbrains.com/issue/KT-36472) Kotlin/JS Gradle plugin iterates over all Gradle tasks, triggering their lazy configuration
- [`KT-36488`](https://youtrack.jetbrains.com/issue/KT-36488) Gradle, JS, IR: Publishing from JS plugin

### Tools. Gradle. Multiplatform

- [`KT-37264`](https://youtrack.jetbrains.com/issue/KT-37264) In intermediate common source sets, internals are not visible from their dependsOn source sets during Gradle build

### Tools. Gradle. Native

- [`KT-36804`](https://youtrack.jetbrains.com/issue/KT-36804) In a project with Kotlin/Native targets and kotlinx.serialization, IDE import fails: Could not create task '...'. / Cannot change dependencies of configuration after it has been resolved.

### Tools. J2K

- [`KT-20120`](https://youtrack.jetbrains.com/issue/KT-20120) J2K: Java 9 `forRemokonst` and `since` methods of `Deprecated` are not processed

### Tools. Scripts

- [`KT-35414`](https://youtrack.jetbrains.com/issue/KT-35414) Switch for `-Xexpression` to `-expression`/`-e` cli argument syntax for JVM cli compiler in 1.4
