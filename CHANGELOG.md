# Change Log

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

- Don't remove high level bosses [\#46](https://github.com/walfie/gbf-raidfinder/issues/46)
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

**Closed issues:**

- Handle case where \>100 tweets are made per tick [\#5](https://github.com/walfie/gbf-raidfinder/issues/5)
- Handle rate limiting [\#4](https://github.com/walfie/gbf-raidfinder/issues/4)

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