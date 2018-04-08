# SlipStream Web UI

An application that provides a graphical interface to cloud management
services that use the CIMI interface.  The tortured acronym comes from
"Cimi resoUrces via a Browser InterfaCe". It is an early **prototype**
but feedback (as GitHub issues) on the UI, source code, and underlying
technologies is welcome.

## Frameworks and Libraries

SlipStream Code:

 * [SlipStream Clojure(Script)
   API](https://github.com/slipstream/SlipStreamClojureAPI): Provides
   a ClojureScript API for the CIMI and CIMI-like resources within
   SlipStream.

Frameworks:

 * [Reagent](https://github.com/reagent-project/reagent): Provides a
   ClojureScript interface to the
   [React](https://facebook.github.io/react/) framework.
 * [re-frame](https://github.com/Day8/re-frame): A framework that
   relies on React for visualization and provides mechanisms for using
   "FRP" for data flow and control.

Widgets:

 * [Semantic UI](https://react.semantic-ui.com/introduction):
   React integration for Semantic UI.


## Development Environment

The essentials for using the development environment are below.

### Browser

To test the code on a SlipStream server (e.g. https://nuv.la/) running
on a different machine, you'll need to start a browser with the XSS
protections disabled.  For Chrome on macOS, this can be done with:

```
$ open /Applications/Google\ Chrome.app \
       --args --disable-web-security --user-data-dir
```

### Development
The development environment requires [`lein`](https://leiningen.org).

Once `lein` is installed, you can setup the interactive environment by
doing the following:

 * In a terminal, start development server for the webui.

     ```
     $ lein dev
     ```
 * You will get automatically a REPL, with Fighweel controls:

     ```
     dev:cljs.user=>
     ```

 * Wait a bit, then browse to
 [http://localhost:3000/webui.html](http://localhost:3000/webui.html).


You should see the client application running.  Any changes you make
to the source files (either ClojureScript sources or HTML templates)
should be reflected immediately in the browser.

### Testing

* In a terminal, start tests for the webui.

     ```
     $ lein test
     ```

* You can also get automatic tests re-execution triggered on your code
  change with following command :

     ```
     $ lein test-auto
     ```

## Integration with IntelliJ

You can import the repository through IntelliJ, using the "leiningen"
method in the dialog.

### Logging

You can reset the logging level for kvlt from the REPL when running
in development mode. From the REPL do:

```
=> (require '[taoensso.timbre :as timbre])
=> (timbre/set-level! :info)
```

The default value is `:debug` which will log all of the HTTP requests
and responses.  This is useful when debugging interactions with
SlipStream, but annoying otherwise.

## Electron

The SlipStream WebUI can be run as an
[electron](https://electronjs.org/) application.  This support is
experimental and feedback is welcome. It has only been tested on Mac
OS and there are numerous things that do not work correctly, notably
those related to links between pages.

To compile the SlipStream WebUI electron application, you must have
[`nodejs`](https://nodejs.org/en/) and [`npm`](https://www.npmjs.com/)
installed on your machine. On Mac OS, these can be installed with
[`homebrew`](https://brew.sh/).

At the root of this repository, run the command:

```sh
$ npm install \
    electron \
    electron-packager \
    electron-installer-dmg
```

This will install both electron and the electron-packager within the
`node_modules` subdirectory.  Update your path with the following:

```
$ export PATH=$PATH:`pwd`/node_modules/.bin
```

Both `electron` and `electron-packager` should be in your PATH.

To build and run the electron application from the command line,
run the following commands:

```
$ lein electron
$ electron .
```

This should compile and then start the SlipStream WebUI as an electron
application.

To package the application,

```
$ electron-packager . CUBIC \
    --platform=darwin \
    --arch=x64 \
    --electron-version=1.8.4 \
    --overwrite \
    --out target
```

You can then run this application with `open
target/CUBIC-darwin-x64/CUBIC.app`.

This can be packaged as a DMG file with the following:

```
$ electron-installer-dmg \
    target/CUBIC-darwin-x64/CUBIC.app \
    CUBIC \
    --out target
```

This will generate the file `CUBIC.dmg` which can then be used to
install the application as usual on Mac OS.



