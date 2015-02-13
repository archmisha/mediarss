/**
 * Date: 06/01/13
 * Time: 14:23
 */

define(['blockUI'],
	function(BlockUI) {
		"use strict";

		return {
			mask: function() {
				$.blockUI({
					message: '<div id="floatingCirclesG"><div class="f_circleG" id="frotateG_01"></div><div class="f_circleG" id="frotateG_02"></div><div class="f_circleG" id="frotateG_03"></div><div class="f_circleG" id="frotateG_04"></div><div class="f_circleG" id="frotateG_05"></div><div class="f_circleG" id="frotateG_06"></div><div class="f_circleG" id="frotateG_07"></div><div class="f_circleG" id="frotateG_08"></div></div>',
					css: { width: '0px', border: '0px', left: '45%', top: '300px' }
				});
			},

			unmask: function() {
				$.unblockUI();
			}
		};
	});
