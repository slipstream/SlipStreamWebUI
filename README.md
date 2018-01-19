# SlipStream Web UI

An application that provides a graphical interface to cloud management
services that use the CIMI interface.  The tortured acronym comes from
"Cimi resoUrces via a Browser InterfaCe".

## Frameworks and Libraries

 - [re-frame](https://github.com/Day8/re-frame)

## Development Mode

### Run application:

```
lein clean
lein figwheel dev
```

Figwheel will automatically push cljs changes to the browser.

Wait a bit, then browse to [http://localhost:3449](http://localhost:3449).

### Run tests:

```
lein clean
lein doo phantom test once
```

The above command assumes that you have
[phantomjs](https://www.npmjs.com/package/phantomjs)
installed. However, please note that
[doo](https://github.com/bensu/doo) can be configured to run cljs.test
in many other JS environments (chrome, ie, safari, opera, slimer,
node, rhino, or nashorn).

## Production Build

To compile clojurescript to javascript:

```
lein clean
lein cljsbuild once min
```

## Legal

Unless otherwise indicated in the source files, all code is subject to
the following copyright and license.

Copyright 2017 Charles A. Loomis, Jr.

Licensed under the Apache License, Version 2.0 (the "License"); you
may not use this file except in compliance with the License.  You may
obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
implied.  See the License for the specific language governing
permissions and limitations under the License.
