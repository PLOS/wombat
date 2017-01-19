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

package org.ambraproject.wombat.freemarker;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateHashModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateModelIterator;
import freemarker.template.TemplateSequenceModel;

class TemplateModelUtil {
  private TemplateModelUtil() {
    throw new AssertionError("Not instantiable");
  }

  static ImmutableList<TemplateModel> getAsList(TemplateModel value) throws TemplateModelException {
    if (value == null) return ImmutableList.of();
    if (value instanceof TemplateSequenceModel) {
      ImmutableList.Builder<TemplateModel> builder = ImmutableList.builder();
      TemplateSequenceModel sequenceModel = (TemplateSequenceModel) value;
      int size = sequenceModel.size();
      for (int i = 0; i < size; i++) {
        builder.add(sequenceModel.get(i));
      }
      return builder.build();
    } else {
      return ImmutableList.of(value);
    }
  }

  static ImmutableMap<String, TemplateModel> getAsMap(TemplateModel value) throws TemplateModelException {
    if (value == null) return ImmutableMap.of();
    if (value instanceof TemplateHashModelEx) {
      TemplateHashModelEx ftlHash = (TemplateHashModelEx) value;
      ImmutableMap.Builder<String, TemplateModel> builder = ImmutableMap.builder();
      for (TemplateModelIterator iterator = ftlHash.keys().iterator(); iterator.hasNext(); ) {
        String key = iterator.next().toString();
        builder.put(key, ftlHash.get(key));
      }
      return builder.build();
    }
    throw new TemplateModelException("Hash type expected");
  }

  static ImmutableListMultimap<String, TemplateModel> getAsMultimap(TemplateModel value) throws TemplateModelException {
    if (value == null) return ImmutableListMultimap.of();
    if (value instanceof TemplateHashModelEx) {
      TemplateHashModelEx ftlHash = (TemplateHashModelEx) value;
      ImmutableListMultimap.Builder<String, TemplateModel> builder = ImmutableListMultimap.builder();
      for (TemplateModelIterator iterator = ftlHash.keys().iterator(); iterator.hasNext(); ) {
        String key = iterator.next().toString();
        TemplateModel model = ftlHash.get(key);
        if (model instanceof TemplateSequenceModel) {
          TemplateSequenceModel sequenceModel = (TemplateSequenceModel) model;
          int size = sequenceModel.size();
          for (int i = 0; i < size; i++) {
            builder.put(key, sequenceModel.get(i));
          }
        } else {
          builder.put(key, model);
        }
      }
      return builder.build();
    }
    throw new TemplateModelException("Hash type expected");
  }

  static boolean getBooleanValue(TemplateModel value) throws TemplateModelException {
    if (value == null) return false;
    if (value instanceof TemplateBooleanModel) {
      return ((TemplateBooleanModel) value).getAsBoolean();
    }
    throw new TemplateModelException("Boolean type expected");
  }

}
