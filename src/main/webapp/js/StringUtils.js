/**
 * Date: 06/01/13
 * Time: 14:23
 */

define([],
    function() {
        "use strict";

        var Util = {
            formatRoute: function(/*arguments*/) {
                var args = Array.prototype.slice.call(arguments);
                var str = args.shift(); //arguments[0] is the base string. The rest of the parameters are the replacing parameters.

                var regexp = new RegExp(':\\w+', 'i');
                var i;
                for (i = 0; i < args.length && regexp.test(str); i++) {
                    str = str.replace(regexp, args[i]);
                }

                // if there are more arguments left and there is *path at the end - substitute
                // also substitute even if no arguments left
                var rest = args.slice(i, args.length).join('/');
                str = str.replace(/\*path/,rest);

                return str;
            },

            formatMessage: function(str, args){
                var length = args.length;
                for (var i = 0; i < length; i++) {
                    var reg = new RegExp("\\{" + i + "\\}", "gm");
                    str = str.replace(reg, args[i]);
                }

                return str;
            }
        }


        /*
        //Tests
        console.log("Dan: urls match = " + (Util.formatRoute('question/:id1', '12345') === 'question/12345'));
        console.log("Dan: urls match = " + (Util.formatRoute('question/:id1/:id2/:id3', '12345', '23456') === 'question/12345/23456/:id3'));
        console.log("Dan: urls match = " + (Util.formatRoute('question/:id1/:id2/*', '12345', '23456') === 'question/12345/23456/*'));
        */

        return Util;

    });
