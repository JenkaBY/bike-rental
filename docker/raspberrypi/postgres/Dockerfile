ARG POSTGRES_IMAGE=postgres:15-alpine
FROM ${POSTGRES_IMAGE}

COPY entrypoint-wrapper.sh /usr/local/bin/entrypoint-wrapper.sh
RUN chmod +x /usr/local/bin/entrypoint-wrapper.sh

ENTRYPOINT ["entrypoint-wrapper.sh"]
CMD ["postgres"]
