fuser -k 10500/tcp || true
fuser -k 10502/tcp || true

DATABASE_USERNAME=$(<production-identityprovider/username.txt)
DATABASE_PASSWORD=$(<production-identityprovider/password.txt)

java -jar production-identityprovider/libs/identityprovider-0.0.1-SNAPSHOT.jar \
  --spring.application.name=identity-provider \
  --grpc.server.port=10502 \
  --server.port=10500 \
  --protocol=https \
  --hostName=csse-s302g6.canterbury.ac.nz \
  --port=443 \
  --rootPath=/prod/identity \
  --photoLocation=production-identityprovider/photos/ \
  --spring.datasource.url=jdbc:mariadb://db2.csse.canterbury.ac.nz/seng302-2022-team600-identityprovider-prod \
  --spring.datasource.driver-class-name=org.mariadb.jdbc.Driver \
  --spring.jpa.hibernate.ddl-auto=create-drop \
  --spring.datasource.username=${DATABASE_USERNAME} \
  --spring.datasource.password=${DATABASE_PASSWORD}