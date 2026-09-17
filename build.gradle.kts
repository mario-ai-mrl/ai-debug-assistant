plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.20"
    id("org.jetbrains.intellij.platform") version "2.5.0"
}

group = "io.github.marioaimrl.aidebugassistant"
version = "0.1.0"

kotlin {
    jvmToolchain(21)
}

repositories {
    mavenCentral()
    intellijPlatform { defaultRepositories() }
}

dependencies {
    intellijPlatform {
        intellijIdeaCommunity("2025.1")
    }
}

intellijPlatform {
    buildSearchableOptions = false
    pluginVerification {
        ides { ide("IC", "2025.1") }
    }
    signing {
        certificateChainFile = layout.file(providers.environmentVariable("SIGNING_CERTIFICATE_FILE").map { file(it) })
        privateKeyFile = layout.file(providers.environmentVariable("SIGNING_PRIVATE_KEY_FILE").map { file(it) })
        password = providers.environmentVariable("PRIVATE_KEY_PASSWORD")
    }
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "251"
        }
    }
}

tasks {
    test { useJUnitPlatform() }
    named("verifyPluginSignature") { dependsOn("signPlugin") }
}
