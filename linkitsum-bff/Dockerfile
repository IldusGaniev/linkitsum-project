FROM linkitsum-deps

ENV GRADLE_USER_HOME=/home/gradle/cache
WORKDIR /app/linkitsum-bff
COPY src ./src
COPY build.gradle settings.gradle ./

# формируем jar файл из исходного кода
# печать логов в консоль тоже зани¢мает время, поэтому когда убедились что все ок - убираем параметр stacktrace
RUN gradle clean bootJar -i --stacktrace

# открытие порта для внешних вызовов и запуск проекта
EXPOSE 8902
ENTRYPOINT ["java", "-jar", "/app/linkitsum-bff/build/libs/linkitsum-bff-0.0.1-SNAPSHOT.jar"]