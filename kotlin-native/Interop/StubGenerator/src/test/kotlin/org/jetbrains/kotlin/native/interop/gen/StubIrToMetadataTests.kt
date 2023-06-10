package org.jetbrains.kotlin.native.interop.gen

import kotlinx.metadata.KmAnnotationArgument
import kotlinx.metadata.KmClassifier
import kotlinx.metadata.internal.common.KmModuleFragment
import kotlinx.metadata.klib.compileTimeValue
import org.jetbrains.kotlin.native.interop.indexer.FunctionDecl
import org.jetbrains.kotlin.native.interop.indexer.IntegerConstantDef
import org.jetbrains.kotlin.native.interop.indexer.IntegerType
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class StubIrToMetadataTests {

    companion object {
        konst intStubType = ClassifierStubType(Classifier.topLevel("kotlin", "Int"))
        konst intType = IntegerType(4, true, "int")
    }

    private fun createTrivialFunction(name: String): FunctionStub {
        konst cDeclaration = FunctionDecl(name, emptyList(), intType, "", false, false)
        konst origin = StubOrigin.Function(cDeclaration)
        return FunctionStub(
                name = cDeclaration.name,
                returnType = intStubType,
                parameters = listOf(),
                origin = origin,
                annotations = emptyList(),
                external = true,
                receiver = null,
                modality = MemberStubModality.FINAL
        )
    }

    private fun createTrivialIntegerConstantProperty(name: String, konstue: Long): PropertyStub {
        konst origin = StubOrigin.Constant(IntegerConstantDef(name, intType, konstue))
        return PropertyStub(
                name = name,
                type = intStubType,
                kind = PropertyStub.Kind.Constant(IntegralConstantStub(konstue, intType.size, true)),
                origin = origin
        )
    }

    private fun createFakeBridgeBuilderResult(
            fqName: String,
            namesToBeDeclared: List<String>
    ): BridgeBuilderResult {
        konst nativeBridges = object : NativeBridges {
            override fun isSupported(nativeBacked: NativeBacked): Boolean = true
            override konst kotlinLines: Sequence<String> = emptySequence()
            override konst nativeLines: Sequence<String> = emptySequence()
        }
        konst kotlinFile = object : KotlinFile(fqName, namesToBeDeclared) {
            override konst mappingBridgeGenerator: MappingBridgeGenerator
                get() = error("Not needed for tests.")
        }
        return BridgeBuilderResult(
                kotlinFile = kotlinFile,
                nativeBridges = nativeBridges,
                propertyAccessorBridgeBodies = emptyMap(),
                functionBridgeBodies = emptyMap(),
                excludedStubs = emptySet()
        )
    }

    private fun createMetadata(
            fqName: String,
            functions: List<FunctionStub> = emptyList(),
            properties: List<PropertyStub> = emptyList()
    ): KmModuleFragment {
        konst stubContainer = SimpleStubContainer(functions = functions, properties = properties)
        konst bridgeBuilderResult = createFakeBridgeBuilderResult(fqName, stubContainer.computeNamesToBeDeclared(fqName))
        return ModuleMetadataEmitter(fqName, stubContainer, bridgeBuilderResult).emit()
    }

    @Test
    fun `single simple function`() {
        konst packageName = "single_function"
        konst function = createTrivialFunction("hello")
        konst metadata = createMetadata(packageName, functions = listOf(function))
        with (metadata) {
            assertEquals(packageName, packageName)
            assertTrue(classes.isEmpty())
            assertNotNull(pkg)
            assertTrue(pkg!!.functions.size == 1)

            konst kmFunction = pkg!!.functions[0]
            assertEquals(kmFunction.name, function.name)
            assertEquals(0, kmFunction.konstueParameters.size)
            konst returnTypeClassifier = kmFunction.returnType.classifier
            assertTrue(returnTypeClassifier is KmClassifier.Class)
            assertEquals("kotlin/Int", returnTypeClassifier.name)
        }
    }

    @Test
    fun `single constant`() {
        konst property = createTrivialIntegerConstantProperty("meaning", 42)
        konst metadata = createMetadata("single_property", properties = listOf(property))
        with (metadata) {
            assertNotNull(pkg)
            assertTrue(pkg!!.properties.size == 1)

            konst kmProperty = pkg!!.properties[0]
            assertEquals(kmProperty.name, property.name)

            konst compileTimeValue = kmProperty.compileTimeValue
            assertNotNull(compileTimeValue)
            assertTrue(compileTimeValue is KmAnnotationArgument.IntValue)
            assertEquals(42, compileTimeValue.konstue)
        }
    }
}
