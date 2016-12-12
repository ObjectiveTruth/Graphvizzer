FROM objectivetruth/nodejs-and-graphviz:latest
EXPOSE 9000

# Server Options
##############
# Get this from Slack when registering the application. Ensures the messages are indeed coming from Slack
ENV GRAPHVIZZER_SLACK_AUTHENTICATION_TOKEN="MyMockToken" \
    GRAPHVIZZER_MAXIMUM_DOT_STRING_LENGTH=500 \
    GRAPHVIZZER_SLACK_APP_SECRET="MyMockSecret" \
    GRAPHVIZZER_SLACK_CLIENT_ID="29667068068.63519026177" \
    GRAPHVIZZER_TEMPORARY_GRAPH_FILE_DIRECTORY="/tmp/"

ADD dist/ /app/

WORKDIR /app/
RUN npm install --production

CMD ["npm", "run", "start-docker"]
