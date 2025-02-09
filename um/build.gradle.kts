plugins {
    id("com.github.node-gradle.node") version "3.5.1"
}

node {
    version.set("18.0.0")
    npmVersion.set("8.0.0")
    download.set(true)
}

tasks.register("buildFrontend") {
    dependsOn("npmInstall")
    doLast {
        exec {
            commandLine("npm", "run", "build")
        }
    }
}
