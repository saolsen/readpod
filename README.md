# readability-podcasts

Lets you download your readability reading list as an audio podcast.

## About

This is just a prototype and runs on heroku. You can hit the endpoint
and get your current reading list as an audio file.

## TODO

In a production system the audio processing should be
decoupled from the api (should be pretty easy) The rendered articles
should be saved somewhere to avoid re-rendering the same
files. Also the actual way it's used needs some thinking through,
(podcast of reading list vs rendering individual articles, etc...)

This is just a proof of concept.
