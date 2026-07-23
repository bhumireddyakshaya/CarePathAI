import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    vus: 100, // 100 Virtual Users
    duration: '1m', // 1 Minute continuous load test
    thresholds: {
        http_req_failed: ['rate<0.05'], // Request failure rate must be under 5%
        http_req_duration: ['p(95)<1500'], // 95% of requests must complete below 1500ms
    },
};

export default function () {
    const url = __ENV.BACKEND_URL || 'https://httpbin.org/get';
    const res = http.get(url);
    
    check(res, {
        'status is 200': (r) => r.status === 200,
        'response time < 1500ms': (r) => r.timings.duration < 1500,
    });

    sleep(0.5);
}
