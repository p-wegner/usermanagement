plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.4.2"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.springdoc.openapi-gradle-plugin") version "1.9.0"
}

openApi {
    outputDir.set(file("$buildDir/docs"))
    outputFileName.set("swagger.json")
    waitTimeInSeconds.set(10)
    customBootRun {
//        args.addAll("--spring.profiles.active=special")
    }
}

tasks.register<Copy>("generateOpenApiJson") {
    outputs.file("$buildDir/docs/swagger.json")
    from("$buildDir/docs/swagger.json")
    into("$buildDir/docs/")
    dependsOn("bootRun")
    finalizedBy("openApiGenerate")
}
val apispec by configurations.creating
artifacts {
    add("apispec", file("$buildDir/docs/swagger.json")) {
        builtBy("generateOpenApiJson")
    }
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.keycloak:keycloak-admin-client:26.0.4")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.4")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
