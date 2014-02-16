HunspellBridJ
=============
[![](http://stillmaintained.com/thomas-joiner/HunspellBridJ.png)](http://stillmaintained.com/thomas-joiner/HunspellBridJ)

[![Build Status](https://travis-ci.org/thomas-joiner/HunspellBridJ.png)](https://travis-ci.org/thomas-joiner/HunspellBridJ)

This library provides an API to interface with Hunspell using BridJ.

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

Check the [Javadocs](http://thomas-joiner.github.com/HunspellBridJ/1.0.0-SNAPSHOT/apidocs) for further information.

Supported Architectures
-----------------------

* Linux x86
* Linux x86_64
* Windows x86
* Windows x64
* Mac OS X x86
* Mac OS X x86_64
