plugins {
    id("java-library")
    id("maven-publish")

    alias(libs.plugins.pluginyml.bukkit)
    alias(libs.plugins.runtask.paper)
    alias(libs.plugins.shadow)
}

group = "dev.booky"
version = "1.1.1-SNAPSHOT"

val plugin: Configuration by configurations.creating {
    isTransitive = false
}

repositories {
    maven("https://repo.cloudcraftmc.de/public/")
}

dependencies {
    compileOnly(libs.paper.api)

    implementation(libs.bstats.bukkit)

    compileOnlyApi(libs.cloudcore)
    compileOnly(libs.commandapi.bukkit.core)

    // testserver dependency plugins
    plugin(variantOf(libs.cloudcore) {classifier("all")})
    plugin(libs.commandapi.bukkit.plugin)
}

java {
    withSourcesJar()
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
        vendor = JvmVendorSpec.ADOPTIUM
    }
}

publishing {
    publications.create<MavenPublication>("maven") {
        artifactId = project.name.lowercase()
        from(components["java"])
    }
}

bukkit {
    main = "$group.vanish.CloudVanishMain"
    apiVersion = "1.20"
    authors = listOf("booky10")
    depend = listOf("CloudCore", "CommandAPI")
}

tasks {
    runServer {
        minecraftVersion("1.21.1")
        pluginJars.from(plugin.resolve())
    }

    shadowJar {
        relocate("org.bstats", "dev.booky.vanish.bstats")
    }

    assemble {
        dependsOn(shadowJar)
    }
}
