fuser -k 10501/tcp || true

DATABASE_USERNAME=$(<production-portfolio/username.txt)
DATABASE_PASSWORD=$(<production-portfolio/password.txt)

java -jar production-portfolio/libs/portfolio-0.0.1-SNAPSHOT.jar \
    --server.port=10501 \
    --spring.application.name=portfolio \
    --grpc.client.identity-provider-grpc-server.address=static://127.0.0.1:10502 \
    --grpc.client.identity-provider-grpc-server.enableKeepAlive=true \
    --grpc.client.identity-provider-grpc-server.keepAliveWithoutCalls=true \
    --grpc.client.identity-provider-grpc-server.negotiationType=plaintext \
    --spring.datasource.url=jdbc:mariadb://db2.csse.canterbury.ac.nz/seng302-2022-team600-portfolio-prod \
    --spring.datasource.driver-class-name=org.mariadb.jdbc.Driver \
    --spring.jpa.hibernate.ddl-auto=create-drop \
    --spring.datasource.username=${DATABASE_USERNAME} \
    --spring.datasource.password=${DATABASE_PASSWORD}
