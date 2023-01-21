jHunspell
=============
[![Project Status: Active - The project has reached a stable, usable state and is being actively developed.](http://www.repostatus.org/badges/latest/active.svg)](http://www.repostatus.org/#active)
[![Build Status](https://github.com/nikialeksey/jhunspell/actions/workflows/ci.yml/badge.svg)](https://travis-ci.org/nikialeksey/jhunspell)

[![Lib version][lib-version-badge]][lib-version-link]

This library provides an API to interface with Hunspell using JNA.

Free software licensed under GPL/LGPL/MPL tri-license, same as Hunspell itself.

Usage
-----

Use of this library is very simple.  First instantiate the `Hunspell` object.

```java
Hunspell speller = new Hunspell("/path/to/dictionary.dic", "/path/to/affix.aff");
```

In order to check whether a word is correctly spelled, use the `#spell(String)` function.

```java
String wordToCheck = ... // the word that you want to check
if ( speller.spell(word) ) {
  // word is spelled correctly
} else {
  // word is misspelled
}
```

If the word is spelled incorrectly, you will probably want to give the users some possible corrections for the word.  In order to do so, use the `#suggest(String)` method.

```java
String misspelledWord = ... // the word that you want suggestions for
List<String> suggestions = speller.suggest(misspelledWord);
```

If you maintain a user dictionary, you can add the words to Hunspell's runtime dictionary (not the dictionary file itself) using the `#add(String)` function.

```java
String userWord = ... // word that isn't in the dictionary
speller.spell(userWord); // returns false
speller.add(userWord);
speller.spell(userWord); // returns true
```

A more advanced feature of Hunspell is that it allows you to add a word using another example word to define the affix flags that should apply to the word.  As an example:

```java
String userWord = ... // word that isn't in the dictionary, but is the same as "monkey"
speller.spell(userWord); // returns false
speller.addWithAffix(userWord, "monkey");
speller.spell(userWord); // all the following return true
speller.spell(userWord+"'s");
speller.spell(userWord+"s");
speller.spell(userWord+"ed");
speller.spell(userWord+"ing");
```

Note that this example is based on the dictionary used for the tests, if you use different dictionaries, the words that are added may not be the same.

If for whatever reason you need to remove the words that the user added, you can do so using the `#remove(String)` function.

```java
String userWord = ... // word that has previously been added with #add or #addWithAffix
speller.spell(userWord); // returns true
speller.remove(userWord);
speller.spell(userWord); // returns false
```

Note that if the word was added with `#addWithAffix(String,String)`, all affixed forms of the word *will* be removed as well.

Supported Architectures
-----------------------

* Linux x86
* Linux x86_64
* Windows x86
* Windows x64
* Mac OS X x86
* Mac OS X x86_64
* Mac OS AARCH64

Maven
-----
This project is available at Maven Central with the following dependency:
```xml
<dependency>
    <groupId>com.nikialeksey</groupId>
    <artifactId>jhunspell</artifactId>
    <version><!-- library version --></version>
</dependency>
```

Changelog
---------
## 1.0.5
- Add macos aarch64 support
- Use JNA instead of BridJ

## 1.0.2

- Update Hunspell libraries to 1.3.4
- Support alternate dictionary encodings
- Add support for `Hunspell_add_dic`

## 1.0.1

- Add support for Mac OS X

## 1.0.0

- Initial release

[lib-version-badge]: https://img.shields.io/maven-central/v/com.nikialeksey/jhunspell.svg?label=maven
[lib-version-link]: https://maven-badges.herokuapp.com/maven-central/com.nikialeksey/jhunspell
