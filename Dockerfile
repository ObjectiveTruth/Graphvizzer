FROM java:8
EXPOSE 9000

# Play Options
##############
# Get this from Slack when registering the application. Ensures the messages are indeed coming from Slack
ENV GRAPHVIZZER_SLACK_AUTHENTICATION_TOKEN "MyMockToken"

ENV GRAPHVIZZER_MAXIMUM_DOT_STRING_LENGTH 500
ENV GRAPHVIZZER_SLACK_APP_SECRET "MyMockSecret"
ENV GRAPHVIZZER_SLACK_CLIENT_ID "29667068068.63519026177"
ENV GRAPHVIZZER_TEMPORARY_GRAPH_FILE_DIRECTORY "/app/tmp/"

##############

ADD target/universal/graphviz-slack-app-1.0-SNAPSHOT.tgz /app/
ENV PLAY_SECRET "where_we're_going_we_don't_need_secrets"


RUN apt-get update && apt-get install -y graphviz && mkdir /app/tmp \
    && rm -rf /var/lib/apt/lists/*

CMD ["/app/graphviz-slack-app-1.0-SNAPSHOT/bin/graphviz-slack-app", "-Dplay.crypto.secret=$PLAY_SECRET"]
