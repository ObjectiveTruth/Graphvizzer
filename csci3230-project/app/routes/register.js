var config = require('../config.js');
var rb = require('request-promise');

var register = function (request, response) {
    rb({
        uri: config.slack.OAUTH_ENDPOINT,
        qs: {
            client_id: config.slack.SLACK_CLIENT_ID,
            client_secret: config.slack.SLACK_APP_SECRET,
            code: request.query.code
        },
        json: true
    }).then(function (data) {
        if (data.ok) {
            response.send(config.messages.BusinessMessages.SuccessfulRegister);
        }else {
            response.send(config.messages.SystemMessages.UnexpectedSystemError);
        }
    }).catch(function (error) {
        response.send(config.messages.SystemMessages.UnexpectedSystemError);
    });
};

module.exports = register;
