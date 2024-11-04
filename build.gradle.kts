import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import io.izzel.taboolib.gradle.*
import io.izzel.taboolib.gradle.Basic
import io.izzel.taboolib.gradle.Bukkit
import io.izzel.taboolib.gradle.BukkitHook
import io.izzel.taboolib.gradle.BukkitUI
import io.izzel.taboolib.gradle.BukkitUtil
import io.izzel.taboolib.gradle.MinecraftEffect
import io.izzel.taboolib.gradle.CommandHelper
import io.izzel.taboolib.gradle.Metrics
import io.izzel.taboolib.gradle.DatabasePlayer


plugins {
    kotlin("jvm") version "2.0.20"
    id("io.izzel.taboolib") version "2.0.20"
}

taboolib {
    env {
        install(Basic)
        install(Bukkit)
        install(BukkitHook)
        install(BukkitUI)
        install(BukkitUtil)
        install(MinecraftEffect)
        install(CommandHelper)
        install(Metrics)
        install(DatabasePlayer)
    }
    description {
        name = "FreeSwitchTitle"
        desc("称号插件")
        contributors {
            name("D_xiaox(星空)")
        }
    }
    version { taboolib = "6.2.0-beta33" }
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("ink.ptms.core:v12004:12004:mapped")
    compileOnly("ink.ptms.core:v12004:12004:universal")
    compileOnly(kotlin("stdlib"))
    compileOnly(fileTree("libs"))
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-Xjvm-default=all")
    }
}

configure<JavaPluginConvention> {
}
kotlin {
    jvmToolchain(8)
}