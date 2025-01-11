plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.sa3id"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.sa3id"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    configurations.all {
        exclude(module = "httpclient")
    }
    packaging {
        resources.excludes.add("META-INF/DEPENDENCIES")
        resources.excludes.add("META-INF/LICENSE")
        resources.excludes.add("META-INF/LICENSE.txt")
        resources.excludes.add("META-INF/license.txt")
        resources.excludes.add("META-INF/NOTICE")
        resources.excludes.add("META-INF/NOTICE.txt")
        resources.excludes.add("META-INF/notice.txt")
        resources.excludes.add("META-INF/ASL2.0")
    }
}

dependencies {



    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.inappmessaging.display)
    implementation("com.google.android.flexbox:flexbox:3.0.0")

    implementation("com.squareup.picasso:picasso:2.8")
    implementation("com.google.android.material:material:1.12.0")

    implementation("com.google.http-client:google-http-client-gson:1.26.0")
    implementation("com.google.api-client:google-api-client-android:1.26.0") {
        exclude(group = "org.apache.httpcomponents")
    }
    implementation("com.google.apis:google-api-services-drive:v3-rev197-1.25.0") {
        exclude(group = "org.apache.httpcomponents")
    }
    implementation("com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava")
    implementation("com.google.android.gms:play-services-auth:20.5.0")

    implementation(libs.glide)
    annotationProcessor(libs.compiler)



    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.firestore)






    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)


}
