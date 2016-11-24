var config = {};

config.status = {
    STATE_FAILURE: 1,
    STATE_SUCCESS: 0
};

config.slack = {
    SLACK_AUTHENTICATION_TOKEN: process.env.GRAPHVIZZER_SLACK_AUTHENTICATION_TOKEN || 'mocktoken',
    SLACK_APP_SECRET: process.env.GRAPHVIZZER_SLACK_APP_SECRET || 'mocksecret',
    SLACK_CLIENT_ID: process.env.GRAPHVIZZER_SLACK_CLIENT_ID || '29667068068.63519026177',
    EPHEMERAL_RESPONSE_TYPE: 'ephemeral',
    CHANNEL_RESPONSE_TYPE: 'in_channel',
    OAUTH_ENDPOINT: 'https://slack.com/api/oauth.access'
};

config.general = {
    MAXIMUM_DOT_STRING_LENGTH_NOT_INCLUDING_NEW_LINES: process.env.GRAPHVIZZER_MAXIMUM_DOT_STRING_LENGTH || 500,
    TEMPORARY_GRAPH_FILE_DIRECTORY: process.env.GRAPHVIZZER_TEMPORARY_GRAPH_FILE_DIRECTORY || './tmp/'
};

config.messages = {
    SystemMessages: {
        // Errors
        BadFormDataFromSlack: 'Incorrect form data from slack',
        BadTokenFromSlack: 'Bad Token Received, are you authorized??',
        UnexpectedSystemError: 'Unexpected System Error: Try raising a bug ' +
            'https://github.com/ObjectiveTruth/Graphvizzer/issues'
    },
    BusinessMessages: {
        // Errors
        BadDOTFormatFromSlack: 'Incorrect format, see the <www.graphviz.org/Documentation/dotguide.pdf|DOT guide> ' +
            'or examples below:\n>>>`digraph{beaver->platypus duck->platypus}`\n`graph{ying--yang}`',
        BadDOTLengthFromSlack: 'DOT string is great than 500 characters, please by kind to my little server',

        // Normal
        ProcessingYourRequest: 'Processing...',
        SuccessfulRegister: 'Successfully Registered!'
    }
};

module.exports = config;
