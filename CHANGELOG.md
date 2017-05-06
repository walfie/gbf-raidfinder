# Change Log

## [v0.3.4](https://github.com/walfie/gbf-raidfinder/tree/v0.3.4) (2017-05-06)
[Full Changelog](https://github.com/walfie/gbf-raidfinder/compare/v0.3.3...v0.3.4)

**Implemented enhancements:**

- Use interned strings for per-user followed boss names [\#116](https://github.com/walfie/gbf-raidfinder/pull/116) ([walfie](https://github.com/walfie))
- Optimize serializing outbound Protobuf messages [\#115](https://github.com/walfie/gbf-raidfinder/pull/115) ([walfie](https://github.com/walfie))

## [v0.3.3](https://github.com/walfie/gbf-raidfinder/tree/v0.3.3) (2017-04-05)
[Full Changelog](https://github.com/walfie/gbf-raidfinder/compare/v0.3.2...v0.3.3)

**Implemented enhancements:**

- Don't keep lv100 bosses on the list forever [\#110](https://github.com/walfie/gbf-raidfinder/issues/110)
- Publish to DockerHub [\#107](https://github.com/walfie/gbf-raidfinder/issues/107)

**Fixed bugs:**

- iOS Safari clipboard copy [\#111](https://github.com/walfie/gbf-raidfinder/issues/111)

**Closed issues:**

- Attribution and Licensing [\#109](https://github.com/walfie/gbf-raidfinder/issues/109)
- Names of Bosses are blank in Heroku Deployment [\#105](https://github.com/walfie/gbf-raidfinder/issues/105)
- \[iOS\] Settings/Subscription cleared during refresh/reopen [\#104](https://github.com/walfie/gbf-raidfinder/issues/104)

**Merged pull requests:**

- Fix iOS Safari clipboard copy [\#113](https://github.com/walfie/gbf-raidfinder/pull/113) ([walfie](https://github.com/walfie))
- Add 30-day max TTL for bosses, regardless of level [\#112](https://github.com/walfie/gbf-raidfinder/pull/112) ([walfie](https://github.com/walfie))
- Publish to Docker [\#108](https://github.com/walfie/gbf-raidfinder/pull/108) ([walfie](https://github.com/walfie))
- Add "Deploy to Heroku" button [\#103](https://github.com/walfie/gbf-raidfinder/pull/103) ([walfie](https://github.com/walfie))

## [v0.3.2](https://github.com/walfie/gbf-raidfinder/tree/v0.3.2) (2016-12-16)
[Full Changelog](https://github.com/walfie/gbf-raidfinder/compare/v0.3.1...v0.3.2)

**Implemented enhancements:**

- Documentation [\#29](https://github.com/walfie/gbf-raidfinder/issues/29)

**Fixed bugs:**

- False raid names can be created using the twitter reset function. [\#98](https://github.com/walfie/gbf-raidfinder/issues/98)

**Closed issues:**

- "Child terminated, stopping" message on websocket disconnect [\#19](https://github.com/walfie/gbf-raidfinder/issues/19)

**Merged pull requests:**

- Ignore daily refresh tweets [\#99](https://github.com/walfie/gbf-raidfinder/pull/99) ([walfie](https://github.com/walfie))
- Update sbt to 0.13.13 [\#97](https://github.com/walfie/gbf-raidfinder/pull/97) ([walfie](https://github.com/walfie))
- Additional Documentation [\#96](https://github.com/walfie/gbf-raidfinder/pull/96) ([krishnaglick](https://github.com/krishnaglick))
- Use scala.collection.breakOut [\#95](https://github.com/walfie/gbf-raidfinder/pull/95) ([walfie](https://github.com/walfie))
- Add documentation [\#94](https://github.com/walfie/gbf-raidfinder/pull/94) ([walfie](https://github.com/walfie))

## [v0.3.1](https://github.com/walfie/gbf-raidfinder/tree/v0.3.1) (2016-10-30)
[Full Changelog](https://github.com/walfie/gbf-raidfinder/compare/v0.3.0...v0.3.1)

**Fixed bugs:**

- Backfilled raid tweets are sometimes missing on startup [\#93](https://github.com/walfie/gbf-raidfinder/issues/93)
- Missing Kirin in raid list? [\#85](https://github.com/walfie/gbf-raidfinder/issues/85)
- Mojibake in bosses.json API endpoint [\#82](https://github.com/walfie/gbf-raidfinder/issues/82)

**Merged pull requests:**

- Fix missing backfill tweets [\#92](https://github.com/walfie/gbf-raidfinder/pull/92) ([walfie](https://github.com/walfie))
- Cache protobuf objects in observable instead of domain objects [\#91](https://github.com/walfie/gbf-raidfinder/pull/91) ([walfie](https://github.com/walfie))
- Keep singleton value for KeepAliveResponse [\#90](https://github.com/walfie/gbf-raidfinder/pull/90) ([walfie](https://github.com/walfie))
- Switch from Heroku Redis to Redis Cloud [\#89](https://github.com/walfie/gbf-raidfinder/pull/89) ([walfie](https://github.com/walfie))
- Limit number of search API calls on startup [\#88](https://github.com/walfie/gbf-raidfinder/pull/88) ([walfie](https://github.com/walfie))
- Update Akka, Monix, Play to latest versions [\#86](https://github.com/walfie/gbf-raidfinder/pull/86) ([walfie](https://github.com/walfie))
- Add charset to JSON content type [\#84](https://github.com/walfie/gbf-raidfinder/pull/84) ([walfie](https://github.com/walfie))
- Revert "Set `-Dfile.encoding=UTF-8` in Heroku settings" [\#83](https://github.com/walfie/gbf-raidfinder/pull/83) ([walfie](https://github.com/walfie))
- Set `-Dfile.encoding=UTF-8` in Heroku settings [\#81](https://github.com/walfie/gbf-raidfinder/pull/81) ([walfie](https://github.com/walfie))

## [v0.3.0](https://github.com/walfie/gbf-raidfinder/tree/v0.3.0) (2016-10-16)
[Full Changelog](https://github.com/walfie/gbf-raidfinder/compare/v0.2.1...v0.3.0)

**Implemented enhancements:**

- Internal metrics on number of active users [\#76](https://github.com/walfie/gbf-raidfinder/issues/76)
- API endpoint for boss list [\#72](https://github.com/walfie/gbf-raidfinder/issues/72)
- Disable notifications for backfill tweets [\#71](https://github.com/walfie/gbf-raidfinder/issues/71)
- Notification sound [\#62](https://github.com/walfie/gbf-raidfinder/issues/62)
- Add custom favicon [\#34](https://github.com/walfie/gbf-raidfinder/issues/34)

**Merged pull requests:**

- Add changelog link to settings menu footer [\#80](https://github.com/walfie/gbf-raidfinder/pull/80) ([walfie](https://github.com/walfie))
- Enable CORS, add name param to bosses API [\#79](https://github.com/walfie/gbf-raidfinder/pull/79) ([walfie](https://github.com/walfie))
- Reduce startup time by several seconds [\#78](https://github.com/walfie/gbf-raidfinder/pull/78) ([walfie](https://github.com/walfie))
- Add metric for number of active users [\#77](https://github.com/walfie/gbf-raidfinder/pull/77) ([walfie](https://github.com/walfie))
- Add `/api/bosses.json` endpoint [\#75](https://github.com/walfie/gbf-raidfinder/pull/75) ([walfie](https://github.com/walfie))
- Add favicon [\#74](https://github.com/walfie/gbf-raidfinder/pull/74) ([walfie](https://github.com/walfie))
- Disable notifications for startup backfill tweets [\#73](https://github.com/walfie/gbf-raidfinder/pull/73) ([walfie](https://github.com/walfie))
- Add audio notifications [\#70](https://github.com/walfie/gbf-raidfinder/pull/70) ([walfie](https://github.com/walfie))

## [v0.2.1](https://github.com/walfie/gbf-raidfinder/tree/v0.2.1) (2016-09-23)
[Full Changelog](https://github.com/walfie/gbf-raidfinder/compare/v0.2.0...v0.2.1)

**Fixed bugs:**

- New guild wars bosses are not automatically translated [\#66](https://github.com/walfie/gbf-raidfinder/issues/66)

**Merged pull requests:**

- Use large image for image hashes [\#67](https://github.com/walfie/gbf-raidfinder/pull/67) ([walfie](https://github.com/walfie))

## [v0.2.0](https://github.com/walfie/gbf-raidfinder/tree/v0.2.0) (2016-09-21)
[Full Changelog](https://github.com/walfie/gbf-raidfinder/compare/v0.1.6...v0.2.0)

**Implemented enhancements:**

- Japanese/English boss name synonyms [\#49](https://github.com/walfie/gbf-raidfinder/issues/49)
- Notify client when server is updated [\#39](https://github.com/walfie/gbf-raidfinder/issues/39)

**Fixed bugs:**

- Sometimes list items are re-rendered unnecessarily [\#55](https://github.com/walfie/gbf-raidfinder/issues/55)
- Sometimes bosses aren't loaded on startup [\#45](https://github.com/walfie/gbf-raidfinder/issues/45)

**Merged pull requests:**

- Auto-translate bosses based on image similarity [\#65](https://github.com/walfie/gbf-raidfinder/pull/65) ([walfie](https://github.com/walfie))

## [v0.1.6](https://github.com/walfie/gbf-raidfinder/tree/v0.1.6) (2016-09-17)
[Full Changelog](https://github.com/walfie/gbf-raidfinder/compare/v0.1.5...v0.1.6)

**Implemented enhancements:**

- Compact mode [\#54](https://github.com/walfie/gbf-raidfinder/issues/54)

**Fixed bugs:**

- English raids only show up if they also have the Japanese name [\#59](https://github.com/walfie/gbf-raidfinder/issues/59)

**Merged pull requests:**

- Reduce minimum column width to 240px [\#61](https://github.com/walfie/gbf-raidfinder/pull/61) ([walfie](https://github.com/walfie))
- Fix English tweets being excluded from stream [\#60](https://github.com/walfie/gbf-raidfinder/pull/60) ([walfie](https://github.com/walfie))
- Allow configurable column width [\#58](https://github.com/walfie/gbf-raidfinder/pull/58) ([walfie](https://github.com/walfie))

## [v0.1.5](https://github.com/walfie/gbf-raidfinder/tree/v0.1.5) (2016-09-16)
[Full Changelog](https://github.com/walfie/gbf-raidfinder/compare/v0.1.4...v0.1.5)

**Merged pull requests:**

- Handle newlines in raid text message [\#57](https://github.com/walfie/gbf-raidfinder/pull/57) ([walfie](https://github.com/walfie))

## [v0.1.4](https://github.com/walfie/gbf-raidfinder/tree/v0.1.4) (2016-09-16)
[Full Changelog](https://github.com/walfie/gbf-raidfinder/compare/v0.1.3...v0.1.4)

**Implemented enhancements:**

- Don't remove high level bosses [\#46](https://github.com/walfie/gbf-raidfinder/issues/46)

**Merged pull requests:**

- Don't remove old bosses in frontend [\#56](https://github.com/walfie/gbf-raidfinder/pull/56) ([walfie](https://github.com/walfie))

## [v0.1.3](https://github.com/walfie/gbf-raidfinder/tree/v0.1.3) (2016-09-14)
[Full Changelog](https://github.com/walfie/gbf-raidfinder/compare/v0.1.2...v0.1.3)

**Implemented enhancements:**

- Add desktop notifications [\#52](https://github.com/walfie/gbf-raidfinder/issues/52)

**Merged pull requests:**

- Add desktop notifications [\#53](https://github.com/walfie/gbf-raidfinder/pull/53) ([walfie](https://github.com/walfie))
- Fix background color of boss selector [\#51](https://github.com/walfie/gbf-raidfinder/pull/51) ([walfie](https://github.com/walfie))

## [v0.1.2](https://github.com/walfie/gbf-raidfinder/tree/v0.1.2) (2016-09-13)
[Full Changelog](https://github.com/walfie/gbf-raidfinder/compare/v0.1.1...v0.1.2)

**Implemented enhancements:**

- Night mode [\#44](https://github.com/walfie/gbf-raidfinder/issues/44)

**Merged pull requests:**

- Add night mode toggle in settings [\#50](https://github.com/walfie/gbf-raidfinder/pull/50) ([walfie](https://github.com/walfie))
- Don't remove high level bosses [\#48](https://github.com/walfie/gbf-raidfinder/pull/48) ([walfie](https://github.com/walfie))

## [v0.1.1](https://github.com/walfie/gbf-raidfinder/tree/v0.1.1) (2016-09-11)
[Full Changelog](https://github.com/walfie/gbf-raidfinder/compare/v0.1.0...v0.1.1)

**Fixed bugs:**

- Highlighted text gets deselected on clipboard copy [\#42](https://github.com/walfie/gbf-raidfinder/issues/42)

**Merged pull requests:**

- Preserve selection after clipboard copy [\#43](https://github.com/walfie/gbf-raidfinder/pull/43) ([walfie](https://github.com/walfie))

## [v0.1.0](https://github.com/walfie/gbf-raidfinder/tree/v0.1.0) (2016-09-11)
[Full Changelog](https://github.com/walfie/gbf-raidfinder/compare/v0.0.3...v0.1.0)

**Implemented enhancements:**

- Preserve bosses between server restarts [\#37](https://github.com/walfie/gbf-raidfinder/issues/37)
- More efficient boss list refresh [\#35](https://github.com/walfie/gbf-raidfinder/issues/35)
- Include English raid tweets [\#3](https://github.com/walfie/gbf-raidfinder/issues/3)

**Fixed bugs:**

- Application stops immediately in prod mode [\#32](https://github.com/walfie/gbf-raidfinder/issues/32)
- Dialog element doesn't work in Firefox [\#20](https://github.com/walfie/gbf-raidfinder/issues/20)

**Merged pull requests:**

- Show project version in settings menu [\#41](https://github.com/walfie/gbf-raidfinder/pull/41) ([walfie](https://github.com/walfie))
- Periodically store known bosses in Redis [\#40](https://github.com/walfie/gbf-raidfinder/pull/40) ([walfie](https://github.com/walfie))
- Fix bug where boss info was sent for every tweet [\#38](https://github.com/walfie/gbf-raidfinder/pull/38) ([walfie](https://github.com/walfie))
- Push new bosses to clients [\#36](https://github.com/walfie/gbf-raidfinder/pull/36) ([walfie](https://github.com/walfie))
- Keep dialog menu open after clicking boss [\#33](https://github.com/walfie/gbf-raidfinder/pull/33) ([walfie](https://github.com/walfie))
- Upgrade akka to 2.4.10 [\#31](https://github.com/walfie/gbf-raidfinder/pull/31) ([walfie](https://github.com/walfie))
- Production enhancements [\#30](https://github.com/walfie/gbf-raidfinder/pull/30) ([walfie](https://github.com/walfie))
- Enable gzip compression on server [\#28](https://github.com/walfie/gbf-raidfinder/pull/28) ([walfie](https://github.com/walfie))
- Deploy to Heroku [\#27](https://github.com/walfie/gbf-raidfinder/pull/27) ([walfie](https://github.com/walfie))
- Update monix to 2.0.0 [\#26](https://github.com/walfie/gbf-raidfinder/pull/26) ([walfie](https://github.com/walfie))
- Get info about followed bosses on reconnect [\#25](https://github.com/walfie/gbf-raidfinder/pull/25) ([walfie](https://github.com/walfie))
- Reconnect on websocket close [\#24](https://github.com/walfie/gbf-raidfinder/pull/24) ([walfie](https://github.com/walfie))
- Add settings menu [\#23](https://github.com/walfie/gbf-raidfinder/pull/23) ([walfie](https://github.com/walfie))
- Rename core to stream [\#22](https://github.com/walfie/gbf-raidfinder/pull/22) ([walfie](https://github.com/walfie))
- Fix Firefox styling issues [\#21](https://github.com/walfie/gbf-raidfinder/pull/21) ([walfie](https://github.com/walfie))
- Support English tweets [\#18](https://github.com/walfie/gbf-raidfinder/pull/18) ([walfie](https://github.com/walfie))
- Remove old bosses from boss list [\#17](https://github.com/walfie/gbf-raidfinder/pull/17) ([walfie](https://github.com/walfie))
- Extract boss level from tweet [\#16](https://github.com/walfie/gbf-raidfinder/pull/16) ([walfie](https://github.com/walfie))
- Add indicators next to followed bosses [\#15](https://github.com/walfie/gbf-raidfinder/pull/15) ([walfie](https://github.com/walfie))
- Rename subscribe to follow [\#14](https://github.com/walfie/gbf-raidfinder/pull/14) ([walfie](https://github.com/walfie))
- Client update [\#13](https://github.com/walfie/gbf-raidfinder/pull/13) ([walfie](https://github.com/walfie))
- Update dependency versions, try sbt-coursier [\#12](https://github.com/walfie/gbf-raidfinder/pull/12) ([walfie](https://github.com/walfie))
- Initial client implementation [\#11](https://github.com/walfie/gbf-raidfinder/pull/11) ([walfie](https://github.com/walfie))
- Rename com.github.walfie.granblue -\> walfie.gbf [\#10](https://github.com/walfie/gbf-raidfinder/pull/10) ([walfie](https://github.com/walfie))
- Update monix-reactive to 2.0-RC11 [\#9](https://github.com/walfie/gbf-raidfinder/pull/9) ([walfie](https://github.com/walfie))
- Replace JSON formats with Protobuf [\#8](https://github.com/walfie/gbf-raidfinder/pull/8) ([walfie](https://github.com/walfie))
- Move .circle.yml to circle.yml [\#7](https://github.com/walfie/gbf-raidfinder/pull/7) ([walfie](https://github.com/walfie))
- Add HTTP server with simple frontend [\#6](https://github.com/walfie/gbf-raidfinder/pull/6) ([walfie](https://github.com/walfie))

## [v0.0.3](https://github.com/walfie/gbf-raidfinder/tree/v0.0.3) (2016-08-16)
[Full Changelog](https://github.com/walfie/gbf-raidfinder/compare/v0.0.2...v0.0.3)

**Merged pull requests:**

- Rewrite using monix-reactive [\#2](https://github.com/walfie/gbf-raidfinder/pull/2) ([walfie](https://github.com/walfie))

## [v0.0.2](https://github.com/walfie/gbf-raidfinder/tree/v0.0.2) (2016-08-13)
[Full Changelog](https://github.com/walfie/gbf-raidfinder/compare/v0.0.1...v0.0.2)

**Merged pull requests:**

- Rewrite using akka-stream [\#1](https://github.com/walfie/gbf-raidfinder/pull/1) ([walfie](https://github.com/walfie))

## [v0.0.1](https://github.com/walfie/gbf-raidfinder/tree/v0.0.1) (2016-08-09)


\* *This Change Log was automatically generated by [github_changelog_generator](https://github.com/skywinder/Github-Changelog-Generator)*