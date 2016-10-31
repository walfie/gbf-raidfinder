# Implementation Details

## Raid boss discovery

gbf-raidfinder uses Twitter's streaming API to find raid tweets. However,
boss names aren't hardcoded into the program -- instead, they're
automatically discovered when users tweet about them.

For Japanese raid tweets, the gbf-raidfinder backend uses the search terms
`Lv20,Lv25,Lv30,...,Lv150` and parses the tweets to get the boss name,
raid ID, and other tweet information (boss image, user details, additional
text). Tweets that don't parse or aren't tweeted from the official in-game
Granblue twitter client are discarded. Tweets are then grouped by boss
name. When a new boss name is found, it gets added to the list of known
bosses.

Note that we can't use `参加者募集！` -- which is in all Japanese raid
tweets -- as a search term to find all raid tweets, since the Twitter
streaming API has a limitation where CJK (Chinese, Japanese, Korean)
tweets can only match on whitespace-separated words. Unfortunately,
Japanese raid tweets don't have spaces in convenient spots.

However, since English doesn't have this limitation, to find all English
raid tweets, we just search for `I need backup!`.

## Automatic translations

gbf-raidfinder is able to automatically translate raid boss names between
English and Japanese by using image similarity.

Each raid tweet contains an image of the raid boss. The English and
Japanese versions of the raid boss image are almost exactly the same,
except for the boss name in the bottom right corner.

When a new raid boss is found, we take a [perceptual
hash](https://en.wikipedia.org/wiki/Perceptual_hashing) of the topmost 75%
of the image (to ignore the differences in the bottom right of the image).
If a boss has the same perceptual hash as another known boss, and they are
the same level and opposite languages, then they are determined to be the
same boss.

This has worked perfectly with one exception: Lvl 120 Medusa has different
Japanese and English images. There is a manual override for this boss.

![English](https://pbs.twimg.com/media/CfqXEh_UsAEb9dw.jpg:small)
![Japanese](https://pbs.twimg.com/media/CfqZlIcVIAAp8e_.jpg:small)

Not all bosses have images -- new event bosses might not have an
associated image until some time after the event has started -- so these
bosses remain untranslated until an image is available.


