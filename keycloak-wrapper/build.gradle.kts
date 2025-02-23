plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.4.2"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.springdoc.openapi-gradle-plugin") version "1.9.0"
    id("com.avast.gradle.docker-compose") version "0.17.6"
    id("com.google.cloud.tools.jib") version "3.4.1"
}

jib {
    from {
        image = "eclipse-temurin:21-jre-alpine"
    }
    to {
        image = "keycloak-wrapper"
        tags = setOf("latest", version.toString())
    }
    container {
        jvmFlags = listOf("-Xms512m", "-Xmx512m")
        ports = listOf("8080")
        environment = mapOf(
            "SPRING_PROFILES_ACTIVE" to "prod"
        )
    }
}

openApi {
    outputDir.set(file("$buildDir/docs"))
    outputFileName.set("swagger.json")
    waitTimeInSeconds.set(10)
    customBootRun {
        args.add("--spring.profiles.active=apidocs")
    }
}

// Copy docker-compose.yml to resources
tasks.register<Copy>("copyDockerCompose") {
    from("docker-compose.yml")
    into("src/main/resources")
}

tasks.processResources {
    dependsOn("copyDockerCompose")
}
tasks.register<Copy>("generateOpenApiJson") {
    outputs.file("$buildDir/docs/swagger.json")
    from("$buildDir/docs/swagger.json")
    into("$buildDir/docs/")
    dependsOn("bootRun")
    finalizedBy("openApiGenerate")
}
tasks.bootRun {
    dependsOn("processResources")
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
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.keycloak:keycloak-admin-client:26.0.4")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.4")
    implementation("org.springframework.boot:spring-boot-docker-compose")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.testcontainers:testcontainers:1.19.7")
    testImplementation("org.testcontainers:junit-jupiter:1.19.7")
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
