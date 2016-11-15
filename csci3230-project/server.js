'use strict';
var express = require('express');
var exec = require('child_process').exec;
var bodyParser = require('body-parser');
var imgur = require('imgur');

var app = express();

app.use(bodyParser.urlencoded({ extended:false })); //just look for name=value kind of data.
app.use(bodyParser.json());

app.get('/', function (req, res) {
    res.send('Hello, world!');
});

app.get('/isAlive', function (req, res) {
    res.send('Yup, it\'s alive!');
});

app.get('/dot', function(request, response) {
	response.sendFile(__dirname + '/dot-input.html');
});

app.post('/process_dot', function(req, res) {
    //console.log(req.body);
    
    //res.send('Your command was submitted. You\'ll be redirected to your image in a few seconds.');
    //var dot = '"graph {a -- b -- d -- c -- f[color=red,penwidth=3.0]; b -- c; d -- e; e -- f; a -- d; }"';
    var dot = '"'+req.body['command']+'"';
    var filename = req.body['filename'];
    var cmd = 'echo ' + dot + ' | dot -Tpng -o ./' + filename;
    var link;
    var imgur_cmd;
    
    //Print the command entered
    console.log('Command entered: ' + req.body['command']);
    
    //Upload to imgur using its API
    imgur.uploadFile('./' + filename)
        .then(function (json) {
            link = json.data.link;
            console.log(link);
            //res.redirect(link + '.png');
        
            var imgur_cmd = 'open ' + link;

            exec(imgur_cmd, function(error, stdout, stderr) {

                if(error) { console.log(error); }
                
                res.send('Your command was received. You will be promped to it\'s link in a few seconds.');
            });
        })
        .catch(function (err) { console.error('Error: ' + err.message); });
});

app.listen(3000, function () {
    console.log('Listening on port 3000!');
});