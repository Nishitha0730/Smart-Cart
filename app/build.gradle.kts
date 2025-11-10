plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.21"
}

android {
    namespace = "com.example.smartcart"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.smartcart"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Expose SUPABASE_URL and SUPABASE_KEY via BuildConfig, values can be provided in gradle.properties
        val supabaseUrl: String? = project.findProperty("SUPABASE_URL") as String?
        val supabaseKey: String? = project.findProperty("SUPABASE_KEY") as String?

        // Debug logging
        println("🔍 Building with SUPABASE_URL: ${if (supabaseUrl.isNullOrBlank()) "NOT FOUND" else supabaseUrl}")
        println("🔍 Building with SUPABASE_KEY length: ${supabaseKey?.length ?: 0}")

        buildConfigField("String", "SUPABASE_URL", if (supabaseUrl != null) "\"$supabaseUrl\"" else "\"\"")
        buildConfigField("String", "SUPABASE_KEY", if (supabaseKey != null) "\"$supabaseKey\"" else "\"\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        // Enable BuildConfig generation so buildConfigField entries are applied
        buildConfig = true
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    // Use explicit coordinates for these dependencies to avoid unresolved catalog accessors
    implementation("androidx.navigation:navigation-compose:2.9.6")
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.play.services.auth)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // The project includes local stubs for the Supabase client under app/src/main/java/io/github/jan/supabase/
    // so we don't need the external dependency that previously failed to resolve.
    // implementation("io.github.jan.supabase:supabase-kt:3.2.6")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")

    implementation("io.github.jan-tennert.supabase:postgrest-kt:2.0.0")
    implementation("io.github.jan-tennert.supabase:realtime-kt:2.0.0")
    implementation("io.github.jan-tennert.supabase:gotrue-kt:2.0.0")

    // Ktor (required by Supabase)
    implementation("io.ktor:ktor-client-android:2.3.7")
    implementation("io.ktor:ktor-client-core:2.3.7")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")
    implementation("io.ktor:ktor-client-logging:2.3.7")

    // Kotlinx Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
}