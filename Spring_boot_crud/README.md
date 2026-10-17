# Spring Boot CRUD - Productos (Arquitectura Hexagonal)

Backend Spring Boot que expone el mismo contrato CRUD de productos sobre dos
persistencias intercambiables gracias a la arquitectura hexagonal: **MySQL (SQL)**
y **MongoDB (NoSQL)**. La persistencia NoSQL se agrega extendiendo la clase
abstracta `ProductRepository` (el puerto), sin crear otra fabrica abstracta.

## Endpoints

| Metodo | SQL (MySQL)                       | NoSQL (MongoDB)                     |
|--------|-----------------------------------|-------------------------------------|
| GET    | `/api/sql/productos`              | `/api/mongo/productos`             |
| GET    | `/api/sql/productos/{id}`         | `/api/mongo/productos/{id}`        |
| POST   | `/api/sql/productos`              | `/api/mongo/productos`             |
| PUT    | `/api/sql/productos/{id}`         | `/api/mongo/productos/{id}`        |
| DELETE | `/api/sql/productos/{id}`         | `/api/mongo/productos/{id}`        |

### Filtros NoSQL con `aggregate`

Cada endpoint devuelve `pipeline` (el pipeline de Mongo usado) y `resultados`.

| Metodo | Ruta                                          | Pipeline destacado           |
|--------|-----------------------------------------------|------------------------------|
| GET    | `/api/mongo/productos/filtros/nombre?texto=`  | `$match` (regex) + `$sort`   |
| GET    | `/api/mongo/productos/filtros/precio?min=&max=` | `$match` (`$gte`/`$lte`) + `$sort` |
| GET    | `/api/mongo/productos/filtros/descripcion?texto=` | `$match` (regex) + `$sort` |
| GET    | `/api/mongo/productos/filtros/resumen-precios` | `$bucketAuto` + `$sum` + `$avg` |

Ejemplo de cuerpo JSON para crear/actualizar:

```json
{ "name": "Portatil", "price": 2500000.00, "description": "Equipo de ejemplo" }
```

## Base de datos

- **MySQL**: ejecute `dbs/productoscruddb.sql` (crea la base `productoscruddb` y la tabla `products`).
- **MongoDB**: la coleccion `products` usa un campo `id` propio (no `_id`). El seed esta en `dbs/mongo-init.js`.

### Opcion Docker

Desde la raiz del repositorio:

```powershell
docker compose up -d
```

Levanta MySQL en `3306` (root/1234) y MongoDB en `27017` con los seeds ya montados.

## Ejecutar

```powershell
cd Spring_boot_crud
.\mvnw.cmd spring-boot:run
```

La API queda en `http://127.0.0.1:8081`.

### Variables de entorno (opcionales)

- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`
- `MONGODB_URI` (por ejemplo una cadena de MongoDB Atlas)

> Nota: en Spring Boot 4 la propiedad de conexion a Mongo es `spring.mongodb.uri`
> (antes `spring.data.mongodb.uri`).

## Estructura hexagonal

```
domain.model.Product                     -> modelo de dominio (compartido)
application.ports.ProductRepository      -> puerto (clase abstracta con el contrato CRUD)
application.service.ProductService       -> caso de uso
infrastructure.persistence.mysql.*       -> adaptador MySQL (JdbcTemplate)
infrastructure.persistence.mongo.*       -> adaptador MongoDB (MongoTemplate + aggregate)
infrastructure.web.controllers.*         -> controladores REST
app.App                                  -> arranque y beans
```
