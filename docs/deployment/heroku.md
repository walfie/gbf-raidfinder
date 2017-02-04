# Heroku Deployment

gbf-raidfinder runs comfortably on Heroku's free tier, for about ~5000
concurrent users. After the 5.5k user mark, performance starts to degrade,
due to Heroku's router not being able to keep up.

The app needs Twitter API credentials to connect to the streaming API.
You can create a new app at [apps.twitter.com](https://apps.twitter.com)
(You can enter whatever name/description/website you want, it doesn't
actually matter). After you have an app, check the "Keys and Access Tokens"
tab, and generate your access token/secret.

Although Heroku is free, it's recommended to add a credit card so Heroku
allows your application to run 24/7 (you won't be charged).

## Quick Deploy

[![Deploy](https://www.herokucdn.com/deploy/button.svg)](https://heroku.com/deploy?template=https://github.com/walfie/gbf-raidfinder)

You can click the image above to deploy the current code in this repository
to Heroku. This will compile and run the app (it may take several minutes
to build the first time), and add Redis Cloud for persistence.

## Manual Deploy

gbf-raidfinder uses [sbt-heroku](https://github.com/heroku/sbt-heroku)
for manual Heroku deployment. To run your own instance, you will need
to set some environment variables and enable Redis Cloud.

* Set Twitter credentials:

  ```sh
  heroku config:add oauth.consumerKey=insert
  heroku config:add oauth.consumerSecret=your
  heroku config:add oauth.accessToken=credentials
  heroku config:add oauth.accessTokenSecret=here
  ```

* Add the [Redis Cloud](https://elements.heroku.com/addons/rediscloud)
  add-on to your project

* Change the following line in [`/build.sbt`](/build.sbt) to point to your
  application name (not "gbf-raidfinder")

  ```scala
  herokuAppName in Compile := "gbf-raidfinder",
  ```

* Run `sbt stage deployHeroku`

