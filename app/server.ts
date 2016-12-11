import * as express from 'express';
var exec = require('child_process').exec;
var bodyParser = require('body-parser');
var imgur = require('imgur');
var mongoose = require('mongoose');
var mockgoose = require('mockgoose');
var favicon = require('serve-favicon');
var fs = require('fs');
import config from './config/config';
var logger = require('./logger/logger.js');

var app = express();

const STATE_SUCCESS = 0;
const STATE_FAILURE = 1;

mongoose.connect(config.general.MONGO_ADDRESS, function (error) {
    logger.warn(config.messages.SystemMessages.NoPersistentDatabaseFound);
    mockgoose(mongoose).then(function () {
        mongoose.connect('mongodb://example.com/userComments');
    });
});

var Schema = mongoose.Schema;

var commentSchema = new Schema(
    {
        username: {
            type: String,
            required: true
        },
        content: {
            type: String,
            required: true
        },
        timestamp: String
    },
    {
        collection: 'comments'
    });

var Comment = mongoose.model('comment', commentSchema);

app.use(favicon(__dirname + '/public/images/favicon.ico'));

app.use(bodyParser.urlencoded({ extended: false }));
app.use(bodyParser.json());

app.use(express.static('public'));

app.set('views', __dirname + '/views');
app.set('view engine', 'pug');

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

app.post('/createImgurLinkForDOTString', require('./routes/createImgurLinkForDotString.js'));

app.get('/register', require('./routes/register.js'));

app.post('/processDOT', function (req, res) {
    if (!req.body.inputDOTString) {
        logger.warn('A request was made to process an empty string using the web-api, sending error back');
        res.send({
            state: STATE_FAILURE,
            message: 'Error processing your dot string'
        });
        return;
    }

    const TEMPORARY_GRAPHVIZ_FILE_PATH = config.general.TEMPORARY_GRAPH_FILE_DIRECTORY + Date.now() + '.png';

    var command = 'echo "' + req.body.inputDOTString + '" | dot -Tpng -o ' + TEMPORARY_GRAPHVIZ_FILE_PATH;

    exec(command, function (error, stdout, stderr) {
        if (error) {
            logger.error(error);
            res.send({
                state: STATE_FAILURE,
                message: 'Error processing your dot string'
            });
        }else {
            imgur.uploadFile(TEMPORARY_GRAPHVIZ_FILE_PATH)
                .then(function (result) {
                    var link = result.data.link;
                    logger.info('Upload to Imgur succeeded. Link: ' + link);
                    res.send({
                        state: STATE_SUCCESS,
                        data: {
                            link: link
                        }
                    });
                })
                .catch(function (err) {
                    logger.error(err, 'Error while uploading to Imgur');
                    res.send({
                        state: STATE_FAILURE,
                        message: 'Error while uploading to imgur'
                    });
                })
                .done(function () {
                    fs.unlink(TEMPORARY_GRAPHVIZ_FILE_PATH, function () {});
                });
        }
    });
});

app.post('/loadAllComments', function (req, res) {
    Comment.find()
        .then(function (results) {
            logger.info('Results for ALL comments search: ' + JSON.stringify(results));
            res.send({ allComments: results });
        })
        .catch(function (error) {
            logger.error(error, 'Error when loading all comments, sending back empty successful result');
            res.send({ allComments: [] });
        });
});

app.post('/submitNewComment', function (req, res) {
    if (!req.body) {
        logger.error('An attempt was made to submit a new comment with no body content, sending back error');
        res.send({
            state: STATE_FAILURE,
            message: config.messages.SystemMessages.ErrorSavingComment });
        return;
    }

    var username = req.body.username || '';
    var commentContent = req.body.userInputComment || '';
    var timestamp = req.body.timestamp || '';

    //save comment locally
    var comment = {
        username: username,
        content: commentContent,
        timestamp: timestamp
    };
    var newComment = new Comment(comment);

    //add to database
    logger.info('Attempting to save new comment to database: ' + JSON.stringify(comment));
    newComment.save(function (error) {
        if (error) {
            logger.error(error, 'Error saving new comment: ' + JSON.stringify(comment));
            res.send({
                state: STATE_FAILURE,
                message: config.messages.SystemMessages.ErrorSavingComment });
        } else {
            logger.info('Successfully saved comment to database: ' + JSON.stringify(comment));
            res.send({
                state: STATE_SUCCESS });
        }
    });
});

var server = app.listen(config.general.LISTEN_PORT, function () {
    logger.info('Listening on port ' + config.general.LISTEN_PORT);
});

module.exports = server;
