package com.example.productoscrud.infrastructure.persistence.mongo;

import com.example.productoscrud.application.ports.ProductRepository;
import com.example.productoscrud.domain.model.Product;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import org.bson.Document;

/** Adaptador secundario: implementa el mismo puerto CRUD con MongoDB. */
@Repository("productRepositoryMongo")
public class ProductRepositoryMongo extends ProductRepository {
    private static final String COLLECTION = "products";
    private final MongoTemplate mongoTemplate;

    public ProductRepositoryMongo(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public List<Product> listar() {
        return mongoTemplate.find(new Query().with(Sort.by("id")), Product.class, COLLECTION);
    }

    @Override
    public Optional<Product> buscarPorId(int id) {
        return Optional.ofNullable(mongoTemplate.findOne(new Query(Criteria.where("id").is(id)), Product.class, COLLECTION));
    }

    @Override
    public Product crear(Product product) {
        Product productToSave = new Product(nextId(), product.getName(), product.getPrice(), product.getDescription());
        return mongoTemplate.insert(productToSave, COLLECTION);
    }

    @Override
    public boolean actualizar(int id, Product product) {
        Update update = new Update().set("name", product.getName()).set("price", product.getPrice())
            .set("description", product.getDescription());
        return mongoTemplate.updateFirst(new Query(Criteria.where("id").is(id)), update, COLLECTION).getMatchedCount() > 0;
    }

    @Override
    public boolean eliminar(int id) {
        return mongoTemplate.remove(new Query(Criteria.where("id").is(id)), COLLECTION).getDeletedCount() > 0;
    }

    // Los cuatro listados siguientes se ejecutan mediante pipelines de MongoDB aggregate.
    public List<Product> filtrarPorNombre(String text) {
        return aggregateProducts(Aggregation.newAggregation(
            Aggregation.match(Criteria.where("name").regex(text, "i")), Aggregation.sort(Sort.by("name"))));
    }

    public List<Product> filtrarPorRangoPrecio(BigDecimal min, BigDecimal max) {
        return aggregateProducts(Aggregation.newAggregation(
            Aggregation.match(Criteria.where("price").gte(min).lte(max)), Aggregation.sort(Sort.by("price"))));
    }

    public List<Product> filtrarPorDescripcion(String text) {
        return aggregateProducts(Aggregation.newAggregation(
            Aggregation.match(Criteria.where("description").regex(text, "i")), Aggregation.sort(Sort.by("id"))));
    }

    public List<Map> resumenPorRangoPrecio() {
        Aggregation pipeline = Aggregation.newAggregation(
            Aggregation.bucketAuto("price", 3).andOutputCount().as("cantidad")
                .andOutput("price").avg().as("precioPromedio"));
        return mongoTemplate.aggregate(pipeline, COLLECTION, Map.class).getMappedResults();
    }

    private List<Product> aggregateProducts(Aggregation pipeline) {
        AggregationResults<Product> result = mongoTemplate.aggregate(pipeline, COLLECTION, Product.class);
        return result.getMappedResults();
    }

    private int nextId() {
        Document counter = mongoTemplate.findAndModify(
            new Query(Criteria.where("_id").is("product-id")),
            new Update().inc("sequence", 1),
            FindAndModifyOptions.options().upsert(true).returnNew(true),
            Document.class,
            "counters"
        );
        return counter.getInteger("sequence");
    }
}
