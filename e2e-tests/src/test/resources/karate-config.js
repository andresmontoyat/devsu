function fn() {
  // The projection in cuenta-service is fed by Kafka: wait for it instead of sleeping.
  karate.configure('retry', { count: 30, interval: 1000 });
  return {
    clienteUrl: karate.properties['e2e.clienteUrl'] || 'http://localhost:8081/api/v1',
    cuentaUrl: karate.properties['e2e.cuentaUrl'] || 'http://localhost:8082/api/v1'
  };
}
