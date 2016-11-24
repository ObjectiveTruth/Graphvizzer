'use strict';

var express = require('express');
var exec = require('child_process').exec;
var bodyParser = require('body-parser');
var imgur = require('imgur');
var mongoose = require('mongoose');
var favicon = require('serve-favicon');
var fs = require('fs');
var config = require('./app/config.js');

var app = express();

app.use(favicon(__dirname + '/public/images/favicon.ico'));

app.use(bodyParser.urlencoded({ extended: false }));
app.use(bodyParser.json());

app.use(express.static('public'));

app.set('views', __dirname + '/views');
app.set('view engine', 'pug');

//mongoose.connect('localhost:27017/userComments');

app.get('/', function (req, res) {
    res.render('index', { title: 'Home', nav: 'home' });
});

app.get('/processDOT', function (req, res) {
    res.render('dot-input', { title: 'DOT', nav: 'dot' });
});

app.get('/reviews', function (req, res) {
    res.render('reviews', { title: 'Reviews', nav: 'review' });
});

app.get('/isAlive', function (req, res) {
    res.send('Yup, it\'s alive!');
});

app.post('/createImgurLinkForDOTString', require('./app/routes/createImgurLinkForDotString.js'));

app.get('/register', require('./app/routes/register.js'));

app.post('/processDOT', function (req, res) {
    console.log(req.body);
    const STATE_FAILURE = 1;
    const STATE_SUCCESS = 0;

    const TEMPORARY_GRAPHVIZ_FILE_PATH = config.general.TEMPORARY_GRAPH_FILE_DIRECTORY + Date.now() + '.png';

    var command = 'echo "' + req.body.inputDOTString + '" | dot -Tpng -o ' + TEMPORARY_GRAPHVIZ_FILE_PATH;

    exec(command, function (error, stdout, stderr) {
        if (error) {
            console.log(error);
            res.send({
                state: STATE_FAILURE,
                message: 'Error processing your dot string'
            });
        }else {
            imgur.uploadFile(TEMPORARY_GRAPHVIZ_FILE_PATH)
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
                    fs.unlink(TEMPORARY_GRAPHVIZ_FILE_PATH);
                });
        }
    });
});

var server = app.listen(9000, function () {
    console.log('Listening on port 9000!');
});

module.exports = server;
