import com.github.jengelman.gradle.plugins.shadow.tasks.ConfigureShadowRelocation

plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "org.ru.vortex"
version = "1.0.0"

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {
    val mindustryVersion = "v140.4"

    compileOnly("com.github.Anuken.Arc:arc-core:$mindustryVersion")
    compileOnly("com.github.Anuken.Mindustry:core:$mindustryVersion")
    compileOnly("org.projectlombok:lombok:1.18.24")

    implementation("net.dv8tion:JDA:5.0.0-beta.2")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.19.0")
    // implementation("com.github.xzxADIxzx.useful-stuffs:server-menus:3261ff23ac")

    implementation("org.mongodb:mongodb-driver-reactivestreams:4.8.1")
    implementation(platform("io.projectreactor:reactor-bom:2020.0.24"))
    implementation(("io.projectreactor:reactor-core"))

    annotationProcessor("org.projectlombok:lombok:1.18.24")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.jar {
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

val relocate = tasks.register<ConfigureShadowRelocation>("relocateShadowJar") {
    target = tasks.shadowJar.get()
    prefix = project.property("props.root-package").toString() + ".shadow"
}

tasks.shadowJar {
    archiveFileName.set("VortexPlugin.jar")
    archiveClassifier.set("plugin")
    dependsOn(relocate)
    minimize()
}