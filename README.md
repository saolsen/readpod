# readpod

Lets you download your readability reading list as an audio podcast.

Readpod has moved, this repository is no longer kept up to date.
See https://github.com/arc90/readpod

## About

This is just a prototype and runs on heroku. After authenticating you
can see your reading list and download the articles as audio files.

## TODO

In a production system the audio processing should be
decoupled from the api (should be pretty easy) The rendered articles
should be saved somewhere to avoid re-rendering the same
files. Also the actual way it's used needs some thinking through,
(podcast of reading list vs rendering individual articles, etc...)

[readpod.herokuapp.com](http://readpod.herokuapp.com)
