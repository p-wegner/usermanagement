plugins {
  id("com.github.node-gradle.node") version "3.5.1"
  id("org.openapi.generator") version "6.5.0"
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

openApiGenerate {
  generatorName.set("typescript-angular")
  inputSpec.set(apispec.singleFile.absolutePath)
  outputDir.set("$projectDir/src/app/api")
  apiPackage.set("com.example.api")
  modelPackage.set("com.example.model")
  invokerPackage.set("com.example.invoker")
}

tasks.named("generateAngularClient") {
  dependsOn("openApiGenerate")
}

tasks.register("buildFrontend") {
  dependsOn("npmInstall", "generateAngularClient")
  doLast {
    exec {
      commandLine("npm", "run", "build")
    }
  }
}

