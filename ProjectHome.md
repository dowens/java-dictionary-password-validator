Dictionary Password Validator is a Java-based utility to validate that a password does or does not contain common dictionary words within it.  It is built to be plug-and-play.

It uses a [Bloom filter](http://en.wikipedia.org/wiki/Bloom_filter) for fast dictionary look ups.  The default provided list of 46,210 US English words initializes within 0.84 seconds using Java 6, has a low false positive percentage of 0.05%, and uses very little memory (767kb).  If you further increase the accuracy of the Bloom filter, you can decrease the likelihood of the false positives (however the default settings are excellent for the list of provided words).  Doing so will increase the memory footprint (since it increases the bit field), and also further adds to the amount of time it takes for the filter to initialize.

Aside from that, you can further add more dictionary words by modifying the already supplied list.  Some additional word lists can be found at: http://wordlist.sourceforge.net

I've tested a 460,000+ word dictionary with an accuracy of 14 (0.17% false positive) and it only took 7 seconds to initialize.  The beauty of the Bloom filter is the look up efficiency is O(1) -- extremely efficient.

Anyway, feel free to use this in your applications.  Out of the box it should work perfect, but there's plenty of room for you to customize it.  In the future I might also implement a Trie, for those that cannot have any false positives (even if the likelihood of it having a false positive is really, really low).


Configurable options include:
  * Dictionary words
  * Bloom filter accuracy (bit set size)
  * Minimum length word size (4 is the default)