# Reagent Evaluation

## Purpose

This repository contains a simple web browser UI for SlipStream, using
ClojureScript and related technologies. 

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
 * [React Toolbox](http://react-toolbox.com/): Implementation of
   Google's Material Design specification.
 * [Material UI](http://www.material-ui.com/): Implementation of
   Google's Material Design specification.  

Helpful Information:

 * [Modern
   ClojureScript](https://github.com/magomimmo/modern-cljs/tree/master/doc/second-edition)
 * [Reagent Introduction](https://reagent-project.github.io)
 * [Bootstrap with
   Reagent](http://nicolovaligi.com/boostrap-components-reagent-clojurescript.html) 

## Development Environment

The organization of the repository comes from the [tenzing application
template](https://github.com/martinklepsch/tenzing).  It uses
[boot](https://github.com/boot-clj/boot) to create an interactive
development environment.  It also provides tasks for running unit
tests and for creating the final artifacts for deployment.

The essentials for using the development environment are below.  See
the tenzing project page for more detailed instructions.

### Browser

To test the code on a SlipStream server (e.g. https://nuv.la/) running
on a different machine, you'll need to start a browser with the XSS
protections disabled.  For Chrome on macOS, this can be done with:

```
$ open /Applications/Google\ Chrome.app \
       --args --disable-web-security --user-data-dir
```

### Boot

The development environment requires [`boot`](http://boot-clj.com).

Once `boot` is installed, you can setup the interactive environment by
doing the following:

 * In a terminal, start development server `boot dev`.
  
     ```
     $ boot dev
     ```
 * In another terminal, start the REPL:
 
     ```
     $ boot repl -c
     boot.user=> (start-repl)
     ...
     cljs.user=> 
     ```

 * Point your browser to
   [http://localhost:3000/](http://localhost:3000). 

You should see the client application running.  Any changes you make
to the source files (either ClojureScript sources or HTML templates)
should be reflected immediately in the browser.

## Integration with IntelliJ

For IntelliJ, the easiest integration is to generate a `project.clj`
file from the `build.boot` file.  This can be done by creating a
function in your boot profile.  There are
[instructions](https://github.com/boot-clj/boot/wiki/For-Cursive-Users)
on the boot wiki.

After you've done this you can just run:

```
$ boot lein-generate
```

to create the `project.clj` file.  You can then import the repository
through IntelliJ, using the "leiningen" method in the dialog.

To integrate the boot command line into the interface, configure the
commands as "external tools".  Use the following:

 * Program: boot
 * Parameters: dev
 * Working directory: $ProjectFileDir$

You can create one for the "dev" task and another for "repl -c".  They
can be started via the "Tools -> External Tools" menu.  When started,
these will show up in the console section of the interface and behave
just like a terminal.

You must still use an external browser to connect to the development
environment.  Chrome is recommended.  Be sure to turn off the security
settings if you're using a SlipStream service running on a different
machine.
