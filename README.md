[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/9a_R28TL)
---

# ArtPond Backend

## CS2031 - Desarrollo Basado en Plataforma

**Integrantes:**

* Fabian Arana Espinoza
* Angel Mattos
* Natalia Ccusi

---

## Índice

1. [Introducción](#introducción)
2. [Identificación del Problema o Necesidad](#identificación-del-problema-o-necesidad)
3. [Descripción de la Solución](#descripción-de-la-solución)
4. [Modelo de Entidades](#modelo-de-entidades)
5. [Testing y Manejo de Errores](#testing-y-manejo-de-errores)
6. [Medidas de Seguridad Implementadas](#medidas-de-seguridad-implementadas)
7. [Eventos y Asincronía](#eventos-y-asincronía)
8. [GitHub & Management](#github--management)
9. [Conclusión](#conclusión)
10. [Apéndices](#apéndices)

---

## Introducción

### Contexto

Hoy en dia muchas plataformas de dibujo estan plagadas de IA y no son muy innovadoras en cuanto a que todas se terminan pareciendo mucho entre si. proponemos nuevas ideas como colocar un mapa que de acceso a distintas publicaciones cuando busques por un lugar en especifico. Asi como un sistema de deteccion de IA centrado en el apoyo de la comunidad facilitando los reportes de esta.

### Objetivos del Proyecto

El objetivo de **ArtPond** es ofrecer una API backend robusta, escalable y segura que permita gestionar publicaciones de usuarios con autenticación basada en JWT, soporte avanzado de etiquetas (tags), subida de imágenes y comentarios (el contenido del mapa sera prinicipalmente generado en el frontend).

---

## Identificación del Problema o Necesidad

### Descripción del Problema

Los artistas digitales carecen de un espacio seguro que les permita publicar sus obras con un control total sobre su contenido y una estructura eficiente para organizarlo mediante etiquetas.

### Justificación

Este proyecto busca crear un entorno donde cada usuario pueda publicar libremente su trabajo, mayor visibilidad con el sistema para hallar obrasa mediante el mapa y recibir retroalimentación, garantizando al mismo tiempo la seguridad y privacidad mediante autenticación moderna y control de acceso.

---

## Descripción de la Solución

### Funcionalidades Implementadas

* **Autenticación y Autorización JWT**

    * Registro e inicio de sesión de usuarios.
    * Generación y validación de tokens JWT.
    * Configuración de seguridad stateless mediante Spring Security.
* **Publicaciones**

    * Creación, visualización y listado de publicaciones.
    * Asociación de múltiples etiquetas y múltiples imágenes.
    * Soporte de paginación.
* **Etiquetas (Tags)**

    * Creación automática de etiquetas si no existen.
    * Relación muchos a muchos entre publicaciones y etiquetas.
* **Imágenes**

    * Asociación de múltiples imágenes a cada publicación.
* **Comentarios**

    * Sistema opcional para agregar comentarios a publicaciones.
    * Paginación para visualizar comentarios.
  
---

## Modelo de Entidades

### Diagrama de Entidades

*(image placeholder)*

### Descripción de Entidades

* **User**: Representa al usuario autenticado. Contiene credenciales y roles.
* **Publication**: Representa una publicación con título, descripción, imágenes y etiquetas.
* **Tag**: Representa una etiqueta asociada a una o más publicaciones.
* **Image**: Representa una imagen asociada a una publicación.
* **Comment**: Representa comentarios hechos por usuarios a publicaciones.

Relaciones principales:

* **User – Publication**: OneToMany
* **Publication – Tag**: ManyToMany
* **Publication – Image**: OneToMany
* **Publication – Comment**: OneToMany

---

## Testing y Manejo de Errores

### Niveles de Testing

Implementacion de postman para probar los endpoints.

### Resultados

Los tests verificaron correctamente la creación de publicaciones, la autenticación de usuarios y la recuperación de publicaciones por etiqueta.

### Manejo de Errores

El sistema implementa un `GlobalExceptionHandler` con @ControllerAdvice que gestiona:

* `ResourceNotFoundException`
* `UnauthorizedException`
* `DuplicateResourceException`
* `ValidationException`

---

## Medidas de Seguridad Implementadas

* **JWT (JSON Web Token):**

    * Generación de tokens en login.
    * Validación de expiración y firma.
    * Filtro personalizado `JwtAuthenticationFilter`.
* **Spring Security:**

    * Configuración stateless (`SecurityContext` sin sesiones).
    * Autorización basada en roles.
* **Prevención de Vulnerabilidades:**

    * Validaciones con `@Valid`, `@NotNull`, `@Email`, `@Size`.
    * Manejo de contraseñas con `BCryptPasswordEncoder`.

---

## Eventos y Asincronía

El proyecto utiliza eventos para desacoplar procesos internos (por ejemplo, notificaciones o acciones posteriores a un registro, expected: external map api response).

* **Eventos personalizados:** definidos en el paquete `authentication.event`.
* **Ejecución asíncrona:** manejada con `@Async` y `@EnableAsync`.

---

## GitHub & Management

* **Docker deployment:**
  Carga automatica de la imagen de Docker a Docker Hub, utilizando Github Actions y llaves secretas.

---

## Conclusión

### Logros del Proyecto

Se implementó un backend funcional, escalable y seguro, cumpliendo con las prácticas REST y las pautas de seguridad modernas.

### Aprendizajes Clave

* Integración de Spring Security y JWT.
* Diseño modular siguiendo el principio de responsabilidad única (SRP).
* Implementación de relaciones JPA y DTOs con mapeo limpio.

### Trabajo Futuro

* Implementacion del mapa con el frontend.
* Desarrollar un sistema de filtrado mas avanzado.

---

## Apéndices

### Licencia

### Referencias

* Documentación oficial de Spring Boot y Spring Security.
* PostgreSQL Documentation.

---
