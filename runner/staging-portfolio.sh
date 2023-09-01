fuser -k 9501/tcp || true

DATABASE_USERNAME=$(<staging-portfolio/username.txt)
DATABASE_PASSWORD=$(<staging-portfolio/password.txt)

java -jar staging-portfolio/libs/portfolio-0.0.1-SNAPSHOT.jar \
    --server.port=9501 \
    --spring.application.name=portfolio \
    --grpc.client.identity-provider-grpc-server.address=static://127.0.0.1:9502 \
    --grpc.client.identity-provider-grpc-server.enableKeepAlive=true \
    --grpc.client.identity-provider-grpc-server.keepAliveWithoutCalls=true \
    --grpc.client.identity-provider-grpc-server.negotiationType=plaintext \
    --spring.datasource.url=jdbc:mariadb://db2.csse.canterbury.ac.nz/seng302-2022-team600-portfolio-test \
    --spring.datasource.driver-class-name=org.mariadb.jdbc.Driver \
    --spring.jpa.hibernate.ddl-auto=create-drop \
    --spring.datasource.username=${DATABASE_USERNAME} \
    --spring.datasource.password=${DATABASE_PASSWORD}
