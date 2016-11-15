var request = require('supertest');

describe('loading express', function () {
    var server;

    beforeEach(function () {
        server = require('./server');
    });

    afterEach(function () {
        server.close();
    });

    it('responds to /', function (done) {
        request(server)
            .get('/')
            .expect('Content-Type', /text\/html/)
            .expect(200, done);
    });

    it('responds to /isAlive', function (done) {
        request(server)
            .get('/isAlive')
            .expect(200, done);
    });

    it('responds to slack messages', function(done) {
        request(server)
            .post('/createImgurLinkForDOTString')
            .send({
                token: 'abc123',
                team_id: 'fake_team',
                team_domain: 'fake_domain',
                channel_id: 'abc1234',
                channel_name: 'fake_channel_name',
                user_id: 'some_user_id',
                user_name: 'some_user_name',
                command: '/gviz digraph{a->b;}',
                text: 'digraph{a->b;}',
                response_url: 'http://slack.com/inputurl123'
            })
            .expect({
                text: 'Processing...\n>>>digraph{a->b;}',
                response_type: 'ephemeral'
            })
            .expect('Content-Type', /application\/json/)
            .expect(200, done);
    });

    it('404 everything else', function (done) {
        request(server)
            .get('/foo/bar')
            .expect(404, done);
    });
});
