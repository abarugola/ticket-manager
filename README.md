# Ticket Manager API

Backend en **Java Spring Boot** para la gestión de conciertos y reserva concurrente de asientos en tiempo real, integrado con **DynamoDB**, **GraphQL** y autenticación híbrida mediante **Google OAuth2 + JWT propio**.

---

## Documentación del Proyecto

* 📄 [Contrato de API (REST & GraphQL)](./api_contract.md)
* 🏛️ [Decisiones de Arquitectura y Diseño](./Documento_Decisiones.md)
* 🐳 [Configuración Docker Compose](./docker-compose.yml)
* 📐 [Esquema de GraphQL](./src/main/resources/graphql/schema.graphqls)

---

## Tecnologías Utilizadas

- **Java 17 / Spring Boot 3.x**
- **Spring GraphQL** & **Spring Security (OAuth2 Resource Server)**
- **Amazon DynamoDB (AWS SDK v1 / DynamoDBMapper)**
- **Docker & Docker Compose** (DynamoDB Local)
- **JSON Web Tokens (jjwt)** para autenticación propia y refresco de tokens
- **Lombok** & **MapStruct**

---

## Requisitos Previos

Asegúrate de tener instalados los siguientes componentes:

1. **Java JDK 17** o superior.
2. **Maven 3.8+** (o utilizar el wrapper `./mvnw` incluido).
3. **Docker** y **Docker Compose** (para ejecutar DynamoDB Local).
4. Un **Google Cloud OAuth 2.0 Client ID** (para autenticación con Google).

---

## Configuración e Instalación

### 1. Clonar el Repositorio
```bash
git clone <URL_DEL_REPOSITORIO>
cd ticket-manager
```

### 2. Iniciar la Base de Datos (DynamoDB Local)
El proyecto incluye una configuración en `docker-compose.yml` para levantar una instancia local de DynamoDB en el puerto `8000`:

```bash
docker-compose up -d
```

### 3. Configurar Propiedades de la Aplicación
Revisa el archivo `src/main/resources/application.yaml` o configura las variables de entorno necesarias:

```yaml
server:
  port: 8080

spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          client-id: TU_GOOGLE_CLIENT_ID.apps.googleusercontent.com
          issuer-uri: https://accounts.google.com

aws:
  dynamodb:
    endpoint: http://localhost:8000
    region: us-east-1
    access-key: dummyKey
    secret-key: dummySecret
```

---

## Ejecución de la Aplicación

### Compilar y Ejecutar con Maven
```bash
./mvnw clean spring-boot:run
```

La aplicación se iniciará en `http://localhost:8080`.

---

## Puntos de Acceso Rápido

- **GraphiQL (IDE interactivo de GraphQL):** `http://localhost:8080/graphiql`
- **Endpoint GraphQL:** `POST http://localhost:8080/graphql`
- **Endpoints Auth:** `http://localhost:8080/auth/*`
- **Endpoints Concert:** `http://localhost:8080/v1/concerts`

---

## Flujo de Autenticación y Refresco

```
[Cliente / Postman] --(1. Login Google)--> [Google OAuth2]
        |
        +--(2. idToken)-------------> [POST /auth/login/google]
                                             |
[Cliente] <-- (3. AccessToken + RefreshToken)-+
   |
   +--(4. Bearer AccessToken)-------> [POST /graphql (confirmReservation)]
   |
   +--(5. Bearer RefreshToken)------> [POST /auth/refresh]
```

1. Obtén el `id_token` de Google mediante OAuth 2.0.
2. Intercámbialo en `POST /auth/login/google` por un **AccessToken (15 min)** y un **RefreshToken (7 días)** propios.
3. Utiliza el `accessToken` en la cabecera `Authorization: Bearer <token>` para las operaciones protegidas de GraphQL.
4. Cuando el `accessToken` expire, renuévalo en `POST /auth/refresh` sin solicitar login al usuario.
