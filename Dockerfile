# 指定基础镜像
FROM openjdk:11-jre-slim

# 拷贝jdk和java项目的包
COPY ./target/gateway-1.0-SNAPSHOT.jar /gateway/gateway.jar

# 暴露端口
EXPOSE 10010
# 入口，java项目的启动命令
ENTRYPOINT java -jar -Xms128m -Xms128m /gateway/gateway.jar --spring.profiles.active=pro
