plugins {
    kotlin("jvm") version "2.0.0"
}

group = "com.dakuo"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    implementation("org.ow2.asm:asm:9.7.1")

}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(8)
}