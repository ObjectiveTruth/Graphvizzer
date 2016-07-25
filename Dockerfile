FROM java:8
EXPOSE 9000

ADD target/universal/graphviz-slack-app-1.0-SNAPSHOT.tgz /app/
ENV PLAY_SECRET "FOSSalltheway"

CMD ["/app/graphviz-slack-app-1.0-SNAPSHOT/bin/graphviz-slack-app", "-Dplay.crypto.secret=$PLAY_SECRET"]


