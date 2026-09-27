// Questlog — project-level build script.
// Every plugin used by any module is declared here (with `apply false`) so the
// build classpath is identical across the project and plugin versions cannot drift.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
}
