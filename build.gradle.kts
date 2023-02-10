import com.github.jengelman.gradle.plugins.shadow.tasks.ConfigureShadowRelocation
import fr.xpdustry.toxopid.dsl.anukenJitpack
import fr.xpdustry.toxopid.dsl.mindustryDependencies
import fr.xpdustry.toxopid.spec.ModMetadata
import fr.xpdustry.toxopid.spec.ModPlatform.HEADLESS

group = "org.ru.vortex"
version = "1.0.1"

plugins {
    id("java")
    id("fr.xpdustry.toxopid") version "3.0.0"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

toxopid {
    compileVersion.set("v141.3")
    runtimeVersion.set("v141.3")
    platforms.add(HEADLESS)
}

repositories {
    mavenCentral()
    anukenJitpack()
}

dependencies {
    mindustryDependencies()

    compileOnly("org.projectlombok:lombok:1.18.24")

    implementation("com.google.code.gson:gson:2.10.1")
    implementation("club.minnced:discord-webhooks:0.8.2")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.19.0")

    implementation(("io.projectreactor:reactor-core"))
    implementation("org.mongodb:mongodb-driver-reactivestreams:4.8.1")
    implementation(platform("io.projectreactor:reactor-bom:2020.0.24"))

    annotationProcessor("org.projectlombok:lombok:1.18.24")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"

    sourceCompatibility = "16"
    targetCompatibility = "16"
}

val metadata = ModMetadata(
    name = "vortex-plugin",
    displayName = "Vortex Plugin",
    author = "lucin, OSPx, nekonya, SSTentaclesSS",
    description = """
        A main plugin for vortex servers

        Check out our repository:
        + https://github.com/Vortex-coders/VortexPlugin

        Thanks for contributing:
        + lucin
        + OSPx
        + nekonya
        + SSTentaclesSS
    """.trimIndent(),
    version = project.version.toString(),
    main = "org.ru.vortex.Vortex"
)

val relocate = tasks.register<ConfigureShadowRelocation>("relocateShadowJar") {
    target = tasks.shadowJar.get()
    prefix = project.property("props.root-package").toString() + ".shadow"
}

tasks.shadowJar {
    doFirst {
        val temp = temporaryDir.resolve("mod.json")
        temp.writeText(metadata.toJson(true))
        from(temp)
    }

    archiveFileName.set("VortexPlugin.jar")
    archiveClassifier.set("plugin")
    dependsOn(relocate)
    minimize()
}
