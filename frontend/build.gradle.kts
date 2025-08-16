plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlinSerialization)
    id("dev.icerock.mobile.multiplatform-resources") version "0.23.0"
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }
    
    jvm("desktop")
    
    js(IR) {
        moduleName = "snippetia-web"
        browser {
            commonWebpackConfig {
                outputFileName = "snippetia.js"
            }
            testTask {
                useKarma {
                    useChromeHeadless()
                    webpackConfig.cssSupport {
                        enabled.set(true)
                    }
                }
            }
        }
        binaries.executable()
    }
    
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
                implementation(compose.materialIconsExtended)
                
                // Networking
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.ktor.client.logging)
                implementation(libs.ktor.client.auth)
                implementation(libs.ktor.client.websockets)
                
                // Navigation
                implementation(libs.voyager.navigator)
                implementation(libs.voyager.screenModel)
                implementation(libs.voyager.transitions)
                implementation(libs.voyager.tabNavigator)
                implementation(libs.voyager.bottomSheetNavigator)
                
                // State Management
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.datetime)
                implementation(libs.kotlinx.collections.immutable)
                
                // Image Loading
                implementation(libs.kamel.image)
                implementation(libs.coil.compose)
                implementation(libs.coil.network.ktor)
                
                // Local Storage & Database
                implementation(libs.multiplatform.settings)
                implementation(libs.sqldelight.runtime)
                implementation(libs.sqldelight.coroutines.extensions)
                
                // Dependency Injection
                implementation(libs.koin.core)
                implementation(libs.koin.compose)
                
                // Code Editor & Highlighting
                implementation(libs.compose.code.editor)
                implementation(libs.compose.markdown)
                
                // WebAuthn & Security
                implementation(libs.webauthn.client)
                
                // File System
                implementation(libs.okio)
                
                // Resources
                implementation(libs.moko.resources)
                implementation(libs.moko.resources.compose)
                
                // Utils
                implementation(libs.uuid)
                implementation(libs.napier)
                
                // Animation
                implementation(libs.compose.animation.graphics)
                
                // Rich Text Editor
                implementation(libs.compose.rich.editor)
                
                // Charts & Analytics
                implementation(libs.compose.charts)
                
                // QR Code
                implementation(libs.qr.kit)
                
                // Permissions
                implementation(libs.moko.permissions)
                implementation(libs.moko.permissions.compose)
            }
        }
        
        val androidMain by getting {
            dependencies {
                implementation(libs.compose.ui.tooling.preview)
                implementation(libs.androidx.activity.compose)
                implementation(libs.androidx.lifecycle.viewmodel.compose)
                implementation(libs.androidx.lifecycle.runtime.compose)
                implementation(libs.androidx.navigation.compose)
                implementation(libs.androidx.work.runtime.ktx)
                implementation(libs.androidx.datastore.preferences)
                implementation(libs.androidx.biometric)
                implementation(libs.androidx.credentials)
                implementation(libs.androidx.credentials.play.services.auth)
                implementation(libs.androidx.browser)
                implementation(libs.androidx.core.splashscreen)
                
                // Networking
                implementation(libs.ktor.client.android)
                implementation(libs.ktor.client.okhttp)
                
                // Database
                implementation(libs.sqldelight.android.driver)
                
                // DI
                implementation(libs.koin.android)
                implementation(libs.koin.androidx.compose)
                
                // Camera & File Picker
                implementation(libs.androidx.camera.camera2)
                implementation(libs.androidx.camera.lifecycle)
                implementation(libs.androidx.camera.view)
                
                // Push Notifications
                implementation(libs.firebase.messaging)
                implementation(libs.firebase.analytics)
                
                // Payment
                implementation(libs.stripe.android)
                
                // Code Execution
                implementation(libs.termux.app)
                
                // Git
                implementation(libs.jgit)
            }
        }
        
        val iosMain by creating {
            dependsOn(commonMain)
            dependencies {
                implementation(libs.ktor.client.darwin)
                implementation(libs.sqldelight.native.driver)
            }
        }
        
        val iosX64Main by getting {
            dependsOn(iosMain)
        }
        
        val iosArm64Main by getting {
            dependsOn(iosMain)
        }
        
        val iosSimulatorArm64Main by getting {
            dependsOn(iosMain)
        }
        
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.ktor.client.cio)
                implementation(libs.kotlinx.coroutines.swing)
                implementation(libs.sqldelight.sqlite.driver)
                
                // File System
                implementation(libs.appdirs)
                
                // System Tray
                implementation(libs.compose.desktop.components.splitpane)
                
                // Git
                implementation(libs.jgit)
                
                // Code Execution
                implementation(libs.pty4j)
            }
        }
        
        val jsMain by getting {
            dependencies {
                implementation(compose.html.core)
                implementation(libs.ktor.client.js)
                implementation(libs.sqldelight.web.worker.driver)
                
                // Monaco Editor
                implementation(npm("monaco-editor", "0.44.0"))
                implementation(npm("@monaco-editor/loader", "1.4.0"))
                
                // Syntax Highlighting
                implementation(npm("highlight.js", "11.9.0"))
                implementation(npm("prismjs", "1.29.0"))
                
                // File System
                implementation(npm("file-saver", "2.0.5"))
                
                // WebAuthn
                implementation(npm("@simplewebauthn/browser", "8.3.4"))
                
                // Payment
                implementation(npm("@stripe/stripe-js", "2.1.11"))
                
                // Git (JS implementation)
                implementation(npm("isomorphic-git", "1.24.5"))
                
                // Code Execution (Web Workers)
                implementation(npm("comlink", "4.4.1"))
                
                // WebRTC for real-time collaboration
                implementation(npm("simple-peer", "9.11.1"))
                
                // Markdown
                implementation(npm("marked", "9.1.2"))
                implementation(npm("dompurify", "3.0.5"))
                
                // Charts
                implementation(npm("chart.js", "4.4.0"))
                
                // Notifications
                implementation(npm("push.js", "1.0.12"))
            }
        }
        
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.ktor.client.mock)
                implementation(libs.turbine)
                implementation(libs.koin.test)
            }
        }
        
        val androidUnitTest by getting {
            dependencies {
                implementation(libs.junit)
                implementation(libs.androidx.test.core)
                implementation(libs.androidx.test.junit)
                implementation(libs.robolectric)
                implementation(libs.mockk.android)
            }
        }
        
        val androidInstrumentedTest by getting {
            dependencies {
                implementation(libs.androidx.test.runner)
                implementation(libs.androidx.test.rules)
                implementation(libs.androidx.compose.ui.test.junit4)
                implementation(libs.androidx.test.espresso.core)
            }
        }
        
        val desktopTest by getting {
            dependencies {
                implementation(compose.desktop.uiTestJUnit4)
                implementation(libs.mockk)
            }
        }
        
        val jsTest by getting {
            dependencies {
                implementation(libs.kotlin.test.js)
            }
        }
    }
}

android {
    namespace = "com.snippetia"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        applicationId = "com.snippetia"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0.0"
        
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        getByName("debug") {
            isDebuggable = true
            applicationIdSuffix = ".debug"
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    
    buildFeatures {
        compose = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }
    
    dependencies {
        debugImplementation(libs.compose.ui.tooling)
        debugImplementation(libs.compose.ui.test.manifest)
    }
}

compose.desktop {
    application {
        mainClass = "com.snippetia.MainKt"

        nativeDistributions {
            targetFormats(
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Rpm
            )
            packageName = "Snippetia"
            packageVersion = "1.0.0"
            description = "The ultimate code sharing platform for developers"
            copyright = "© 2024 Snippetia. All rights reserved."
            vendor = "Snippetia"
            
            windows {
                iconFile.set(project.file("src/commonMain/resources/icon.ico"))
                upgradeUuid = "18159995-d967-4CD2-8885-77BFA97CFA9F"
            }
            macOS {
                iconFile.set(project.file("src/commonMain/resources/icon.icns"))
                bundleID = "com.snippetia.desktop"
                appCategory = "public.app-category.developer-tools"
            }
            linux {
                iconFile.set(project.file("src/commonMain/resources/icon.png"))
                packageName = "snippetia"
                debMaintainer = "support@snippetia.dev"
                menuGroup = "Development"
                appRelease = "1"
                appCategory = "Development"
            }
        }
        
        buildTypes.release.proguard {
            configurationFiles.from(project.file("compose-desktop.pro"))
        }
    }
}

multiplatformResources {
    multiplatformResourcesPackage = "com.snippetia.resources"
    multiplatformResourcesClassName = "SharedRes"
}