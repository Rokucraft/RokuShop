plugins {
    `java-library`
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("xyz.jpenilla.run-paper") version "2.1.0"
}

group = "com.rokucraft"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.19.4-R0.1-SNAPSHOT")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
    implementation("cloud.commandframework:cloud-paper:1.8.3")
    implementation("org.spongepowered:configurate-yaml:4.1.2")
    implementation("net.kyori:adventure-serializer-configurate4:4.13.1") {
        isTransitive = false
    }
    implementation("org.incendo.interfaces:interfaces-paper:1.0.0-SNAPSHOT")
}

tasks {
    assemble {
        dependsOn(shadowJar)
    }

    shadowJar {
        isEnableRelocation = true
        relocationPrefix = "${project.group}.${project.name}.libs"
    }

    runServer {
        minecraftVersion("1.19.4")
    }

    processResources {
        val props = "version" to version
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}
