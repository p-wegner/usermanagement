plugins {
    id("com.github.node-gradle.node") version "3.5.1"
}

node {
    version.set("18.0.0")
    npmVersion.set("8.0.0")
    download.set(true)
}

tasks.register("generateAngularClient") {
    inputs.file("$rootDir/src/main/resources/swagger.json")
    doLast {
        exec {
            commandLine("java", "-jar", "openapi-generator-cli.jar", "generate", "-i", "$rootDir/src/main/resources/swagger.json", "-g", "typescript-angular", "-o", "src/app/api")
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

