#!/usr/bin/env python

# Copyright (c) 2017 Public Library of Science
#
# Permission is hereby granted, free of charge, to any person obtaining a
# copy of this software and associated documentation files (the "Software"),
# to deal in the Software without restriction, including without limitation
# the rights to use, copy, modify, merge, publish, distribute, sublicense,
# and/or sell copies of the Software, and to permit persons to whom the
# Software is furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in
# all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
# THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
# FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
# DEALINGS IN THE SOFTWARE.

from __future__ import print_function
from __future__ import with_statement
import re
import textwrap


def make_comment(comment_body, indent=0, width=79):
    line_prefix = '# '

    # Split on blank lines, and smooth out all other whitespace
    paragraphs = [re.sub(r'\s+', ' ', paragraph.strip())
                  for paragraph in re.split(r'\n\s*\n', comment_body)]

    tab = ' ' * indent
    text_line_width = width - indent - len(line_prefix)
    lines = []
    for p in paragraphs:
        if lines:
            lines.append(tab + line_prefix.strip())
        text_lines = textwrap.wrap(p, width=text_line_width)
        lines += [(tab + line_prefix + t) for t in text_lines]
    return '\n'.join(lines)
