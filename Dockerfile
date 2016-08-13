FROM objectivetruth/java-and-graphviz:latest
EXPOSE 9000

# Play Options
##############
# Get this from Slack when registering the application. Ensures the messages are indeed coming from Slack
ENV GRAPHVIZZER_SLACK_AUTHENTICATION_TOKEN="MyMockToken" \
    GRAPHVIZZER_MAXIMUM_DOT_STRING_LENGTH=500 \
    GRAPHVIZZER_SLACK_APP_SECRET="MyMockSecret" \
    GRAPHVIZZER_SLACK_CLIENT_ID="29667068068.63519026177" \
    GRAPHVIZZER_TEMPORARY_GRAPH_FILE_DIRECTORY="/app/tmp/" \
    GRAPHVIZZER_CUSTOM_ERROR_HANDLER="common.error_handlers.ProdErrorHandler" \
    GRAPHVIZZER_PLAY_SECRET="where_we're_going_we_don't_need_secrets"

ADD target/universal/graphviz-slack-app-1.0-SNAPSHOT.tgz /app/

# Where the temporary files will go, has to be the same as the above GRAPHVIZZER_TEMPORARY_GRAPH_FILE_DIRECTORY
RUN mkdir -p /app/tmp

CMD ["/app/graphviz-slack-app-1.0-SNAPSHOT/bin/graphviz-slack-app", "-Dplay.crypto.secret=$GRAPHVIZZER_PLAY_SECRET"]
