/*
 * Copyright 2026 Strelitzia. All rights reserved.
 */

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.spotbugs.snom.SpotBugsTask
import java.text.SimpleDateFormat
import java.util.*

plugins {
    checkstyle
    id("com.github.spotbugs") version "6.5.12"
    id("com.gradleup.shadow") version "9.6.1" apply false
    id("xyz.jpenilla.run-paper") version "3.1.0"
    java
}

group = "com.crimsonwarpedcraft.exampleplugin"

fun getTime(): String {
    val sdf = SimpleDateFormat("yyMMdd-HHmm")
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    return sdf.format(Date())
}

version = (if (!hasProperty("ver")) {
    "${getTime()}-SNAPSHOT"
} else {
    val ver = property("ver") as String
    val base = if (ver.startsWith("v")) ver.drop(1) else ver.replace('/', '-')
    if (ver.startsWith("v") && !ver.lowercase().contains("-rc-")) base else "$base-SNAPSHOT"
}).uppercase()

allprojects {
    repositories {
        maven {
            name = "papermc"
            url = uri("https://repo.papermc.io/repository/maven-public/")
            content {
                includeModule("io.papermc.paper", "paper-api")
                includeModule("net.md-5", "bungeecord-chat")
                includeGroup("io.papermc.adventure")
            }
        }

        maven {
            name = "minecraft"
            url = uri("https://libraries.minecraft.net")
            content {
                includeModule("com.mojang", "brigadier")
            }
        }

        mavenCentral()

        maven {
            name = "jitpack"
            url = uri("https://jitpack.io")
            content {
                includeGroup("com.github.CrimsonWarpedcraft")
            }
        }
    }
}

subprojects {

    apply(plugin = "java")
    apply(plugin = "com.github.spotbugs")
    apply(plugin = "com.gradleup.shadow")

    val mockitoAgent = configurations.create("mockitoAgent")

    dependencies {
        compileOnly("io.papermc.paper:paper-api:26.2.build.129-stable")

        // Code quality and unit testing. Not required for code functionality.
        compileOnly("com.github.spotbugs:spotbugs-annotations:4.10.4")
        compileOnly("org.projectlombok:lombok:1.18.48")
        annotationProcessor("org.projectlombok:lombok:1.18.48")
        spotbugsPlugins("com.h3xstream.findsecbugs:findsecbugs-plugin:1.14.0")
        testCompileOnly("com.github.spotbugs:spotbugs-annotations:4.10.4")
        testCompileOnly("org.projectlombok:lombok:1.18.48")
        testAnnotationProcessor("org.projectlombok:lombok:1.18.48")
        testImplementation("io.papermc.paper:paper-api:26.2.build.129-stable")
        testImplementation("org.junit.jupiter:junit-jupiter:6.1.3")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher:6.1.3")

        // Example dependencies. Paper plugins do not require these libraries.
        implementation("com.github.CrimsonWarpedcraft:cw-commons:v0.3.3")
        // PluginConfig imports annotations from Jackson and Hibernate Validator directly.
        implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.22.3")
        implementation("dev.jorel:commandapi-paper-shade:12.1.0") {
            // Upstream metadata references unpublished modules already bundled in this JAR.
            // https://github.com/CommandAPI/CommandAPI/issues/704
            isTransitive = false
        }
        implementation("org.hibernate.validator:hibernate-validator:9.1.4.Final")

        testImplementation("org.mockito:mockito-core:5.24.0")
        mockitoAgent("org.mockito:mockito-core:5.24.0") { isTransitive = false }
    }

    tasks.test {
        useJUnitPlatform()
        jvmArgs("-javaagent:${mockitoAgent.asPath}")
    }

    val shadowJar = tasks.named<ShadowJar>("shadowJar") {
        archiveBaseName.set("${rootProject.name}-${project.name.replace(".", "-")}")
        archiveVersion.set(provider { project.version.toString() })
        archiveClassifier.set("")
        filesMatching("META-INF/services/**") {
            duplicatesStrategy = DuplicatesStrategy.INCLUDE
        }
        mergeServiceFiles()
        relocate("dev.jorel.commandapi", "${project.group}.commandapi")
        relocate("com.fasterxml", "${project.group}.fasterxml")
        relocate("org.yaml.snakeyaml", "${project.group}.snakeyaml")
        relocate("org.hibernate.validator", "${project.group}.hibernatevalidator")
        relocate("jakarta.validation", "${project.group}.jakartavalidation")
        relocate("org.jboss.logging", "${project.group}.jbosslogging")
        minimize {
            exclude(dependency("dev.jorel:commandapi-paper-shade:.*"))
            exclude(dependency("com.fasterxml.jackson.core:.*:.*"))
            exclude(dependency("com.fasterxml.jackson.dataformat:.*:.*"))
            exclude(dependency("com.fasterxml:classmate:.*"))
            exclude(dependency("org.hibernate.validator:.*:.*"))
            exclude(dependency("jakarta.validation:.*:.*"))
            exclude(dependency("org.yaml:snakeyaml:.*"))
            exclude(dependency("org.jboss.logging:.*:.*"))
            exclude(dependency("com.github.CrimsonWarpedcraft:cw-commons:.*"))
        }
    }

    tasks.jar {
        enabled = false
    }

    tasks.assemble {
        dependsOn(shadowJar)
    }
}

tasks.runServer {
    minecraftVersion("26.2")
}


testing {
    suites {
        register<JvmTestSuite>("integrationTest") {
            useJUnitJupiter("6.1.1")

            dependencies {
                implementation(sourceSets.main.get().output)
                implementation("io.papermc.paper:paper-api:26.2.build.129-stable")
            }

            targets {
                all {
                    testTask.configure {
                        shouldRunAfter(tasks.test)
                        jvmArgs("--enable-native-access=ALL-UNNAMED")
                    }
                }
            }
        }
    }
}

configurations.named("integrationTestImplementation") {
    extendsFrom(configurations.implementation.get())
}

tasks.processResources {
    filesMatching("**/plugin.yml") {
        expand(mapOf("NAME" to rootProject.name, "VERSION" to version, "PACKAGE" to project.group))
    }
}

checkstyle {
    toolVersion = "14.0.0"
    maxWarnings = 0
}

configurations.named("checkstyle") {
    resolutionStrategy.capabilitiesResolution
        .withCapability("com.google.collections:google-collections") {
            select("com.google.guava:guava:23.0")
        }
}

tasks.withType<Checkstyle>().configureEach {
    reports {
        xml.required.set(false)
        html.required.set(true)
    }
}

tasks.withType<SpotBugsTask>().configureEach {
    reports.create("html") {
        required.set(true)
    }
    reports.create("xml") {
        required.set(false)
    }
}

tasks.register("printProjectName") {
    doLast {
        println(rootProject.name)
    }
}