/*
 * Copyright (c) 2017 Public Library of Science
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

(function($) {

	module('jQuery#placeholder');

	test('caches results of feature tests', function() {
		strictEqual(typeof $.fn.placeholder.input, 'boolean', '$.fn.placeholder.input');
		strictEqual(typeof $.fn.placeholder.textarea, 'boolean', '$.fn.placeholder.textarea');
	});

	if ($.fn.placeholder.input && $.fn.placeholder.textarea) {
		return;
	}

	var testElement = function($el) {

		var el = $el[0];
		var placeholder = el.getAttribute('placeholder');

		strictEqual($el.placeholder(), $el, 'should be chainable');

		strictEqual(el.value, placeholder, 'should set `placeholder` text as `value`');
		strictEqual($el.prop('value'), '', 'propHooks works properly');
		strictEqual($el.val(), '', 'valHooks works properly');
		ok($el.hasClass('placeholder'), 'should have `placeholder` class');

		// test on focus
		$el.focus();
		strictEqual(el.value, '', '`value` should be the empty string on focus');
		strictEqual($el.prop('value'), '', 'propHooks works properly');
		strictEqual($el.val(), '', 'valHooks works properly');
		ok(!$el.hasClass('placeholder'), 'should not have `placeholder` class on focus');

		// and unfocus (blur) again
		$el.blur();

		strictEqual(el.value, placeholder, 'should set `placeholder` text as `value`');
		strictEqual($el.prop('value'), '', 'propHooks works properly');
		strictEqual($el.val(), '', 'valHooks works properly');
		ok($el.hasClass('placeholder'), 'should have `placeholder` class');

		// change the value
		$el.val('lorem ipsum');
		strictEqual($el.prop('value'), 'lorem ipsum', '`$el.val(string)` should change the `value` property');
		strictEqual(el.value, 'lorem ipsum', '`$el.val(string)` should change the `value` attribute');
		ok(!$el.hasClass('placeholder'), '`$el.val(string)` should remove `placeholder` class');

		// and clear it again
		$el.val('');
		strictEqual($el.prop('value'), '', '`$el.val("")` should change the `value` property');
		strictEqual(el.value, placeholder, '`$el.val("")` should change the `value` attribute');
		ok($el.hasClass('placeholder'), '`$el.val("")` should re-enable `placeholder` class');

		// make sure the placeholder property works as expected.
		strictEqual($el.prop('placeholder'), placeholder, '$el.prop(`placeholder`) should return the placeholder value');
		$el.prop('placeholder', 'new placeholder');
		strictEqual($el.prop('placeholder'), 'new placeholder', '$el.prop(`placeholder`, <string>) should set the placeholder value');
		strictEqual($el.value, 'new placeholder', '$el.prop(`placeholder`, <string>) should update the displayed placeholder value');
		$el.prop('placeholder', placeholder);
	};

	test('emulates placeholder for <input type=text>', function() {
		testElement( $('#input-type-text') );
	});

	test('emulates placeholder for <input type=search>', function() {
		testElement( $('#input-type-search') );
	});

	test('emulates placeholder for <input type=email>', function() {
		testElement( $('#input-type-email') );
	});

	test('emulates placeholder for <input type=url>', function() {
		testElement( $('#input-type-url') );
	});

	test('emulates placeholder for <input type=tel>', function() {
		testElement( $('#input-type-tel') );
	});

	test('emulates placeholder for <input type=tel>', function() {
		testElement( $('#input-type-tel') );
	});

	test('emulates placeholder for <input type=password>', function() {
		var selector = '#input-type-password';

		var $el = $(selector);
		var el = $el[0];

		var placeholder = el.getAttribute('placeholder');

		strictEqual($el.placeholder(), $el, 'should be chainable');

		// Re-select the element, as it gets replaced by another one in some browsers
		$el = $(selector);
		el = $el[0];

		strictEqual(el.value, placeholder, 'should set `placeholder` text as `value`');
		strictEqual($el.prop('value'), '', 'propHooks works properly');
		strictEqual($el.val(), '', 'valHooks works properly');
		ok($el.hasClass('placeholder'), 'should have `placeholder` class');

		// test on focus
		$el.focus();

		// Re-select the element, as it gets replaced by another one in some browsers
		$el = $(selector);
		el = $el[0];

		strictEqual(el.value, '', '`value` should be the empty string on focus');
		strictEqual($el.prop('value'), '', 'propHooks works properly');
		strictEqual($el.val(), '', 'valHooks works properly');
		ok(!$el.hasClass('placeholder'), 'should not have `placeholder` class on focus');

		// and unfocus (blur) again
		$el.blur();

		// Re-select the element, as it gets replaced by another one in some browsers
		$el = $(selector);
		el = $el[0];

		strictEqual(el.value, placeholder, 'should set `placeholder` text as `value`');
		strictEqual($el.prop('value'), '', 'propHooks works properly');
		strictEqual($el.val(), '', 'valHooks works properly');
		ok($el.hasClass('placeholder'), 'should have `placeholder` class');

	});

	test('emulates placeholder for <textarea></textarea>', function() {
		testElement( $('#textarea') );
	});

}(jQuery));
