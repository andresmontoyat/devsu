function fn() {
  return {
    baseUrl: karate.properties['karate.baseUrl'] || 'http://localhost:8082/api/v1'
  };
}
