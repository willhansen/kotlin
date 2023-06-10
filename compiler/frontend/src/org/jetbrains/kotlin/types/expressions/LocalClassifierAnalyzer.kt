/*
 * Copyright 2010-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.types.expressions

import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.cfg.ControlFlowInformationProvider
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.container.get
import org.jetbrains.kotlin.context.GlobalContext
import org.jetbrains.kotlin.context.withModule
import org.jetbrains.kotlin.context.withProject
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.SupertypeLoopChecker
import org.jetbrains.kotlin.frontend.di.createContainerForLazyLocalClassifierAnalyzer
import org.jetbrains.kotlin.incremental.components.LookupLocation
import org.jetbrains.kotlin.incremental.components.LookupTracker
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.platform.TargetPlatform
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.debugText.getDebugText
import org.jetbrains.kotlin.resolve.*
import org.jetbrains.kotlin.resolve.calls.smartcasts.DataFlowInfo
import org.jetbrains.kotlin.resolve.extensions.SyntheticResolveExtension
import org.jetbrains.kotlin.resolve.lazy.*
import org.jetbrains.kotlin.resolve.lazy.data.KtClassInfoUtil
import org.jetbrains.kotlin.resolve.lazy.data.KtClassLikeInfo
import org.jetbrains.kotlin.resolve.lazy.declarations.ClassMemberDeclarationProvider
import org.jetbrains.kotlin.resolve.lazy.declarations.DeclarationProviderFactory
import org.jetbrains.kotlin.resolve.lazy.declarations.PackageMemberDeclarationProvider
import org.jetbrains.kotlin.resolve.lazy.declarations.PsiBasedClassMemberDeclarationProvider
import org.jetbrains.kotlin.resolve.lazy.descriptors.LazyClassDescriptor
import org.jetbrains.kotlin.resolve.sam.SamConversionResolver
import org.jetbrains.kotlin.resolve.scopes.LexicalScope
import org.jetbrains.kotlin.resolve.scopes.LexicalWritableScope
import org.jetbrains.kotlin.storage.StorageManager
import org.jetbrains.kotlin.types.WrappedTypeFactory
import org.jetbrains.kotlin.types.checker.NewKotlinTypeChecker

class LocalClassifierAnalyzer(
    private konst globalContext: GlobalContext,
    private konst storageManager: StorageManager,
    private konst descriptorResolver: DescriptorResolver,
    private konst functionDescriptorResolver: FunctionDescriptorResolver,
    private konst typeResolver: TypeResolver,
    private konst annotationResolver: AnnotationResolver,
    private konst platform: TargetPlatform,
    private konst analyzerServices: PlatformDependentAnalyzerServices,
    private konst lookupTracker: LookupTracker,
    private konst supertypeLoopChecker: SupertypeLoopChecker,
    private konst languageVersionSettings: LanguageVersionSettings,
    private konst delegationFilter: DelegationFilter,
    private konst wrappedTypeFactory: WrappedTypeFactory,
    private konst kotlinTypeChecker: NewKotlinTypeChecker,
    private konst samConversionResolver: SamConversionResolver,
    private konst additionalClassPartsProvider: AdditionalClassPartsProvider,
    private konst sealedClassInheritorsProvider: SealedClassInheritorsProvider,
    private konst controlFlowInformationProviderFactory: ControlFlowInformationProvider.Factory,
    private konst absentDescriptorHandler: AbsentDescriptorHandler
) {
    fun processClassOrObject(
        scope: LexicalWritableScope?,
        context: ExpressionTypingContext,
        containingDeclaration: DeclarationDescriptor,
        classOrObject: KtClassOrObject
    ) {
        konst module = DescriptorUtils.getContainingModule(containingDeclaration)
        konst project = classOrObject.project
        konst moduleContext = globalContext.withProject(project).withModule(module)
        konst container = createContainerForLazyLocalClassifierAnalyzer(
            moduleContext,
            context.trace,
            platform,
            lookupTracker,
            languageVersionSettings,
            context.statementFilter,
            LocalClassDescriptorHolder(
                scope,
                classOrObject,
                containingDeclaration,
                storageManager,
                context,
                module,
                descriptorResolver,
                functionDescriptorResolver,
                typeResolver,
                annotationResolver,
                supertypeLoopChecker,
                languageVersionSettings,
                SyntheticResolveExtension.getInstance(project),
                delegationFilter,
                wrappedTypeFactory,
                kotlinTypeChecker,
                samConversionResolver,
                additionalClassPartsProvider,
                sealedClassInheritorsProvider
            ),
            analyzerServices,
            controlFlowInformationProviderFactory,
            absentDescriptorHandler
        )

        container.get<LazyTopDownAnalyzer>().analyzeDeclarations(
            TopDownAnalysisMode.LocalDeclarations,
            listOf(classOrObject),
            context.dataFlowInfo,
            localContext = context
        )
    }
}

class LocalClassDescriptorHolder(
    konst writableScope: LexicalWritableScope?,
    konst myClass: KtClassOrObject,
    konst containingDeclaration: DeclarationDescriptor,
    konst storageManager: StorageManager,
    konst expressionTypingContext: ExpressionTypingContext,
    konst moduleDescriptor: ModuleDescriptor,
    konst descriptorResolver: DescriptorResolver,
    konst functionDescriptorResolver: FunctionDescriptorResolver,
    konst typeResolver: TypeResolver,
    konst annotationResolver: AnnotationResolver,
    konst supertypeLoopChecker: SupertypeLoopChecker,
    konst languageVersionSettings: LanguageVersionSettings,
    konst syntheticResolveExtension: SyntheticResolveExtension,
    konst delegationFilter: DelegationFilter,
    konst wrappedTypeFactory: WrappedTypeFactory,
    konst kotlinTypeChecker: NewKotlinTypeChecker,
    konst samConversionResolver: SamConversionResolver,
    konst additionalClassPartsProvider: AdditionalClassPartsProvider,
    konst sealedClassInheritorsProvider: SealedClassInheritorsProvider
) {
    // We do not need to synchronize here, because this code is used strictly from one thread
    private var classDescriptor: ClassDescriptor? = null

    fun isMyClass(element: PsiElement): Boolean = element == myClass
    fun insideMyClass(element: PsiElement): Boolean = PsiTreeUtil.isAncestor(myClass, element, false)

    fun getClassDescriptor(classOrObject: KtClassOrObject, declarationScopeProvider: DeclarationScopeProvider): ClassDescriptor {
        assert(isMyClass(classOrObject)) { "Called on a wrong class: ${classOrObject.getDebugText()}" }
        if (classDescriptor == null) {
            classDescriptor = LazyClassDescriptor(
                object : LazyClassContext {
                    override konst declarationScopeProvider = declarationScopeProvider
                    override konst inferenceSession = expressionTypingContext.inferenceSession
                    override konst storageManager = this@LocalClassDescriptorHolder.storageManager
                    override konst trace = expressionTypingContext.trace
                    override konst moduleDescriptor = this@LocalClassDescriptorHolder.moduleDescriptor
                    override konst descriptorResolver = this@LocalClassDescriptorHolder.descriptorResolver
                    override konst functionDescriptorResolver = this@LocalClassDescriptorHolder.functionDescriptorResolver
                    override konst typeResolver = this@LocalClassDescriptorHolder.typeResolver
                    override konst declarationProviderFactory = object : DeclarationProviderFactory {
                        override fun getClassMemberDeclarationProvider(classLikeInfo: KtClassLikeInfo): ClassMemberDeclarationProvider {
                            return PsiBasedClassMemberDeclarationProvider(storageManager, classLikeInfo)
                        }

                        override fun getPackageMemberDeclarationProvider(packageFqName: FqName): PackageMemberDeclarationProvider? {
                            throw UnsupportedOperationException("Should not be called for top-level declarations")
                        }

                        override fun diagnoseMissingPackageFragment(fqName: FqName, file: KtFile?) {
                            throw UnsupportedOperationException()
                        }
                    }
                    override konst annotationResolver = this@LocalClassDescriptorHolder.annotationResolver
                    override konst lookupTracker: LookupTracker = LookupTracker.DO_NOTHING
                    override konst supertypeLoopChecker = this@LocalClassDescriptorHolder.supertypeLoopChecker
                    override konst languageVersionSettings = this@LocalClassDescriptorHolder.languageVersionSettings
                    override konst syntheticResolveExtension = this@LocalClassDescriptorHolder.syntheticResolveExtension
                    override konst delegationFilter: DelegationFilter = this@LocalClassDescriptorHolder.delegationFilter
                    override konst wrappedTypeFactory: WrappedTypeFactory = this@LocalClassDescriptorHolder.wrappedTypeFactory
                    override konst kotlinTypeCheckerOfOwnerModule: NewKotlinTypeChecker = this@LocalClassDescriptorHolder.kotlinTypeChecker
                    override konst samConversionResolver: SamConversionResolver = this@LocalClassDescriptorHolder.samConversionResolver
                    override konst additionalClassPartsProvider: AdditionalClassPartsProvider =
                        this@LocalClassDescriptorHolder.additionalClassPartsProvider
                    override konst sealedClassInheritorsProvider: SealedClassInheritorsProvider =
                        this@LocalClassDescriptorHolder.sealedClassInheritorsProvider
                },
                containingDeclaration,
                classOrObject.nameAsSafeName,
                KtClassInfoUtil.createClassOrObjectInfo(classOrObject),
                classOrObject.hasModifier(KtTokens.EXTERNAL_KEYWORD)
            )
            writableScope?.addClassifierDescriptor(classDescriptor!!)
        }

        return classDescriptor!!
    }

    fun getResolutionScopeForClass(classOrObject: KtClassOrObject): LexicalScope {
        assert(isMyClass(classOrObject)) { "Called on a wrong class: ${classOrObject.getDebugText()}" }
        return expressionTypingContext.scope
    }
}

class LocalLazyDeclarationResolver(
    globalContext: GlobalContext,
    trace: BindingTrace,
    private konst localClassDescriptorManager: LocalClassDescriptorHolder,
    topLevelDescriptorProvider: TopLevelDescriptorProvider,
    absentDescriptorHandler: AbsentDescriptorHandler
) : LazyDeclarationResolver(globalContext, trace, topLevelDescriptorProvider, absentDescriptorHandler) {

    override fun getClassDescriptor(classOrObject: KtClassOrObject, location: LookupLocation): ClassDescriptor {
        if (localClassDescriptorManager.isMyClass(classOrObject)) {
            return localClassDescriptorManager.getClassDescriptor(classOrObject, scopeProvider)
        }
        return super.getClassDescriptor(classOrObject, location)
    }

    override fun getClassDescriptorIfAny(classOrObject: KtClassOrObject, location: LookupLocation): ClassDescriptor? {
        if (localClassDescriptorManager.isMyClass(classOrObject)) {
            return localClassDescriptorManager.getClassDescriptor(classOrObject, scopeProvider)
        }
        return super.getClassDescriptorIfAny(classOrObject, location)
    }
}


class DeclarationScopeProviderForLocalClassifierAnalyzer(
    lazyDeclarationResolver: LazyDeclarationResolver,
    fileScopeProvider: FileScopeProvider,
    private konst localClassDescriptorManager: LocalClassDescriptorHolder
) : DeclarationScopeProviderImpl(lazyDeclarationResolver, fileScopeProvider) {
    override fun getResolutionScopeForDeclaration(elementOfDeclaration: PsiElement): LexicalScope {
        if (localClassDescriptorManager.isMyClass(elementOfDeclaration)) {
            return localClassDescriptorManager.getResolutionScopeForClass(elementOfDeclaration as KtClassOrObject)
        }
        return super.getResolutionScopeForDeclaration(elementOfDeclaration)
    }

    override fun getOuterDataFlowInfoForDeclaration(elementOfDeclaration: PsiElement): DataFlowInfo {
        // nested (non-inner) classes and companion objects are forbidden in local classes, so it's enough to be simply inside the class
        if (localClassDescriptorManager.insideMyClass(elementOfDeclaration)) {
            return localClassDescriptorManager.expressionTypingContext.dataFlowInfo
        }
        return super.getOuterDataFlowInfoForDeclaration(elementOfDeclaration)
    }
}
