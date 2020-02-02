Hi, [krisds](https://github.com/krisds) here.

What you have here is a dump, graciously shared by [Semmle/GitHub](https://github.com/Semmle), of my work on their (at time of writing now defunct) support for COBOL. This includes:

* the extractor, based on my [Koopa project](https://github.com/krisds/koopa)
* scripts to generate the database scheme and extraction logic from a [language spec](extractor/tools/spec.py)
* the library of [COBOL queries](queries/semmlecode-cobol-queries) which could be run on a COBOL project

As not all dependencies are included in this copy you won't be able to build or run it as-is. I'm hoping that at some point in the future enough will be opened up for the general public to write their own extractors, at which time this repo could kickstart support for COBOL.

I would like to thank Semmle and GitHub again for sharing this with me and allowing me to share it with you.
