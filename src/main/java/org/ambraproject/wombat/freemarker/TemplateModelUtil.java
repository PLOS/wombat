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
