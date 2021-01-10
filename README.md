# Spring Framework 정리 자료
처음 배우는 스프링 부트2 Batch 부분 정리 문서

## Requirements
- [JDK 1.8+](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
- [gradle](https://gradle.org/)
- [MySQL](https://www.mysql.com/)

**MySQL 설치 이후 실행하고 application.yml에 맞게 설정해야하며 schema-mysql.sql로 먼저 Batch 관련 테이블을 만들어줘야함**

## Build
```
$ ./gradlew build
```

## Run
```
$ java -jar target/*.jar
```

## 책과 다른 점
- 시큐리티 설정관련 User 값 삭제
- 7.7 멀티 스레드 부분 정리 안함
- Tasklet 방식 정리 안함
