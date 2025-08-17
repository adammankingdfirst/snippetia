plugins {
    // Apply plugins to subprojects
}

allprojects {
    repositories {
        mavenCentral()
        google()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://repo.spring.io/milestone")
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}