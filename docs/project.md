# Project Details

gbf-raidfinder is written in Scala (both frontend and backend) and is split
into multiple subprojects:

* `server`: A Play Framework app that handles websocket requests and
  serves static assets
* `stream`: Used by `server` for streaming-related functionality
* `client`: Scala.js frontend app
* `protocol`: Protobuf definitions for websocket communication between
  server and client

## Setup

### Add Twitter Credentials

Twitter credentials are needed to read from the Twitter API.

gbf-raidfinder uses [twitter4j](https://github.com/yusuke/twitter4j), so
credentials can be configured in a few different ways:

* Add a `twitter4j.properties` file to one of the resource directories
  (such as `stream/src/main/resources/twitter4j.properties`)
* Set system properties
* Set environment variables

See the [twitter4j configuration
docs](http://twitter4j.org/en/configuration.html) for more details.

### Redis (Optional)

Since the boss list is determined at runtime, it would be inconvenient to
have an empty boss list every time the app starts up. gbf-raidfinder can
be configured to use Redis to persist boss data and load it on startup.

The Redis URL can be set in
[`/server/src/main/resources/application.conf`](/server/src/main/resources/application.conf/)
using the `application.cache.redisUrl` key.

If this key is empty, gbf-raidfinder will not persist any data between
restarts.

### Run Locally

* Ensure you have [jre](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html), [sbt](http://www.scala-sbt.org/), and [python 2.x](https://www.python.org/downloads/) installed prior to running the app.

The Play application can be run with `sbt run` and going to
[localhost:9000](http://localhost:9000). This will also compile the
client-side JavaScript and make it available to the server.

To compile just the client, you can run `sbt client/fastOptJS` and open
`client/target/scala-2.11/classes/public/index.html` in a browser (either
via local filesystem or some other HTTP server). If viewed via local
filesystem, the client will connect to `gbf-raidfinder.aikats.us` instead
of a locally-running server.

## Heroku Deployment

gbf-raidfinder uses [sbt-heroku](https://github.com/heroku/sbt-heroku) for
Heroku deployment. To run your own instance, you will need to set some
environment variables and enable Redis Cloud.

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

