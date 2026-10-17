# React CRUD de productos (SQL + NoSQL)

Frontend en React (Vite) que consume el backend Spring Boot y permite gestionar
el CRUD de productos con **dos persistencias** elegibles desde un menu:
**SQL / MySQL** y **NoSQL / MongoDB**.

## Ejecutar

Primero inicia el backend `Spring_boot_crud` en el puerto `8081`. Luego, desde esta carpeta:

```powershell
npm ci
npm run dev
```

Abre [http://127.0.0.1:5175/](http://127.0.0.1:5175/).

## Que ofrece la pantalla

- Menu para alternar entre el CRUD SQL (`/api/sql/productos`) y el CRUD NoSQL (`/api/mongo/productos`).
- Listar, crear, editar y eliminar productos en la persistencia seleccionada.
- En la pestana NoSQL, un panel de **filtros con `aggregate`**: nombre, descripcion,
  rango de precio y resumen automatico. Al aplicar un filtro se muestran los resultados
  y se imprime el **pipeline de Mongo** utilizado.

Si cambias la direccion del backend, copia `.env.example` como `.env.local`, modifica
`VITE_API_URL` y reinicia Vite.

## Compilar

```powershell
npm run build
```

La salida queda en `dist/`.
