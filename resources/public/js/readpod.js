// readpod.js
// stephen olsen

function getAndPlayAudio(article_id) {
    //Beginnings, will need to more robustly check if it's playing or
    //if it's loaded and all that fun stuff.
    $.ajax({
        url: 'article/' + article_id,
        success: function(data) {
            if (data.status === "Rendered") {
                soundManager.createSound({
                    id: article_id,
                    url: data.url
                });
                soundManager.play(article_id);
            }
            else if (data.status === "Processing") {
                setTimeout(function () { getAndPlayAudio(article_id); }, 5000);
            }
        }
    });
}

function rowClick(e) {
    var id = $(e.currentTarget).parent().attr("id");
    console.log("clicked: " + id);
    getAndPlayAudio(id);
}

$(document).ready(function () {
    soundManager.setup({
        url: 'swf/',
        flashVersion: 8,
        useHighProformance: true,
        wmode: 'transparent',

//        onready: function() {

//            var mySound = soundManager.createSound({
//                id: 'aSound',
//                url: '/path/to/an.mp3'
                // onload: function() { console.log('sound loaded!', this); }
                // other options here..
//            });

//            mySound.play();

//        },

        // optional: ontimeout() callback for handling start-up failure

    });

    // Play callbacks, this is going to need reworking.
    $(".track_title").on("click", rowClick);
    $(".track_number").on("click", rowClick);

});
