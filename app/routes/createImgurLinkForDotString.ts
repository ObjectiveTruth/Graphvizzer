import imgur = require('imgur');
import config from '../config/config';
import * as child_process from 'child_process';
let exec = child_process.exec;
let rb = require('request-promise');
let fs = require('fs');
let logger = require('../logger/logger.js');

export default function (request, response) {
    const TEMPORARY_GRAPHVIZ_FILE_PATH = config.general.TEMPORARY_GRAPH_FILE_DIRECTORY + Date.now() + '.png';

    const inputDotString = request.body.text;
    const inputToken = request.body.token;
    const responseURL = request.body.response_url;

    if (inputToken !== config.slack.SLACK_AUTHENTICATION_TOKEN) {
        response.send({
            text: config.messages.SystemMessages.BadTokenFromSlack,
            response_type: config.slack.EPHEMERAL_RESPONSE_TYPE
        });
        return;
    }else if (inputDotString.length > config.general.MAXIMUM_DOT_STRING_LENGTH_NOT_INCLUDING_NEW_LINES) {
        response.send({
            text: config.messages.BusinessMessages.BadDOTLengthFromSlack,
            response_type: config.slack.EPHEMERAL_RESPONSE_TYPE
        });
        return;
    }else {
        response.send({
            text: config.messages.BusinessMessages.ProcessingYourRequest + '\n>>>' + inputDotString,
            response_type: config.slack.EPHEMERAL_RESPONSE_TYPE
        });
    }
    const command = 'dot -Tpng -o ' + TEMPORARY_GRAPHVIZ_FILE_PATH;

    let dotProcess = exec(command, function (error, stdout, stderr) {
        if (error) {
            logger.error(error.message, 'Error processing the DOT file');
            rb({
                method: 'POST',
                uri: responseURL,
                body: {
                    text: config.messages.BusinessMessages.BadDOTFormatFromSlack,
                    response_type: config.slack.EPHEMERAL_RESPONSE_TYPE
                },
                json: true
            });
        }
    });
    dotProcess.stdin.end(inputDotString);
    dotProcess.on('exit', (code) => {
        if (code === 0) {
            imgur.uploadFile(TEMPORARY_GRAPHVIZ_FILE_PATH)
                .then(function (response) {
                    const link = response.data.link;
                    logger.info('Upload to imgur Succeeded, link: ' + link);
                    rb({
                        method: 'POST',
                        uri: responseURL,
                        body: {
                            text: link,
                            response_type: config.slack.CHANNEL_RESPONSE_TYPE
                        },
                        json: true
                    });
                })
                .catch(function (err) {
                    logger.error(err, 'Error uploading dot file to imgur');
                    rb({
                        method: 'POST',
                        uri: responseURL,
                        body: {
                            text: config.messages.SystemMessages.UnexpectedSystemError,
                            response_type: config.slack.EPHEMERAL_RESPONSE_TYPE
                        },
                        json: true
                    });
                })
                .done(() => {fs.unlink(TEMPORARY_GRAPHVIZ_FILE_PATH, () => {}); });
        }else {
            fs.unlink(TEMPORARY_GRAPHVIZ_FILE_PATH, () => {});
        }
    });
};
