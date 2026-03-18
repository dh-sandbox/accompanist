/*
 * Copyright 2024 The Android Open Source Project
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

import com.android.build.gradle.LibraryExtension
import com.google.accompanist.configureKotlinAndroid
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.register
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.tasks.JacocoReport

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
                apply("org.jetbrains.kotlin.android")
                apply("jacoco")
            }

            extensions.configure<JacocoPluginExtension> {
                toolVersion = "0.8.11"
            }

            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(this)
                defaultConfig.targetSdk = 35
                defaultConfig.testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

                buildFeatures.buildConfig = false

                testOptions.animationsDisabled = true
                // The resource prefix is derived from the module name,
                // so resources inside ":core:module1" must be prefixed with "core_module1_"
                resourcePrefix = path.split("""\W""".toRegex()).drop(1).distinct().joinToString(separator = "_").lowercase() + "_"
            }

            tasks.register<JacocoReport>("jacocoTestReport") {
                reports {
                    xml.required.set(true)
                    html.required.set(false)
                }
                sourceDirectories.setFrom(
                    files("${projectDir}/src/main/java", "${projectDir}/src/main/kotlin")
                )
                classDirectories.setFrom(
                    fileTree("${buildDir}/tmp/kotlin-classes/debug")
                )
                executionData.setFrom(
                    fileTree(buildDir).include("jacoco/testDebugUnitTest.exec")
                )
                onlyIf {
                    executionData.files.any { it.exists() }
                }
            }

            dependencies {
                add("androidTestImplementation", kotlin("test"))
                add("testImplementation", kotlin("test"))
            }
        }
    }
}
