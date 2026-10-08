# Documento de Decisiones de Diseño e Ingeniería

---

## 1. Arquitectura de APIs e Integración
* **Decisión de Diseño:** Implementación de una arquitectura híbrida con **Spring GraphQL** para la interacción con el cliente (Queries y Mutations) y **REST API con Spring Boot** para la gestión e integración interna.
---

## 2. Seguridad y Autenticación
* **Decisión de Diseño:** Uso de **Spring Security** integrado con proveedores de identidad externos (Google OAuth2/JWT).
* **Alternativas Consideradas:** Implementación de un mecanismo customizado de autenticación y manejo manual de tokens/sesiones.
* **Justificación:** Spring Security proporciona un marco maduro y modular para asegurar endpoints de forma declarativa. Facilita la integración nativa con librerías de OAuth2/OIDC, delegando la verificación del proveedor de identidad y manteniendo un alto estándar de seguridad sin reinventar la rueda.

---

## 3. Modelo de Datos y Control de Asientos (DynamoDB)
* **Decisión de Diseño:** Separación de la entidad **Concierto** y la entidad **Asiento** en tablas independientes en Amazon DynamoDB, utilizando **Conditional Checks** para la confirmación de compras.
* **Alternativas Consideradas:**
    1. *Estructura incrustada:* Guardar el mapa de asientos como un mapa/lista dentro del mismo documento del concierto.
    2. *Creación bajo demanda:* Generar el registro del asiento en base de datos únicamente al momento de la compra.
* **Justificación:**
    * *Tabla separada:* Permite consultar y actualizar el estado de asientos individuales sin colisionar con las actualizaciones del concierto.
    * *Modelado previo:* Crear los registros de los asientos en estado `AVAILABLE` permite realizar búsquedas y consultas de disponibilidad de forma limpia e indexada.
    * *Conditional Checks:* DynamoDB garantiza concurrencia segura mediante escrituras condicionales. Así se evita el *overbooking* o condiciones de carrera garantizando que la reserva solo sea exitosa si el asiento mantiene el estado permitido.

---

## 4. Parametrización en la Creación de Conciertos
* **Decisión de Diseño:** Recepción de un listado con especificaciones por fila (`row`, `seatCount`, `price`) para generar la distribución del concierto iterativamente.
* **Alternativas Consideradas:** Definir una matriz homogénea (e.g., cantidad global de filas por una cantidad fija de asientos por fila).
* **Justificación:** Los recintos reales tienen estructuras irregulares donde cada sección o fila varía en capacidad y nivel de precio. Recibir las especificaciones por fila brinda flexibilidad comercial y física en la configuración de las localidades.

---

## 5. Estructura de Código y Manejo de Excepciones
* **Decisión de Diseño:** Organización por capas (**Controller, Service, Repository**), patrón **Mapper** para el aislamiento entre entidades DTO/Dominio y **Global Handlers** dedicados.
* **Alternativas Consideradas:** Lógica de negocio acoplada en controladores o en mappers implícitos.
* **Justificación:** Mantiene una separación de responsabilidades clara y alta mantenibilidad. Adicionalmente, contar con manejadores de excepciones desacoplados (uno para GraphQL y otro para las APIs REST) permite devolver respuestas con los formatos estandarizados de error que exige cada especificación protocolar.

---

## 6. Manejo Asíncrono de Eventos tras la Reserva
* **Decisión de Diseño:** Desacoplamiento del evento de reserva exitosa mediante un patrón guiado por eventos (*Event-Driven*) para actualizar el estado del concierto y notificar al usuario.
* **Alternativas Consideradas:** Actualización síncrona inmediata en base de datos del concierto dentro de la misma transacción de reserva del asiento.
* **Justificación:** Asignar la responsabilidad de actualizar contadores o estados globales del concierto a la transacción directa de reserva incrementaría la latencia y el bloqueo concurrente. Emitir un evento permite:
    1. Mantener la transacción de compra liviana y rápida.
    2. Notificar al usuario por correo/push de forma asíncrona.
    3. Extender la arquitectura para que otros módulos (analítica, facturación) reaccionen al evento sin alterar la lógica central de reservas.