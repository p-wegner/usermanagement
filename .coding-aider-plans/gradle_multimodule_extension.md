[Coding Aider Plan]

## Overview
This plan outlines the steps to extend the existing server-client monorepo project to use Gradle multimodules. The backend will provide a generated OpenAPI JSON as an artifact for the frontend to use. The frontend project will have a Gradle project that delegates tasks to Node.js using a Gradle Node plugin. The OpenAPI JSON specification will be used in the frontend build to generate Angular API client code.

## Problem Description
Currently, the project is structured as a monorepo with separate backend and frontend projects. The backend does not generate an OpenAPI JSON artifact, and the frontend does not have a Gradle project to manage its build process. This setup limits the integration between the backend and frontend, particularly in terms of API client generation.

## Goals
- Convert the project into a Gradle multimodule project.
- Configure the backend to generate an OpenAPI JSON artifact.
- Set up a Gradle project for the frontend that delegates tasks to Node.js.
- Use the OpenAPI JSON specification in the frontend build to generate Angular API client code.

## Additional Notes and Constraints
- Ensure compatibility with existing project dependencies and configurations.
- Maintain the current functionality of both backend and frontend applications.
- Use the Gradle Node plugin to manage Node.js tasks in the frontend.

## References
- [Gradle Multimodule Projects](https://docs.gradle.org/current/userguide/multi_project_builds.html)
- [OpenAPI Generator](https://openapi-generator.tech/)
- [Gradle Node Plugin](https://github.com/node-gradle/gradle-node-plugin)

<!-- SUBPLAN:gradle_multimodule_backend -->
[Subplan: Backend OpenAPI Generation](gradle_multimodule_backend.md)
<!-- END_SUBPLAN -->

<!-- SUBPLAN:gradle_multimodule_frontend -->
[Subplan: Frontend Gradle Integration](gradle_multimodule_frontend.md)
<!-- END_SUBPLAN -->
