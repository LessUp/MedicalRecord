plugins {
    kotlin("jvm") version "1.9.24"
    kotlin("plugin.serialization") version "1.9.24"
    id("io.ktor.plugin") version "2.3.7"
    application
}

group = "com.lessup.medledger"
version = "0.1.0"

application {
    mainClass.set("com.lessup.medledger.ApplicationKt")
}

repositories {
    mavenCentral()
}

dependencies {
    // Ktor Server
    implementation("io.ktor:ktor-server-core-jvm:2.3.7")
    implementation("io.ktor:ktor-server-netty-jvm:2.3.7")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:2.3.7")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:2.3.7")
    implementation("io.ktor:ktor-server-auth-jvm:2.3.7")
    implementation("io.ktor:ktor-server-auth-jwt-jvm:2.3.7")
    implementation("io.ktor:ktor-server-cors-jvm:2.3.7")
    implementation("io.ktor:ktor-server-status-pages-jvm:2.3.7")
    implementation("io.ktor:ktor-server-call-logging-jvm:2.3.7")
    
    // Database
    implementation("org.jetbrains.exposed:exposed-core:0.46.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.46.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.46.0")
    implementation("org.jetbrains.exposed:exposed-java-time:0.46.0")
    implementation("org.postgresql:postgresql:42.7.1")
    implementation("com.zaxxer:HikariCP:5.1.0")
    
    // Koin DI
    implementation("io.insert-koin:koin-ktor:3.5.3")
    implementation("io.insert-koin:koin-logger-slf4j:3.5.3")
    
    // Logging
    implementation("ch.qos.logback:logback-classic:1.4.14")
    
    // BCrypt for password hashing
    implementation("org.mindrot:jbcrypt:0.4")
    
    // AWS S3 SDK for file storage
    implementation("aws.sdk.kotlin:s3:1.0.30")
    
    // Testing
    testImplementation("io.ktor:ktor-server-tests-jvm:2.3.7")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.9.24")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}
