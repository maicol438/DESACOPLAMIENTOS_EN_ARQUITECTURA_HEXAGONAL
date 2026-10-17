// Se ejecuta automáticamente solo la primera vez que se crea el volumen de Mongo.
// Mantiene la misma estructura funcional de Product usada por MySQL.
db = db.getSiblingDB('productoscrudmongo');

if (db.products.countDocuments() === 0) {
  db.products.insertMany([
    { id: 1, name: 'Portátil', price: NumberDecimal('2500000.00'), description: 'Equipo de ejemplo para programación' },
    { id: 2, name: 'Mouse', price: NumberDecimal('45000.00'), description: 'Mouse USB' },
    { id: 3, name: 'Teclado', price: NumberDecimal('85000.00'), description: 'Teclado para el laboratorio' },
    { id: 4, name: 'Monitor', price: NumberDecimal('780000.00'), description: 'Monitor IPS de 24 pulgadas' }
  ]);
}

db.products.createIndex({ id: 1 }, { unique: true });
db.products.createIndex({ name: 1 });
db.products.createIndex({ price: 1 });
db.counters.updateOne({ _id: 'product-id' }, { $setOnInsert: { sequence: 4 } }, { upsert: true });
