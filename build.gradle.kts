// Top-level build file where you can add configuration options common to all sub-projects/modules.

// Sube la versión de Kotlin que usa el plugin de Android ya que Firebase necesita una más nueva
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.21")
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.google.services) apply false
}