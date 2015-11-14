# hexlib

A Clojure hex grid resource library.

This library came from constructing a solution for the
[2015 ICFP contest's](http://2015.icfpcontest.org/) hexagonal tetris problem.

The inspiration for much of this library came from
http://www.redblobgames.com/grids/hexagons/.

## Leiningen

[![Clojars Project](http://clojars.org/hexlib/latest-version.svg)](http://clojars.org/hexlib)

## Usage

~~~clojure
  ; <hex> :- [<col: int> <row: int>]
    (hexlib.core/odd-r->cube <hex>) ;; returns a [<x: int> <y: int> <z: int>]
  ; <problem> :- {defined in the ICFP problem statement}
    (hexlib.loader/load-boards <problem>) ;; returns a [<board>...]
  ; <command> :- `:w` | `:e` | `:se` | `:sw` | `:ccw` | `:cw` 
    (hexlib.tetris/board-transition <board> <command>)
    (hexlib.tetris/score-game <board>)
~~~

## License

Copyright © 2015 Andrew Roetker

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
