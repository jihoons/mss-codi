## 스타일 코디 서비스
### 주요 기능
* 각 카테고리별 최저가/최고가 브랜드 정보 조회
* 모든 카테고리를 한 브랜드로 스타일링 할 경우 최저가/최고가 브랜드 정보 조회
* 특정 카테고리 최저가/최고가 브랜드 목록 조회
* 브랜드 추가 및 상품 추가/수정/삭제

### 전제 조건
* 전체 서비스는 MSA 형식을 따름
  * 상품 서비스
    * 상품, 브랜드 변경은 상품 관리 서비스에서 수행
    * 상품 추가/변경/삭제가 발생하면 Event 발행
  * 코디 서비스
    * 상품 추가/변경/삭제 이벤트 Consume 하여 코디를 위한 별도 상품 정보 보관
    * 사용자에게 코디 정보 조회
* 카테고리, 브랜드 이름은 중복될 수 없음

### 전제 조건 중 임의로 변경한 사항 
* 상품 서비스 미구현
  * 코디 서비스에서 상품 변경 API 구현으로 대체
* Event Publishing/Consumer 내부 Queue 사용
  * Kafka/Kinesis 와 같은 외부 MQ를 사용하지 않고 내부 Queue 를 사용
  * Event Publishing 도 임의로 JPA event 로 처리
  * 한 Process 에서 동작하지만 분리되어 동작되도록 구현
* Cache Hit Rate 가 높을 것으로 예상되어 Cache를 적용하였으나 

### 요구 사항 정의
* 각 카테고리별 최저가/최고가 브랜드 정보 조회
  * 입력값에 최저가인지 최고가인지 Parameter 전송  
  * 각 카테고리별로 최저가/최고가 상품을 가지고 있는 브랜드와 가격 조회
  * 만약 같은 가격의 브랜드가 있을 경우에는 최근에 등록된 상품 기준으로 출력
  * "Frontend에서 값 변경없이 아래와 같은 화면을 출력하기 위한 JSON"
    * HTML을 요청하는 것이라고 판단하지 않음
      * 여러가지 스타이링이나 Web 이외에 Front에서 제한등이 있을 것으로 판단
    * JSON 값을 가공하지 않고 단순 Mapping으로 화면을 출력한다로 판단
      * 가격 숫자 Formatting도 백엔드 처리 
* 모든 카테고리를 한 브랜드로 스타일링 할 경우 최저가/최고가 브랜드 정보 조회
  * 입력값에 최저가인지 최고가인지 Parameter 전송
  * 브랜드별 모든 카테고리 최저가/최고가 상품 가격 합계를 구하고 그중 최저가/최고가 브랜드 조회
  * 단 모든 카테고리에 상품이 존재하지 않으면 해당 브랜드 제외
* 특정 카테고리 최저가/최고가 브랜드 목록 조회
  * 입력값으로 조회할 카테고리 지정
  * 입력한 카테고리중 최저가 브랜드와 최고가 브랜드의 가격 조회
  * 같은 가격이 존재할 수 있으므로 브랜드는 Array로 전달
* 브랜드 추가
  * 브랜드명을 입력 받아 브랜드 추가
* 상품 추가/변경/삭제
  * 카테고리, 브랜드, 가격을 입력받아 상품 추가
  * 상품ID와 카테고리, 브랜드, 가격을 입력받아서 상품 정보 갱신
  * 상품ID로 상품 삭제
    * 실제 DELETE 하지 않고 상태값 변경 처리

### 설계
* Data 
  * 카테고리, 브랜드, 상품 기본 정보 저장
  * 요약 정보 벼로 저장
    * 브랜드나 상품수가 적지 않아서 매번 상품 정보에서 조회하는 것은 비효율적으로 판단
    * 각 카테고리별 최저가/최고가 상품 정보 저장
    
     | 컬럼         | 설명         |
     |------------|------------|
     | category   | 카테고리 정보    |
     | price_type | 최저가/최고가 구분 |
     | product    | 상품 정보      |
     | price      | 상품 가격      | 
     
    * 각 브랜드/카테고리별 최저가/최고가 상품 정보 저장

    | 컬럼         | 설명         |
    |------------|------------|
    | category   | 카테고리 정보    |
    | brand      | 브랜드 정보     |
    | price_type | 최저가/최고가 구분 |
    | product    | 상품 정보      |
    | price      | 상품 가격      |

    * 최저가/최고가 상품을 보여줘야 될 수 있기 때문에 브랜드가 아닌 상품 정보 저장
    * price_type은 총 4가지로 구분
      * min : 최저가
      * max : 최고가
      * latest_min : 최저가중 최근에 변경된 상품
      * latest_max : 최고가중 최근에 변경된 상품

* API
  * 각 카테고리별 최저가/최고가 브랜드 정보 조회
    * path: /codi/all
    * method: GET
    * 요청 parameters
      * Query String 으로 전달 
      * priceType: 최저가/최고가 구분
        * 최저가: min (default value)
        * 최고가: max
    * 응답 Json
      
    | 필드명  | Data Type       | 설명                     |
    |------|-----------------|------------------------|
    | 카테고리 | BrandOfCategory | 카테고리별 최저가/최고가 브랜드 및 가격 |
    | 총액   | String          | 총액                     |
    * BrandOfCategory

    | 필드명  | Data Type | 설명   |
    |------|-----------|------|
    | 카테고리 | String    | 카테고리 |
    | 브랜드  | String    | 브랜드  |
    | 가격   | String    | 가격   |
  
    * 요청 예제 (카테고리별 최고가 브랜드 조회)
      * ```http://localhost:8081/codi/all```
    * 응답 예제 (카테고리별 최고가 브랜드 조회 응답)
    ```json
    {
        "카테고리별상품": [
            {
                "카테고리": "상의",
                "브랜드": "I",
                "가격": "11,400"
            },
            {
                "카테고리": "아우터",
                "브랜드": "F",
                "가격": "7,200"
            },
            {
                "카테고리": "바지",
                "브랜드": "A",
                "가격": "4,200"
            },
            {
                "카테고리": "스니커즈",
                "브랜드": "E",
                "가격": "9,900"
            },
            {
                "카테고리": "가방",
                "브랜드": "D",
                "가격": "2,500"
            },
            {
                "카테고리": "모자",
                "브랜드": "B",
                "가격": "2,000"
            },
            {
                "카테고리": "양말",
                "브랜드": "D",
                "가격": "2,400"
            },
            {
                "카테고리": "액세서리",
                "브랜드": "I",
                "가격": "2,400"
            }
        ],
        "총액": "42,000"
    }
    ```
  
  * 모든 카테고리를 한 브랜드로 스타일링 할 경우 최저가/최고가 브랜드 정보 조회
    * path: /codi/brand
    * method: GET
    * 요청 parameters
        * Query String 으로 전달
        * priceType: 최저가/최고가 구분
            * 최저가: min (default value)
            * 최고가: max
    * 응답 Json 

    | 필드명 | Data Type | 설명                              |
    |-----|-----------|---------------------------------|
    | 최저가 | Brand     | 최저가브랜드 카테고리별 가격, 요청이 최저가인 경우 사용 |
    | 최고가 | Brand     | 최고가브랜드 카테고리별 가격, 요청이 최고가인 경우 사용 |
      * Brand

    | 필드명  | Data Type     | 설명          |
    |------|---------------|-------------|
    | 브랜드  | String        | 브랜드         |
    | 카테고리 | Array<String> | 카테고리별 가격 목록 |
    | 총액   | String        | 가격          |
      * PriceOfCategory

    | 필드명  | Data Type | 설명   |
    |------|-----------|------|
    | 카테고리 | String    | 카테고리 |
    | 가격   | String    | 가격   |
    
    * 요청 예제 (한브랜드로 코디했을 때 최저가 정보)
        * ```http://localhost:8081/codi/brand```
    * 응답 예제 (한브랜드로 코디했을 때 최저가 정보)
    ```json
    {
        "최저가": {
            "브랜드": "D",
            "카테고리": [
                {
                    "카테고리": "상의",
                    "가격": "10,100"
                },
                {
                    "카테고리": "아우터",
                    "가격": "5,100"
                },
                {
                    "카테고리": "바지",
                    "가격": "3,000"
                },
                {
                    "카테고리": "스니커즈",
                    "가격": "9,500"
                },
                {
                    "카테고리": "가방",
                    "가격": "2,500"
                },
                {
                    "카테고리": "모자",
                    "가격": "1,500"
                },
                {
                    "카테고리": "양말",
                    "가격": "2,400"
                },
                {
                    "카테고리": "액세서리",
                    "가격": "2,000"
                }
            ],
            "총액": "36,100"
        }
    }
    ```
    * 특정 카테고리 최저가/최고가 브랜드 목록 조회
        * path: /codi/category
        * method: GET
        * 요청 parameters
            * Query String 으로 전달
            * category: 조회할 카테고리명
        * 응답 Json 

      | 필드명  | Data Type    | 설명        |
      |------|--------------|-----------|
      | 카테고리 | String       | 카테고리      |
      | 최저가  | Array<Brand> | 최저가브랜드 목록 |
      | 최고가  | Array<Brand> | 최고가브랜드 목록 |
        * Brand

      | 필드명 | Data Type | 설명  |
      |-----|-----------|-----|
      | 브랜드 | String    | 브랜드 |
      | 가격  | String    | 가격  |
        * 요청 예제 (스니커즈 카테고리 최저가, 최고가 브랜드 조회)
            * ```http://localhost:8081/codi/category?category=스니커즈```
        * 응답 예제 (스니커즈 카테고리 최저가, 최고가 브랜드 조회)
      ```json
      {
        "카테고리": "스니커즈",
        "최저가": [
            {
                "브랜드": "G",
                "가격": "9,000"
            },
            {
                "브랜드": "A",
                "가격": "9,000"
            }
        ],
        "최고가": [
            {
                "브랜드": "E",
                "가격": "9,900"
            }
        ]
      }
      ```
  * 브랜드 추가
      * path: /brand
      * method: POST
      * 요청 parameters
          * Body에 Json 으로 전달 (Content-Type: application/json)

          | 필드명  | Data Type | 설명   |
          |------|-----------|------|
          | name | String    | 브랜드명 |
      * 응답 Json

          | 필드명  | Data Type | 설명     |
          |------|-----------|--------|
          | id   | long      | 브랜드 ID |
          | name | String    | 브랜드명   |
      * 요청 예제
        * POST /brand
    ```json
        {
            "name": "Z",
            "price": 200000
        }
    ```  
    * 응답 예제     
    ```json
        {
            "name": "Z",
            "id": 10
        }
    ```  
  * 상품 추가
      * path: /product
      * method: POST
      * 요청 parameters
          * Body에 Json 으로 전달 (Content-Type: application/json)

        | 필드명      | Data Type | 설명   |
        |----------|-----------|------|
        | category | String    | 카테고리 |
        | brand    | String    | 브랜드  |
        | price    | long      | 가격   |
      * 응답 Json

        | 필드명  | Data Type | 설명    |
        |----------|-----------|-------|
        | id   | long      | 상품 ID |
        | category | String    | 카테고리  |
        | brand    | String    | 브랜드   |
        | price    | long      | 가격    |
    * 요청 예제
      * POST /product
    ```json
        {
            "brand": "A",
            "category": "상의",
            "price": 200000
        }
    ```  
      * 응답 예제
    ```json
        {
            "brand": "A",
            "category": "상의",
            "price": 200000,
            "id": 74
        }
    ```  

  * 상품 변경
      * path: /product
      * method: PUT
      * 요청 parameters
          * Body에 Json 으로 전달 (Content-Type: application/json)

        | 필드명      | Data Type | 설명    |
        |----------|-----------|-------|
        | id       | long      | 상품 ID |
        | category | String    | 카테고리  |
        | brand    | String    | 브랜드   |
        | price    | long      | 가격    |
      * 응답 Json

        | 필드명      | Data Type | 설명    |
        |----------|-----------|-------|
        | id       | long      | 상품 ID |
        | category | String    | 카테고리  |
        | brand    | String    | 브랜드   |
        | price    | long      | 가격    |
      * 요청 예제
        * PUT /product
    ```json
        {
            "id": 74,
            "brand": "A",
            "category": "상의",
            "price": 20000
        }
    ```  
      * 응답 예제
    ```json
        {
            "brand": "A",
            "category": "상의",
            "price": 20000,
            "id": 74
        }
    ```  

  * 상품 삭제
      * path: /product/{id}
      * method: DELETE
      * 요청 parameters
          * path에 상품 ID 추가
      * 응답 Json (삭제된 상품 정보)

        | 필드명      | Data Type | 설명    |
                |----------|-----------|-------|
        | id       | long      | 상품 ID |
        | category | String    | 카테고리  |
        | brand    | String    | 브랜드   |
        | price    | long      | 가격    |
      * 요청 예제
        * DELETE /product/74
      * 응답 예제
    ```json
        {
            "brand": "A",
            "category": "상의",
            "price": 20000,
            "id": 74
        }
    ```  

### 주요 구현 방법 
  * Spring Boot 사용
    * MVC 사용
      * 요건상 webflux보다 더 적합하다 판단
  * 상품 데이터 관리
    * H2 사용
    * Spring Data JPA 및 Hibernate 사용
    * QueryDSL 사용
    * 개발 및 테스트일 경우 기본 데이터를 입력하도록 구현
      * com.mss.codi.config.DevConfiguration 에서 설정
      * dev, test profile에서만 Bean으로 동작
      * json 파일에 기본 샘플 데이터 설정
      * [기본 데이터](src/main/resources/sample/sample.json)
  * Codi 관련 API Cache 적용
    * API 특성상 Cache Hit Rate가 높을 것으로 예상되어 Cache 적용
    * 실제 작업시에는 Redis나 Hazelcast 와 같은 Storage를 적용할 수 있지만 여기서는 Heap 에 저장
      * com.mss.codi.service.CodiCacheService
    * 상품 정보가 변경되면 Cache 정보 갱신하도록 구현
  * 상품 추가/변경/삭제시 이벤트 처리
    * JPA event Listener (com.mss.codi.config.ProductChangeListener) 를 이용
    * Event가 발생하면 상품 ID와 추가/변경/삭제를 정보를 Message Broker에 전달
      * com.mss.codi.service.event.ProductChangeEventBroker
      * 단순 Queue 추가
    * Event Consume
      * Consumer(com.mss.codi.service.event.ProductChangeEventConsumer) 가 Broker에 접근 Message 요청
      * 요청이 있으면 com.mss.codi.service.event.ProductChangeEventHandler Interface를 구현한 Bean 호출
      * 상품 삭제와 추가/변경에 따라 카테고리별 최저가/최고가 상품, 브랜드/카테고리별 최저가/최고가 상품 저장
      * 상품 변경이 동시에 발생할 경우 최저가/최고가 집계 문제가 될 수 있어서 카테고리별 Lock 사용
        * 현재 구현은 하나의 Thread에서 Consume 하게 되어 있어서 문제 없음 
        * 실제 서비스에서는 Consumer가 여러개 일 수 있어서 동시성 문제가 발생 가능
        * com.mss.codi.service.event.ProductGlobalLockService 에서 Lock 관리
          * Redis나 Hazelcast 등을 이용하여 Lock 관리하도록 확장 가능
        * Kafka 등의 Queue 사용시 Category 단위로 Partitioning 하면 상대적으로 Lock의 필요성이 줄일 수 있음
      * 최저가/최고가 상품 변경이 발생되면 Cache 내역 수정

### 개발 환경
  * Java 21 version
  * Spring Boot 3.4
    * Spring mvc 로 개발
  * Java 언어
    * 최근에 kotlin보다 Java를 더 많이 사용해서 java로 사용
  * H2 DB
    * 개발 및 테스트용도로 사용
  * gradle

### 설정
* 기본적으로 Spring Boot를 사용해서 Server 설정등은 Spring Boot Configuration 사용
* Application 추가 설정

| 설정 이름                     | Data type | 기본값 | 설명                                        |
|---------------------------|-----------|-----|-------------------------------------------|
| codi.category.brand.count | long      | 5   | 카테고리별 최저가/최고가 조회시 보여 줄 최대 건수              |
| codi.brand.product.count  | long      | 5   | 한브랜드당 최대 노출할 상품 수                         | 

### 테스트, 실행, 빌드
* 테스트
  * ```./gradlew test``` 
* 실행
  * 기본 port 8081 
  * ```./gradlew bootRun```  
* 빌드 후 실행
  * ```./gradlew bootJar```
  * builds/libs/mss-codi-api-{VERSION}.jar 파일 생성
  * ```java -jar builds/libs/mss-codi-api-{VERSION}.jar```

