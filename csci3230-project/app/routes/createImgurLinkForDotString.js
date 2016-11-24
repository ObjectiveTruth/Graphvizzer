var config = require('../config.js');
var imgur = require('imgur');
var exec = require('child_process').exec;
var rb = require('request-promise');

var createImgurLinkForDotString = function (request, response) {
    console.log(request.body);

    const TEMPORARY_GRAPHVIZ_FILE_PATH = config.general.TEMPORARY_GRAPH_FILE_DIRECTORY + Date.now() + '.png';
    console.log(TEMPORARY_GRAPHVIZ_FILE_PATH);

    var inputDotString = request.body.text;
    var inputToken = request.body.token;
    var responseURL = request.body.response_url;

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

    var command = 'echo "' + request.body.text + '" | dot -Tpng -o ' + TEMPORARY_GRAPHVIZ_FILE_PATH;

    exec(command, function (error, stdout, stderr) {
        if (error) {
            console.log(error);
            rb({
                method: 'POST',
                uri: responseURL,
                body: {
                    text: config.messages.BusinessMessages.BadDOTFormatFromSlack,
                    response_type: config.slack.EPHEMERAL_RESPONSE_TYPE
                },
                json: true
            });
        }else {
            imgur.uploadFile(TEMPORARY_GRAPHVIZ_FILE_PATH)
                .then(function (response) {
                    var link = response.data.link;
                    console.log('Upload to imgur Succeeded, link: ' + link);
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
                    console.error('Error: ' + err.message);
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
                .done(function () {
                    fs.unlink(TEMPORARY_GRAPHVIZ_FILE_PATH, function () {});
                });
        }
    });
};

module.exports = createImgurLinkForDotString;
