(ns sixsq.slipstream.webui.debug
  "Initializes the debugging/logging tools and permits a third-party
   SlipStream service to be specified.

   The tools can be turned on/off via a Google Closure parameter. You can do
   this with:

       {:compiler-options {:closure-defines {'sixsq.slipstream.webui.debug/DEV false}}

   setting the appropriate boolean value.

   The logging level will default to 'WARN'. To change this, use the following:

       {:compiler-options {:closure-defines {'sixsq.slipstream.webui.debug/LOGGING_LEVEL \"DEBUG\"}}

   All of the usual debugging levels are permitted: DEBUG, INFO, WARN, and ERROR.

   For debugging purposes, the URL for the SlipStream server can be set
   explicitly. To do this, set the HOST_URL parameter like so:

       {:compiler-options {:closure-defines {'sixsq.slipstream.webui.debug/SLIPSTREAM_URL \"https://nuv.la\"}}

   NOTE: When using an endpoint other than the one serving the javascript code
   you MUST turn off the XSS protections of the browser."
  (:require
    [devtools.core :as devtools]
    [taoensso.timbre :as timbre]
    [clojure.string :as str]
    [sixsq.slipstream.webui.utils :as utils]))

(goog-define DEV false)

(defn initialize-debugging-tools []
  (when (identical? DEV true)
    (devtools/install!)
    (enable-console-print!)))


(goog-define LOGGING_LEVEL "WARN")

(defn initialize-logging-level []
  (let [level (keyword (str/lower-case LOGGING_LEVEL))]
    (timbre/set-level! level)))


(goog-define SLIPSTREAM_URL "")

(def slipstream-url (delay (if-not (str/blank? SLIPSTREAM_URL) SLIPSTREAM_URL (utils/host-url))))
