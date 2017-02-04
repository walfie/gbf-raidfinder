# gbf-raidfinder

[gbf-raidfinder](http://gbf-raidfinder.aikats.us/) is a site for finding
[Granblue Fantasy](http://granbluefantasy.jp) raid tweets.

![Screenshot](http://i.imgur.com/utjVgBV.png)

[Heroku](https://heroku.com/deploy) |
[DockerHub](https://hub.docker.com/r/walfie/gbf-raidfinder/) |
[Twitter](https://twitter.com/gbf_raidfinder)

## Features

gbf-raidfinder is similar to manually configuring boss names as search
terms in TweetDeck, but with some additional functionality:

* No Twitter login required
* Uses Twitter streaming API
  * Tweets show up in real-time (TweetDeck search columns use polling)
* Only shows tweets that were sent from the game
* Groups English and Japanese raid tweets in the same column
* Raid IDs are copied when the tweet is clicked on
* Desktop notifications (also with copy on click, and image preview)
* Sound notifications, with configurable notification sound per boss
* Real-time list of raid bosses to choose from
  * No need to know the boss name ahead of time
  * Adds new bosses automatically when they're tweeted about
  * Boss names are automatically translated when possible

## Documentation

* [Implementation details](/docs/implementation.md): explains some
  runtime logic (raid boss discovery, automatic translations, etc)
* [Project details](/docs/project.md): project structure and local
  development
* [Deployment](/docs/deployment/README.md): how to deploy to Heroku or
  run the app in a Docker container

