plugins {
  id("com.github.node-gradle.node") version "3.5.1"
  id("org.openapi.generator") version "7.10.0"
}

node {
  version.set("23.7.0")
  npmVersion.set("10.9.2")
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
  inputSpec.set(apispec.singleFile.toURI().toString())
  outputDir.set("$projectDir/src/app/api")
  apiPackage.set("com.example.api")
  modelPackage.set("com.example.model")
  invokerPackage.set("com.example.invoker")
  configOptions.set(mapOf(
    "ngVersion" to "19.0.0",
    "supportsES6" to "true",
    "nullSafeAdditionalProps" to "true",
    "fileNaming" to "kebab-case",
    "enumNameSuffix" to "Enum",
    "stringEnums" to "true",
    "enumPropertyNaming" to "UPPERCASE",
    "serviceSuffix" to "Service",
    "serviceFileSuffix" to ".service",
    "modelFileSuffix" to ".model",
    "apiModulePrefix" to "Api",
    "useSingleRequestParameter" to "true"
  ))
}

tasks.register("buildFrontend") {
  dependsOn("npm_install", "openApiGenerate")
  finalizedBy("npm_run_build")
}

tasks.named("npm_run_build") {
  dependsOn("npm_install")
}

