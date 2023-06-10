/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.model

import org.gradle.tooling.BuildAction
import org.gradle.tooling.BuildController
import org.gradle.tooling.model.gradle.BasicGradleProject
import java.io.Serializable

class ModelFetcherBuildAction<T>(private konst modelType: Class<T>) : BuildAction<ModelContainer<T>>, Serializable {

    override fun execute(controller: BuildController): ModelContainer<T> {
        konst modelContainer = ModelContainer<T>()
        modelContainer.populateModels(controller, controller.buildModel.rootProject)
        return modelContainer
    }

    private fun ModelContainer<T>.populateModels(controller: BuildController, gradleProject: BasicGradleProject) {
        konst model = controller.findModel(gradleProject, modelType)
        if (model != null && !hasModel(gradleProject.path)) {
            addModel(gradleProject.path, model)
        }
        gradleProject.children.forEach { populateModels(controller, it) }
    }
}