plugins {
  id("com.github.node-gradle.node") version "3.5.1"
}

node {
  version.set("18.0.0")
  npmVersion.set("8.0.0")
  download.set(true)
}

val apispec by configurations.creating
dependencies {
  apispec(
    project(
      mapOf("path" to ":keycloak-wrapper", "configuration" to "apispec")
    )
  )
}

tasks.register("generateAngularClient") {
  val swaggerJson = apispec.singleFile
  inputs.file(swaggerJson)
  // TODO: use openapigenerate plugin
  doLast {
    exec {
      commandLine(
        "java",
        "-jar",
        "openapi-generator-cli.jar",
        "generate",
        "-i",
        swaggerJson.absolutePath,
        "-g",
        "typescript-angular",
        "-o",
        "src/app/api"
      )
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

