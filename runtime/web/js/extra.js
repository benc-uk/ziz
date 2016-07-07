// This is the part where I set up the three adapters.
// Please choose the one you need and discard others.
// I did this because I observed that in some frameworks (especially ExtJS),
// using the standard DOM modifiers breaks up the framework's inner workings.
 
OFC = {}

OFC.none = {
    name: "pure DOM",
    version: function(src) { return document.getElementById(src).get_version() },
    rasterize: function (src, dst) {
      var _dst = document.getElementById(dst)
      e = document.createElement("div")
      e.innerHTML = OFC.none.image(src)
      _dst.parentNode.replaceChild(e, _dst);
    },
    image: function(src) {return "<img src='data:image/png;base64," + document.getElementById(src).get_img_binary() + "' />"},
    popup: function(src) {
        var img_win = window.open('', 'Charts: Export as Image')
        with(img_win.document) {
            write("<html><head><title>Charts: Export as Image<\/title><\/head><body>" + Control.OFC.image(src) + "<\/body><\/html>") }
     }
}
 