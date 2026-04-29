package com.lotto;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                // 외부 API 호출이 일어나지 않도록, 컨텍스트 로딩만 검증
                "spring.cache.type=none"
        }
)
class LottoApplicationTests {

    @Test
    void contextLoads() {
        // 빈 등록/구성이 정상인지만 확인
    }
}

