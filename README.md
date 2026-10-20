<p align="center">
  <img src="images/logo.png" alt="Logo de Jade Barbershop" width="500">
</p>

<h1 align="center">Jade Barbershop</h1>

<p align="center">
  Sistema de escritorio para la gestión de citas de una barbería.
</p>

<p align="center">
  <img alt="Java" src="https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white">
  <img alt="Java Swing" src="https://img.shields.io/badge/Java%20Swing-007396?style=for-the-badge&logo=java&logoColor=white">
  <img alt="Estado" src="https://img.shields.io/badge/Estado-En%20desarrollo-yellow?style=for-the-badge">
</p>

---

## Descripción

**Jade Barbershop** es una aplicación de escritorio desarrollada en **Java** con interfaz gráfica **Swing**. Su objetivo es centralizar la operación diaria de una barbería: administrar clientes, agendar citas, gestionar tipos de servicio, controlar la lista de espera y configurar el horario de atención.

## Funcionalidades principales

- **Gestión de clientes:** registro, consulta, actualización y eliminación.
- **Gestión de citas:** creación, reprogramación y cancelación, con calendario de citas.
- **Tipos de servicio:** catálogo de servicios ofrecidos por la barbería.
- **Lista de espera:** administración de clientes en espera de un espacio disponible.
- **Notificaciones:** envío de correos de confirmación de citas.
- **Horario de atención:** configuración de los días y horas en que se atiende.
- **Persistencia:** almacenamiento y recuperación de los datos en archivos.

## Tecnologías

| Tecnología | Uso |
| --- | --- |
| Java | Lenguaje de programación |
| Java Swing | Interfaz gráfica de escritorio |
| Programación Orientada a Objetos | Diseño y organización del código |
| Archivos en disco | Persistencia de la información |
| Eclipse | Entorno de desarrollo del proyecto |

## Requisitos

- **JDK 17** o superior (el proyecto usa `module-info.java`, por lo que requiere Java 9+).
- **Eclipse IDE for Java Developers** (recomendado, el proyecto incluye `.project` y `.classpath`).
- Credenciales SMTP si se desea probar el envío de notificaciones por correo.

## Como ejecutar

### Desde Eclipse

1. Abrir Eclipse y seleccionar **File → Import…**
2. Elegir **General → Existing Projects into Workspace**.
3. Seleccionar la carpeta raíz del repositorio y confirmar con **Finish**.
4. Ejecutar la clase principal con **Run → Run As → Java Application**.

### Desde la línea de comandos

Compilar todas las fuentes:

```bash
javac -d bin $(find src -name "*.java")
```

Ejecutar la aplicación (una vez implementado el punto de entrada):

```bash
java -cp bin interfaz.Interfaz
```

> **Nota:** esta primera versión del repositorio contiene el esqueleto del proyecto (paquetes y clases base), por lo que todavía no existe un método `main` ejecutable.

## Diagrama UML

El diseño de clases del sistema se encuentra en [`UML_Barberia_v3.html`](UML_Barberia_v3.html). Para visualizarlo, abrir el archivo en cualquier navegador web.

## Equipo

| Integrante |
| --- |
| Julián Solórzano |
| Andrey Morales |
| Alexei Quesada |
| Maricruz Vásquez |

## Estado del proyecto

🚧 **En desarrollo.** Actualmente el repositorio cuenta con la estructura base del proyecto (modelo, control e interfaz) y el diagrama UML. Las funcionalidades descritas se irán incorporando en las siguientes iteraciones.
