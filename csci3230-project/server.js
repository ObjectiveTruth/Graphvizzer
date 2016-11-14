'use strict';
var express = require('express');
var exec = require('child_process').exec;
var bodyParser = require('body-parser');

var app = express();

app.use(bodyParser.urlencoded({ extended:false })); //just look for name=value kind-of data.
app.use(bodyParser.json());

app.get('/', function (req, res) {
    res.send('Hello World!');
});

app.get('/isAlive', function (req, res) {
    res.send('Yup, it\'s alive!');
});

app.get('/dot', function(request, response) {
	response.sendFile(__dirname + '/dot-input.html');
});

app.post('/process_dot', function(req, res) {
    
    // console.log(req.body);

    //res.send('Your command was submitted.');
    
    //var dot = '"graph {a -- b -- d -- c -- f[color=red,penwidth=3.0]; b -- c; d -- e; e -- f; a -- d; }"';
    var dot = '"'+req.body['command']+'"';
    var filename = req.body['filename'];

    var cmd = 'echo ' + dot + ' | dot -Tpng -o ./' + filename;
    
    console.log('DOT entered: ' + req.body['command']);
    
    exec(cmd, function(error, stdout, stderr) {
        
        var path = __dirname + '/' + filename;
        
        if(error) {
            console.log(error);
        }
        
        //res.send('Created image "' + filename + '" in the current directory.');
        
        res.sendFile(path);
    });
    
});


app.listen(3000, function () {
    console.log('Example app listening on port 3000!');
});