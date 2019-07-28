# BrookTagger
A simple attempt at a part-of-speech tagger

## Description
This program tags words in a sentence with one of the 8 basic parts of speech: Nouns, Verbs, Adjectives, Adverbs, Conjunctions, Pronouns, Prepositions, and Determiners.  
This table can be used to determine how a word should be tagged based on how a more advanced tagger like NLTK would tag it:

Official POS|Simplified POS
-------------------|------------
JJ adjective |	Adjective
JJR adjective, comparative 
JJS adjective, superlative 
EX existential there |	Adverb
RB adverb
RBR adverb, comparative	
RBS adverb, superlative
RP particle
WRB wh-abverb
CC coordinating conjunction |	Conjunction
DT determiner |	Determiner
PDT predeterminer	
WDT wh-determiner	
CD cardinal digit |	Noun
NN noun, singular
NNS noun plural
NNP proper noun, singular	
NNPS proper noun, plural	
IN preposition/subordinating conjunction | Preposition
TO to go 'to' the store.	
PRP personal pronoun|	Pronoun
PRP$ possessive pronoun
WP wh-pronoun
WP$ possessive wh-pronoun
MD modal| Verb
VB verb, base form
VBD verb, past tense
VBG verb, gerund/present participle
VBN verb, past participle	
VBP verb, sing. present, non-3d
VBZ verb, 3rd person sing. present	
FW foreign word |	None
LS list marker	
POS possessive ending	
SYM symbols	
UH interjection	

## Using the program
This program takes sentences via stdin and outputs tagged words in the form of \<POS>\\\<Word>.
While testing, I used python to provide sentences to the tagger, with this code:  
`sp=subprocess.Popen(['java', '-jar', 'BrookTagger.jar'],stdin=PIPE,stdout=PIPE)`  
`taggedSentences=sp.communicate("\n".join(sentences).encode())[0].decode("utf-8").split("\r\n")`  
This opens the tagger, tags an array of strings (`sentences`), closes the tagger, and returns the tagged strings (`taggedSentences`)

## Testing
I used this dataset to test the tagger:
https://www.kaggle.com/stanfordu/stanford-natural-language-inference-corpus#snli_1.0_train.csv
Which resulted in a success rate of 87.76% (tagged words: 3,532,333 correct vs 492,874 incorrect).

## Licencing
I originally got thesaurus.dat from https://www.openoffice.org/lingucomponent/thesaurus.html, but it was labelled differently (MyThes-1.zip\\MyThes-1.0\\th_en_US_new.dat) and has been modified. It came with WordNet_license.txt and license.readme with it, so I guess they apply. I think license.readme refers to a program that is not included with this, but I'm not a lawyer so I put it in there anyway. Everything else (including thesaurus_ext.txt) was written by me and is free to use as per the unlicence.
