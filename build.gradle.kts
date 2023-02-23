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

    compileOnly("com.github.Anuken.Arc:arc-core:$mindustryVersion")
    compileOnly("com.github.Anuken.MindustryJitpack:core:$mindustryVersion")
    compileOnly("com.github.Anuken.MindustryJitpack:server:$mindustryVersion")

    implementation("com.google.code.gson:gson:2.10.1")
    implementation("club.minnced:discord-webhooks:0.8.2")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.19.0")

    implementation(("io.projectreactor:reactor-core"))
    implementation("org.mongodb:mongodb-driver-reactivestreams:4.8.1")
    implementation(platform("io.projectreactor:reactor-bom:2020.0.24"))
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"

    sourceCompatibility = "18"
    targetCompatibility = "18"
}

tasks.jar {
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}
