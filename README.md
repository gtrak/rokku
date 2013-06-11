# Rokku

A little clojure roku client library using UPnP to identity the device, and the REST API

## Usage

button-press functionality, valid keys include (:backspace :info :search :home :play :back :fwd :enter :right :left :instant-replay :rev :down :up :select):

    (request :play)


on-screen keyboard functionality:

    (letters "type something")

## License

Copyright © 2013 Gary Trakhman

Distributed under the Eclipse Public License, the same as Clojure.
