package com.example.productoscrud.infrastructure.web.controllers;

import com.example.productoscrud.application.service.ProductService;
import com.example.productoscrud.domain.model.Product;
import com.example.productoscrud.infrastructure.persistence.mongo.ProductRepositoryMongo;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mongo/productos")
@CrossOrigin(origins = "*")
public class MongoProductController {
    private final ProductService service;
    private final ProductRepositoryMongo repository;

    public MongoProductController(@Qualifier("mongoProductService") ProductService service,
                                  ProductRepositoryMongo repository) {
        this.service = service;
        this.repository = repository;
    }

    @GetMapping public List<Product> listar() { return service.listar(); }
    @GetMapping("/{id}") public ResponseEntity<Product> buscar(@PathVariable int id) {
        return service.buscarPorId(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
    @PostMapping public ResponseEntity<?> crear(@RequestBody Product product) {
        return product.esValido() ? ResponseEntity.status(HttpStatus.CREATED).body(service.crear(product))
            : ResponseEntity.badRequest().body("Nombre, precio o descripción inválidos");
    }
    @PutMapping("/{id}") public ResponseEntity<?> actualizar(@PathVariable int id, @RequestBody Product product) {
        if (!product.esValido()) return ResponseEntity.badRequest().body("Nombre, precio o descripción inválidos");
        return service.actualizar(id, product).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
    @DeleteMapping("/{id}") public ResponseEntity<Void> eliminar(@PathVariable int id) {
        return service.eliminar(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/filtros/nombre") public Map<String, Object> porNombre(@RequestParam String texto) {
        return Map.of("pipeline", List.of(Map.of("$match", Map.of("name", Map.of("$regex", texto, "$options", "i"))), Map.of("$sort", Map.of("name", 1))), "resultados", repository.filtrarPorNombre(texto));
    }
    @GetMapping("/filtros/precio") public Map<String, Object> porPrecio(@RequestParam BigDecimal min, @RequestParam BigDecimal max) {
        return Map.of("pipeline", List.of(Map.of("$match", Map.of("price", Map.of("$gte", min, "$lte", max))), Map.of("$sort", Map.of("price", 1))), "resultados", repository.filtrarPorRangoPrecio(min, max));
    }
    @GetMapping("/filtros/descripcion") public Map<String, Object> porDescripcion(@RequestParam String texto) {
        return Map.of("pipeline", List.of(Map.of("$match", Map.of("description", Map.of("$regex", texto, "$options", "i"))), Map.of("$sort", Map.of("id", 1))), "resultados", repository.filtrarPorDescripcion(texto));
    }
    @GetMapping("/filtros/resumen-precios") public Map<String, Object> resumen() {
        return Map.of("pipeline", List.of(Map.of("$bucketAuto", Map.of("groupBy", "$price", "buckets", 3, "output", Map.of("cantidad", Map.of("$sum", 1), "precioPromedio", Map.of("$avg", "$price"))))), "resultados", repository.resumenPorRangoPrecio());
    }
}
