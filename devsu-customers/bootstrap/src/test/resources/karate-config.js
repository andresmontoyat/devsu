function fn() {
    karate.configure('connectTimeout', 10000);
    karate.configure('readTimeout', 10000);

    let context = karate.properties['server.servlet.context-path'] || '/devsu-accounts';

    let port = karate.properties['server.port'] || '8181';
    let protocol = 'http';
    if (karate.properties['server.protocol'] === 'true') {
        protocol = 'https';
        karate.configure('ssl', true);
    }

    const baseUrl = `${protocol}://localhost:${port}${context}`;
    return {
        baseUrl: baseUrl,
    };
}