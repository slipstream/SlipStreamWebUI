/*
  If EventSource (Server Sent Event) is not present, load
  a polyfill implementation to provide it.  This is natively
  supported (and unneeded) on all major browsers except
  Microsoft Edge (of course).
*/
function polyfillEventSource() {
  if ('undefined' === typeof EventSource) {
    var script = document.createElement('script');
    script.type = 'text/javascript';
    script.src = '/webui/js/eventsource.min.js';
    script.async = false;

    var head = document.getElementsByTagName('head')[0];
    head.appendChild(script);

    console.log("EventSource: polyfill implementation");
  } else {
    console.log("EventSource: native implementation");
  }
}

polyfillEventSource();
