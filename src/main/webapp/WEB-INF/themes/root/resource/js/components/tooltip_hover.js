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

/*doc
 ---
 title: tooltip - hover
 name: hover_tooltip
 category: js widgets
 ---

---

this widget assumes that your trigger and your tooltip are within the same container
* add your hooks to the markup:
** add `[data-js-tooltip-hover=trigger]` to the container
** add `[data-js-tooltip-hover=target]` to the target
* include the scss needed - from components.scss
**add `@include js-tooltip-container();` to the container
**add `@include js-tooltip-target();` to the target
**style accordingly

```html_example
 <div class="print-article" id="printArticle" data-js-tooltip-hover="trigger">

 Print
 <ul class="print-options" data-js-tooltip-hover="target">
 <li>
 <a title="Print Article">
 Print article
 </a>
 </li>
 </ul>
 </div>

```

 ```sass_example
 .print-article{
 @extend .button-big;
 @include js-tooltip-container;
 @include plos-grid-column(77);}

 .print-options{
 @extend .tooltip-aside;
 @include js-tooltip-target( $top: rem-calc(40));

 }

 ```
 */

;(function ($) {
  var s;
  tooltip_hover = {

    settings: {
      hover_trigger: '[data-js-tooltip-hover=trigger]',
      hover_target: '[data-js-tooltip-hover=target]',
      class_trigger: 'highlighted',
      class_target: 'visible',
      class_shim: 'shim'
    },

    init: function () {
      // kick things off
      //this.settings = $.extend(this.settings, options);
      //  s = this.settings;
      this.action();
    },

    action: function () {
      s = this.settings;
        // unbind to only allow one binding per object if the tooltips are reinitialized
      $(s.hover_trigger).off("mouseenter mouseleave").mouseenter(function () {
        $(this).addClass(s.class_trigger).find(s.hover_target).addClass(s.class_target).before('<div class=' + s.class_shim + '> </div>');  //TODO: make the shim get the height
      }).mouseleave(function () {
        $(this).removeClass(s.class_trigger).find(s.hover_target).removeClass(s.class_target);
        $('.shim').remove();
      });
    }

  };
})(jQuery);

