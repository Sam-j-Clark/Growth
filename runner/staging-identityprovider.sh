fuser -k 9500/tcp || true
fuser -k 9502/tcp || true

DATABASE_USERNAME=$(<staging-identityprovider/username.txt)
DATABASE_PASSWORD=$(<staging-identityprovider/password.txt)


java -jar staging-identityprovider/libs/identityprovider-0.0.1-SNAPSHOT.jar \
  --spring.application.name=identity-provider \
  --grpc.server.port=9502 \
  --server.port=9500 \
  --protocol=https \
  --hostName=csse-s302g6.canterbury.ac.nz \
  --port=443 \
  --rootPath=/test/identity \
  --photoLocation=staging-identityprovider/photos/ \
  --spring.datasource.url=jdbc:mariadb://db2.csse.canterbury.ac.nz/seng302-2022-team600-identityprovider-test \
  --spring.datasource.driver-class-name=org.mariadb.jdbc.Driver \
  --spring.jpa.hibernate.ddl-auto=create-drop \
  --spring.datasource.username=${DATABASE_USERNAME} \
  --spring.datasource.password=${DATABASE_PASSWORD}
