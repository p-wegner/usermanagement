[Coding Aider Plan]

## Overview
This subplan focuses on configuring the backend to generate an OpenAPI JSON artifact. This artifact will be used by the frontend to generate Angular API client code.

## Problem Description
The backend currently does not generate an OpenAPI JSON artifact, which is necessary for the frontend to automatically generate API client code.

## Goals
- Configure the backend to generate an OpenAPI JSON artifact.
- Ensure the OpenAPI JSON is available as a build artifact.

## Additional Notes and Constraints
- Use SpringDoc OpenAPI to generate the OpenAPI JSON.
- Ensure the OpenAPI JSON is updated with each build.

## References
- [SpringDoc OpenAPI](https://springdoc.org/)

