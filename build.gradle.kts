// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    val kotlinVersion = "1.8.10"
    id("com.android.application") version "8.1.2" apply false
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin") version "2.0.1" apply false
    id("com.google.dagger.hilt.android") version "2.47" apply false
    kotlin("android") version kotlinVersion apply false
}