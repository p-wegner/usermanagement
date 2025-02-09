a gradle multimodule project to build a simple keycloak usermanagement ui
- a kotlin spring boot backend will provide a rest api that delegates calls to keycloak
- an angular frontend

features:
- manage users: create, edit, delete, search by name with pagination
- manage groups: create, edit, delete, search by name with pagination
- manage roles and composite roles: create, edit, delete, search by name with pagination
- assign roles to users and groups

# Run backend
```shell
./keycloak-wrapper/gradlew.bat :keycloak-wrapper:bootRun
```

# Generate OpenAPI JSON
```shell
./keycloak-wrapper/gradlew.bat :backend:generateOpenApiJson
```
# Run frontend
http://localhost:8080/swagger-ui/index.htm
