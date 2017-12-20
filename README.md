# Web UI

## Purpose

This repository contains a simple web browser UI for SlipStream, using
ClojureScript and related technologies.  It is an early **prototype**
but feedback (as GitHub issues) on the UI, source code, and underlying
technologies is welcome.

It also contains, **reagent components** which are integrated to the
**SlipStream UI**. The purpose of those components are to migrate to
re-frame framework in nearly futur.

At the moment, it is not packaged and can only be run from a 
development environment.  See the instructions below for starting up
the web UI.

## Useful Tools (and Commentary)

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

 * [React-Bootstrap](https://react-bootstrap.github.io): React
   bindings for Bootstrap widgets. 
 * [re-com](https://github.com/Day8/re-com): Pure ClojureScript
   widgets for web applications.  Comes with warning about portability
   between browsers.
 * [Material UI](http://www.material-ui.com/): Implementation of
   Google's Material Design specification.
 * [Semantic UI](https://react.semantic-ui.com/introduction):
   React integration for Semantic UI.

Helpful Information:

 * [Modern
   ClojureScript](https://github.com/magomimmo/modern-cljs/tree/master/doc/second-edition)
 * [Reagent Introduction](https://reagent-project.github.io)
 * [Bootstrap with
   Reagent](http://nicolovaligi.com/boostrap-components-reagent-clojurescript.html) 

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

#### Development
The development environment requires [`lein`](https://leiningen.org).

Once `lein` is installed, you can setup the interactive environment by
doing the following:

 * In a terminal, start development server for the webui.
  
     ```
     $ lein dev-webui
     ```
 * You will get automatically a REPL, with Fighweel controls:
 
     ```
     dev-webui:cljs.user=>
     ```

 * A new page in your browser should automatically be open and point to:
   [http://localhost:3000/webui/index.html](http://localhost:3000/webui/index.html).

You should see the client application running.  Any changes you make
to the source files (either ClojureScript sources or HTML templates)
should be reflected immediately in the browser.

* To get a dev environment on SlipStream UI components.

     ```
     $ lein dev-legacy
     ```

#### Testing

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
