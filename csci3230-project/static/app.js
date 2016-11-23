$(document).ready(function () {
    $('#create-link-button').click(function (event) {
        event.preventDefault();
        var inputDOTString = $('#input-DOT-string').val();
        console.log(inputDOTString);

        $.post('/processDOT', { inputDOTString: inputDOTString })
            .then(function (result) {
                console.log(result);
            })
            .catch(function (error) {
                //TODO handle me!!!!
                console.log(error);
            });
    });
});
