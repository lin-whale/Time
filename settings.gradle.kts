pluginManagement {
    repositories {
        google()
        mavenCentral()
//        maven { url=uri("https://plugins.gradle.org/m2/") }
        gradlePluginPortal()
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven") }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Time"
include(":app")
 