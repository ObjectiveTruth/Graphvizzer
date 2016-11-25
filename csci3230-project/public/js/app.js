function createNewComment(name, comment, time) {
	var centerDiv = $('<div>').addClass('row');
	var colDiv = $('<div>').addClass('col-sm-8 col-sm-offset-2 col-md-8 col-md-offset-2');
	var panelDiv = $('<div>').addClass('panel panel-default');
	var panelHeadingDiv = $('<div>').addClass('panel-heading');
	var panelBodyDiv = $('<div>').addClass('panel-body').html(comment);
	var usernameStrong = $('<strong>').html(name);
	var timestampSpan = $('<span>').addClass('text-muted').html(' on ' + time);

	usernameStrong.append(timestampSpan);

	panelHeadingDiv.append(usernameStrong);

	panelDiv.append(panelHeadingDiv)
			.append(panelBodyDiv);

	colDiv.append(panelDiv);

	centerDiv.append(colDiv);

	$('#all-comments').append(centerDiv);
	
}

function clearReviewFields() {
	$('#input-name').val('');
	$('#input-comment').val('');
}

$(document).ready(function () {
		
	$.post('/loadAllComments')
		.then(function(result) {
		
			$.each(result.allComments, function(index, comment) {
			createNewComment(comment.username, comment.content, comment.timestamp);
			});
		})
		.catch(function(error) {
			
			console.log('There\'s been an error loading past comments.');
		
		});
		
    $('#create-link-button').click(function (event) {
        event.preventDefault();
		
        var inputDOTString = $('#input-DOT-string').val();
        
		console.log('from app.js: '+inputDOTString);
		var pTag = $('<p>');

        $.post('/processDOT', { inputDOTString: inputDOTString })
            .then(function (result) {
				
                var link = result.data.link;
				var secureLink = link.replace('http', 'https');
			
				pTag.addClass('success')
					.text('Success! Scroll down to see your image.');
			
				$('#warning-dot').html(pTag);
			
				var imgTag = $('<img>').attr("src", secureLink);
			
				$('#result').html('')
							.append(imgTag)
							.append('<br><br>')
							.append(secureLink);

            })
            .catch(function (error) {
			
				if (error) {
					pTag.addClass('error')
						.text('Uh... are you sure that syntax is valid? Please try again.');

					$('#warning-dot').html(pTag);
				}

            });
    });
	
	$('#submit-comment-button').click(function (event) {
		event.preventDefault();
		var pTag = $('<p>');
		
		var username = $('#input-name').val();	
		var comment = $('#input-comment').val();
		var currentTime = new Date().toUTCString();			
		
		if (username == '')
			username = 'Anonymous';
		
		if ($('#input-comment').val().length <= 250) {
			
			if (comment.length > 1) {
				
				createNewComment(username, comment, currentTime);
				clearReviewFields();
						
				$.post('/submitNewComment', {username: username, userInputComment: comment, timestamp: currentTime})
					.then(function(result) {
						pTag.addClass('success')
							.text('Your comment has been submitted successfully.');
					})
					.catch(function(error) {
						pTag.addClass('error')
							.text('Something went wrong... and your comment was not submitted. Oops, sorry!');
					});
			} else {
				pTag.addClass('error')
					.text('You may not submit empty comments. Please try again.');
			}
		} else {
			pTag.addClass('error')
					.text('Your comment is too long! Remember the max. count 250 characers.');
		}
		
		$('#warning-comment').html(pTag);
	});
	
});