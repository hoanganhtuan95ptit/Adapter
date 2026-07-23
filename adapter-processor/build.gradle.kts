plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
    id("maven-publish")
}

kotlin {
    jvmToolchain(17)
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components.findByName("java"))
        }
    }
}

dependencies {
    // KSP API — compileOnly: KSP runtime tự inject, không cần đưa vào POM
    implementation(libs.symbol.processing.api)

    // KotlinPoet — implementation: bắt buộc có mặt khi processor chạy trong project tiêu thụ.
    // Phải là implementation (không phải compileOnly) để được ghi vào POM và
    // Gradle tự resolve khi project tiêu thụ dùng ksp("...adapter-processor...")
    implementation(libs.kotlinpoet)
    implementation(libs.kotlinpoet.ksp)

    // auto-register-processor — runtimeOnly: đưa vào POM scope runtime để KSP của project
    // tiêu thụ tự discover qua ServiceLoader và chạy song song với AdapterProcessor.
    // Không dùng api/implementation để tránh leak vào compile classpath của project tiêu thụ.
    implementation(libs.auto.register.processor)
}
