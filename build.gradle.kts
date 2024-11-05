val kotlin_version: String by project
val logback_version: String by project
val ktor_version: String by project
val kotlin_css_version: String by project

plugins {
    kotlin("jvm") version "2.0.0"
    id("io.ktor.plugin") version "2.3.12"
    kotlin("plugin.serialization") version "1.8.0"
}

group = "monitoring"
version = "0.0.1"

tasks.register<Jar>("jarClient") {
    archiveClassifier.set("client")
    manifest {
        attributes["Main-Class"] = "monitoring.client.ClientKt"
    }
    from(sourceSets["main"].output)
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.register<Jar>("jarServer") {
    archiveClassifier.set("server")
    manifest {
        attributes["Main-Class"] = "monitoring.server.ServerKt"
    }
    from(sourceSets["main"].output)
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.register("buildAllJars") {
    dependsOn("jarServer", "jarClient")
}

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    testImplementation("io.ktor:ktor-server-tests:$ktor_version")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlin_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
    implementation("io.ktor:ktor-server-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-server-html-builder:$ktor_version")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-css:$kotlin_css_version")
    implementation("com.github.UnitTestBot:jacodb:neo-SNAPSHOT")
    implementation("io.ktor:ktor-server-websockets:$ktor_version")
}
