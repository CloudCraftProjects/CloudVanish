plugins {
    id("java-library")
    id("maven-publish")

    id("xyz.jpenilla.run-paper") version "1.0.6"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.2"
}

group = "dev.booky"
version = "1.0.0"

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnlyApi("io.papermc.paper:paper-api:1.19.2-R0.1-SNAPSHOT")
    api("org.bstats:bstats-bukkit:3.0.0")
}

java {
    withSourcesJar()
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

publishing {
    publications.create<MavenPublication>("maven") {
        artifactId = project.name.toLowerCase()
        from(components["java"])
    }
}

bukkit {
    main = "$group.vanish.CloudVanishMain"
    apiVersion = "1.19"
    authors = listOf("booky10")
}

tasks {
    runServer {
        minecraftVersion("1.19.2")
    }

    shadowJar {
        relocate("org.bstats", "dev.booky.vanish.bstats")
    }

    assemble {
        dependsOn(shadowJar)
    }
}
