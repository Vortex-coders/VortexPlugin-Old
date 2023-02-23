plugins {
    id("java")
}

group = "org.ru.vortex"
version = "1.0.1"

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {
    val mindustryVersion = "v142"

    // Mindustry
    compileOnly("com.github.Anuken.Arc:arc-core:$mindustryVersion")
    compileOnly("com.github.Anuken.MindustryJitpack:core:$mindustryVersion")

    // Other dependencies
    implementation("org.slf4j:slf4j-api:2.0.3")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("club.minnced:discord-webhooks:0.8.2")

    // Reactive dependencies
    implementation(("io.projectreactor:reactor-core"))
    implementation("org.mongodb:mongodb-driver-reactivestreams:4.8.1")
    implementation(platform("io.projectreactor:reactor-bom:2020.0.24"))

    // Dependencies for sockets
    implementation("com.password4j:password4j:1.7.0")
    implementation("com.esotericsoftware.kryo:kryo5:5.4.0")
    implementation("org.java-websocket:Java-WebSocket:1.5.3")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"

    sourceCompatibility = "18"
    targetCompatibility = "18"
}

tasks.jar {
    archiveFileName.set("VortexPlugin.jar")
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}
