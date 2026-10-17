# Desacoplamiento en Arquitectura Hexagonal

Actividad individual: **el mismo CRUD de productos con dos persistencias
intercambiables** (MySQL/SQL y MongoDB/NoSQL), demostrando el desacoplamiento que
da la **arquitectura hexagonal**. El frontend React permite elegir la persistencia
desde un menú y, en el caso NoSQL, aplicar 4 filtros implementados con pipelines
de MongoDB (`aggregate`), imprimiendo el pipeline usado.

```
React (frontend)  ──HTTP──►  Controlador REST  ──►  ProductService  ──►  ProductRepository (puerto, clase abstracta)
                                                                             ├── ProductRepositoryMySQL  (adaptador)
                                                                             └── ProductRepositoryMongo  (adaptador)
```

## Contenido del repositorio

```
.
├── Spring_boot_crud/        Backend (Java 21 + Spring Boot 4)
│   ├── src/main/java/...    Código hexagonal (domain, application, infrastructure)
│   ├── src/main/resources/  application.properties
│   ├── dbs/                 Scripts de base de datos (MySQL y seed de Mongo)
│   ├── mvnw, mvnw.cmd       Maven Wrapper (no necesitas Maven instalado)
│   └── pom.xml
├── react_crud/              Frontend (React 19 + Vite)
│   ├── src/                 App.jsx, main.jsx, styles.css
│   ├── .env.example         Plantilla de variables de entorno
│   └── package.json
├── docker-compose.yml       MySQL + MongoDB listos con los seeds
└── README.md                Este manual
```

## 1. Requisitos previos

| Herramienta | Versión sugerida | Notas |
|-------------|------------------|-------|
| JDK         | 21 o superior    | El backend usa Java 21 |
| Node.js     | 20 o superior    | Incluye `npm` |
| MySQL       | 8.x              | Opcional si usas Docker |
| MongoDB     | 6.x o superior   | Opcional si usas Docker |
| Docker      | (opcional)       | Sirve para levantar ambas bases |

> Alternativa rápida: si tienes Docker, el paso 2 con `docker compose up -d`
> levanta MySQL y MongoDB ya con datos de ejemplo y te ahorras instalar las bases.

## 2. Inicializar las bases de datos

### Opción A - Docker (recomendada)

Desde la raíz del repositorio:

```powershell
docker compose up -d
```

Esto crea:
- **MySQL** en `localhost:3306` (usuario `root`, contraseña `1234`) con la base `productoscruddb`.
- **MongoDB** en `localhost:27017` con la base `productoscrudmongo`.

Los scripts de `Spring_boot_crud/dbs/` se ejecutan automáticamente la primera vez.

### Opción B - Instalaciones locales

1. **MySQL**: ejecuta el script `Spring_boot_crud/dbs/productoscruddb.sql`
   (crea la base `productoscruddb`, la tabla `products` y datos de ejemplo).
2. **MongoDB**: ejecuta `Spring_boot_crud/dbs/mongo-init.js`
   (crea `productoscrudmongo`, la colección `products`, índices y datos de ejemplo).

## 3. Inicializar dependencias y ejecutar el backend

```powershell
cd Spring_boot_crud
.\mvnw.cmd spring-boot:run      # Windows
# ./mvnw spring-boot:run        # Linux / macOS
```

La primera vez, el Maven Wrapper descarga Maven y las dependencias en `~/.m2`
(no se suben al repositorio). La API queda en `http://127.0.0.1:8081`.

## 4. Inicializar dependencias y ejecutar el frontend

En otra terminal:

```powershell
cd react_crud
npm ci            # instala las dependencias exactas del package-lock.json
npm run dev
```

Abre [http://127.0.0.1:5175/](http://127.0.0.1:5175/).

> `node_modules/` **no** está en el repositorio: se regenera con `npm ci`.
> `target/` del backend tampoco: se regenera al compilar con Maven.

## 5. Uso de la aplicación

En la pantalla hay un menú con dos opciones:

- **SQL / MySQL** → CRUD sobre `http://127.0.0.1:8081/api/sql/productos`.
- **NoSQL / MongoDB** → CRUD sobre `http://127.0.0.1:8081/api/mongo/productos`
  y un panel de **filtros con `aggregate`** que muestra los resultados y el
  pipeline de Mongo utilizado.

## 6. Endpoints REST

| Método | SQL / MySQL                    | NoSQL / MongoDB                 |
|--------|--------------------------------|---------------------------------|
| GET    | `/api/sql/productos`           | `/api/mongo/productos`          |
| GET    | `/api/sql/productos/{id}`      | `/api/mongo/productos/{id}`     |
| POST   | `/api/sql/productos`           | `/api/mongo/productos`          |
| PUT    | `/api/sql/productos/{id}`      | `/api/mongo/productos/{id}`     |
| DELETE | `/api/sql/productos/{id}`      | `/api/mongo/productos/{id}`     |

Cuerpo JSON para crear/actualizar:

```json
{ "name": "Portátil", "price": 2500000.00, "description": "Equipo de ejemplo" }
```

### Filtros NoSQL (pipelines `aggregate`)

Cada respuesta incluye `pipeline` (el pipeline usado) y `resultados`. Están en
`MongoProductController` / `ProductRepositoryMongo`.

| Método | Ruta                                              | Pipeline destacado |
|--------|---------------------------------------------------|--------------------|
| GET    | `/api/mongo/productos/filtros/nombre?texto=`      | `$match` (regex) + `$sort` |
| GET    | `/api/mongo/productos/filtros/precio?min=&max=`   | `$match` (`$gte`/`$lte`) + `$sort` |
| GET    | `/api/mongo/productos/filtros/descripcion?texto=` | `$match` (regex) + `$sort` |
| GET    | `/api/mongo/productos/filtros/resumen-precios`    | `$bucketAuto` + `$sum` + `$avg` |

## 7. Variables de entorno

No hay secretos en el repositorio. El backend usa valores por defecto para
desarrollo local y los puedes sobrescribir con variables de entorno:

| Variable | Descripción | Valor por defecto |
|----------|-------------|-------------------|
| `DB_HOST` | Host de MySQL | `localhost` |
| `DB_PORT` | Puerto de MySQL | `3306` |
| `DB_NAME` | Base de datos | `productoscruddb` |
| `DB_USER` | Usuario de MySQL | `root` |
| `DB_PASSWORD` | Contraseña de MySQL | `1234` |
| `MONGODB_URI` | Cadena de conexión de MongoDB (local o Atlas) | `mongodb://localhost:27017/productoscrudmongo` |

Frontend: copia `react_crud/.env.example` a `react_crud/.env.local` y ajusta
`VITE_API_URL` si tu backend corre en otra dirección. Los archivos `.env.local`
están ignorados por Git.

## 8. Notas técnicas

- **Spring Boot 4** renombró la propiedad de MongoDB: usa `spring.mongodb.uri`
  (antes `spring.data.mongodb.uri`).
- En MongoDB, el documento guarda un campo `id` propio (no se usa el `_id`
  nativo), por eso el modelo `Product` anota su id con `@Field("id")`.
- El campo de precio se guarda como `Decimal128` en Mongo y como `DECIMAL(12,2)`
  en MySQL, reflejando `BigDecimal` en el dominio.
- La persistencia NoSQL se agregó **extendiendo la clase abstracta**
  `ProductRepository` (el puerto), sin crear otra fábrica abstracta.

## 9. Arquitectura hexagonal (dónde está cada cosa)

```
domain.model.Product                    Modelo de dominio (independiente de la infraestructura)
application.ports.ProductRepository     Puerto: clase abstracta con el contrato CRUD
application.service.ProductService      Caso de uso: orquesta el puerto
infrastructure.persistence.mysql.*      Adaptador SQL (JdbcTemplate)
infrastructure.persistence.mongo.*      Adaptador NoSQL (MongoTemplate + aggregate)
infrastructure.web.controllers.*        Adaptadores de entrada (REST)
app.App                                 Arranque de Spring y definición de beans
```
