function fn() {
    karate.configure('connectTimeout', 10000);
    karate.configure('readTimeout', 10000);

    let context = karate.properties['server.servlet.context-path'] || '/devsu-movements';

    let port = karate.properties['server.port'] || '8181';
    let protocol = 'http';
    if (karate.properties['server.protocol'] === 'true') {
        protocol = 'https';
        karate.configure('ssl', true);
    }

    const baseUrl = `${protocol}://localhost:${port}${context}`;

    let UUID = Java.type('java.util.UUID');
    function genTraceId() {
        return UUID.randomUUID().toString().replace(/-/g, '').toLowerCase();
    }

    karate.configure('headers', { 'trace-id': genTraceId() });
    return {
        baseUrl: baseUrl,
    };
}