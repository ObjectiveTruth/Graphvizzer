'use strict';
var express = require('express');
var exec = require('child_process').exec;
var bodyParser = require('body-parser');
var imgur = require('imgur');
var mongoose = require('mongoose');
var favicon = require('serve-favicon');
var fs = require('fs');

var app = express();

const STATE_SUCCESS = 0;
const STATE_FAILURE = 1;

app.use(favicon(__dirname + '/public/images/favicon.ico'));

app.use(bodyParser.urlencoded({ extended:false }));
app.use(bodyParser.json());

app.use(express.static('public'));
//app.use('/', express.static('public'));

//configure view engine
app.set('views', __dirname + '/views');
app.set('view engine', 'pug');

//connect to database
mongoose.connect('localhost:27017/userComments');

app.get('/', function (req, res) {
	res.render('index');
});

app.get('/isAlive', function (req, res) {
    res.send('Yup, it\'s alive!');
});

app.get('/processDOT', function(req, res) {
    res.render('dot-input', {title:'Graphizzer'})
});

app.post('/processDOT', function(req, res) {

	console.log(req.body);

    const filenameBasedOnCurrentUnixTime = 'temp/' + Date.now() + '.png';
	
	var command = 'echo "' + req.body.inputDOTString + '" | dot -Tpng -o ' + filenameBasedOnCurrentUnixTime;
	
	exec(command, function(error, stdout, stderr) {

		if(error) {
			console.log(error);
			res.send({state:STATE_FAILURE,
					 message: 'Error processing your DOT string.'});
		} else {
			imgur.uploadFile('./' + filenameBasedOnCurrentUnixTime)
                .then(function (result) {
                    var link = result.data.link;
                    console.log('Upload to Imgur succeeded. Link: ' + link);
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
                    fs.unlink(filenameBasedOnCurrentUnixTime, function() {
						console.log('File deleted');
					});
                });
			
		}
	});
});

var server = app.listen(3000, function () {
    console.log('Listening on port 3000!');
});

module.exports = server;