# OpenJDK 17을 기반 이미지로 설정
FROM openjdk:17-jdk
# 빌드된 JAR 파일 경로 정의
ARG JAR_FILE=build/libs/diary-0.0.1-SNAPSHOT.jar
# JAR 파일을 컨테이너 내부로 복사
COPY ${JAR_FILE} /app.jar
# 도커 컨테이너를 시작할 때 실행할 명령어
ENTRYPOINT ["java","-jar","/app.jar"]