plugins {
    id("com.github.node-gradle.node") version "3.5.1"
}

node {
    version.set("18.0.0")
    npmVersion.set("8.0.0")
    download.set(true)
}

tasks.register("generateAngularClient") {
    doLast {
        exec {
            commandLine("java", "-jar", "openapi-generator-cli.jar", "generate", "-i", "../keycloak-wrapper/build/generated/openapi.yaml", "-g", "typescript-angular", "-o", "src/app/api")
        }
    }
}

tasks.register("buildFrontend") {
    dependsOn("npmInstall", "generateAngularClient")
    doLast {
        exec {
            commandLine("npm", "run", "build")
        }
    }
}
