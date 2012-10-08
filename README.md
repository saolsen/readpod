# readpod

Lets you download your readability reading list as an audio podcast.

## About

prototype on dotcloud, needs migration to aws as the worker needs more
memory than dotcloud gives.

## TODO

In a production system the audio processing should be
decoupled from the api (should be pretty easy) The rendered articles
should be saved somewhere to avoid re-rendering the same
files. Also the actual way it's used needs some thinking through,
(podcast of reading list vs rendering individual articles, etc...)


