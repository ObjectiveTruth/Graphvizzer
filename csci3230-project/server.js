'use strict';

var express = require('express');
var exec = require('child_process').exec;
var bodyParser = require('body-parser');
var imgur = require('imgur');
var fs = require('fs');
var config = require('./app/config.js');

var app = express();
const STATE_SUCCESS = 0;
const STATE_FAILURE = 1;

app.use(bodyParser.urlencoded({ extended: false }));
app.use(bodyParser.json());

app.get('/isAlive', function (req, res) {
    res.send('Yup, it\'s alive!');
});

app.post('/createImgurLinkForDOTString', require('./app/routes/createImgurLinkForDotString.js'));

app.get('/register', require('./app/routes/register.js'));

app.post('/processDOT', function (req, res) {
    console.log(req.body);

    const filenameBasedOnCurrentUnixTime = 'temp/' + Date.now() + '.png';

    var command = 'echo "' + req.body.inputDOTString + '" | dot -Tpng -o ' + filenameBasedOnCurrentUnixTime;

    exec(command, function (error, stdout, stderr) {
        if (error) {
            console.log(error);
            res.send({
                state: STATE_FAILURE,
                message: 'Error processing your dot string'
            });
        }else {
            imgur.uploadFile('./' + filenameBasedOnCurrentUnixTime)
                .then(function (result) {
                    var link = result.data.link;
                    console.log('Upload to imgur Succeeded, link: ' + link);
                    res.send({
                        state: STATE_SUCCESS,
                        data: {
                            link: link
                        }
                    });
                })
                .catch(function (err) {
                    console.error('Error: ' + err.message);
                    res.send({
                        state: STATE_FAILURE,
                        message: 'Error while uploading to imgur'
                    });
                })
                .done(function () {
                    fs.unlink(filenameBasedOnCurrentUnixTime);
                });
        }
    });
});

app.use('/', express.static('static'));

var server = app.listen(9000, function () {
    console.log('Listening on port 9000!');
});

module.exports = server;
