import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import io.izzel.taboolib.gradle.*
import org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8
import io.izzel.taboolib.gradle.Bukkit
import io.izzel.taboolib.gradle.CommandHelper
import io.izzel.taboolib.gradle.I18n
import io.izzel.taboolib.gradle.Metrics
import io.izzel.taboolib.gradle.MinecraftChat
import io.izzel.taboolib.gradle.MinecraftEffect
import io.izzel.taboolib.gradle.Database
import io.izzel.taboolib.gradle.DatabasePlayer
import io.izzel.taboolib.gradle.BukkitUtil
import io.izzel.taboolib.gradle.BukkitUI
import io.izzel.taboolib.gradle.BukkitHook
import io.izzel.taboolib.gradle.Basic


plugins {
    java
    id("io.izzel.taboolib") version "2.0.37"
    id("org.jetbrains.kotlin.jvm") version "2.2.0"
}

taboolib {
    env {
        install(Bukkit)
        install(CommandHelper)
        install(I18n)
        install(Metrics)
        install(MinecraftChat)
        install(MinecraftEffect)
        install(Database)
        install(DatabasePlayer)
        install(BukkitUtil)
        install(BukkitUI)
        install(BukkitHook)
        install(Basic)
    }
    description {
        name = "FreeSwitchTitle"
        contributors {
            name("星空")
        }
    }
    version { taboolib = "6.3.0-932e79c" }
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
    compilerOptions {
        jvmTarget.set(JVM_1_8)
        freeCompilerArgs.add("-Xjvm-default=all")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}