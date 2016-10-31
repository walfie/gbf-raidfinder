# gbf-raidfinder

[gbf-raidfinder](http://gbf-raidfinder.aikats.us/) is a site for finding
[Granblue Fantasy](http://granbluefantasy.jp) raid tweets.

![Screenshot](http://i.imgur.com/utjVgBV.png)

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

For implementation details for some of the above features (including raid
boss discovery and automatic translations), see
[`/docs/implementation.md`](/docs/implementation.md).

For project details (including running the app locally and deploying to
Heroku), see [`/docs/project.md`](/docs/project.md).

