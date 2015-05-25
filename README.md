##termpose
A markup language, and symbol tree representation format. Noise-free syntax and pure, flexible data models.

###It's arguably more regular than s-expressions
Parsed termpose resolves to a tree of terms and their associated sequences (EG `array(a b c d e)`, 'array' is the term, the letters are the terms in its sequence, and those letters sequences are empty). This convention eliminates the edge cases of termless empty lists and varying element types: every entity in the tree is a term with a string at its head which tells the parser how to take it, and a tail of similar entities pertaining to it. Symbols are not a separate type, but a component of the fundamental unit. In practice, these differences do not set termpose sublanguages apart from other languages, as, with the right domain-specific language, anything had by json or lisp or yaml can be easily replicated. Termpose mimicing yaml looks (marginally)better than yaml. Termpose mimicing JSON looks (unambiguously)better than json. Termpose mimicking xml is not even a contest. Termpose mimicking lisp.. well, if you want that I should refer you to http://srfi.schemers.org/srfi-110/srfi-110.html , but termpose would do a commendable job.


##Syntax

Termpose pays attention to indentation. Expressions like
```
a
	b
	c
		d
	e
```
will be equivalent to `a(b c(d) e)`.

Termpose tries to keep out of your namespace, attributing its own meanings to the characters `":()`, but leaving ```\/?-+=[]*&^%$#@!`~;'.,<>``` for your domain-specific languages to define as they please.

###Spec by example
```python
A B
#equivalent to A(B)
A B C
#equivalent to A(B C)
A
  B
  C
#equivalent
A(B C)
#equivalent
A(B C)
  D E
#A(B C D(E)).
A(B C) D
  E
  F
#A contains everything.
A B C D
  E
  F
#equivalent
A B C D:
  E
  F
#A(B C D(E F)). If the line sequence is terminated by a colon, the last element takes the indent sequence, rather than the first.
A B(C)
  D
#A(B(C) D)
A B(C
  D
#equivalent (crazy? Thing is: Since indentation is supported and suffient for expressing containments spread over multiple lines, there is no need to allow brackets to do this. So we don't. Stemming from that, since a bracket cannot possibly continue over multiple lines, we just close them automatically at the end of the line.)
A B:C D:E
#A contains B and D, but C and E are contained by their colon buddy, B and D respectively
A:B:C:D:E
#A(B(C(D(E))))
A(B(C(D(E
#equivalent, in this case
A "a string"
#A("a string"). (all symbols are just strings, but this one contains a space, so it needs quotes.)
A "a string
#equivalent. Closing quote isn't needed if the line ends.
A"a string
#equivalent
A "
  a string that
  is multiline
#A contains "a string that↩is multiline"
```
<!-- A B, C D, E F
#A(B C(D E(F))). Commas start a new term within the line. -->

##Surely as a burgeoning format the parser is woefully inefficient?
The community has lead me unto the impression that Jackson, the Json parser for Java is, like, *super fast*. I compared my early, unoptimized, Scala-based termpose parser with Jackson, and there is basically no discernable difference between their performance. Maybe I just have efficient coding habits. Maybe it's the noodley graphic way I architected the thing. Either way, the termpose parser is perfectly competitive already. On that dimension.


###You compare termpose to lisp. Show that it could genuinely serve as a substrate for a nice programming language.

Fine.

```python
var i 0
while <(i 5
  print 'Hello
  if ==(0 %(i 3
    then
      println "' worldly people
    else
      println "' anxious dog
  +=(i 1

>Hello anxious dog
>Hello worldly people
>Hello worldly people
>Hello anxious dog
>Hello worldly people
```

##Termpose is better for typing json than json

for
```javascript
{
  "highlight_line": true,
  "ignored_packages":
  [
    "Floobits",
    "SublimeLinter",
    "Vintage",
    {
      "ref":"http://enema.makopool.com/",
      "name":"enema",
      "update":"yes"
    },
    [1, 2, 3]
  ],
  "indent_guide_options":
  {
    "draw_active": true
  },
  "overlay scroll bars": "enabled",
  "show tab close buttons": false,
  "tab_size": 2,
  "theme": "Spacegray.sublime-theme",
  "word_wrap": "true"
}

```
```python
highlight_line true
ignored_packages [:
  Floobits
  SublimeLinter
  Vintage
  { ref:"http://enema.makopool.com/" name:enema update:yes
  [ 1 2 3
indent_guide_options
  draw_active true
"overlay scroll bars" enabled
"show tab close buttons" false
tab_size 2
theme Spacegray.sublime-theme
word_wrap true
```
I think that works out very well. `'` may be used an escape for instances like, say, representing the string `"true"`(parsed termpose has no record of whether there were double quotes, so that alone wouldn't do).


##How about XML?

Termpose → single line XML translation is partially implemented, see the function translateTermposeToSingleLineXML.

Here's how one would write the first half of my homepage in termpose's XML dialect:
```python
html
  head
    title ."about mako
    meta -charset:utf-8
    link -rel:"shortcut icon" -href:DGFavicon.png
    style ."
      CSS plaintext...
    script -language:javascript ."
      Javascript plaintext...
  body
    div -id:centerControlled
      table tbody:
        tr
          td
          td
            div -id:downgust
              canvas -height:43 -width:43
        tr
          td -class:leftCol ."who
          td -class:rightCol ."mako yass, global handle @makoConstruct.
        ...
```