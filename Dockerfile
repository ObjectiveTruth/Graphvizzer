FROM java:8
EXPOSE 9000

ADD target/universal/graphviz-slack-app-1.0-SNAPSHOT.tgz /app/
ENV PLAY_SECRET "where_we're_going_we_don't_need_secrets"

RUN apt-get update && apt-get install -y graphviz && mkdir /app/tmp \
    && rm -rf /var/lib/apt/lists/*

CMD ["/app/graphviz-slack-app-1.0-SNAPSHOT/bin/graphviz-slack-app", "-Dplay.crypto.secret=$PLAY_SECRET"]
