## Welcome to Phoenicia

[![Join the chat at https://gitter.im/XPRIZE/GLEXP-Team-The-Linguaculturalists](https://badges.gitter.im/XPRIZE/GLEXP-Team-The-Linguaculturalists.svg)](https://gitter.im/XPRIZE/GLEXP-Team-The-Linguaculturalists?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

Phoenicia is an Android game designed to teach literacy and numeracy to children in an unguided way.

<img src="screenshot.png" width="480"/>

## Gameplay

In Phoenicia, learning to read, write and do basic math is a consequence of playing the game, rather than the goal of it. For the player, the fun is in building up their virtual world, creating things in it, and selling those items for other items.

Children learn and retain information better when it is necessary part of the goal they are trying to reach. By making the learning aspect a requirement, and slowly adjusting the difficulty of that as the player progresses, the game remains entertaining and motivating while reinvorcing the knowledge that the player gains along the way.

#### Letter recognition
Players will start with a small farm or garden (depending on locale) where they will start raising crops of letters. Each letter will produce it's name and sound through various player interactions to build an association between the letter and the sound it represents.

#### Literacy

Once a few common consonants and vowels are produced, the player will be taught how to combine them to form short, familiar words. These newly formed words become assets that the player uses to progress through the game. Creating words consumes the letters used to form it, so the player much continuously raise and harvest (or purchase) new ones.

As the player progresses in the game, new letters will become available to them, and thus new words as well. They will be prompted to try and build specific words, and encouraged throughout the game to experiment with letter combinations on their own.

#### Numeracy

Progression through the game is driven by a marketplace where the player can sell their words and letters, earning in-game currency. This currency can then be used to purchase additional letters (raising them takes time, and not all letters can be raised as crops). During these exchanges the player will be encouraged to calculate small sums and differences themselves to speed up gameplay, with a fallback to counting up or down one item at a time.

#### Socializing

Even though Phoenicia is designed to be a single-player, offline game, the social aspect of learning has not been forgotten. Players will be encouraged to share the words they learn to spell with their friends, who can then create those words (if they have the letters) even if they haven't themselves progressed to the level where that word will be introduced. These words can then be sold in the marketplace for a higher in-game currency value than the words at the player's own level. This gives the **knowledge** of spelling a social and economic value, which in turn will encourage the children and their peers to obtain more of it.

#### Localization

Phoenicia is powered by "Locale Packs" which provide unique artwork, sounds, letter and word assets specific to a targeted community. In addition, each locale will have it's own specific progression of letters and words defined for that locale.

You can learn how to build your own Locale Pack by reading our [Localization](docs/Localization.md) guide.


## Building & Contributing

You should be able to clone Phoenicia's Git repository and open it in Android Studio. It is being developed on Ubuntu 16.04 and Android Studio 
1.1. [Dependencies](#dependencies) are included in the Git branch.

For full contributing requirements, please read our [Contributing](docs/Contributing.md) docs.

## Dependencies

In addition to Android, Phoenicia makes use of:

* AndEngine developed by Nicolas Gramlich
    * [GLES2-AnchorCenter](https://github.com/nicolasgramlich/AndEngine/tree/GLES2-AnchorCenter) branch
    * Included from app/src/main/libs/andengine.jar
* AndEngineTMXTiledMapExtension by Nicolas Gramlich 
    * [GLES2-AnchorCenter_isometric](https://github.com/Niffy/AndEngineTMXTiledMapExtension/tree/GLES2-AnchorCenter_isometric) branch by Paul Robinson
    * Inline in app/src/main/java/org/andengine/extension/tmx/
