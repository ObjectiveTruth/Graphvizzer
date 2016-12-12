import config from '../config/config';
import rb from 'request-promise';
var logger = require('../logger/logger');

let register = function (request, response) {
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
            logger.info('Successfully Registered a new team: ' + JSON.stringify(data));
            response.send(config.messages.BusinessMessages.SuccessfulRegister);
        }else {
            logger.error(data, 'Failed to register a new team, bad response from Slack OAUTH endpoint');
            response.send(config.messages.SystemMessages.UnexpectedSystemError);
        }
    }).catch(function (error) {
        logger.error(error, 'Failed to register a new team, bad response from Slack OAUTH endpoint');
        response.send(config.messages.SystemMessages.UnexpectedSystemError);
    });
};

module.exports = register;
