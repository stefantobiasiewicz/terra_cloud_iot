FROM amazoncorretto:18
EXPOSE 9000
ADD /target/cloud_iot-0.0.1.jar demo.jar
ENTRYPOINT ["java","-jar","demo.jar"]