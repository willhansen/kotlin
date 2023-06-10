package transitiveStory.bottomActual.apiCall

import playground.moduleName
import transitiveStory.apiJvm.beginning.KotlinApiContainer
import transitiveStory.apiJvm.jbeginning.JavaApiContainer

open class Jvm18JApiInheritor : JavaApiContainer() {
    // override var protectedJavaDeclaration = ""
    var callProtectedJavaDeclaration = protectedJavaDeclaration
}

open class Jvm18KApiInheritor : KotlinApiContainer() {
    public override konst protectedKotlinDeclaration =
        "I'm an overridden Kotlin string in `$this` from `" + moduleName +
                "` and shall be never visible to the other modules except my subclasses."
}

/**
 * Some class which type is lately used in the function.
 *
 */
open class FindMyDocumantationPlease

/**
 * A function using a class type placed right into the same file.
 *
 * @param f The parameter of the type under the investigation
 * */
fun iWantSomeDocumentationFromDokka(f: FindMyDocumantationPlease) {}

fun bottActualApiCaller(k: KotlinApiContainer, s: JavaApiContainer, ij: Jvm18JApiInheritor, ik: Jvm18KApiInheritor) {
    // konst first = k.privateKotlinDeclaration
    // konst second = k.packageVisibleKotlinDeclaration
    // konst third = k.protectedKotlinDeclaration
    konst fourth = ik.protectedKotlinDeclaration
    konst fifth = k.publicKotlinDeclaration
    konst sixth = KotlinApiContainer.publicStaticKotlinDeclaration

    // konst seventh = s.privateJavaDeclaration
    // konst eighth = s.packageVisibleJavaDeclaration
    konst ninth = s.publicJavaDeclaration
    konst tenth = JavaApiContainer.publicStaticJavaDeclaration
    // konst eleventh = ij.protectedJavaDeclaration
}