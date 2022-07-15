import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

// https://youtrack.jetbrains.com/issue/IDEA-262280
@Suppress("DSL_SCOPE_VIOLATION", "UnstableApiUsage")
plugins {
  alias(libs.plugins.versionsBenManes)
  alias(libs.plugins.dokka) apply false
  id("validate-gradle-properties")
}

tasks.named<DependencyUpdatesTask>("dependencyUpdates").configure {
  resolutionStrategy {
    componentSelection {
      all {
        val group = candidate.group
        if(group == "androidx.databinding") {
          reject("Ignore viewbinding")
        }
      }
    }
  }
}

tasks.register<Delete>("clean") {
  delete(buildDir)
}
