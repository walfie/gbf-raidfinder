# Docker

Docker images are created with [sbt-native-packager](https://github.com/sbt/sbt-native-packager)
and published on DockerHub at [walfie/gbf-raidfinder](https://hub.docker.com/r/walfie/gbf-raidfinder/).

## Pulling

You can pull the latest pre-built tagged image using:

```sh
docker pull walfie/gbf-raidfinder
```

### Running

The container exposes port 9000 as its HTTP port, and the application
expects various environment variables to be set:

```
# Twitter API keys used by Twitter4J
oauth.consumerKey=*****
oauth.consumerSecret=*****
oauth.accessToken=*****
oauth.accessTokenSecret=*****

# Optional Redis connection string
GBF_REDIS_URL=redis://user:pass@host:1234
```

These can be set with the `-e` option or by saving them in a file and using
`--env-file`.

For example, if the above was saved as the file `/path/to/env`, and you
wanted to run the app on port 5555 of the host machine:

```
docker run -p5555:9000 --env-file /path/to/env walfie/gbf-raidfinder
```

The above would bind port 9000 of the application to port 1234 of the
host machine, and set environment variables based on the file.

This is equivalent to:

```
docker run -p5555:9000 \
  -e oauth.consumerKey=***** \
  -e oauth.consumerSecret=***** \
  -e oauth.accessToken=***** \
  -e oauth.accessTokenSecret=***** \
  -e GBF_REDIS_URL=redis://user:pass@host:1234 \
  walfie/gbf-raidfinder
```

On success, the application will be available on port 5555 of the host
machine.

### Building

If you want to build the image yourself instead of using the pre-built
one on DockerHub, you can run the following in the root directory:

```
sbt docker:publish
```

