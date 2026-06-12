FROM postgres:15-alpine

COPY create_second_db.sh /docker-entrypoint-initdb.d/create_second_db.sh
RUN chmod +x /docker-entrypoint-initdb.d/create_second_db.sh

