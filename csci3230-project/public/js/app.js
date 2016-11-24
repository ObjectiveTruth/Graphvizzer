$(document).ready(function () {
	
    $('#create-link-button').click(function (event) {
        event.preventDefault();
        var inputDOTString = $('#input-DOT-string').val();
        
		console.log('from app.js: '+inputDOTString);

        $.post('/processDOT', { inputDOTString: inputDOTString })
            .then(function (result) {
                console.log(result.data.link);
				$('#result').html('<img src="'+result.data.link+'"><br>')
							.append(result.data.link);

            })
            .catch(function (error) {
                //TODO handle me!!!!
                console.log(error);
            });
    });
});