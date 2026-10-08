# Contrato de API - Ticket Manager

Este documento define la especificación formal de los endpoints REST de autenticación y el esquema de GraphQL para la gestión de conciertos y reservas.

---

## Autenticación y Seguridad

| Endpoint                          | Método | Seguridad | Descripción                                               |
|:----------------------------------| :--- | :--- |:----------------------------------------------------------|
| `/auth/login/google`              | `POST` | Pública | Intercambia `idToken` de Google por JWTs propios.         |
| `/auth/refresh`                   | `POST` | Pública | Renueva el `accessToken` usando el `refreshToken`.        |
| `/v1/concerts`                    | `POST` | **Requerida** | Creacion de conciertos.                             |
| `/graphql` (Queries)              | `POST` | Pública / Opcional | Consulta libre de conciertos y disponibilidad.            |
| `/graphql` (`confirmReservation`) | `POST` | **Requerida** | Confirma una reserva de asientos con Bearer Token propio. |

---

## 1. Endpoints REST (Autenticación)

### 1.1 Login con Google
Valida el token de identidad de Google y retorna los tokens propios del sistema.

- **URL:** `/auth/login/google`
- **Método:** `POST`
- **Headers:** `Content-Type: application/json`

#### Request Body:
```json
{
  "idToken": "eyJhbGciOiJSUzI1NiIsImtpZCI6..."
}
```

#### Response (200 OK):
```json
{
  "accessToken": "eyJhbGciOiJIUzI1Ni...",
  "refreshToken": "eyJhbGciOiJIUzI1Ni...",
  "expiresInSeconds": 900
}
```

#### Response (401 Unauthorized):
```json
{
  "message": "Error al validar token de Google: Token expirado o inválido"
}
```

---

### 1.2 Refresh Token
Permite la emisión de un nuevo `accessToken` utilizando un `refreshToken` válido.

- **URL:** `/auth/refresh`
- **Método:** `POST`
- **Headers:** `Content-Type: application/json`

#### Request Body:
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1Ni..."
}
```

#### Response (200 OK):
```json
{
  "accessToken": "eyJhbGciOiJIUzI1Ni...",
  "expiresInSeconds": 900
}
```

#### Response (401 Unauthorized):
```json
{
  "message": "Refresh Token expirado o inválido. Inicie sesión de nuevo."
}
```

---

### 3. Crear Concierto

Crea un nuevo concierto junto con la estructura de sus asientos/filas correspondientes en DynamoDB.

* **URL:** `/v1/concerts`
* **Método:** `POST`
* **Autenticación:** Requiere Bearer Token
* **Headers:** `Content-Type: application/json`

#### Request Body:

```json
{
  "name": "Rock Fest 2026",
  "artist": "The Rockers",
  "date": "2026-11-20T20:00:00Z",
  "venue": "Estadio Nacional",
  "status": "UPCOMING",
  "rows": [
    {
      "rowName": "A",
      "seatCount": 10,
      "price": 150.00
    },
    {
      "rowName": "B",
      "seatCount": 15,
      "price": 100.00
    }
  ]
}
```

#### Response (201 Created)
```json
{
  "id": "c1a2b3c4-5678-90ab-cdef-1234567890ab",
  "name": "Rock Fest 2026",
  "artist": "The Rockers",
  "date": "2026-11-20T20:00:00Z",
  "venue": "Estadio Nacional",
  "status": "UPCOMING"
}
```

#### Response (400 Bad Request)
```json
{
  "message": "Bad request",
  "errors": [
    {
      "field": "name",
      "defaultMessage": "no debe estar vacío"
    }
  ]
}
```
---

## 3. API GraphQL (`POST /graphql`)

### 3.1 Esquema GraphQL (SDL)

```graphql
enum ConcertStatus {
  SCHEDULED
  IN_SALE
  SOLD_OUT
  CANCELLED
}

enum SeatStatus {
  AVAILABLE
  HELD
  SOLD
}

type ConcertSummary {
  id: ID!
  name: String!
  artist: String!
  date: String!
  venue: String!
  status: ConcertStatus!
}

type SeatDetailDTO {
  seatId: String!
  row: String!
  number: Int!
  status: SeatStatus!
  price: Float!
}

type ConcertDetailDTO {
  id: ID!
  name: String!
  artist: String!
  date: String!
  venue: String!
  status: ConcertStatus!
  availableSeats: [SeatDetailDTO!]!
}

input RowDetailRequestDTO {
  row: String!
  seatNumbers: [Int!]!
}

input ReservationRequestDTO {
  concertId: String!
  seats: [RowDetailRequestDTO!]!
}

type ReservationResponseDTO {
  reservationId: String!
  concertId: String!
  reservedSeatsCount: Int!
  totalPrice: Float!
  status: String!
}

type Query {
  concerts: [ConcertSummary!]!
  concertsByStatus(status: ConcertStatus!): [ConcertSummary!]!
  concertById(id: ID!): ConcertSummary
  concertDetailById(id: ID!): ConcertDetailDTO
}

type Mutation {
  confirmReservation(input: ReservationRequestDTO!): ReservationResponseDTO!
}
```

---

### 3.2 Ejemplos de Consultas y Respuestas GraphQL

#### A. Obtener lista de conciertos (Pública)
```graphql
query GetConcerts {
  concerts {
    id
    name
    artist
    date
    status
  }
}
```
**Respuesta (200 OK):**
```json
{
  "data": {
    "concerts": [
      {
        "id": "c101",
        "name": "World Tour 2026",
        "artist": "Coldplay",
        "date": "2026-11-15T20:00:00Z",
        "status": "IN_SALE"
      }
    ]
  }
}
```

---

#### B. Obtener detalle de concierto y asientos disponibles (Pública)
```graphql
query GetConcertDetail {
  concertDetailById(id: "c101") {
    id
    name
    venue
    availableSeats {
      seatId
      row
      number
      price
      status
    }
  }
}
```

---

#### C. Confirmar Reserva de Asientos (Protegida)
- **Header Requerido:** `Authorization: Bearer <accessToken_propio>`

```graphql
mutation ConfirmReservation {
  confirmReservation(input: {
    concertId: "c101",
    seats: [
      { row: "A", seatNumbers: [1, 2] }
    ]
  }) {
    reservationId
    concertId
    reservedSeatsCount
    totalPrice
    status
  }
}
```

**Respuesta Exitosa (200 OK):**
```json
{
  "data": {
    "confirmReservation": {
      "reservationId": "RES-88492-X",
      "concertId": "c101",
      "reservedSeatsCount": 2,
      "totalPrice": 250.00,
      "status": "CONFIRMED"
    }
  }
}
```

**Respuesta sin Autenticar / Token Inválido (200 OK - Formato de Error GraphQL):**
```json
{
  "errors": [
    {
      "message": "Token JWT requerido o inválido",
      "path": ["confirmReservation"],
      "extensions": {
        "classification": "UNAUTHORIZED"
      }
    }
  ],
  "data": {
    "confirmReservation": null
  }
}
```